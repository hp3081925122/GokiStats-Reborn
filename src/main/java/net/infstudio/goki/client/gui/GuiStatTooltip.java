package net.infstudio.goki.client.gui;

import net.infstudio.goki.GokiStats;
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
    private static boolean renderLayerLogged;
    private final StatBase stat;
    private final Player player;
    private final int padding = 4;
    private final Minecraft mc = Minecraft.getInstance();

    public GuiStatTooltip(StatBase stat, Player player) {
        this.stat = stat;
        this.player = player;
    }

    public void render(GuiGraphics graphics, int drawX, int drawY, float partialTicks) {
        render(graphics, drawX, drawY, partialTicks, GuiStats.STATUS_BUTTON_HEIGHT);
    }

    public void render(GuiGraphics graphics, int drawX, int drawY, float partialTicks, int buttonHeight) {
        if (mc.screen == null) return;
        int screenWidth = mc.screen.width;
        int screenHeight = mc.screen.height;
        int wrapWidth = Math.max(1, Math.min(260, screenWidth - padding * 2 - 4));
        List<Line> lines = new ArrayList<>();
        int level = DataHelper.getPlayerStatLevel(player, stat);
        add(lines, stat.getLocalizedName() + " " + I18n.get("ui.gokistats_reborn.level", level, stat.getLimit()), -13312, wrapWidth);
        add(lines, stat.getLocalizedDescription(player), -1, wrapWidth);
        add(lines, level >= stat.getLimit() ? I18n.get("ui.max") : I18n.get("ui.cost", stat.getCost(level)), -16724737, wrapWidth);
        if (Screen.hasControlDown()) {
            int refund = level > 0 ? (int) (stat.getCost(level - 1) * GokiConfig.SERVER.globalRevertFactor.get()) : 0;
            add(lines, I18n.get("ui.return", refund), -16724737, wrapWidth);
        }
        int reverted = DataHelper.getPlayerRevertStatLevel(player, stat);
        if (reverted > 0) {
            int limit = GokiConfig.SERVER.globalMaxRevertLevel.get();
            add(lines, I18n.get("ui.reverted", reverted, limit == -1 ? I18n.get("ui.infinite") : Integer.toString(limit)), -16724737, wrapWidth);
        }
        if (Screen.hasControlDown()) {
            boolean canRevert = DataHelper.canPlayerRevertStat(player, stat);
            add(lines, I18n.get(canRevert ? "ui.revert" : "ui.norevert"), canRevert ? 0xFF0467FF : 0xFFFB1A00, wrapWidth);
        } else add(lines, I18n.get("ui.hover"), 0xFF0467FF, wrapWidth);
        int width = lines.stream().mapToInt(line -> mc.font.width(line.text())).max().orElse(0) + padding * 2;
        int height = lines.size() * mc.font.lineHeight + padding * 2;
        float scale = Math.min(1F, Math.min(Math.max(1, screenWidth - 4) / (float) width, Math.max(1, screenHeight - 4) / (float) height));
        int actualWidth = (int) Math.ceil(width * scale);
        int actualHeight = (int) Math.ceil(height * scale);
        int x = Math.max(2, Math.min(drawX - actualWidth / 2, screenWidth - actualWidth - 2));
        int y = drawY - actualHeight - 2;
        if (y < 2) y = drawY + buttonHeight + 4;
        y = Math.max(2, Math.min(y, screenHeight - actualHeight - 2));
        graphics.flush();
        graphics.pose().pushPose();
        graphics.pose().translate(x, y, 400);
        graphics.pose().scale(scale, scale, 1F);
        try {
            graphics.fill(0, 0, width, height, -872415232);
            drawBorder(graphics, 0, height, width, height, -1);
            graphics.flush();
            int textY = padding;
            for (Line line : lines) {
                graphics.drawString(mc.font, line.text(), padding, textY, line.color());
                textY += mc.font.lineHeight;
            }
            if (!renderLayerLogged) {
                GokiStats.log.debug("Rendering skill tooltip on overlay layer: z=400, width={}, height={}, scale={}, x={}, y={}, base batch flushed", width, height, scale, x, y);
                renderLayerLogged = true;
            }
        } finally {
            try {
                graphics.flush();
            } finally {
                graphics.pose().popPose();
            }
        }
    }

    private void add(List<Line> lines, String text, int color, int width) {
        for (FormattedCharSequence line : mc.font.split(Component.literal(text), width)) lines.add(new Line(line, color));
    }

    private void drawBorder(GuiGraphics graphics, int x, int y, int width, int height, int color) {
        graphics.hLine(x - 1, x + width, y, color);
        graphics.hLine(x - 1, x + width, y - height, color);
        graphics.vLine(x - 1, y, y - height, color);
        graphics.vLine(x + width, y, y - height, color);
    }

    private record Line(FormattedCharSequence text, int color) {}
}
