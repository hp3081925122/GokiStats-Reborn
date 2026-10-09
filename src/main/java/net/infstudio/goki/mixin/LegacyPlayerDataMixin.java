package net.infstudio.goki.mixin;

import net.infstudio.goki.GokiStats;
import net.infstudio.goki.api.capability.CapabilityStat;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public abstract class LegacyPlayerDataMixin {
    @Inject(method = "load", at = @At("TAIL"))
    private void gokistatsReborn$importLegacy(net.minecraft.world.level.storage.ValueInput input, CallbackInfo callback) {
        if ((Object) this instanceof Player player
                && !input.childOrEmpty("neoforge:attachments").keySet().contains("gokistats_reborn:stat_storage")) {
            var legacy = input.childOrEmpty("ForgeCaps");
            if (legacy.keySet().contains("gokistats:stat_storage")) {
                var storage = CapabilityStat.deserializeNBT(legacy.read("gokistats:stat_storage", CompoundTag.CODEC).orElseGet(CompoundTag::new));
                player.setData(CapabilityStat.STAT, storage);
                GokiStats.log.debug("Imported {} legacy Forge skill states for {}", storage.stateMap.size(), player.getUUID());
            }
        }
    }
}
