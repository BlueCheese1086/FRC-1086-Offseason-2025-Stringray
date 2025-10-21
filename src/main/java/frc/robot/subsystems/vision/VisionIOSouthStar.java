package frc.robot.subsystems.vision;

import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Pose3d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Rotation3d;
import edu.wpi.first.math.geometry.Transform2d;
import edu.wpi.first.math.geometry.Translation3d;
import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableInstance;
import frc.robot.util.Camera;
import java.util.function.Supplier;
import org.littletonrobotics.junction.Logger;

/*
 * This class handles object detection by logging detected algae and transforming them by the drive to provide
 * robot relavtive poses for nearby algae
 */
public class VisionIOSouthStar implements VisionIO {

  private final NetworkTable table;
  private Supplier<Pose2d> driveSupplier;
  private Camera cam;

  public VisionIOSouthStar(Supplier<Pose2d> drivePose, Camera camera) {
    this.table = NetworkTableInstance.getDefault().getTable("JetsonAI");
    this.driveSupplier = drivePose;
    this.cam = camera;
    this.cam.clear();
  }

  @Override
  public void updateInputs(VisionIOInputs inputs) {
    int index = 0;
    cam.setPose(new Pose3d(driveSupplier.get()));
    cam.log();
    for (String key : table.getSubTables()) {
      NetworkTable algaeEntry = table.getSubTable(key);

      double[] poseArray = algaeEntry.getEntry("pose").getDoubleArray(new double[3]);
      Transform2d offset = new Transform2d(poseArray[0], poseArray[1], new Rotation2d());

      if (poseArray != null && poseArray.length == 3) {

        Logger.recordOutput("Vision/OJ/Offsets", offset);
        Logger.recordOutput("Vision/OJ/Algae", driveSupplier.get().transformBy(offset));
        Translation3d translation =
            new Translation3d(driveSupplier.get().transformBy(offset).getTranslation());
        cam.addAlgae(
            index,
            new Pose3d(translation.getX(), translation.getY(), poseArray[2], new Rotation3d()));
      }
      index++;
    }
  }
}
