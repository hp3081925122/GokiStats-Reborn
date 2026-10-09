package net.infstudio.goki.common.network;

import net.infstudio.goki.common.network.handler.PacketSyncHandler;
import net.infstudio.goki.common.network.message.*;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import java.util.function.Supplier;

public class GokiPacketHandler {
    public static <T extends IMessage> StreamCodec<RegistryFriendlyByteBuf, T> codec(Supplier<T> factory) {
        return StreamCodec.of((buffer, message) -> message.toBytes(buffer), buffer -> {
            T message = factory.get();
            message.fromBytes(buffer);
            return message;
        });
    }
    public static void registerMessages(RegisterPayloadHandlersEvent event) {
        var registrar = event.registrar("1");
        registrar.playToServer(C2SStatSync.TYPE, codec(C2SStatSync::new), PacketSyncHandler::acceptC2S);
        registrar.playToServer(C2SRequestStatSync.TYPE, codec(C2SRequestStatSync::new), PacketSyncHandler::onRequestSyncAll);
    }
}
