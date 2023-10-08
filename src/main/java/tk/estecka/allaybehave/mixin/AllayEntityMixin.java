package tk.estecka.allaybehave.mixin;

import org.spongepowered.asm.mixin.Mixin;
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
import tk.estecka.allaybehave.AllayUtil;

@Mixin(AllayEntity.class)
public abstract class AllayEntityMixin
extends LivingEntityMixin
{
	private final AllayEntity allaybehave$this = (AllayEntity)(Object)this;

	@Inject( method="tick", at=@At("TAIL") )
	private void	allaybehave$CheckForBeholdingPlayers(CallbackInfo info) {
		if (!allaybehave$this.getWorld().isClient()) {
			PlayerEntity player = AllayUtil.GetBeholderOrLiked(allaybehave$this);
			if (AllayUtil.IsPlayerBeholding(allaybehave$this, player))
				AllayUtil.SetBeheld(allaybehave$this, player);
		}
	}

	@Inject( method="damage", at=@At("HEAD"), cancellable=true )
	private void allaybehave$SendOff(DamageSource source, float amount, CallbackInfoReturnable<Boolean> info){
		Entity attacker = source.getAttacker();
		if (source.getAttacker() == null
		|| !AllayUtil.IsLikedOrBeholder(allaybehave$this, attacker))
			return;

		boolean isBeheld = AllayUtil.IsBeheld(allaybehave$this);
		boolean isLiked = allaybehave$this.getBrain().hasMemoryModule(MemoryModuleType.LIKED_PLAYER);

		if (isBeheld){
			AllayUtil.BreakBeheld(allaybehave$this);
			SoundEvent sound = isLiked ? SoundEvents.ENTITY_ALLAY_ITEM_GIVEN : SoundEvents.ENTITY_ALLAY_ITEM_TAKEN;
			allaybehave$this.getWorld().playSoundFromEntity(null, allaybehave$this, sound, SoundCategory.NEUTRAL, 2, 1);
		}

		Vec3d  knockbackDir = attacker.getEyePos().subtract(allaybehave$this.getEyePos());
		double knockbackStr = (isLiked&&isBeheld) ? 0.15 : 0.4;
		allaybehave$this.takeKnockback(knockbackStr, knockbackDir.x, knockbackDir.z);
		info.setReturnValue(false);
	}

	@Inject( method="damage", at=@At("RETURN") )
	private void allaybehave$BreakOffBeheld(DamageSource dmg, float amount, CallbackInfoReturnable<Boolean> info){
		if (info.getReturnValue() || AllayUtil.IsBeheld(allaybehave$this))
			AllayUtil.BreakBeheld(allaybehave$this);
	}

	@Override
	protected void allaybehave$DontPushAway(Entity other, CallbackInfo info){
		if (other instanceof PlayerEntity)
			info.cancel();
	}

}
