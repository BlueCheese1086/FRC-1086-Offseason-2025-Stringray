// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot;

import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.GenericHID.RumbleType;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.Commands;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import edu.wpi.first.wpilibj2.command.button.CommandXboxController;
import edu.wpi.first.wpilibj2.command.button.Trigger;
import frc.robot.commands.DriveCommands;
import frc.robot.subsystems.climb.Climb;
import frc.robot.subsystems.drive.Drive;
import frc.robot.subsystems.elevator.Elevator;
import frc.robot.subsystems.elevator.ElevatorConstants.ElevatorSetpoint;
import frc.robot.subsystems.gripper.Gripper;
import frc.robot.subsystems.hopper.Hopper;
import frc.robot.subsystems.outtake.Outtake;
import frc.robot.subsystems.outtake.OuttakeConstants;
import frc.robot.util.BranchManager;
import frc.robot.util.BranchManager.Branch;
import frc.robot.util.FieldConstants.ReefConstants;
import frc.robot.util.FieldConstants.SourceConstants;
import frc.robot.util.WebServer;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import org.littletonrobotics.junction.AutoLogOutput;
import org.littletonrobotics.junction.Logger;
import org.littletonrobotics.junction.mechanism.LoggedMechanism2d;
import org.littletonrobotics.junction.mechanism.LoggedMechanismLigament2d;

/** Add your docs here. */
public class Superstructure extends SubsystemBase {

  public static enum State {
    IDLE,
    CORAL_INTAKE,
    ALGAE_INTAKE,
    CORAL_OUTTAKE,
    CLIMB_PULL,
    CLIMB_READY,
    AUTO_SCORE,
    AUTO_INTAKE,
  }

  // Maps to store triggers for state requests and state-based conditions
  private Map<State, Trigger> stateRequests = new EnumMap<>(State.class);
  private Map<State, Trigger> stateTriggers = new EnumMap<>(State.class);

  private List<Pose2d> leftPoses;
  private List<Pose2d> rightPoses;

  // Logging the current and previous robot state
  @AutoLogOutput(key = "RobotState/CurrentState")
  private State currentState = State.IDLE;

  @AutoLogOutput(key = "RobotState/PreviousState")
  private State previousState = State.IDLE;

  @AutoLogOutput(key = "RobotState/AutoIntake")
  private Trigger autoIntakeRequest;

  @AutoLogOutput(key = "RobotState/AutoScore")
  private Trigger autoScoreRequest;

  @AutoLogOutput(key = "RobotState/ExitRequest")
  private Trigger cancelRequest;

  private final Drive drive;
  private final Gripper gripper;
  private final Elevator elevator;
  private final Hopper hopper;
  private final Outtake outtake;
  private final Climb climb;
  private WebServer server;
  private BranchManager manager;

  private final CommandXboxController driver;
  private final LoggedMechanism2d body = new LoggedMechanism2d(1.0, 1.0);
  private final LoggedMechanismLigament2d elevatorLigament =
      new LoggedMechanismLigament2d("Elevator", 1.7, 90.0);

  /** Creates a new Superstructure. */
  public Superstructure(
      Drive drive,
      Elevator elevator,
      Outtake outtake,
      Hopper hopper,
      Gripper gripper,
      Climb climb,
      BranchManager manager,
      CommandXboxController driver,
      Trigger autoIntakeRequest,
      Trigger exitRequest) {

    // Initializing Subsystem
    this.drive = drive;
    this.gripper = gripper;
    this.elevator = elevator;
    this.driver = driver;
    this.climb = climb;
    this.hopper = hopper;
    this.outtake = outtake;
    this.manager = manager;
    this.autoIntakeRequest = autoIntakeRequest;
    this.cancelRequest = exitRequest;

    new Thread(
            () -> {
              try {
                server = new WebServer(1086);
              } catch (Exception e) {
                e.printStackTrace();
              }
            })
        .start();

    var root = body.getRoot("Mech", 0.8, 0.0125);
    root.append(elevatorLigament);

    stateRequests.put(State.IDLE, driver.povLeft());
    stateRequests.put(State.CORAL_INTAKE, driver.leftTrigger());
    stateRequests.put(State.ALGAE_INTAKE, driver.leftTrigger());
    stateRequests.put(State.CORAL_OUTTAKE, driver.rightTrigger());

    for (State state : State.values()) {
      stateTriggers.put(
          state, new Trigger(() -> this.currentState == state && DriverStation.isEnabled()));
    }

    this.setupIdle();
  }

  public void setupIdle() {
    stateTriggers.get(State.IDLE).and(autoIntakeRequest).onTrue(this.forceState(State.AUTO_INTAKE));
    ;

    stateTriggers
        .get(State.IDLE)
        .and(driver.leftBumper())
        .and(() -> !leftPoses.isEmpty())
        .onTrue(
            DriveCommands.autoAlign(drive, () -> drive.getPose().nearest(leftPoses))
                .until(
                    () ->
                        DriveCommands.isNear(drive.getPose().nearest(leftPoses), drive.getPose())));

    stateTriggers
        .get(State.IDLE)
        .and(driver.rightBumper())
        .and(() -> !rightPoses.isEmpty())
        .onTrue(
            DriveCommands.autoAlign(drive, () -> drive.getPose().nearest(rightPoses))
                .until(
                    () ->
                        DriveCommands.isNear(
                            drive.getPose().nearest(rightPoses), drive.getPose())));

    stateTriggers
        .get(State.IDLE)
        .onTrue(
            Commands.parallel(
                outtake.setVoltage(() -> 0.0),
                gripper.setVoltage(() -> 0.0),
                hopper.setVoltage(() -> 0.0)));
  }

  public void setupAutoIntake() {
    stateTriggers.get(State.AUTO_INTAKE).onTrue(this.autoIntake(outtake, hopper));

    stateTriggers
        .get(State.AUTO_INTAKE)
        .and(() -> outtake.getDetected())
        .and(autoScoreRequest)
        .onTrue(this.forceState(State.AUTO_SCORE));
  }

  // Auto Score defualts to L4 unless specified to change
  // TODO: this is going to be changed to auto level based on scored knowlede of
  // robot
  public void setupAutonomus() {
    stateTriggers.get(State.AUTO_SCORE).and(cancelRequest).onTrue(this.forceState(State.IDLE));

    stateTriggers
        .get(State.AUTO_SCORE)
        .and(driver.leftTrigger())
        .onTrue(this.alignScore(elevator, outtake, ElevatorSetpoint.L4, true));

    stateTriggers
        .get(State.AUTO_SCORE)
        .and(driver.rightTrigger())
        .onTrue(this.alignScore(elevator, outtake, ElevatorSetpoint.L4, false));
  }

  public Command autoIntake(Outtake outtake, Hopper hopper) {
    return Commands.parallel(
            DriveCommands.autoAlign(drive, () -> SourceConstants.getNearestSource(drive::getPose))
                .until(
                    () ->
                        DriveCommands.isNear(
                            SourceConstants.getNearestSource(drive::getPose), drive.getPose())),
            outtake.setVoltage(() -> OuttakeConstants.intake),
            hopper.setVoltage(() -> OuttakeConstants.intake))
        .until(() -> outtake.getDetected());
  }

  public Command alignScore(
      Elevator elevator, Outtake outtake, ElevatorSetpoint setpoint, boolean left) {
    boolean l4 = setpoint.equals(ElevatorSetpoint.L4) ? true : false;
    return Commands.sequence(
        DriveCommands.autoAlign(drive, () -> ReefConstants.getBestBranch(drive::getPose, left, l4))
            .until(
                () ->
                    DriveCommands.isNear(
                        ReefConstants.getBestBranch(drive::getPose, left, l4), drive.getPose())),
        this.setFutureElevatorTarget(setpoint),
        elevator.setExtension().until(() -> elevator.atSetpoint()),
        outtake.setVoltage(() -> OuttakeConstants.L4).until(() -> !outtake.getDetected()),
        this.setFutureElevatorTarget(ElevatorSetpoint.INTAKE),
        elevator.setExtension());
  }

  public Command setFutureElevatorTarget(ElevatorSetpoint setpoint) {
    return Commands.runOnce(() -> elevator.selectFutureTarget(setpoint), elevator);
  }

  @Override
  public void periodic() {
    // This method will be called once per scheduler run
    if (Robot.isSimulation() || !DriverStation.isFMSAttached()) {
      // Log All Necessary Superstructure Data
      Logger.recordOutput("Superstructure/Mechanism", body);
    }

    if (server.getLastLevelSelected() != null) {
      this.mapWebServerToId(
          server.getLastCircleClicked(), ElevatorSetpoint.valueOf(server.getLastLevelSelected()));
    }

    this.logBranchPoses();
  }

  private void logBranchPoses() {
    leftPoses = manager.leftBranches.values().stream().map(Branch::pose).toList();

    rightPoses = manager.rightBranches.values().stream().map(Branch::pose).toList();

    Logger.recordOutput("Branches/Left/Poses", leftPoses.toArray(new Pose2d[0]));
    Logger.recordOutput("Branches/Right/Poses", rightPoses.toArray(new Pose2d[0]));
  }

  public void mapWebServerToId(int id, ElevatorSetpoint setpoint) {
    switch (id) {
      case 0 -> manager.removeRightBranchSetpoint(0, setpoint);
      case 1 -> manager.removeLeftBranchSetpoint(0, setpoint);
      case 2 -> manager.removeRightBranchSetpoint(1, setpoint);
      case 3 -> manager.removeLeftBranchSetpoint(1, setpoint);
      case 4 -> manager.removeRightBranchSetpoint(2, setpoint);
      case 5 -> manager.removeLeftBranchSetpoint(2, setpoint);
      case 6 -> manager.removeLeftBranchSetpoint(3, setpoint);
      case 7 -> manager.removeRightBranchSetpoint(3, setpoint);
      case 8 -> manager.removeLeftBranchSetpoint(4, setpoint);
      case 9 -> manager.removeRightBranchSetpoint(4, setpoint);
      case 10 -> manager.removeLeftBranchSetpoint(5, setpoint);
      case 11 -> manager.removeRightBranchSetpoint(5, setpoint);
      default -> {
        DriverStation.reportWarning("Id is Null", true);
      }
    }
  }

  private Command forceState(State nextState) {
    return Commands.runOnce(
        () -> {
          System.out.println("Changing state to " + nextState);
          previousState = currentState;
          currentState = nextState;
        });
  }

  public static Command rumbleCommand(
      CommandXboxController controller, double seconds, double intensity) {
    return Commands.run(
            () -> {
              controller.setRumble(RumbleType.kBothRumble, intensity);
            })
        .withTimeout(seconds)
        .finallyDo(
            () -> {
              controller.setRumble(RumbleType.kBothRumble, 0.0);
            });
  }
}
