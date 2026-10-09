package net.infstudio.goki.api.capability;

import net.infstudio.goki.api.stat.StatBase;
import net.infstudio.goki.api.stat.StatState;
import net.infstudio.goki.api.stat.StatStorage;
import net.infstudio.goki.common.utils.Reference;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.attachment.IAttachmentSerializer;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import java.util.function.Supplier;

public class CapabilityStat {
    private static final DeferredRegister<AttachmentType<?>> TYPES = DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, Reference.MODID);
    public static final Supplier<AttachmentType<StatStorage>> STAT = TYPES.register("stat_storage", () -> AttachmentType.builder(StatStorage::new)
            .serialize(new IAttachmentSerializer<CompoundTag, StatStorage>() {
                public StatStorage read(IAttachmentHolder holder, CompoundTag tag, HolderLookup.Provider provider) {
                    return deserializeNBT(tag);
                }
                public CompoundTag write(StatStorage storage, HolderLookup.Provider provider) {
                    return serializeNBT(storage);
                }
            }).build());

    public static void register(IEventBus bus) { TYPES.register(bus); }

    public static CompoundTag serializeNBT(StatStorage storage) {
        var result = new CompoundTag();
        storage.stateMap.forEach((stat, state) -> {
            var tag = new CompoundTag();
            tag.putInt("level", state.level);
            tag.putInt("revertedLevel", state.revertedLevel);
            result.put(stat.getRegistryName().toString(), tag);
        });
        return result;
    }

    public static StatStorage deserializeNBT(CompoundTag nbt) {
        var storage = new StatStorage();
        for (var key : nbt.getAllKeys()) {
            var id = ResourceLocation.tryParse(key);
            if (id == null) continue;
            if (id.getNamespace().equals(Reference.LEGACY_MODID)) id = ResourceLocation.fromNamespaceAndPath(Reference.MODID, id.getPath());
            var stat = StatBase.REGISTRY.get(id);
            if (stat == null) continue;
            var tag = nbt.getCompound(key);
            storage.stateMap.put(stat, new StatState(stat, tag.getInt("level"), tag.getInt("revertedLevel")));
        }
        return storage;
    }
}
