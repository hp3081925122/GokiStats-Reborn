package net.infstudio.goki.common.network.message;

import net.minecraft.network.FriendlyByteBuf;

public class C2SRequestStatSync implements IMessage {
    public static final net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type<C2SRequestStatSync> TYPE = new net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type<>(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(net.infstudio.goki.common.utils.Reference.MODID, "c2srequeststatsync"));
    public net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type<C2SRequestStatSync> type() { return TYPE; }
    @Override
    public void fromBytes(FriendlyByteBuf buf) {
    }

    @Override
    public void toBytes(FriendlyByteBuf buf) {
    }
}
