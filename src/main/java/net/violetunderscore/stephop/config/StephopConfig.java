package net.violetunderscore.stephop.config;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;

@Config(name = "stephop")
public class StephopConfig implements ConfigData {
    public float step_height = 1.2f;
    public boolean require_crouching = true;
    public boolean disable_weird_movement_checks = true;
}
