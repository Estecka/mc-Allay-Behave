package tk.estecka.allaybehave;

import net.fabricmc.api.ModInitializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AllayBehave implements ModInitializer {
	public static final Logger LOGGER = LoggerFactory.getLogger("Allay, Behave!");

	@Override
	public void onInitialize() {
		AllayUtil.Initialize();
	}
}
