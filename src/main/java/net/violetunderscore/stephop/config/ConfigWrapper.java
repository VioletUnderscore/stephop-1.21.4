package net.violetunderscore.stephop.config;

import me.shedaniel.autoconfig.AutoConfig;

public class ConfigWrapper {
    public static StephopConfig config;

    public static void loadConfig() {
        config = AutoConfig.getConfigHolder(StephopConfig.class).getConfig();
    }

    public static void saveConfig(StephopConfig stephopConfig) {
        AutoConfig.getConfigHolder(StephopConfig.class).setConfig(stephopConfig);
        AutoConfig.getConfigHolder(StephopConfig.class).save();
    }
}
