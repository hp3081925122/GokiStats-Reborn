package net.infstudio.goki.common.network.message;

import net.minecraft.network.FriendlyByteBuf;

public interface IMessage extends net.minecraft.network.protocol.common.custom.CustomPacketPayload {
    void fromBytes(FriendlyByteBuf buf);
    void toBytes(FriendlyByteBuf buf);
}
