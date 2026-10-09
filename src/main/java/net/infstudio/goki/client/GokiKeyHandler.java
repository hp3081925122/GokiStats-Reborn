package net.infstudio.goki.client;

import net.infstudio.goki.client.gui.GuiStats;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@OnlyIn(Dist.CLIENT)
public class GokiKeyHandler {
    public static final KeyMapping statsMenu = new KeyMapping("ui.openmenu", 89, "key.categories.gokistats");

    @SubscribeEvent
    public void keyDown(InputEvent.Key event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null && mc.screen == null && statsMenu.consumeClick()) {
            mc.setScreen(new GuiStats());
        }
    }
}
