// Copyright 2021-2025 FRC 6328
// http://github.com/Mechanical-Advantage
//
// This program is free software; you can redistribute it and/or
// modify it under the terms of the GNU General Public License
// version 3 as published by the Free Software Foundation or
// available in the root directory of this project.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// GNU General Public License for more details.

package frc.robot.subsystems.vision;

import edu.wpi.first.apriltag.AprilTagFieldLayout;
import edu.wpi.first.apriltag.AprilTagFields;
import edu.wpi.first.math.geometry.Rotation3d;
import edu.wpi.first.math.geometry.Transform3d;
import edu.wpi.first.math.util.Units;

/** Add your docs here. */
public class VisionConstants {
  public static final double maxYawDeviation = 10;
  public static final int yawHistorySize = 15; // last 5 accepted pos6es

  // Basic filtering thresholds
  public static double maxAmbiguity = 0.25; // 0.3d
  public static double maxZError = 0.95; // 0.75

  // Standard deviation baselines, for 1 meter distance and 1 tag
  // (Adjusted automatically based on distance and # of tags)
  public static double linearStdDevBaseline = 4.0; // Meters
  public static double angularStdDevBaseline = 4.8; // Radians

  // Standard deviation multipliers for each camera
  // (Adjust to trust some cameras more than others)
  public static double[] cameraStdDevFactors =
      new double[] {
        1.0, // Camera 0
        1.0 // Camera 1
      };

  // Multipliers to apply for MegaTag 2 observations
  public static double linearStdDevMegatag2Factor = 0.5; // More stable than full 3D solve
  public static double angularStdDevMegatag2Factor =
      Double.POSITIVE_INFINITY; // No rotation data available

  public static AprilTagFieldLayout fieldLayout =
      AprilTagFieldLayout.loadField(AprilTagFields.k2025ReefscapeAndyMark);
  ;

  public static Transform3d robotToLeftCam =
      new Transform3d(
          Units.inchesToMeters(12.066),
          Units.inchesToMeters(11.906),
          Units.inchesToMeters(8.355),
          new Rotation3d(0.0, -Units.degreesToRadians(13.125000), Units.degreesToRadians(-30)));
  public static Transform3d robotToRightCam =
      new Transform3d(
          Units.inchesToMeters(12.066),
          Units.inchesToMeters(-11.906),
          Units.inchesToMeters(8.355),
          new Rotation3d(0.0, -Units.degreesToRadians(13.125000), Units.degreesToRadians(30)));

  public static double trigLinearStdDevBaseline = 0.1; // Meters
  public static double trigAngularStdDevBaseline = Double.POSITIVE_INFINITY; // Radians

  public static double multitagLinearStdDevBaseline = 0.35; // Meters
  public static double multitagAngularStdDevBaseline = 0.14; // Radians
}
