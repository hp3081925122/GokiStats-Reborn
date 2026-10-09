package net.infstudio.goki.client.gui;

import net.minecraft.client.gui.GuiGraphicsExtractor;
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
    public void extractContents(@Nonnull GuiGraphicsExtractor stack, int mouseX, int mouseY, float partialTicks) {
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

    private void drawBorder(GuiGraphicsExtractor stack) {
        stack.horizontalLine(-1, this.width, 0, BORDER_COLOR);
        stack.horizontalLine(-1, this.width, this.height, BORDER_COLOR);
        stack.verticalLine(-1, 0, this.height, BORDER_COLOR);
        stack.verticalLine(this.width, 0, this.height, BORDER_COLOR);
    }

    private void drawDisabled(GuiGraphicsExtractor stack, Minecraft mc, int mouseX, int mouseY) {
        stack.pose().pushMatrix();
        stack.pose().translate(this.getX(), this.getY());
        stack.fill(0, 0, this.width, this.height, -2011028958);
        stack.centeredText(mc.font,
                this.getMessage(),
                this.width / 2,
                this.height / 2 - mc.font.lineHeight / 2 + 1,
                3355443);
        drawBorder(stack);
        stack.pose().popMatrix();
    }

    private void drawIdle(GuiGraphicsExtractor stack, Minecraft mc, int mouseX, int mouseY) {
        stack.pose().pushMatrix();
        stack.pose().translate(this.getX(), this.getY());
        stack.fill(0,
                0,
                this.width,
                this.height,
                this.backgroundColor + -2013265920);
        stack.centeredText(mc.font,
                this.getMessage(),
                this.width / 2,
                this.height / 2 - mc.font.lineHeight / 2 + 1,
                16777215);
        drawBorder(stack);
        stack.pose().popMatrix();
    }

    private void drawHover(GuiGraphicsExtractor stack, Minecraft mc, int mouseX, int mouseY) {
        stack.pose().pushMatrix();
        stack.pose().translate(this.getX(), this.getY());
        stack.fill(0,
                0,
                this.width,
                this.height,
                this.backgroundColor + -16777216);
        stack.centeredText(mc.font,
                this.getMessage(),
                this.width / 2,
                this.height / 2 - mc.font.lineHeight / 2 + 1,
                16763904);
        drawBorder(stack);
        stack.pose().popMatrix();
    }

    private void drawDown(GuiGraphicsExtractor stack, Minecraft mc, int mouseX, int mouseY) {
        stack.pose().pushMatrix();
        stack.pose().translate(this.getX(), this.getY());
        stack.fill(0, 0, this.width, this.height, -16777216);
        stack.centeredText(mc.font,
                this.getMessage(),
                this.width / 2,
                this.height / 2 - mc.font.lineHeight / 2 + 1,
                16763904);
        drawBorder(stack);
        stack.pose().popMatrix();
    }

    public void setBackgroundColor(int color) {
        this.backgroundColor = color;
    }

    @Override
    public boolean keyReleased(net.minecraft.client.input.KeyEvent event) {
        if (this.active && this.visible) {
            int keyCode = event.key();
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
    public void onPress(net.minecraft.client.input.InputWithModifiers input) {
        super.onPress(input);
        this.pressed = true;
    }

    public void onRelease() {
        this.pressed = false;
    }

    public boolean isPressed() {
        return this.pressed;
    }

}
