package tk.estecka.allaybehave.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.passive.AllayEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import tk.estecka.allaybehave.AllayUtil;

@Mixin(AllayEntity.class)
public abstract class AllayEntityMixin
extends LivingEntity
{
	private final AllayEntity allay = (AllayEntity)(Object)this;

	AllayEntityMixin(EntityType<? extends AllayEntity> type, World world){
		super(type, world);
	}

	@Inject( method="tick", at=@At("TAIL") )
	private void	CheckForBeholdingPlayers(CallbackInfo info) {
		if (!allay.getWorld().isClient()) {
			PlayerEntity player = AllayUtil.GetBeholderOrLiked(allay);
			if (AllayUtil.IsPlayerBeholding(allay, player))
				AllayUtil.SetBeheld(allay, player);
		}
	}

	@Inject( method="damage", at=@At("HEAD"), cancellable=true )
	private void SendOff(DamageSource source, float amount, CallbackInfoReturnable<Boolean> info){
		Entity attacker = source.getAttacker();
		if (source.getAttacker() == null
		|| !AllayUtil.IsLikedOrBeholder(allay, attacker))
			return;

		boolean isBeheld = AllayUtil.IsBeheld(allay);
		boolean isLiked = allay.getBrain().hasMemoryModule(MemoryModuleType.LIKED_PLAYER);

		if (isBeheld){
			AllayUtil.BreakBeheld(allay);
			SoundEvent sound = isLiked ? SoundEvents.ENTITY_ALLAY_ITEM_GIVEN : SoundEvents.ENTITY_ALLAY_ITEM_TAKEN;
			allay.getWorld().playSoundFromEntity(null, allay, sound, SoundCategory.NEUTRAL, 2, 1);
		}

		Vec3d  knockbackDir = attacker.getEyePos().subtract(allay.getEyePos());
		double knockbackStr = (isLiked&&isBeheld) ? 0.15 : 0.4;
		allay.takeKnockback(knockbackStr, knockbackDir.x, knockbackDir.z);
		info.setReturnValue(false);
	}

	@Inject( method="damage", at=@At("RETURN") )
	private void BreakOffBeheld(DamageSource dmg, float amount, CallbackInfoReturnable<Boolean> info){
		if (info.getReturnValue() || AllayUtil.IsBeheld(allay))
			AllayUtil.BreakBeheld(allay);
	}

	@Override
	public void pushAway(Entity other){
		if (other instanceof PlayerEntity
		&& (allay.getWorld().isClient() || AllayUtil.IsLikedOrBeholder(allay, other)))
			return;
		else
			super.pushAwayFrom(other);
	}

}
