package net.infstudio.goki.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;

public final class ClientInput {
    public static boolean hasControlDown() {
        var window = Minecraft.getInstance().getWindow();
        return InputConstants.isKeyDown(window, 341) || InputConstants.isKeyDown(window, 345);
    }
}
