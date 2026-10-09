package net.infstudio.goki.common.handlers;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.infstudio.goki.api.stat.Stats;
import net.infstudio.goki.common.init.GokiSounds;
import net.infstudio.goki.common.stat.tool.StatMiningMagician;
import net.infstudio.goki.common.utils.DataHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.common.loot.LootModifier;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nonnull;

public class GokiLootModifier extends LootModifier {
    public static final Codec<GokiLootModifier> CODEC = RecordCodecBuilder.create(instance -> codecStart(instance).apply(instance, GokiLootModifier::new));
    private static final ThreadLocal<Boolean> APPLYING_TREASURE = ThreadLocal.withInitial(() -> false);

    protected GokiLootModifier(LootItemCondition[] conditions) {
        super(conditions);
    }

    @Override
    public Codec<? extends IGlobalLootModifier> codec() {
        return CODEC;
    }

    @Nonnull
    @Override
    protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
        if (APPLYING_TREASURE.get() || !context.hasParam(LootContextParams.BLOCK_STATE)
                || !context.hasParam(LootContextParams.ORIGIN)
                || !(context.getParamOrNull(LootContextParams.THIS_ENTITY) instanceof Player player)) {
            return generatedLoot;
        }
        var block = context.getParam(LootContextParams.BLOCK_STATE).getBlock();
        var position = BlockPos.containing(context.getParam(LootContextParams.ORIGIN));
        if (Stats.TREASURE_FINDER.isEnabled() && DataHelper.getPlayerStatLevel(player, Stats.TREASURE_FINDER) > 0) {
            var blockId = ForgeRegistries.BLOCKS.getKey(block);
            var id = new ResourceLocation(blockId.getNamespace(), "treasure_finder/" + blockId.getPath());
            var table = context.getResolver().getLootTable(id);
            var items = new ObjectArrayList<ItemStack>();
            APPLYING_TREASURE.set(true);
            try {
                table.getRandomItems(context, items::add);
            } finally {
                APPLYING_TREASURE.remove();
            }
            generatedLoot.addAll(items);
            if (!items.isEmpty()) {
                player.level().playSound(null, position, GokiSounds.TREASURE, SoundSource.BLOCKS, 1.0F, 1.0F);
            }
        }
        if (Stats.MINING_MAGICIAN.isEnabled() && DataHelper.getPlayerStatLevel(player, Stats.MINING_MAGICIAN) > 0 && Stats.MINING_MAGICIAN.isEffectiveOn(block)) {
            boolean magicHappened = false;
            int originalSize = generatedLoot.size();
            for (int i = 0; i < originalSize; i++) {
                if (player.getRandom().nextDouble() * 100.0D > Stats.MINING_MAGICIAN.getBonus(player)) continue;
                var item = generatedLoot.get(i);
                if (item.getItem() instanceof BlockItem blockItem && blockItem.getBlock() == block) {
                    var extra = ForgeRegistries.BLOCKS.tags().getTag(StatMiningMagician.MAGICIAN_ORE).getRandomElement(context.getRandom());
                    if (extra.isPresent()) {
                        var stack = new ItemStack(extra.get(), item.getCount());
                        if (!stack.isEmpty()) {
                            generatedLoot.add(stack);
                            magicHappened = true;
                        }
                    }
                } else {
                    var extra = ForgeRegistries.ITEMS.tags().getTag(StatMiningMagician.MAGICIAN_ITEM).getRandomElement(context.getRandom());
                    if (extra.isPresent()) {
                        generatedLoot.add(new ItemStack(extra.get(), item.getCount()));
                        magicHappened = true;
                    }
                    break;
                }
            }
            if (magicHappened) {
                player.level().playSound(null, position, GokiSounds.MAGICIAN, SoundSource.BLOCKS, 0.3F, 1.0F);
            }
        }
        return generatedLoot;
    }
}
