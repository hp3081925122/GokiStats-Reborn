package net.infstudio.goki.common.handlers;

import net.infstudio.goki.common.network.GokiPacketHandler;
import net.infstudio.goki.common.network.message.S2CSyncAll;
import net.infstudio.goki.common.utils.DataHelper;
import net.infstudio.goki.common.utils.Reference;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber(modid = Reference.MODID)
public class SyncEventHandler {
    public static void syncPlayerData(Player player) {
        if (!(player instanceof ServerPlayer)) return;
        DataHelper.resetMaxHealth(player);
        PacketDistributor.sendToPlayer((ServerPlayer) player, new S2CSyncAll(player));
    }

    @SubscribeEvent
    public static void playerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        syncPlayerData(event.getEntity());
    }

    @SubscribeEvent
    public static void playerChangedWorld(PlayerEvent.PlayerChangedDimensionEvent event) {
        syncPlayerData(event.getEntity());
    }

    public static void playerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        syncPlayerData(event.getEntity());
    }
}
