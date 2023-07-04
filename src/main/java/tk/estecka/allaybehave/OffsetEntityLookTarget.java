package tk.estecka.allaybehave;

import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.brain.EntityLookTarget;
import net.minecraft.util.math.Vec3d;

public class OffsetEntityLookTarget 
extends EntityLookTarget
{
	private final double offset;

	public OffsetEntityLookTarget(Entity entity, boolean useEyeHeight, double offset){
		super(entity, useEyeHeight);
		this.offset = offset;
	}

	public OffsetEntityLookTarget(EntityLookTarget target, double offset){
		super(target.getEntity(), true);
		this.offset = offset;
	}

	@Override
	public Vec3d getPos(){
		return super.getPos().add(0, offset, 0);
	}
}
