package net.infstudio.goki.api.capability;

import net.infstudio.goki.api.stat.StatBase;
import net.infstudio.goki.api.stat.StatState;
import net.infstudio.goki.api.stat.StatStorage;
import net.infstudio.goki.common.utils.Reference;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
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
            .serialize(new IAttachmentSerializer<StatStorage>() {
                public StatStorage read(IAttachmentHolder holder, net.minecraft.world.level.storage.ValueInput input) {
                    return deserializeNBT(input.read(com.mojang.serialization.MapCodec.assumeMapUnsafe(CompoundTag.CODEC)).orElseGet(CompoundTag::new));
                }
                public boolean write(StatStorage storage, net.minecraft.world.level.storage.ValueOutput output) {
                    output.store(serializeNBT(storage));
                    return true;
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
        for (var key : nbt.keySet()) {
            var id = Identifier.tryParse(key);
            if (id == null) continue;
            if (id.getNamespace().equals(Reference.LEGACY_MODID)) id = Identifier.fromNamespaceAndPath(Reference.MODID, id.getPath());
            var stat = StatBase.REGISTRY.getValue(id);
            if (stat == null) continue;
            var tag = nbt.getCompoundOrEmpty(key);
            storage.stateMap.put(stat, new StatState(stat, tag.getIntOr("level", 0), tag.getIntOr("revertedLevel", 0)));
        }
        return storage;
    }
}
