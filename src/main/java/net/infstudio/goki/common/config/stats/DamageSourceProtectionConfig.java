package net.infstudio.goki.common.config.stats;

import net.minecraftforge.common.ForgeConfigSpec;
import java.util.List;

public class DamageSourceProtectionConfig extends StatConfig {
    public final ForgeConfigSpec.ConfigValue<List<? extends String>> damageSources;

    public DamageSourceProtectionConfig(ForgeConfigSpec.Builder builder, String[] defaults) {
        super(builder);
        damageSources = builder.comment("生效的伤害来源标识 / Damage source message IDs affected by this skill")
                .defineListAllowEmpty("damageSources", List.of(defaults), value -> value instanceof String);
    }
}
