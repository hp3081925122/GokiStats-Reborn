package net.infstudio.goki.common.stat.tool;

import net.infstudio.goki.common.utils.Reference;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.*;

public class StatDigging extends ToolSpecificStat {
    private static final TagKey<Item> TAG = ItemTags.create(ResourceLocation.fromNamespaceAndPath(Reference.MODID, "digging"));

    public StatDigging(int id, String key, int limit) {
        super(id, key, limit);
    }

    @Override
    public TagKey<Item> getTag() {
        return TAG;
    }

    @Override
    public boolean isItemSupported(ItemStack item) {
        return super.isItemSupported(item);
    }

    @Override
    public double getBonus(int level) {
        return getFinalBonus((float) Math.pow(level, 1.3D) * 0.01523F);
    }
}
