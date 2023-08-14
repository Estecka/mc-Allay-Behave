package tk.estecka.allaybehave;

import net.fabricmc.api.ModInitializer;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import tk.estecka.allaybehave.mixin.IAllayEntityMixin;
import java.util.LinkedList;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.common.collect.ImmutableList;

public class AllayBehave implements ModInitializer {
	public static final Logger LOGGER = LoggerFactory.getLogger("Allay, Behave!");

	static public final MemoryModuleType<PlayerEntity> IS_BEHELD = Registry.register(
		Registries.MEMORY_MODULE_TYPE,
		new Identifier("allaybehave", "is_beheld"),
		new MemoryModuleType<PlayerEntity>(Optional.empty())
	);

	@Override
	public void onInitialize() {
		var oldList = IAllayEntityMixin.get_MEMORY_MODULES();
		var newList = new LinkedList<MemoryModuleType<?>>(oldList);

		newList.add(IS_BEHELD);

		IAllayEntityMixin.set_MEMORY_MODULES(ImmutableList.copyOf(newList));
	}
}
