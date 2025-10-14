// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.subsystems.vision;

import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Transform2d;
import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableInstance;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Supplier;
import org.littletonrobotics.junction.Logger;
/*
 * This class handles object detection by logging detected algae and transforming them by the drive to provide 
 * robot relavtive poses for nearby algae
 */
public class VisionIOSouthStar implements VisionIO {

  private final NetworkTable table;
  private Supplier<Pose2d> driveSupplier;
  private List<Pose2d> detectedAlgae = new ArrayList<>();

  public VisionIOSouthStar(Supplier<Pose2d> drivePose) {
    this.table = NetworkTableInstance.getDefault().getTable("JetsonAI");
    this.driveSupplier = drivePose;
  }

  @Override
  public void updateInputs(VisionIOInputs inputs) {
    detectedAlgae.clear();
    for (String key : table.getSubTables()) {
      NetworkTable algaeEntry = table.getSubTable(key);

      double[] poseArray = algaeEntry.getEntry("pose").getDoubleArray(new double[3]);

      if (poseArray != null && poseArray.length == 3) {
        Transform2d offset = new Transform2d(poseArray[0], poseArray[1], new Rotation2d());

        Logger.recordOutput("Vision/OJ/Offsets", offset);

        Logger.recordOutput("Vision/OJ/Algae", driveSupplier.get().transformBy(offset));
        detectedAlgae.add(driveSupplier.get().transformBy(offset));
        Logger.recordOutput("Vision/OJ/DetectedAlgae", detectedAlgae.toArray(new Pose2d[0]));
      }
    }
  }
}
