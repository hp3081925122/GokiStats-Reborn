package net.infstudio.goki.client.network.handler;

import net.infstudio.goki.client.gui.GuiStats;
import net.infstudio.goki.common.network.message.S2COpenGui;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.function.Supplier;

public class PacketGuiClientHandler {
    public static void acceptOpenGui(S2COpenGui message, IPayloadContext context) {
        context.enqueueWork(() -> openGuiInternal(context));
    }

    @OnlyIn(Dist.CLIENT)
    public static void openGuiInternal(IPayloadContext context) {
        Minecraft.getInstance().setScreen(new GuiStats());
    }
}
