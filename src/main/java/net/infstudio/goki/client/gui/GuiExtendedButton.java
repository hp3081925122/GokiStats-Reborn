package net.infstudio.goki.client.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

import javax.annotation.Nonnull;

public class GuiExtendedButton extends Button {
    public static final int BORDER_COLOR = -16777216;

    private int backgroundColor;
    private boolean pressed = false;
    public boolean disabled = false;
    public final int id;

    public GuiExtendedButton(int id, int x, int y, int width, int height, Component text, int color, OnPress onPress) {
        super(x, y, width, height, text, onPress, DEFAULT_NARRATION);
        this.id = id;
        this.backgroundColor = color;
    }


    @Override
    public void renderWidget(@Nonnull GuiGraphics stack, int mouseX, int mouseY, float partialTicks) {
        if (!this.disabled) {
            if (!isUnderMouse(mouseX, mouseY)) {
                drawIdle(stack, Minecraft.getInstance(), mouseX, mouseY);
            } else if (this.pressed) {
                drawDown(stack, Minecraft.getInstance(), mouseX, mouseY);
            } else {
                drawHover(stack, Minecraft.getInstance(), mouseX, mouseY);
            }
        } else
            drawDisabled(stack, Minecraft.getInstance(), mouseX, mouseY);
    }

    public boolean isUnderMouse(int mouseX, int mouseY) {
        return (mouseX >= this.getX()) && (mouseY >= this.getY()) && (mouseX < this.getX() + this.width) && (mouseY < this.getY() + this.height);
    }

    private void drawBorder(GuiGraphics stack) {
        stack.hLine(-1, this.width, 0, BORDER_COLOR);
        stack.hLine(-1, this.width, this.height, BORDER_COLOR);
        stack.vLine(-1, 0, this.height, BORDER_COLOR);
        stack.vLine(this.width, 0, this.height, BORDER_COLOR);
    }

    private void drawDisabled(GuiGraphics stack, Minecraft mc, int mouseX, int mouseY) {
        stack.pose().pushPose();
        stack.pose().translate(this.getX(), this.getY(), 0.0F);
        stack.fill(0, 0, this.width, this.height, -2011028958);
        stack.drawCenteredString(mc.font,
                this.getMessage(),
                this.width / 2,
                this.height / 2 - mc.font.lineHeight / 2 + 1,
                3355443);
        drawBorder(stack);
        stack.pose().popPose();
    }

    private void drawIdle(GuiGraphics stack, Minecraft mc, int mouseX, int mouseY) {
        stack.pose().pushPose();
        stack.pose().translate(this.getX(), this.getY(), 0.0F);
        stack.fill(0,
                0,
                this.width,
                this.height,
                this.backgroundColor + -2013265920);
        stack.drawCenteredString(mc.font,
                this.getMessage(),
                this.width / 2,
                this.height / 2 - mc.font.lineHeight / 2 + 1,
                16777215);
        drawBorder(stack);
        stack.pose().popPose();
    }

    private void drawHover(GuiGraphics stack, Minecraft mc, int mouseX, int mouseY) {
        stack.pose().pushPose();
        stack.pose().translate(this.getX(), this.getY(), 0.0F);
        stack.fill(0,
                0,
                this.width,
                this.height,
                this.backgroundColor + -16777216);
        stack.drawCenteredString(mc.font,
                this.getMessage(),
                this.width / 2,
                this.height / 2 - mc.font.lineHeight / 2 + 1,
                16763904);
        drawBorder(stack);
        stack.pose().popPose();
    }

    private void drawDown(GuiGraphics stack, Minecraft mc, int mouseX, int mouseY) {
        stack.pose().pushPose();
        stack.pose().translate(this.getX(), this.getY(), 0.0F);
        stack.fill(0, 0, this.width, this.height, -16777216);
        stack.drawCenteredString(mc.font,
                this.getMessage(),
                this.width / 2,
                this.height / 2 - mc.font.lineHeight / 2 + 1,
                16763904);
        drawBorder(stack);
        stack.pose().popPose();
    }

    public void setBackgroundColor(int color) {
        this.backgroundColor = color;
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        if (this.active && this.visible) {
            if (keyCode != 257 && keyCode != 32 && keyCode != 335) {
                return false;
            } else {
                this.onRelease();
                return true;
            }
        } else {
            return false;
        }
    }

    @Override
    public void onPress() {
        super.onPress();
        this.pressed = true;
    }

    public void onRelease() {
        this.pressed = false;
    }

    public boolean isPressed() {
        return this.pressed;
    }

}
