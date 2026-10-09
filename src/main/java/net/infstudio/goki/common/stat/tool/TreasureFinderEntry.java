package net.infstudio.goki.common.stat.tool;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public class TreasureFinderEntry {
    public String block;
    public String item;
    public int minimumLevel;
    public int chance;

    public TreasureFinderEntry(Block block, Item item, int minimumLevel, int chance) {
        this.block = BuiltInRegistries.BLOCK.getKey(block).toString();
        this.item = BuiltInRegistries.ITEM.getKey(item).toString();
        this.minimumLevel = minimumLevel;
        this.chance = chance;
    }

    public Block getBlock() {
        return BuiltInRegistries.BLOCK.get(ResourceLocation.parse(block));
    }

    public Item getItem() {
        return BuiltInRegistries.ITEM.get(ResourceLocation.parse(item));
    }
}
