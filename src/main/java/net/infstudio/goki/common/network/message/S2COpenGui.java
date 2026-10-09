package net.infstudio.goki.common.network.message;

import net.minecraft.network.FriendlyByteBuf;

public class S2COpenGui implements IMessage {
    public static final net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type<S2COpenGui> TYPE = new net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type<>(net.minecraft.resources.Identifier.fromNamespaceAndPath(net.infstudio.goki.common.utils.Reference.MODID, "s2copengui"));
    public net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type<S2COpenGui> type() { return TYPE; }
    public int ID;

    public S2COpenGui() {
    }

    public S2COpenGui(int ID) {
        this.ID = ID;
    }

    @Override
    public void fromBytes(FriendlyByteBuf buf) {
        ID = buf.readInt();
    }

    @Override
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeInt(ID);
    }
}
