package net.infstudio.goki.client.gui;

import net.neoforged.neoforge.network.PacketDistributor;
import net.infstudio.goki.GokiStats;
import net.infstudio.goki.api.stat.StatBase;
import net.infstudio.goki.common.network.GokiPacketHandler;
import net.infstudio.goki.common.network.message.C2SRequestStatSync;
import net.infstudio.goki.common.network.message.C2SStatSync;
import net.infstudio.goki.common.utils.DataHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

public class GuiStats extends Screen {
    public static final int STATUS_BUTTON_WIDTH = 24;
    public static final int STATUS_BUTTON_HEIGHT = 24;
    public static final int IMAGE_ROWS = 10;
    private static final int MARGIN = 10;
    private static final int HEADER_HEIGHT = 28;
    private static final int FOOTER_HEIGHT = 36;
    private static final int MAX_COLUMNS = 7;
    private final Player player = Minecraft.getInstance().player;
    private boolean requestedSync;
    private int gridTop;
    private GuiStatButton heldButton;
    private int holdTicks;
    private int nextRepeatTick;
    private int awaitingLevel;
    private int awaitingTicks;

    public GuiStats() {
        super(Component.translatable("ui.gokistats_reborn.menu_title"));
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void init() {
        stopHolding();
        int count = StatBase.stats.size();
        int labelWidth = STATUS_BUTTON_WIDTH;
        for (var stat : StatBase.stats) {
            String label = I18n.get("ui.gokistats_reborn.level", DataHelper.getPlayerStatLevel(player, stat), stat.getLimit());
            labelWidth = Math.max(labelWidth, font.width(label));
        }
        int cellWidth = Math.max(36, Math.min(64, labelWidth + 8));
        int rowPitch = STATUS_BUTTON_HEIGHT + 3 + font.lineHeight + 10;
        int availableWidth = Math.max(1, width - MARGIN * 2);
        int availableHeight = Math.max(1, height - HEADER_HEIGHT - FOOTER_HEIGHT);
        int columns = 1;
        float scale = 0F;
        for (int candidate = 1; candidate <= Math.min(MAX_COLUMNS, Math.max(1, count)); candidate++) {
            int rows = Math.max(1, (count + candidate - 1) / candidate);
            int gridHeight = rows * rowPitch - 10;
            float candidateScale = Math.min(1F, Math.min(availableWidth / (float) (candidate * cellWidth), availableHeight / (float) gridHeight));
            if (candidateScale >= scale) {
                columns = candidate;
                scale = candidateScale;
            }
        }
        int rows = Math.max(1, (count + columns - 1) / columns);
        int gridHeight = (int) Math.ceil((rows * rowPitch - 10) * scale);
        gridTop = HEADER_HEIGHT + (availableHeight - gridHeight) / 2;
        int buttonWidth = Math.max(1, Math.round(STATUS_BUTTON_WIDTH * scale));
        int buttonHeight = Math.max(1, Math.round(STATUS_BUTTON_HEIGHT * scale));
        for (int index = 0; index < count; index++) {
            int row = index / columns;
            int rowColumns = Math.min(columns, count - row * columns);
            float rowWidth = rowColumns * cellWidth * scale;
            int x = Math.round((width - rowWidth) / 2F + (index % columns * cellWidth + (cellWidth - STATUS_BUTTON_WIDTH) / 2F) * scale);
            int y = Math.round(gridTop + row * rowPitch * scale);
            addRenderableWidget(new GuiStatButton(index, x, y, buttonWidth, buttonHeight, StatBase.stats.get(index), player, this::actionPerformed,
                    scale, Math.max(1, Math.round((cellWidth - 6) * scale))));
        }
        GokiStats.log.debug("Stats screen layout: skills={}, columns={}, rows={}, scale={}, cellWidth={}, screen={}x{}", count, columns, rows, scale, cellWidth, width, height);
        if (!requestedSync) {
            PacketDistributor.sendToServer(new C2SRequestStatSync());
            requestedSync = true;
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        if (heldButton != null && !heldButton.isUnderMouse(mouseX, mouseY)) stopHolding();
        super.render(graphics, mouseX, mouseY, partialTick);
        drawFittedText(graphics, title.getString(), Math.max(5, gridTop - 20), 0xFFFFFFFF);
        drawFittedText(graphics, I18n.get("ui.gokistats_reborn.menu_hint"), height - 28, 0xFFCCCCCC);
        drawFittedText(graphics, I18n.get("ui.currentxp", DataHelper.getXPTotal(player)), height - 14, 0xFFFFFFFF);
        for (var renderable : renderables) {
            if (renderable instanceof GuiStatButton button && button.isUnderMouse(mouseX, mouseY)) {
                new GuiStatTooltip(button.stat, player).render(graphics, button.getX() + button.getWidth() / 2, button.getY() - 1, partialTick, button.getHeight());
                break;
            }
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
        stopHolding();
        GuiStatButton target = null;
        if (mouseButton == 0 && !hasControlDown()) {
            for (var renderable : renderables) {
                if (renderable instanceof GuiStatButton button && button.isUnderMouse((int) mouseX, (int) mouseY)) {
                    target = button;
                    break;
                }
            }
        }
        int level = target == null ? -1 : DataHelper.getPlayerStatLevel(player, target.stat);
        boolean handled = super.mouseClicked(mouseX, mouseY, mouseButton);
        if (handled && target != null && canUpgrade(target)) {
            heldButton = target;
            nextRepeatTick = 8;
            awaitingLevel = level;
            GokiStats.log.debug("Started hold-to-upgrade: skill={}, level={}, delayTicks=8, intervalTicks=2", target.stat.key, level);
        }
        return handled;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int mouseButton) {
        if (mouseButton == 0) stopHolding();
        return super.mouseReleased(mouseX, mouseY, mouseButton);
    }

    @Override
    public void tick() {
        super.tick();
        if (heldButton == null) return;
        if (minecraft == null || !minecraft.isWindowActive() || hasControlDown() || !canUpgrade(heldButton)) {
            stopHolding();
            return;
        }
        holdTicks++;
        int level = DataHelper.getPlayerStatLevel(player, heldButton.stat);
        if (awaitingLevel >= 0) {
            if (level == awaitingLevel) {
                if (++awaitingTicks >= 40) {
                    GokiStats.log.debug("Stopped hold-to-upgrade after sync timeout: skill={}, level={}", heldButton.stat.key, level);
                    stopHolding();
                }
                return;
            }
            if (level != awaitingLevel + 1) {
                stopHolding();
                return;
            }
            awaitingLevel = -1;
        }
        if (holdTicks >= nextRepeatTick) {
            actionPerformed(heldButton);
            awaitingLevel = level;
            awaitingTicks = 0;
            nextRepeatTick = holdTicks + 2;
        }
    }

    @Override
    public void removed() {
        stopHolding();
        super.removed();
    }

    private boolean canUpgrade(GuiStatButton button) {
        int level = DataHelper.getPlayerStatLevel(player, button.stat);
        return button.active && button.visible && button.stat.isEnabled() && level < button.stat.getLimit()
                && DataHelper.getXPTotal(player) >= button.stat.getCost(level);
    }

    private void stopHolding() {
        if (heldButton != null) GokiStats.log.debug("Stopped hold-to-upgrade: skill={}, ticks={}", heldButton.stat.key, holdTicks);
        heldButton = null;
        holdTicks = 0;
        nextRepeatTick = 0;
        awaitingLevel = -1;
        awaitingTicks = 0;
    }

    private void drawFittedText(GuiGraphics graphics, String text, int y, int color) {
        float scale = Math.min(1F, Math.max(1, width - MARGIN * 2) / (float) Math.max(1, font.width(text)));
        graphics.pose().pushPose();
        try {
            graphics.pose().translate(width / 2F, y, 0);
            graphics.pose().scale(scale, scale, 1F);
            graphics.drawCenteredString(font, text, 0, 0, color);
        } finally {
            graphics.pose().popPose();
        }
    }

    protected void actionPerformed(Button button) {
        if (button instanceof GuiStatButton statButton && statButton.id >= 0 && statButton.id < StatBase.stats.size())
            PacketDistributor.sendToServer(new C2SStatSync(statButton.id, hasControlDown() ? -1 : 1));
    }
}
