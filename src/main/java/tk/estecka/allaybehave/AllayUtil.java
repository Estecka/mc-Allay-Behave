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
import net.minecraft.world.GameRules;
import java.util.Optional;
import java.util.UUID;
import org.jetbrains.annotations.Nullable;

public class AllayUtil 
{
	static Box	GetSearchBox(PlayerEntity player){
		int range = player.getWorld().getGameRules().getInt(AllayRules.CALL_RANGE);
		return new Box(
			player.getX() - range,
			player.getY() - range,
			player.getZ() - range,
			player.getX() + range,
			player.getY() + range,
			player.getZ() + range
		);
	}

	static StareInfo	GetStareReq(boolean isInitialCall, GameRules rules){
		var info = new StareInfo();

		info.distance = rules.get(AllayRules.CALL_RANGE).get();
		info.hasLineOfSight = isInitialCall;
		if (!isInitialCall)
			info.cosDelta = 1.0 - Math.cos(Math.toRadians(rules.get(AllayRules.CALL_FOV).get()/2));
		else
			info.cosDelta = 0.02;

		return info;
	}

	static public @Nullable PlayerEntity GetLikedPlayer(LivingEntity allay){
		Optional<UUID> optUuid = allay.getBrain().getOptionalRegisteredMemory(MemoryModuleType.LIKED_PLAYER);
		if (optUuid.isEmpty())
			return null;

		return allay.getWorld().getPlayerByUuid(optUuid.get());
	}

	static public Optional<PlayerEntity> GetCaller(LivingEntity allay){
		return allay.getBrain().getOptionalRegisteredMemory(AllayBehave.CALLING_PLAYER);
	}
	
	static public @Nullable PlayerEntity GetCallerOrLiked(LivingEntity allay){
		return AllayUtil.GetCaller(allay).orElseGet(()->AllayUtil.GetLikedPlayer(allay));
	}

	static public boolean IsLikedOrCaller(LivingEntity allay, Entity other){
		Optional<PlayerEntity> caller = AllayUtil.GetCaller(allay);
		if (caller.isPresent() && caller.get() == other)
			return true;

		Optional<UUID> liked = allay.getBrain().getOptionalRegisteredMemory(MemoryModuleType.LIKED_PLAYER);
		if (liked.isPresent() && liked.get().equals(other.getUuid()))
			return true;

		return false;
	}

	static public void RefreshCall(AllayEntity allay, PlayerEntity caller){
		Brain<AllayEntity> brain = allay.getBrain();

		if (!IsCalled(allay)){
			var world = allay.getWorld();
			float pitch = Util.getRandom(AllayEntity.THROW_SOUND_PITCHES, world.getRandom());

			caller.playSound(SoundEvents.ENTITY_ALLAY_ITEM_THROWN, SoundCategory.NEUTRAL, 1.75f, pitch);
			world.playSoundFromEntity(caller, allay, SoundEvents.ENTITY_ALLAY_ITEM_THROWN, SoundCategory.NEUTRAL, 1, pitch);
		}

		int callDuration = (int)(20 * allay.getWorld().getGameRules().get(AllayRules.CALL_DURATION).get());
		brain.forget(MemoryModuleType.LIKED_NOTEBLOCK);
		brain.forget(MemoryModuleType.LOOK_TARGET);
		brain.forget(MemoryModuleType.IS_PANICKING);
		brain.remember(MemoryModuleType.LIKED_NOTEBLOCK_COOLDOWN_TICKS, callDuration);
		brain.remember(MemoryModuleType.ITEM_PICKUP_COOLDOWN_TICKS, callDuration);
		brain.remember(AllayBehave.CALLING_PLAYER, caller, callDuration);
	}

	static public void BreakCall(AllayEntity allay){
		Brain<AllayEntity> brain = allay.getBrain();

		brain.forget(MemoryModuleType.LIKED_NOTEBLOCK_COOLDOWN_TICKS);
		brain.forget(MemoryModuleType.ITEM_PICKUP_COOLDOWN_TICKS);
		brain.forget(AllayBehave.CALLING_PLAYER);
	}

	static public boolean IsCalled(LivingEntity allay){
		return allay.getBrain().hasMemoryModule(AllayBehave.CALLING_PLAYER);
	}

	static public boolean	IsPlayerStaring(AllayEntity allay, PlayerEntity player){
		boolean isInitialCall = !IsCalled(allay);
		return (player != null)
		    && (!player.isSpectator())
		    && (!isInitialCall || player.isSneaking())
		    && (StareInfo.IsStaring(player, allay, GetStareReq(isInitialCall, allay.getWorld().getGameRules()), isInitialCall))
		    ;
	}

	static private boolean CanNameCall(PlayerEntity player, AllayEntity allay, String calledName){
		return AllayUtil.IsLikedOrCaller(allay, player)
		    && allay.hasCustomName()
		    && allay.getCustomName().getString().equals(calledName)
		    ;
	}

	static public void	CallNamedAllays(PlayerEntity player, String name) {
		var allays = player.getWorld().getEntitiesByClass(AllayEntity.class, GetSearchBox(player), (allay)->CanNameCall(player, allay, name));
		for (AllayEntity a : allays)
			RefreshCall(a, player);
	}
}
