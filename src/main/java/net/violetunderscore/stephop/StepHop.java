package net.violetunderscore.stephop;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.JanksonConfigSerializer;
import net.fabricmc.api.ModInitializer;

import net.violetunderscore.stephop.config.ConfigWrapper;
import net.violetunderscore.stephop.config.StephopConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StepHop implements ModInitializer {
	public static final String MOD_ID = "stephop";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
        AutoConfig.register(StephopConfig.class, JanksonConfigSerializer::new);
        ConfigWrapper.loadConfig();
	}
}