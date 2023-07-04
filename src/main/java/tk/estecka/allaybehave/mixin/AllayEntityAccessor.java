package tk.estecka.allaybehave.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

import com.google.common.collect.ImmutableList;

import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.passive.AllayEntity;

@Mixin(AllayEntity.class)
public interface AllayEntityAccessor {
	
	@Accessor("MEMORY_MODULES")
	static ImmutableList<MemoryModuleType<?>>	get_MEMORY_MODULES()
	{ throw new AssertionError(); }

	@Accessor("MEMORY_MODULES")
	@Mutable
	static void set_MEMORY_MODULES(ImmutableList<MemoryModuleType<?>> value)
	{ throw new AssertionError(); }
}
