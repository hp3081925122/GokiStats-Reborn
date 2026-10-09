package net.infstudio.goki.common.stat;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.infstudio.goki.api.stat.Stat;
import net.infstudio.goki.api.stat.StatBase;
import net.infstudio.goki.common.utils.DataHelper;
import net.infstudio.goki.common.utils.Reference;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.registries.DeferredRegister;
import java.util.function.Supplier;

public record StatLootCondition(Stat stat, int minLevel) implements LootItemCondition {
    public static final MapCodec<StatLootCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Identifier.CODEC.xmap(id -> StatBase.REGISTRY.getValue(id), Stat::getRegistryName).fieldOf("stat").forGetter(StatLootCondition::stat),
            Codec.INT.fieldOf("minLevel").forGetter(StatLootCondition::minLevel)).apply(instance, StatLootCondition::new));
    public static final DeferredRegister<MapCodec<? extends LootItemCondition>> CONDITIONS = DeferredRegister.create(Registries.LOOT_CONDITION_TYPE, Reference.MODID);
    public static final Supplier<MapCodec<? extends LootItemCondition>> STAT_LOOT_CONDITION = CONDITIONS.register("stat_level", () -> CODEC);

    public MapCodec<? extends LootItemCondition> codec() { return CODEC; }
    public boolean test(LootContext context) {
        return context.getOptionalParameter(LootContextParams.THIS_ENTITY) instanceof Player player
                && stat != null && DataHelper.getPlayerStatLevel(player, stat) >= minLevel;
    }
}
