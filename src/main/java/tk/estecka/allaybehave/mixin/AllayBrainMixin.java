package tk.estecka.allaybehave.mixin;

import java.util.ArrayList;
import java.util.Optional;
import java.util.function.Function;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.EntityLookTarget;
import net.minecraft.entity.ai.brain.LookTarget;
import net.minecraft.entity.ai.brain.task.Task;
import net.minecraft.entity.ai.brain.task.WalkTowardsLookTargetTask;
import net.minecraft.entity.passive.AllayBrain;
import net.minecraft.entity.passive.AllayEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import tk.estecka.allaybehave.AllayUtil;
import tk.estecka.allaybehave.OffsetLookTarget;

@Mixin(AllayBrain.class)
public abstract class AllayBrainMixin
{
	static private Optional<LookTarget>	getBeholderLookTarget(LivingEntity allay){
		Optional<PlayerEntity> beholder = AllayUtil.GetBeholder(allay);
		if (beholder.isEmpty())
			return Optional.empty();
		else
			return Optional.of(new EntityLookTarget(beholder.get(), true));
	}

	@Inject( method="rememberNoteBlock", at=@At("HEAD"), cancellable=true )
	static private void	refuseWhenBeheld(LivingEntity allay, BlockPos pos, CallbackInfo info){
		if (AllayUtil.IsBeheld(allay))
			info.cancel();
	}

	@Inject( method="getLookTarget", at=@At("HEAD"), cancellable=true )
	static private void	getBeholder(LivingEntity allay, CallbackInfoReturnable<Optional<LookTarget>> info) {
		Optional<LookTarget> beholder = getBeholderLookTarget(allay);
		if (beholder.isPresent())
			info.setReturnValue(beholder);
	}

	@ModifyArg( method="addIdleActivities", index=1, at=@At(value="INVOKE", target="net/minecraft/entity/ai/brain/Brain.setTaskList (Lnet/minecraft/entity/ai/brain/Activity;Lcom/google/common/collect/ImmutableList;Ljava/util/Set;)V") )
	static private ImmutableList<? extends Pair<Integer, ? extends Task<? super AllayEntity>>> allaybehave$AddCustomActivities(ImmutableList<? extends Pair<Integer, ? extends Task<? super AllayEntity>>> indexedTasks) {
		var newlist = new ArrayList<Pair<Integer, ? extends Task<? super AllayEntity>>>(indexedTasks.size() + 1);
		var followTask = WalkTowardsLookTargetTask.create(
			AllayBrainMixin::getBeholderLookTarget,
			AllayUtil::IsBeheld,
			4, 1, 1.65f
		);
		
		int i=0;
		newlist.add(Pair.of(i++, followTask));
		for (var pair : indexedTasks)
			newlist.add(Pair.of(i++, pair.getSecond()));

		return ImmutableList.copyOf(newlist);
	}

	@ModifyArg( method="addIdleActivities", index=0, at=@At(value="invoke", target="net/minecraft/entity/ai/brain/task/GiveInventoryToLookTargetTask.<init> (Ljava/util/function/Function;FI)V") )
	static private Function<LivingEntity, Optional<LookTarget>>	allaybehave$OffsetGiveTarget(Function<LivingEntity, Optional<LookTarget>> targetSupplier){
		return (entity) -> {
			var target = targetSupplier.apply(entity);
			if (target.isPresent())
				return Optional.of(new OffsetLookTarget(target.get(), 1));
			return target;
		};
	}

}
