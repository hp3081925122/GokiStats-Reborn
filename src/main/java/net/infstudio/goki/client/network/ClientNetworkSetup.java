package net.infstudio.goki.client.network;

import net.infstudio.goki.client.network.handler.PacketGuiClientHandler;
import net.infstudio.goki.client.network.handler.PacketSyncClientHandler;
import net.infstudio.goki.common.network.GokiPacketHandler;
import net.infstudio.goki.common.network.message.S2COpenGui;
import net.infstudio.goki.common.network.message.S2CStatSync;
import net.infstudio.goki.common.network.message.S2CSyncAll;
import net.infstudio.goki.common.utils.Reference;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

@EventBusSubscriber(value = Dist.CLIENT, modid = Reference.MODID)
public final class ClientNetworkSetup {
    @SubscribeEvent
    public static void registerMessages(RegisterPayloadHandlersEvent event) {
        var registrar = event.registrar("1");
        registrar.playToClient(S2CSyncAll.TYPE, GokiPacketHandler.codec(S2CSyncAll::new), PacketSyncClientHandler::acceptSyncAll);
        registrar.playToClient(S2CStatSync.TYPE, GokiPacketHandler.codec(S2CStatSync::new), PacketSyncClientHandler::acceptSync);
        registrar.playToClient(S2COpenGui.TYPE, GokiPacketHandler.codec(S2COpenGui::new), PacketGuiClientHandler::acceptOpenGui);
    }
}
