package tk.estecka.allaybehave;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.passive.AllayEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Util;
import net.minecraft.util.math.Box;
import java.util.Optional;
import java.util.UUID;
import org.jetbrains.annotations.Nullable;

public class AllayUtil 
{
	static public final int	BEHELD_DURATION = 5*20;
	static public final int	CALL_RANGE = 32;

	static private final StareInfo CALL_REQUIREMENTS = new StareInfo(){{
		this.distance = CALL_RANGE;
		this.dotAngle = 0.02;
		this.hasLineOfSight = true;
	}};
	static private final StareInfo STARE_REQUIREMENT = new StareInfo(){{
		this.distance = CALL_RANGE;
		this.dotAngle = 0.5;
		this.hasLineOfSight = false;
	}};

	static Box	GetSearchBox(PlayerEntity player){
		return new Box(
			player.getX() - CALL_RANGE,
			player.getY() - CALL_RANGE,
			player.getZ() - CALL_RANGE,
			player.getX() + CALL_RANGE,
			player.getY() + CALL_RANGE,
			player.getZ() + CALL_RANGE
		);
	}

	@Nullable
	static public PlayerEntity GetLikedPlayer(LivingEntity allay){
		Optional<UUID> optUuid = allay.getBrain().getOptionalRegisteredMemory(MemoryModuleType.LIKED_PLAYER);
		if (optUuid.isEmpty())
			return null;

		return allay.getWorld().getPlayerByUuid(optUuid.get());
	}

	static public Optional<PlayerEntity> GetBeholder(LivingEntity allay){
		return allay.getBrain().getOptionalRegisteredMemory(AllayBehave.IS_BEHELD);
	}
	
	@Nullable
	static public PlayerEntity GetBeholderOrLiked(LivingEntity allay){
		return AllayUtil.GetBeholder(allay).orElseGet(()->AllayUtil.GetLikedPlayer(allay));
	}

	static public boolean IsLikedOrBeholder(LivingEntity allay, Entity other){
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
		brain.remember(AllayBehave.IS_BEHELD, beholder, BEHELD_DURATION);
	}
	static public void BreakBeheld(AllayEntity allay){
		Brain<AllayEntity> brain = allay.getBrain();

		brain.forget(MemoryModuleType.LIKED_NOTEBLOCK_COOLDOWN_TICKS);
		brain.forget(MemoryModuleType.ITEM_PICKUP_COOLDOWN_TICKS);
		brain.forget(AllayBehave.IS_BEHELD);
	}

	static public boolean IsBeheld(LivingEntity allay){
		return allay.getBrain().hasMemoryModule(AllayBehave.IS_BEHELD);
	}

	static public boolean	IsPlayerBeholding(AllayEntity allay, PlayerEntity player){
		boolean isBeheld = IsBeheld(allay);
		StareInfo req = isBeheld ? STARE_REQUIREMENT : CALL_REQUIREMENTS;

		return (player != null)
		    && (isBeheld || player.isSneaking())
		    && (StareInfo.IsStaring(player, allay, req, !isBeheld))
		    ;
	}

	static private boolean CanNameCall(PlayerEntity player, AllayEntity allay, String calledName){
		return AllayUtil.IsLikedOrBeholder(allay, player)
		    && allay.hasCustomName()
		    && allay.getCustomName().getString().equals(calledName)
		    ;
	}

	static public void	CallNamedAllay(PlayerEntity player, String name) {
		var allays = player.getWorld().getEntitiesByClass(AllayEntity.class, GetSearchBox(player), (allay)->CanNameCall(player, allay, name));
		for (AllayEntity a : allays)
			SetBeheld(a, player);
	}
}
