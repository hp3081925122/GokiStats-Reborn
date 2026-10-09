package net.infstudio.goki.common.stat.tool;

import net.infstudio.goki.common.utils.Reference;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

public class StatChopping extends ToolSpecificStat {
    private static final TagKey<Item> TAG = ItemTags.create(Identifier.fromNamespaceAndPath(Reference.MODID, "chopping"));

    public StatChopping(int id, String key, int limit) {
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
