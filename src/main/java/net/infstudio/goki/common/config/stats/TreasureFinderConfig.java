package net.infstudio.goki.common.config.stats;

import net.infstudio.goki.common.stat.tool.TreasureFinderEntry;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.ArrayList;
import java.util.List;

public class TreasureFinderConfig extends StatConfig {
    public List<TreasureFinderEntry> entries = new ArrayList<>();

    public TreasureFinderConfig(ModConfigSpec.Builder builder) {
        super(builder);
    }
}
