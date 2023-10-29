package tk.estecka.allaybehave.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.passive.AllayEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.Vec3d;
import tk.estecka.allaybehave.AllayRules;
import tk.estecka.allaybehave.AllayUtil;

@Mixin(AllayEntity.class)
public abstract class AllayEntityMixin
extends LivingEntityMixin
{
	@Unique
	private final AllayEntity allay = (AllayEntity)(Object)this;

	@Inject( method="tick", at=@At("TAIL") )
	private void	allaybehave$CheckForCallingPlayers(CallbackInfo info) {
		if (!allay.getWorld().isClient() && allay.getWorld().getGameRules().getBoolean(AllayRules.STARE_CALL)) {
			PlayerEntity player = AllayUtil.GetCallerOrLiked(allay);
			if (AllayUtil.IsPlayerStaring(allay, player))
				AllayUtil.RefreshCall(allay, player);
		}
	}

	@Inject( method="damage", at=@At("HEAD"), cancellable=true )
	private void allaybehave$SendOff(DamageSource source, float amount, CallbackInfoReturnable<Boolean> info){
		Entity attacker = source.getAttacker();
		if (source.getAttacker() == null
		|| !AllayUtil.IsLikedOrCaller(allay, attacker))
			return;

		boolean isCalled = AllayUtil.IsCalled(allay);
		boolean isLiked = allay.getBrain().hasMemoryModule(MemoryModuleType.LIKED_PLAYER);

		if (isCalled){
			AllayUtil.BreakCall(allay);
			SoundEvent sound = isLiked ? SoundEvents.ENTITY_ALLAY_ITEM_GIVEN : SoundEvents.ENTITY_ALLAY_ITEM_TAKEN;
			allay.getWorld().playSoundFromEntity(null, allay, sound, SoundCategory.NEUTRAL, 2, 1);
		}

		Vec3d  knockbackDir = attacker.getEyePos().subtract(allay.getEyePos());
		double knockbackStr = (isLiked && isCalled) ? 0.15 : 0.4;
		allay.takeKnockback(knockbackStr, knockbackDir.x, knockbackDir.z);
		info.setReturnValue(false);
	}

	@Inject( method="damage", at=@At("RETURN") )
	private void allaybehave$BreakOffCall(DamageSource dmg, float amount, CallbackInfoReturnable<Boolean> info){
		if (info.getReturnValue() || AllayUtil.IsCalled(allay))
			AllayUtil.BreakCall(allay);
	}

	@Override
	protected void allaybehave$DontPushAway(Entity other, CallbackInfo info){
		if (other instanceof PlayerEntity)
			info.cancel();
	}

}
