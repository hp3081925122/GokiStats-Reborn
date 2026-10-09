package net.infstudio.goki.client;

import net.infstudio.goki.client.gui.GuiStats;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.bus.api.SubscribeEvent;

@OnlyIn(Dist.CLIENT)
public class GokiKeyHandler {
    public static final KeyMapping statsMenu = new KeyMapping("ui.openmenu", 89, "key.categories.gokistats_reborn");

    @SubscribeEvent
    public void keyDown(InputEvent.Key event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null && mc.screen == null && statsMenu.consumeClick()) {
            mc.setScreen(new GuiStats());
        }
    }
}
