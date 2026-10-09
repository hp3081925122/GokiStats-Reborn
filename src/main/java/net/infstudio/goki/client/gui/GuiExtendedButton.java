package net.infstudio.goki.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

public class GuiExtendedButton extends Button {
    private int backgroundColor;
    private boolean pressed;
    public boolean disabled;
    public final int id;

    public GuiExtendedButton(int id, int x, int y, int width, int height, Component text, int color, OnPress onPress) {
        super(x, y, width, height, text, onPress, DEFAULT_NARRATION);
        this.id = id;
        this.backgroundColor = color;
    }

    @Override
    public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        UiSkin skin = UiSkin.get();
        String state = disabled || !active ? "disabled" : pressed ? "pressed" : isHoveredOrFocused() ? "hover" : "normal";
        skin.draw(graphics, "button_" + state, getX(), getY(), width, height, backgroundColor | 0xFF000000);
        graphics.drawCenteredString(Minecraft.getInstance().font, getMessage(), getX() + width / 2,
                getY() + (height - Minecraft.getInstance().font.lineHeight) / 2, skin.color(disabled || !active ? "muted" : "text"));
    }

    public boolean isUnderMouse(int x, int y) {
        return visible && x >= getX() && y >= getY() && x < getX() + width && y < getY() + height;
    }

    public void setBackgroundColor(int color) {
        backgroundColor = color;
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        if (!disabled) super.onClick(mouseX, mouseY);
    }

    @Override
    public void onPress() {
        if (!disabled) {
            pressed = true;
            super.onPress();
        }
    }

    @Override
    public void onRelease(double mouseX, double mouseY) {
        onRelease();
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 257 || keyCode == 32 || keyCode == 335) {
            onRelease();
            return true;
        }
        return super.keyReleased(keyCode, scanCode, modifiers);
    }

    public void onRelease() {
        pressed = false;
    }

    public boolean isPressed() {
        return pressed;
    }
}
