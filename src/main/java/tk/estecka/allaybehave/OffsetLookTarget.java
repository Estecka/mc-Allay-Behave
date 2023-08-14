package tk.estecka.allaybehave;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.LookTarget;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class OffsetLookTarget 
implements LookTarget
{
	private final LookTarget inner;
	private final double offset;

	public OffsetLookTarget(LookTarget inner, double offset){
		this.inner = inner;
		this.offset = offset;
	}

	public Vec3d getPos(){
		return inner.getPos().add(0, offset, 0);
	}

	public BlockPos getBlockPos(){
		return inner.getBlockPos().add(0, (int)offset, 0);
	}

	public boolean isSeenBy(LivingEntity e){
		return inner.isSeenBy(e);
	}
}
