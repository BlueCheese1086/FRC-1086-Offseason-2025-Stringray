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

  private final NetworkTable table;
  private List<AlgaePoses> algaePoses = new LinkedList<>();
  private Supplier<Pose2d> driveSupplier;

  public VisionIOSouthStar(Supplier<Pose2d> drivePose) {
    this.table =
        NetworkTableInstance.getDefault()
            .getTable("Vision")
            .getSubTable("detections")
            .getSubTable("algae");
    this.driveSupplier = drivePose;
  }

  @Override
  public void updateInputs(VisionIOInputs inputs) {
    for (String key : table.getSubTables()) {
      NetworkTable algaeEntry = table.getSubTable(key);

      double[] poseArray = algaeEntry.getEntry("pose").getDoubleArray(new double[3]);

      if (poseArray != null && poseArray.length == 3) {
        Transform2d offset = new Transform2d(poseArray[0], poseArray[1], new Rotation2d());

        algaePoses.add(new AlgaePoses(driveSupplier.get().transformBy(offset)));
      }
    }
  }
}
