package frc.robot.subsystems.vision;

import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Pose3d;
import frc.robot.util.Camera;
import java.util.function.Supplier;

public class VisionIOSouthStarSim implements VisionIO {

  private Supplier<Pose2d> pose;
  private Camera simCam;

  public VisionIOSouthStarSim(Supplier<Pose2d> driveSupplier, Camera simCam) {
    this.pose = driveSupplier;
    this.simCam = simCam;
  }

  @Override
  public void updateInputs(VisionIOInputs inputs) {
    simCam.setPose(new Pose3d(pose.get()));
    simCam.log();
  }
}
