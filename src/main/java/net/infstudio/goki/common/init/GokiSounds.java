package net.infstudio.goki.common.init;

import com.mojang.serialization.MapCodec;
import net.infstudio.goki.common.handlers.GokiLootModifier;
import net.infstudio.goki.common.utils.Reference;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public class GokiSounds {
    private static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(Registries.SOUND_EVENT, Reference.MODID);
    private static final DeferredRegister<MapCodec<? extends IGlobalLootModifier>> MODIFIERS = DeferredRegister.create(NeoForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, Reference.MODID);
    public static final SoundEvent TREASURE = SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath(Reference.MODID, "treasure"));
    public static final SoundEvent MAGICIAN = SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath(Reference.MODID, "magician"));
    public static final SoundEvent REAPER = SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath(Reference.MODID, "reaper"));

    public static void register(IEventBus eventBus) {
        SOUNDS.register("treasure", () -> TREASURE);
        SOUNDS.register("magician", () -> MAGICIAN);
        SOUNDS.register("reaper", () -> REAPER);
        MODIFIERS.register("loot", () -> GokiLootModifier.CODEC);
        SOUNDS.register(eventBus);
        MODIFIERS.register(eventBus);
    }
}
