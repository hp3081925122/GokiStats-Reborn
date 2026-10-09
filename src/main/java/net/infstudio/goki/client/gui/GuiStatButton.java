package net.infstudio.goki.client.gui;

import net.infstudio.goki.api.stat.StatBase;
import net.infstudio.goki.common.utils.DataHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

public class GuiStatButton extends Button {
    public final StatBase stat;
    public final Player player;
    public final int id;

    public GuiStatButton(int id, int x, int y, int width, int height, StatBase stat, Player player, OnPress onPress) {
        super(x, y, width, height, Component.literal(stat.getLocalizedName()), onPress, DEFAULT_NARRATION);
        this.id = id;
        this.stat = stat;
        this.player = player;
    }

    @Override
    public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        UiSkin skin = UiSkin.get();
        int level = DataHelper.getPlayerStatLevel(player, stat);
        boolean highlighted = isUnderMouse(mouseX, mouseY) || isFocused();
        String state;
        if (!stat.isEnabled()) state = "disabled";
        else if (Screen.hasControlDown()) state = DataHelper.canPlayerRevertStat(player, stat) ? "refund" : "disabled";
        else if (level >= stat.getLimit()) state = "maximum";
        else if (DataHelper.getXPTotal(player) < stat.getCost(level)) state = "unaffordable";
        else state = highlighted ? "hover" : "normal";
        skin.draw(graphics, "button_" + state, getX(), getY(), width, height);
        int iconSize = skin.layout().iconSize;
        skin.drawIcon(graphics, stat.imageID, state, getX() + (width - iconSize) / 2, getY() + (height - iconSize) / 2, iconSize);
        String label = !stat.isEnabled() ? "X" : level >= stat.getLimit() ? "*" + level + "*" : Integer.toString(level);
        graphics.drawCenteredString(Minecraft.getInstance().font, label, getX() + width / 2, getY() + height + skin.layout().levelGap,
                skin.color(stat.isEnabled() && level >= stat.getLimit() ? "maximum" : "level"));
        setMessage(Component.literal(stat.getLocalizedName() + " L" + level));
    }

    public boolean isUnderMouse(int x, int y) {
        return visible && x >= getX() && y >= getY() && x < getX() + width && y < getY() + height;
    }
}
