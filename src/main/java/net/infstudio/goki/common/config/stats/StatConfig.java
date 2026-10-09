package net.infstudio.goki.common.config.stats;

import net.neoforged.neoforge.common.ModConfigSpec;

public class StatConfig {
    public ModConfigSpec.DoubleValue costMultiplier;
    public ModConfigSpec.IntValue maxLevel;
    public ModConfigSpec.DoubleValue bonusMultiplier;

    public StatConfig(ModConfigSpec.Builder builder) {
        costMultiplier = builder.comment("此技能的升级经验倍率 / Cost multiplier for this stat")
                .defineInRange("costMultiplier", 1d, 0d, Double.MAX_VALUE);
        maxLevel = builder.comment("此技能的最高等级，-1 使用默认值，0 禁用 / Max Level Limit for this stat, -1 for leave it as-is, 0 for disable this stat.")
                .defineInRange("maxLevel", -1, -1, Integer.MAX_VALUE);
        bonusMultiplier = builder.comment("此技能的效果倍率 / Bonus multiplier for this stat")
                .defineInRange("bonusMultiplier", 1d, 0d, Double.MAX_VALUE);
    }
}
