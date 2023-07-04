package tk.estecka.allaybehave;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.passive.AllayEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.math.Vec3d;
import tk.estecka.allaybehave.mixin.AllayEntityAccessor;

import java.util.LinkedList;
import java.util.Optional;
import java.util.UUID;

import org.jetbrains.annotations.Nullable;

import com.google.common.collect.ImmutableList;

public class AllayUtil 
{
	static public final MemoryModuleType<PlayerEntity> IS_BEHELD = Registry.register(
		Registries.MEMORY_MODULE_TYPE,
		new Identifier("allaybehave", "is_beheld"),
		new MemoryModuleType<PlayerEntity>(Optional.empty())
	);
	static public final int    BEHELD_DURATION = 5*20;
	static public final int    STARE_DISTANCE = 32;
	static public final int    STARE_SQUARED_DISTANCE = STARE_DISTANCE*STARE_DISTANCE;
	static public final double STARE_SENSITIVITY = 0.02;

	static public void	Initialize()
	{
		var oldList = AllayEntityAccessor.get_MEMORY_MODULES();
		var newList = new LinkedList<MemoryModuleType<?>>(oldList);

		newList.add(IS_BEHELD);

		AllayEntityAccessor.set_MEMORY_MODULES(ImmutableList.copyOf(newList));
	}

	@Nullable
	static public PlayerEntity GetLikedPlayer(LivingEntity allay){
		Optional<UUID> optUuid = allay.getBrain().getOptionalRegisteredMemory(MemoryModuleType.LIKED_PLAYER);
		if (optUuid.isEmpty())
			return null;

		return allay.getWorld().getPlayerByUuid(optUuid.get());
	}

	static public Optional<PlayerEntity> GetBeholder(LivingEntity allay){
		return allay.getBrain().getOptionalRegisteredMemory(AllayUtil.IS_BEHELD);
	}
	
	@Nullable
	static public PlayerEntity GetBeholderOrLiked(LivingEntity allay){
		return AllayUtil.GetBeholder(allay).orElseGet(()->AllayUtil.GetLikedPlayer(allay));
	}

	static public boolean IsLikedOrBeholden(LivingEntity allay, Entity other){
		Optional<PlayerEntity> beholden = AllayUtil.GetBeholder(allay);
		if (beholden.isPresent() && other==beholden.get())
			return true;

		Optional<UUID> liked = allay.getBrain().getOptionalRegisteredMemory(MemoryModuleType.LIKED_PLAYER);
		if (liked.isPresent() && liked.get().equals(other.getUuid()))
			return true;

		return false;
	}

	static public void SetBeheld(AllayEntity allay, PlayerEntity beholder){
		Brain<AllayEntity> brain = allay.getBrain();

		if (!IsBeheld(allay)){
			var world = allay.getWorld();
			float pitch = Util.getRandom(AllayEntity.THROW_SOUND_PITCHES, world.getRandom());

			beholder.playSound(SoundEvents.ENTITY_ALLAY_ITEM_THROWN, SoundCategory.NEUTRAL, 1.75f, pitch);
			world.playSoundFromEntity(beholder, allay, SoundEvents.ENTITY_ALLAY_ITEM_THROWN, SoundCategory.NEUTRAL, 1, pitch);
		}
			
		brain.forget(MemoryModuleType.LIKED_NOTEBLOCK);
		brain.forget(MemoryModuleType.LOOK_TARGET);
		brain.forget(MemoryModuleType.IS_PANICKING);
		brain.remember(MemoryModuleType.LIKED_NOTEBLOCK_COOLDOWN_TICKS, BEHELD_DURATION);
		brain.remember(MemoryModuleType.ITEM_PICKUP_COOLDOWN_TICKS, BEHELD_DURATION);
		brain.remember(AllayUtil.IS_BEHELD, beholder, BEHELD_DURATION);
	}
	static public void BreakBeheld(AllayEntity allay){
		Brain<AllayEntity> brain = allay.getBrain();

		brain.forget(MemoryModuleType.LIKED_NOTEBLOCK_COOLDOWN_TICKS);
		brain.forget(MemoryModuleType.ITEM_PICKUP_COOLDOWN_TICKS);
		brain.forget(AllayUtil.IS_BEHELD);
	}

	static public boolean IsBeheld(LivingEntity allay){
		return allay.getBrain().hasMemoryModule(IS_BEHELD);
	}

	static public boolean	IsStaring(LivingEntity beholder, LivingEntity target){
		return IsStaring(beholder, target, 0.025, false);
	}
	
	// Based on the enderman's staring logic
	static public boolean	IsStaring(LivingEntity beholder, LivingEntity target, double sensitivity, boolean throughWalls){
		Vec3d stareDir = beholder.getRotationVec(1.0f).normalize();
		Vec3d targetDir = new Vec3d(
			target.getX   () - beholder.getX   (),
			target.getEyeY() - beholder.getEyeY(),
			target.getZ   () - beholder.getZ   ()
		);
		double distance = targetDir.length();
		double dotAngle = stareDir.dotProduct(targetDir.normalize());
		if (dotAngle > 1.0 - (sensitivity/distance))
			return throughWalls || beholder.canSee(target);
		return false;
	}

	static public boolean	IsPlayerBeholding(AllayEntity allayEntity, PlayerEntity player){
		AllayEntity allay = (AllayEntity)allayEntity;

		return (player != null)
		    && (player.isSneaking() || IsBeheld(allay))
		    && (STARE_SQUARED_DISTANCE > player.getEyePos().squaredDistanceTo(allay.getEyePos()))
		    && (IsStaring(player, allay, STARE_SENSITIVITY, false))
		    ;
	}
}
