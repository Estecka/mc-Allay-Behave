package tk.estecka.allaybehave.tasks;

import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.task.SingleTickTask;
import net.minecraft.entity.passive.AllayEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import tk.estecka.allaybehave.AllayRules;
import tk.estecka.allaybehave.AllayUtil;

public class TeleportTask 
extends SingleTickTask<AllayEntity>
{
	public boolean trigger(ServerWorld world, AllayEntity allay, long time){
		if (!allay.getWorld().getGameRules().getBoolean(AllayRules.DO_TELEPORT)
		|| (allay.getBrain().hasMemoryModule(MemoryModuleType.LIKED_NOTEBLOCK))
		|| (allay.getBrain().hasMemoryModule(MemoryModuleType.NEAREST_VISIBLE_WANTED_ITEM)))
			return false;

		PlayerEntity player = AllayUtil.GetLikedPlayer(allay);
		if (player == null || player.isSpectator() || player.getWorld() != allay.getWorld())
			return false;

		double dist = allay.getSquaredDistanceToAttackPosOf(player);
		int min = allay.getWorld().getGameRules().getInt(AllayRules.TELEPORT_DIST);
		if (dist < (min*min) || (64*64) < dist)
			return false;

		Vec3d targetPos = player.getPos().add(0, 0.5, 0);
		Box  targetBox = allay.getBoundingBox().offset(allay.getPos().multiply(-1).add(targetPos));
		if (player.getWorld().containsFluid(targetBox)
		|| (!player.getWorld().isSpaceEmpty(allay, targetBox)))
			return false;

		allay.refreshPositionAfterTeleport(targetPos);
		return true;
	}
}
