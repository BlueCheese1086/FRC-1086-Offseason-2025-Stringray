// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.subsystems.vision;

import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Transform2d;
import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableInstance;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Supplier;

public class VisionIOSouthStar implements VisionIO {

  private NetworkTable table;
  private String pathToData;
  private List<AlgaePoses> algaePoses = new LinkedList<>();
  private Supplier<Pose2d> drive;

  public VisionIOSouthStar(Supplier<Pose2d> drivePose, String directory) {
    this.table = NetworkTableInstance.getDefault().getTable("Algae Detection");
    this.pathToData = directory;
    this.drive = drivePose;
  }

  @Override
  public void updateInputs(VisionIOInputs inputs) {
    if (table.getEntry(pathToData).isValid()) {
      Pose2d targetAlgae =
          drive
              .get()
              .plus(
                  new Transform2d(
                      table.getEntry(pathToData).getDouble(0),
                      table.getEntry(pathToData).getDouble(0),
                      Rotation2d.kZero));

      algaePoses.add(new AlgaePoses(targetAlgae));
    }
  }
}
