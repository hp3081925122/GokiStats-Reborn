package net.infstudio.goki.client.gui;

import net.infstudio.goki.api.stat.StatBase;
import net.infstudio.goki.common.utils.DataHelper;
import net.infstudio.goki.common.utils.Reference;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

public class GuiStatButton extends Button {
    public StatBase stat;
    public Player player;
    public static final int INACTIVE_X = 0;
    public static final int ACTIVATED_X = 24;
    public static final int DISABLED_X = 48;
    public static final int MAXIMUM_X = 72;
    public final int id;
    private final Minecraft mc = Minecraft.getInstance();
    private final float labelScale;
    private final int labelWidth;

    public GuiStatButton(int id, int x, int y, int width, int height, StatBase stat, Player player, OnPress onPress) {
        this(id, x, y, width, height, stat, player, onPress, 1F, Math.max(width, 30));
    }

    public GuiStatButton(int id, int x, int y, int width, int height, StatBase stat, Player player, OnPress onPress, float labelScale, int labelWidth) {
        super(x, y, width, height, Component.literal(stat.getLocalizedName()), onPress, DEFAULT_NARRATION);
        this.id = id;
        this.stat = stat;
        this.player = player;
        this.labelScale = labelScale;
        this.labelWidth = labelWidth;
    }

    @Override
    public void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks) {
        int iconY = 24 * (stat.imageID % GuiStats.IMAGE_ROWS);
        int level = DataHelper.getPlayerStatLevel(player, stat);
        int limit = stat.getLimit();
        int iconX = isUnderMouse(mouseX, mouseY) || isFocused() ? ACTIVATED_X : INACTIVE_X;
        if (DataHelper.getXPTotal(player) < stat.getCost(level)) iconX = INACTIVE_X;
        if (level >= limit) iconX = MAXIMUM_X;
        if (net.infstudio.goki.client.ClientInput.hasControlDown()) iconX = DataHelper.canPlayerRevertStat(player, stat) ? ACTIVATED_X : DISABLED_X;
        if (!stat.isEnabled()) iconX = DISABLED_X;
        iconX += stat.imageID % 20 / 10 * 24 * 4;
        var texture = stat.imageID >= 20 ? Reference.RPG_ICON_2_TEXTURE_LOCATION : Reference.RPG_ICON_TEXTURE_LOCATION;
        graphics.blit(net.minecraft.client.renderer.RenderPipelines.GUI_TEXTURED, texture, getX(), getY(), iconX * 2F, iconY * 2F, width, height, 48, 48, 512, 512);
        String label = I18n.get("ui.gokistats_reborn.level", level, limit);
        int color = !stat.isEnabled() ? 0xFF999999 : level >= limit ? 0xFFFFCC00 : 0xFFFFFFFF;
        float scale = Math.min(labelScale, labelWidth / (float) Math.max(1, mc.font.width(label)));
        graphics.pose().pushMatrix();
        try {
            graphics.pose().translate(getX() + width / 2F, getY() + height + Math.max(1, Math.round(3 * labelScale)));
            graphics.pose().scale(scale, scale);
            graphics.centeredText(mc.font, label, 0, 0, color);
        } finally {
            graphics.pose().popMatrix();
        }
        setMessage(Component.literal(stat.getLocalizedName() + " " + label));
    }


    public boolean isUnderMouse(int mouseX, int mouseY) {
        return visible && mouseX >= getX() && mouseY >= getY() && mouseX < getX() + width && mouseY < getY() + height;
    }

    public boolean mousePressed(Minecraft minecraft, int mouseX, int mouseY) {
        return active && isUnderMouse(mouseX, mouseY);
    }

    public String getHoverMessage(int which) {
        return which == 0 ? stat.getLocalizedName() + " " + I18n.get("ui.gokistats_reborn.level", DataHelper.getPlayerStatLevel(player, stat), stat.getLimit())
                : stat.getLocalizedDescription(player);
    }
}
