package net.infstudio.goki.common.network.message;

import net.minecraft.network.FriendlyByteBuf;

public class S2CStatSync implements IMessage {
    public static final net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type<S2CStatSync> TYPE = new net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type<>(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(net.infstudio.goki.common.utils.Reference.MODID, "s2cstatsync"));
    public net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type<S2CStatSync> type() { return TYPE; }
    public int stat, amount, reverted;

    public S2CStatSync() {
    }

    public S2CStatSync(int stat, int amount, int reverted) {
        this.stat = stat;
        this.amount = amount;
        this.reverted = reverted;
    }

    @Override
    public void fromBytes(FriendlyByteBuf buf) {
        stat = buf.readInt();
        amount = buf.readInt();
        reverted = buf.readInt();
    }

    @Override
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeInt(stat);
        buf.writeInt(amount);
        buf.writeInt(reverted);
    }
}
