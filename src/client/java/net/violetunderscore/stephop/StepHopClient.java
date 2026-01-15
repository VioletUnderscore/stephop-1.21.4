package net.violetunderscore.stephop;

import net.fabricmc.api.ClientModInitializer;
import net.violetunderscore.stephop.config.ConfigWrapper;

public class StepHopClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
        ConfigWrapper.loadConfig();
	}
}