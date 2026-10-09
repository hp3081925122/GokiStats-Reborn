package net.infstudio.goki.common.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public interface Configurable<T> {
    T createConfig(ModConfigSpec.Builder builder);
}
