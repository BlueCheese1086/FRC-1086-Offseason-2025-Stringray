// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.util;

import edu.wpi.first.math.geometry.Pose2d;
import frc.robot.subsystems.elevator.ElevatorConstants.ElevatorSetpoint;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BranchManager {
  public record Branch(Pose2d pose, List<ElevatorSetpoint> setpoint) {}

  public final Map<Integer, Branch> leftBranches = new HashMap<>();
  public final Map<Integer, Branch> rightBranches = new HashMap<>();

  public void loadBranches(List<Pose2d> leftList, List<Pose2d> rightList) {

    for (int i = 0; i < 6; i++) {

      List<ElevatorSetpoint> leftSetpoints = new ArrayList<>();
      List<ElevatorSetpoint> rightSetpoints = new ArrayList<>();

      switch (i) {
        case 0:
          leftSetpoints.add(ElevatorSetpoint.L2);
          leftSetpoints.add(ElevatorSetpoint.L3);
          leftSetpoints.add(ElevatorSetpoint.L4);
          rightSetpoints.add(ElevatorSetpoint.L2);
          rightSetpoints.add(ElevatorSetpoint.L3);
          rightSetpoints.add(ElevatorSetpoint.L4);
          break;

        case 1:
          leftSetpoints.add(ElevatorSetpoint.L2);
          leftSetpoints.add(ElevatorSetpoint.L3);
          leftSetpoints.add(ElevatorSetpoint.L4);
          rightSetpoints.add(ElevatorSetpoint.L2);
          rightSetpoints.add(ElevatorSetpoint.L3);
          rightSetpoints.add(ElevatorSetpoint.L4);
          break;
        case 2:
          leftSetpoints.add(ElevatorSetpoint.L2);
          leftSetpoints.add(ElevatorSetpoint.L3);
          leftSetpoints.add(ElevatorSetpoint.L4);
          rightSetpoints.add(ElevatorSetpoint.L2);
          rightSetpoints.add(ElevatorSetpoint.L3);
          rightSetpoints.add(ElevatorSetpoint.L4);
          break;
        case 3:
          leftSetpoints.add(ElevatorSetpoint.L2);
          leftSetpoints.add(ElevatorSetpoint.L3);
          leftSetpoints.add(ElevatorSetpoint.L4);
          rightSetpoints.add(ElevatorSetpoint.L2);
          rightSetpoints.add(ElevatorSetpoint.L3);
          rightSetpoints.add(ElevatorSetpoint.L4);
          break;
        case 4:
          leftSetpoints.add(ElevatorSetpoint.L2);
          leftSetpoints.add(ElevatorSetpoint.L3);
          leftSetpoints.add(ElevatorSetpoint.L4);
          rightSetpoints.add(ElevatorSetpoint.L2);
          rightSetpoints.add(ElevatorSetpoint.L3);
          rightSetpoints.add(ElevatorSetpoint.L4);
          break;
        case 5:
          leftSetpoints.add(ElevatorSetpoint.L2);
          leftSetpoints.add(ElevatorSetpoint.L3);
          leftSetpoints.add(ElevatorSetpoint.L4);
          rightSetpoints.add(ElevatorSetpoint.L2);
          rightSetpoints.add(ElevatorSetpoint.L3);
          rightSetpoints.add(ElevatorSetpoint.L4);
          break;

        default:
          leftSetpoints.add(ElevatorSetpoint.L1);
          rightSetpoints.add(ElevatorSetpoint.L1);
          break;
      }

      leftBranches.put(i, new Branch(leftList.get(i), leftSetpoints));
      rightBranches.put(i, new Branch(rightList.get(i), rightSetpoints));
    }
  }

  public void updateLeftBranchSetpoint(int branchId, ElevatorSetpoint setpoint) {
    Branch old = leftBranches.get(branchId);
    if (old != null) {
      List<ElevatorSetpoint> newList = new ArrayList<>(old.setpoint());
      newList.add(setpoint);
      leftBranches.put(branchId, new Branch(old.pose(), newList));
    }
  }

  public void updateRightBranchSetpoint(int branchId, ElevatorSetpoint setpoint) {
    Branch old = rightBranches.get(branchId);
    if (old != null) {
      List<ElevatorSetpoint> newList = new ArrayList<>(old.setpoint());
      newList.add(setpoint);
      rightBranches.put(branchId, new Branch(old.pose(), newList));
    }
  }

  public Branch getLeftBranch(int id) {
    return leftBranches.get(id);
  }

  public Branch getRightBranch(int id) {
    return rightBranches.get(id);
  }

  public Pose2d getLeftPose(int id) {
    return leftBranches.get(id).pose();
  }

  public Pose2d getRightPose(int id) {
    return rightBranches.get(id).pose();
  }

  public void removeLeftBranchSetpoint(int id, ElevatorSetpoint setpoint) {
    Branch branch = leftBranches.get(id);
    if (branch == null) return;

    branch.setpoint().remove(setpoint);

    if (branch.setpoint().isEmpty()) {
      leftBranches.remove(id);
    }
  }

  public void removeRightBranchSetpoint(int id, ElevatorSetpoint setpoint) {
    Branch branch = rightBranches.get(id);
    if (branch == null) return;

    branch.setpoint().remove(setpoint);

    if (branch.setpoint().isEmpty()) {
      rightBranches.remove(id);
    }
  }

  public ElevatorSetpoint getLeftSetpoint(int id) {
    return leftBranches.get(id).setpoint().get(3);
  }

  public ElevatorSetpoint getRightSetpoint(int id) {
    return rightBranches.get(id).setpoint().get(3);
  }
}
