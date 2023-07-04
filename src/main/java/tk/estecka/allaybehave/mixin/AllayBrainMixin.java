package tk.estecka.allaybehave.mixin;

import java.util.LinkedList;
import java.util.Optional;
import java.util.Set;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.Activity;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.EntityLookTarget;
import net.minecraft.entity.ai.brain.LookTarget;
import net.minecraft.entity.ai.brain.MemoryModuleState;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.task.GiveInventoryToLookTargetTask;
import net.minecraft.entity.ai.brain.task.Task;
import net.minecraft.entity.ai.brain.task.WalkTowardsLookTargetTask;
import net.minecraft.entity.passive.AllayBrain;
import net.minecraft.entity.passive.AllayEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import tk.estecka.allaybehave.AllayUtil;
import tk.estecka.allaybehave.OffsetEntityLookTarget;

@Mixin(AllayBrain.class)
public abstract class AllayBrainMixin
{
	@Shadow static private Optional<LookTarget> getLikedLookTarget(LivingEntity allay) { throw new AssertionError(); }
	@Shadow static private Optional<LookTarget> getLookTarget(LivingEntity allay) { throw new AssertionError(); }

	static private Optional<LookTarget>	getBeholderLookTarget(LivingEntity allay){
		Optional<PlayerEntity> beholder = AllayUtil.GetBeholder(allay);
		if (beholder.isEmpty())
			return Optional.empty();
		else
			return Optional.of(new EntityLookTarget(beholder.get(), true));
	}


	static private Task<? super AllayEntity> CreatePlayerStareTask(){
		return WalkTowardsLookTargetTask.create(
			AllayBrainMixin::getBeholderLookTarget,
			AllayUtil::IsBeheld,
			4, 1, 1.65f
		);
	}

	@Inject(
		method = "rememberNoteBlock",
		at = @At("HEAD"),
		cancellable = true
	)
	static private void	refuseWhenBeheld(LivingEntity allay, BlockPos pos, CallbackInfo info){
		if (AllayUtil.IsBeheld(allay))
			info.cancel();
	}

	@Inject(
		method = "getLookTarget",
		at = @At("HEAD"),
		cancellable = true
	)
	static private void	getBeholder(LivingEntity allay, CallbackInfoReturnable<Optional<LookTarget>> info) {
		Optional<LookTarget> beholder = getBeholderLookTarget(allay);
		if (beholder.isPresent())
			info.setReturnValue(beholder);
	}

	@Redirect(
		method = "addIdleActivities",
		at = @At(
			value = "INVOKE",
			target = "net/minecraft/entity/ai/brain/Brain.setTaskList (Lnet/minecraft/entity/ai/brain/Activity;Lcom/google/common/collect/ImmutableList;Ljava/util/Set;)V"
		)
	)
	static private void AddCustomActivities(Brain<AllayEntity> brain, Activity activity, ImmutableList<? extends Pair<Integer, ? extends Task<? super AllayEntity>>> indexedTasks, Set<Pair<MemoryModuleType<?>, MemoryModuleState>> requiredMemories) 
	{
		var newlist = new LinkedList<Pair<Integer, ? extends Task<? super AllayEntity>>>();
		
		int i=0;
		newlist.add(Pair.of(i++, CreatePlayerStareTask()));
		for (var pair : indexedTasks){
			Task<? super AllayEntity> task = pair.getSecond();
			if (task instanceof GiveInventoryToLookTargetTask)
				task = new GiveInventoryToLookTargetTask<>(AllayBrainMixin::GetUppedLookTarget, 2.25f, 20);
			newlist.add(Pair.of(i++, task));
		}
		
		brain.setTaskList(activity, ImmutableList.copyOf(newlist), requiredMemories);
	}

	static private Optional<LookTarget> GetUppedLookTarget(LivingEntity allay){
		Optional<LookTarget> t = getLookTarget(allay);
		if (t.isEmpty())
			return t;

		LookTarget lt = t.get();
		if (!(lt instanceof EntityLookTarget))
			return t;

		return Optional.of(new OffsetEntityLookTarget((EntityLookTarget)lt, 1));
	}

	// @Redirect(
	// 	method = "addCoreActivities",
	// 	at = @At(
	// 		value = "INVOKE",
	// 		target = "net/minecraft/entity/ai/brain/Brain.setTaskList (Lnet/minecraft/entity/ai/brain/Activity;ILcom/google/common/collect/ImmutableList;)V"
	// 	)
	// )
	// static private void AddCustomActivities(Brain<AllayEntity> brain, Activity activity, int begin, ImmutableList<? extends Task<? super AllayEntity>> builtin_list) 
	// {
	// 	var newlist = new LinkedList<Task<? super AllayEntity>>();

	// 	newlist.add(CreatePlayerStareTask());
	// 	newlist.addAll(builtin_list);

	// 	brain.setTaskList(activity, begin, ImmutableList.copyOf(newlist));
	// }

}
