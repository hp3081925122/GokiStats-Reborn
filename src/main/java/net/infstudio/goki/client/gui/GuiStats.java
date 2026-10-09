package net.infstudio.goki.client.gui;

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
    private final Player player = Minecraft.getInstance().player;
    private UiSkin skin;
    private float viewScale = 1F;
    private int canvasWidth, canvasHeight, panelX, panelY, panelWidth, panelHeight;
    private boolean requestedSync;

    public GuiStats() {
        super(Component.translatable("ui.gokistats_reborn.title"));
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void init() {
        skin = UiSkin.get();
        UiSkin.Layout layout = skin.layout();
        int count = StatBase.totalStats.orElse(0);
        int rows = Math.max(1, (count + layout.columns - 1) / layout.columns);
        int pitch = layout.buttonSize + layout.levelGap + font.lineHeight + layout.rowGap;
        panelWidth = layout.padding * 2 + layout.columns * layout.buttonSize + (layout.columns - 1) * layout.columnGap;
        panelHeight = layout.headerHeight + rows * pitch - layout.rowGap + layout.footerHeight;
        viewScale = Math.min(1F, Math.min(Math.max(1, width - 8) / (float) panelWidth, Math.max(1, height - 8) / (float) panelHeight));
        canvasWidth = (int) (width / viewScale);
        canvasHeight = (int) (height / viewScale);
        panelX = (canvasWidth - panelWidth) / 2;
        panelY = (canvasHeight - panelHeight) / 2;
        if (!requestedSync) {
            GokiPacketHandler.CHANNEL.sendToServer(new C2SRequestStatSync());
            requestedSync = true;
        }
        for (int index = 0; index < count; index++) {
            int row = index / layout.columns;
            int columns = Math.min(layout.columns, count - row * layout.columns);
            int rowWidth = columns * layout.buttonSize + (columns - 1) * layout.columnGap;
            int x = panelX + (panelWidth - rowWidth) / 2 + index % layout.columns * (layout.buttonSize + layout.columnGap);
            int y = panelY + layout.headerHeight + row * pitch;
            addRenderableWidget(new GuiStatButton(index, x, y, layout.buttonSize, layout.buttonSize, StatBase.stats.get(index), player, this::actionPerformed));
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        if (skin != UiSkin.get()) rebuildWidgets();
        int logicalX = (int) (mouseX / viewScale);
        int logicalY = (int) (mouseY / viewScale);
        UiSkin.Layout layout = skin.layout();
        graphics.pose().pushPose();
        graphics.pose().scale(viewScale, viewScale, 1F);
        skin.draw(graphics, "backdrop", 0, 0, canvasWidth + 1, canvasHeight + 1);
        skin.draw(graphics, "panel", panelX, panelY, panelWidth, panelHeight);
        skin.draw(graphics, "header", panelX + layout.headerInset, panelY + layout.headerTop, panelWidth - layout.headerInset * 2, layout.headerBarHeight);
        drawFittedText(graphics, title.getString(), canvasWidth / 2, panelY + layout.headerTop + layout.titleOffset, panelWidth - layout.padding * 2, skin.color("title"));
        int footerY = panelY + panelHeight - layout.footerBottom - layout.footerBarHeight;
        skin.draw(graphics, "footer", panelX + layout.padding, footerY, panelWidth - layout.padding * 2, layout.footerBarHeight);
        drawFittedText(graphics, I18n.get("ui.currentxp", (double) DataHelper.getXPTotal(player)), canvasWidth / 2, footerY + layout.xpOffset, panelWidth - layout.padding * 2, skin.color("text"));
        drawFittedText(graphics, I18n.get("ui.gokistats_reborn.hint"), canvasWidth / 2, panelY + panelHeight - layout.hintBottom, panelWidth - layout.padding * 2, skin.color("muted"));
        super.render(graphics, logicalX, logicalY, partialTick);
        GuiStatButton hovered = null;
        GuiStatButton focused = null;
        for (var renderable : renderables) {
            if (renderable instanceof GuiStatButton button) {
                if (button.isUnderMouse(logicalX, logicalY)) hovered = button;
                if (button.isFocused()) focused = button;
            }
        }
        GuiStatButton selected = hovered != null ? hovered : focused;
        if (selected != null)
            new GuiStatTooltip(selected.stat, player).render(graphics, selected.getX() + selected.getWidth() / 2, selected.getY(), selected.getHeight(), canvasWidth, canvasHeight);
        graphics.flush();
        graphics.pose().popPose();
    }

    private void drawFittedText(GuiGraphics graphics, String text, int centerX, int y, int maxWidth, int color) {
        float scale = Math.min(1F, maxWidth / (float) Math.max(1, font.width(text)));
        graphics.pose().pushPose();
        graphics.pose().translate(centerX, y, 0);
        graphics.pose().scale(scale, scale, 1F);
        graphics.drawCenteredString(font, text, 0, 0, color);
        graphics.flush();
        graphics.pose().popPose();
    }

    @Override
    public boolean mouseClicked(double x, double y, int button) {
        return super.mouseClicked(x / viewScale, y / viewScale, button);
    }

    @Override
    public boolean mouseReleased(double x, double y, int button) {
        return super.mouseReleased(x / viewScale, y / viewScale, button);
    }

    @Override
    public boolean mouseDragged(double x, double y, int button, double dx, double dy) {
        return super.mouseDragged(x / viewScale, y / viewScale, button, dx / viewScale, dy / viewScale);
    }

    @Override
    public void mouseMoved(double x, double y) {
        super.mouseMoved(x / viewScale, y / viewScale);
    }

    @Override
    public boolean mouseScrolled(double x, double y, double delta) {
        return super.mouseScrolled(x / viewScale, y / viewScale, delta);
    }

    protected void actionPerformed(Button button) {
        if (button instanceof GuiStatButton statButton && statButton.id >= 0 && statButton.id < StatBase.totalStats.orElse(0))
            GokiPacketHandler.CHANNEL.sendToServer(new C2SStatSync(statButton.id, hasControlDown() ? -1 : 1));
    }
}
