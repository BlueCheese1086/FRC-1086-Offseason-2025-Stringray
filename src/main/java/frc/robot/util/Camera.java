package frc.robot.util;

import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Pose3d;
import edu.wpi.first.math.geometry.Transform3d;
import edu.wpi.first.math.geometry.Translation3d;
import java.util.*;
import org.littletonrobotics.junction.Logger;

public class Camera {
  private static String name;
  private static Pose3d camPose;
  private static double horizFOV;
  private static double vertFOV;
  private static double maxRange;
  private static List<Algae> allAlgae;

  private static final Map<String, Pose3d> lastKnownAlgae = new HashMap<>();

  public Camera(
      String name,
      double horizFOVDeg,
      double vertFOVDeg,
      double maxRangeMeters,
      List<Algae> algae) {
    Camera.name = name;
    Camera.camPose = new Pose3d();
    Camera.horizFOV = Math.toRadians(horizFOVDeg);
    Camera.vertFOV = Math.toRadians(vertFOVDeg);
    Camera.maxRange = maxRangeMeters;
    Camera.allAlgae = new ArrayList<>(algae);
  }

  public void setPose(Pose3d newPose) {
    Camera.camPose = newPose;
  }

  public Pose3d getPose() {
    return camPose;
  }

  public static boolean canSeeTarget(Pose3d targetPose) {
    var rel = new Transform3d(camPose, targetPose);

    Translation3d relTrans = rel.getTranslation();
    double distance = relTrans.getNorm();
    if (distance > maxRange) return false;

    if (relTrans.getX() <= 0) return false;

    double yaw = Math.atan2(relTrans.getY(), relTrans.getX());
    double pitch = Math.atan2(relTrans.getZ(), relTrans.getX());

    return Math.abs(yaw) < horizFOV / 2 && Math.abs(pitch) < vertFOV / 2;
  }

  public static List<Algae> getVisibleAlgae() {
    return allAlgae.stream().filter(a -> canSeeTarget(a.getPose())).toList();
  }

  public void addAlgae(int index, Pose3d pose) {
    allAlgae.add(new Algae("Algae " + index, pose));
  }

  public void clear() {
    allAlgae.clear();
    lastKnownAlgae.clear();
  }

  public void log() {
    var visible = getVisibleAlgae();

    if (!visible.isEmpty()) {
      lastKnownAlgae.clear();
      for (Algae algae : visible) {
        lastKnownAlgae.put(algae.getName(), algae.getPose());
      }
    }

    var allKnownPoses = lastKnownAlgae.values().toArray(new Pose3d[0]);

    Logger.recordOutput(name + "/VisibleAlgae", allKnownPoses);
    Logger.recordOutput(name + "/CameraPose", this.getPose());
  }

  public static List<Pose2d> returnArrayOfSeenAlgae() {
    var visible = getVisibleAlgae();

    if (!visible.isEmpty()) {
      lastKnownAlgae.clear();
      for (Algae algae : visible) {
        lastKnownAlgae.put(algae.getName(), algae.getPose());
      }
    }

    var poseList =
        lastKnownAlgae.values().stream()
            .map(
                pose3d ->
                    new Pose2d(pose3d.getX(), pose3d.getY(), pose3d.getRotation().toRotation2d()))
            .toList();

    Logger.recordOutput(name + "/2dALGAE", poseList.toArray(new Pose2d[0]));
    return poseList;
  }
}
