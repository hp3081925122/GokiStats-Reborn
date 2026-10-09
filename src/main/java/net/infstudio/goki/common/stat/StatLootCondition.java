package net.infstudio.goki.common.stat;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.infstudio.goki.api.stat.Stat;
import net.infstudio.goki.api.stat.StatBase;
import net.infstudio.goki.common.utils.DataHelper;
import net.infstudio.goki.common.utils.Reference;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import net.neoforged.neoforge.registries.DeferredRegister;
import java.util.function.Supplier;

public record StatLootCondition(Stat stat, int minLevel) implements LootItemCondition {
    public static final MapCodec<StatLootCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            ResourceLocation.CODEC.xmap(id -> StatBase.REGISTRY.get(id), Stat::getRegistryName).fieldOf("stat").forGetter(StatLootCondition::stat),
            Codec.INT.fieldOf("minLevel").forGetter(StatLootCondition::minLevel)).apply(instance, StatLootCondition::new));
    public static final DeferredRegister<LootItemConditionType> CONDITIONS = DeferredRegister.create(Registries.LOOT_CONDITION_TYPE, Reference.MODID);
    public static final Supplier<LootItemConditionType> STAT_LOOT_CONDITION = CONDITIONS.register("stat_level", () -> new LootItemConditionType(CODEC));

    public LootItemConditionType getType() { return STAT_LOOT_CONDITION.get(); }
    public boolean test(LootContext context) {
        return context.getParamOrNull(LootContextParams.THIS_ENTITY) instanceof Player player
                && stat != null && DataHelper.getPlayerStatLevel(player, stat) >= minLevel;
    }
}
