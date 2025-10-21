package frc.robot.util;

import edu.wpi.first.math.geometry.Pose3d;

public class Algae {
  private final String name;
  private final Pose3d pose;

  public Algae(String name, Pose3d pose) {
    this.name = name;
    this.pose = pose;
  }

  public String getName() {
    return name;
  }

  public Pose3d getPose() {
    return pose;
  }
}
