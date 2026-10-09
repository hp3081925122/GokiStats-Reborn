package net.infstudio.goki.common.stat.tool;

import net.infstudio.goki.common.utils.Reference;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

public class StatMining extends ToolSpecificStat {
    private static final TagKey<Item> TAG = ItemTags.create(new ResourceLocation(Reference.MODID, "mining"));

    public StatMining(int id, String key, int limit) {
        super(id, key, limit);
    }

    @Override
    public TagKey<Item> getTag() {
        return TAG;
    }

    @Override
    public double getBonus(int level) {
        return getFinalBonus((float) Math.pow(level, 1.3D) * 0.01523F);
    }
}
