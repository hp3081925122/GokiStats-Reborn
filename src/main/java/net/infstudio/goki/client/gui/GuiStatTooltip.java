package net.infstudio.goki.client.gui;

import net.infstudio.goki.api.stat.StatBase;
import net.infstudio.goki.common.config.GokiConfig;
import net.infstudio.goki.common.utils.DataHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.List;

public class GuiStatTooltip {
    private final StatBase stat;
    private final Player player;

    public GuiStatTooltip(StatBase stat, Player player) {
        this.stat = stat;
        this.player = player;
    }

    public void render(GuiGraphics graphics, int anchorX, int anchorY, int buttonHeight, int screenWidth, int screenHeight) {
        UiSkin skin = UiSkin.get();
        UiSkin.Layout layout = skin.layout();
        var font = Minecraft.getInstance().font;
        int maxWidth = Math.max(1, Math.min(layout.tooltipMaxWidth, screenWidth - (layout.tooltipMargin + layout.tooltipPadding) * 2));
        List<Line> lines = new ArrayList<>();
        int level = DataHelper.getPlayerStatLevel(player, stat);
        add(lines, stat.getLocalizedName() + " L" + level, "tooltip_title", maxWidth);
        add(lines, stat.getLocalizedDescription(player), "tooltip_text", maxWidth);
        if (!stat.isEnabled()) add(lines, I18n.get("ui.gokistats_reborn.disabled"), "error", maxWidth);
        if (level >= stat.getLimit()) add(lines, I18n.get("ui.max"), "tooltip_value", maxWidth);
        else add(lines, I18n.get("ui.cost", (double) stat.getCost(level)), "tooltip_value", maxWidth);
        if (Screen.hasControlDown()) {
            int refund = level > 0 ? (int) (stat.getCost(level - 1) * GokiConfig.SERVER.globalRevertFactor.get()) : 0;
            add(lines, I18n.get("ui.return", (double) refund), "tooltip_value", maxWidth);
        }
        int reverted = DataHelper.getPlayerRevertStatLevel(player, stat);
        if (reverted > 0) {
            int limit = GokiConfig.SERVER.globalMaxRevertLevel.get();
            add(lines, I18n.get("ui.reverted", reverted, limit == -1 ? I18n.get("ui.infinite") : Integer.toString(limit)), "tooltip_value", maxWidth);
        }
        boolean canRevert = stat.isEnabled() && DataHelper.canPlayerRevertStat(player, stat);
        if (Screen.hasControlDown()) add(lines, I18n.get(canRevert ? "ui.revert" : "ui.norevert"), canRevert ? "action" : "error", maxWidth);
        else if (stat.isEnabled()) add(lines, I18n.get("ui.hover"), "action", maxWidth);
        int width = lines.stream().mapToInt(line -> font.width(line.text())).max().orElse(0) + layout.tooltipPadding * 2;
        int height = lines.size() * font.lineHeight + layout.tooltipPadding * 2;
        float scale = Math.min(1F, Math.max(1, screenHeight - layout.tooltipMargin * 2) / (float) height);
        int actualWidth = (int) Math.ceil(width * scale);
        int actualHeight = (int) Math.ceil(height * scale);
        int x = Math.max(layout.tooltipMargin, Math.min(anchorX - actualWidth / 2, screenWidth - actualWidth - layout.tooltipMargin));
        int y = anchorY - actualHeight - layout.tooltipMargin;
        if (y < layout.tooltipMargin) y = anchorY + buttonHeight + layout.tooltipMargin;
        y = Math.max(layout.tooltipMargin, Math.min(y, screenHeight - actualHeight - layout.tooltipMargin));
        graphics.flush();
        graphics.pose().pushPose();
        graphics.pose().translate(x, y, 400);
        graphics.pose().scale(scale, scale, 1F);
        skin.draw(graphics, "tooltip", 0, 0, width, height);
        int textY = layout.tooltipPadding;
        for (Line line : lines) {
            graphics.drawString(font, line.text(), layout.tooltipPadding, textY, skin.color(line.color()), false);
            textY += font.lineHeight;
        }
        graphics.flush();
        graphics.pose().popPose();
    }

    private void add(List<Line> lines, String text, String color, int width) {
        for (FormattedCharSequence line : Minecraft.getInstance().font.split(Component.literal(text), width))
            lines.add(new Line(line, color));
    }

    private record Line(FormattedCharSequence text, String color) {}
}
