package tk.estecka.allaybehave;

import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Vec3d;

public class StareInfo 
{
	public double	distance;
	/**
	 * The cosine of the allowed angle, but remapped from [1, -1] to [0, 1].
	 */
	public double	cosDelta;
	public boolean	hasLineOfSight;

	static public StareInfo	IsStaring(LivingEntity beholder, LivingEntity target){
		StareInfo info = new StareInfo();

		Vec3d stareDir = beholder.getRotationVec(1.0f).normalize();
		Vec3d targetDir = new Vec3d(
			target.getX   () - beholder.getX   (),
			target.getEyeY() - beholder.getEyeY(),
			target.getZ   () - beholder.getZ   ()
		);

		info.distance = targetDir.length();
		info.cosDelta = 1.0 - stareDir.dotProduct(targetDir.normalize());
		info.hasLineOfSight = beholder.canSee(target);

		return info;
	}

	static public boolean IsStaring(LivingEntity beholder, LivingEntity target, StareInfo req, boolean distanceScalesAngle){
		Vec3d targetDir = new Vec3d(
			target.getX   () - beholder.getX   (),
			target.getEyeY() - beholder.getEyeY(),
			target.getZ   () - beholder.getZ   ()
		);

		double distance = targetDir.length();
		if (distance*distance > req.distance*req.distance)
			return false;

		Vec3d stareDir = beholder.getRotationVec(1.0f).normalize();
		double cosDelta = 1.0 - stareDir.dotProduct(targetDir.normalize());

		if (distanceScalesAngle){
			if (cosDelta > (req.cosDelta/distance))
				return false;
		}
		else if (cosDelta > req.cosDelta)
			return false;

		return !req.hasLineOfSight || beholder.canSee(target);
	}
}
