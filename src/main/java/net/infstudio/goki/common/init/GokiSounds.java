package net.infstudio.goki.common.init;

import com.mojang.serialization.Codec;
import net.infstudio.goki.common.handlers.GokiLootModifier;
import net.infstudio.goki.common.utils.Reference;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class GokiSounds {
    private static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, Reference.MODID);
    private static final DeferredRegister<Codec<? extends IGlobalLootModifier>> MODIFIERS = DeferredRegister.create(ForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, Reference.MODID);
    public static final SoundEvent TREASURE = SoundEvent.createVariableRangeEvent(new ResourceLocation(Reference.MODID, "treasure"));
    public static final SoundEvent MAGICIAN = SoundEvent.createVariableRangeEvent(new ResourceLocation(Reference.MODID, "magician"));
    public static final SoundEvent REAPER = SoundEvent.createVariableRangeEvent(new ResourceLocation(Reference.MODID, "reaper"));

    public static void register(IEventBus eventBus) {
        SOUNDS.register("treasure", () -> TREASURE);
        SOUNDS.register("magician", () -> MAGICIAN);
        SOUNDS.register("reaper", () -> REAPER);
        MODIFIERS.register("loot", () -> GokiLootModifier.CODEC);
        SOUNDS.register(eventBus);
        MODIFIERS.register(eventBus);
    }
}
