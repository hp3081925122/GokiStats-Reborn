package net.infstudio.goki.client.gui;

import com.google.gson.Gson;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.logging.LogUtils;
import net.infstudio.goki.api.stat.StatBase;
import net.infstudio.goki.common.utils.Reference;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.HashMap;
import java.util.HashSet;

@Mod.EventBusSubscriber(modid = Reference.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class UiSkin {
    private static final ResourceLocation DEFINITION = new ResourceLocation(Reference.MODID, "gui/skin.json");
    private static UiSkin current;
    private Map<String, Patch> patches;
    private Map<String, String> colors;
    private List<Atlas> icons;
    private Layout layout;
    private transient Map<String, Integer> parsedColors = new HashMap<>();

    @SubscribeEvent
    public static void registerReloadListeners(RegisterClientReloadListenersEvent event) {
        event.registerReloadListener((ResourceManagerReloadListener) UiSkin::reload);
    }

    public static UiSkin get() {
        if (current == null) current = bundled();
        return current;
    }

    private static void reload(ResourceManager manager) {
        try (InputStream stream = manager.getResource(DEFINITION).orElseThrow().open()) {
            UiSkin replacement = read(stream);
            var checked = new HashSet<ResourceLocation>();
            for (Patch patch : replacement.patches.values()) {
                if (checked.add(patch.location())) checkTexture(manager, patch.location(), patch.width, patch.height);
            }
            for (Atlas atlas : replacement.icons) {
                if (checked.add(atlas.location())) checkTexture(manager, atlas.location(), atlas.width, atlas.height);
            }
            current = replacement;
        } catch (Exception exception) {
            LogUtils.getLogger().error("Failed to load GokiStats UI skin; keeping the previous valid skin", exception);
            get();
        }
    }

    private static void checkTexture(ResourceManager manager, ResourceLocation location, int width, int height) throws Exception {
        try (InputStream stream = manager.getResource(location).orElseThrow().open(); NativeImage image = NativeImage.read(stream)) {
            if (image.getWidth() != width || image.getHeight() != height)
                throw new IllegalArgumentException("UI texture dimensions do not match the skin definition: " + location);
        }
    }

    private static UiSkin bundled() {
        try (InputStream stream = UiSkin.class.getResourceAsStream("/assets/" + DEFINITION.getNamespace() + "/" + DEFINITION.getPath())) {
            return read(Objects.requireNonNull(stream, "Missing bundled UI skin"));
        } catch (Exception exception) {
            throw new IllegalStateException("Invalid bundled GokiStats UI skin", exception);
        }
    }

    private static UiSkin read(InputStream stream) {
        UiSkin skin = new Gson().fromJson(new InputStreamReader(stream, StandardCharsets.UTF_8), UiSkin.class);
        skin.validate();
        return skin;
    }

    private void validate() {
        for (String name : List.of("backdrop", "panel", "header", "footer", "tooltip", "button_normal", "button_hover", "button_pressed", "button_disabled", "button_maximum", "button_refund", "button_unaffordable")) {
            Patch patch = Objects.requireNonNull(patches.get(name), "Missing UI patch: " + name);
            patch.validate();
        }
        for (String name : List.of("title", "text", "muted", "level", "maximum", "tooltip_title", "tooltip_text", "tooltip_value", "action", "error")) color(name);
        Objects.requireNonNull(layout, "Missing UI layout").validate();
        if (icons == null || icons.isEmpty()) throw new IllegalArgumentException("Missing icon atlases");
        for (Atlas atlas : icons) atlas.validate();
        for (var stat : StatBase.stats) atlas(stat.imageID);
    }

    public Layout layout() {
        return layout;
    }

    public int color(String name) {
        Integer cached = parsedColors.get(name);
        if (cached != null) return cached;
        String value = Objects.requireNonNull(colors.get(name), "Missing UI color: " + name);
        if (!value.matches("#[0-9a-fA-F]{8}")) throw new IllegalArgumentException("Expected #AARRGGBB for " + name);
        int parsed = (int) Long.parseLong(value.substring(1), 16);
        parsedColors.put(name, parsed);
        return parsed;
    }

    public void draw(GuiGraphics graphics, String name, int x, int y, int width, int height) {
        draw(graphics, name, x, y, width, height, -1);
    }

    public void draw(GuiGraphics graphics, String name, int x, int y, int width, int height, int tint) {
        if (width <= 0 || height <= 0) return;
        Patch patch = patches.get(name);
        int border = Math.min(patch.border, Math.min(width / 2, height / 2));
        int[] sourceWidths = {patch.border, patch.sourceWidth - patch.border * 2, patch.border};
        int[] sourceHeights = {patch.border, patch.sourceHeight - patch.border * 2, patch.border};
        int[] destWidths = {border, width - border * 2, border};
        int[] destHeights = {border, height - border * 2, border};
        int rgba = multiply(patch.tint, tint);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        graphics.setColor((rgba >> 16 & 255) / 255F, (rgba >> 8 & 255) / 255F, (rgba & 255) / 255F, (rgba >>> 24) / 255F);
        try {
            int sy = patch.sourceY, dy = y;
            for (int row = 0; row < 3; row++) {
                int sx = patch.sourceX, dx = x;
                for (int column = 0; column < 3; column++) {
                    if (destWidths[column] > 0 && destHeights[row] > 0 && sourceWidths[column] > 0 && sourceHeights[row] > 0)
                        graphics.blit(patch.location(), dx, dy, destWidths[column], destHeights[row], sx, sy, sourceWidths[column], sourceHeights[row], patch.width, patch.height);
                    sx += sourceWidths[column];
                    dx += destWidths[column];
                }
                sy += sourceHeights[row];
                dy += destHeights[row];
            }
        } finally {
            graphics.setColor(1F, 1F, 1F, 1F);
            RenderSystem.disableBlend();
        }
    }

    private static int multiply(String color, int other) {
        int first = (int) Long.parseLong(color.substring(1), 16);
        int result = 0;
        for (int shift = 0; shift <= 24; shift += 8)
            result |= ((first >>> shift & 255) * (other >>> shift & 255) / 255) << shift;
        return result;
    }

    private Atlas atlas(int id) {
        return icons.stream().filter(atlas -> id >= atlas.firstId && id < atlas.firstId + atlas.count).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No icon atlas for image ID " + id));
    }

    public void drawIcon(GuiGraphics graphics, int id, String state, int x, int y, int size) {
        Atlas atlas = atlas(id);
        int local = id - atlas.firstId;
        int u = (local / atlas.rows * atlas.stateColumns + atlas.states.get(state)) * atlas.cellWidth;
        int v = local % atlas.rows * atlas.cellHeight;
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        try {
            graphics.blit(atlas.location(), x, y, size, size, u, v, atlas.cellWidth, atlas.cellHeight, atlas.width, atlas.height);
        } finally {
            RenderSystem.disableBlend();
        }
    }

    private static final class Patch {
        private String texture;
        private int width, height, border;
        private int sourceX, sourceY, sourceWidth, sourceHeight;
        private String tint;
        private ResourceLocation location;

        private ResourceLocation location() {
            if (location == null) location = new ResourceLocation(texture);
            return location;
        }

        private void validate() {
            location();
            if (width < 1 || height < 1 || width > 4096 || height > 4096 || sourceX < 0 || sourceY < 0 || sourceWidth < 1 || sourceHeight < 1 || (long) sourceX + sourceWidth > width || (long) sourceY + sourceHeight > height || border < 0 || border * 2 >= Math.min(sourceWidth, sourceHeight))
                throw new IllegalArgumentException("Invalid UI slice: " + texture);
            if (tint == null || !tint.matches("#[0-9a-fA-F]{8}")) throw new IllegalArgumentException("Invalid UI tint: " + texture);
        }
    }

    private static final class Atlas {
        private String texture;
        private int firstId, count, width, height, cellWidth, cellHeight, rows, stateColumns;
        private Map<String, Integer> states;
        private ResourceLocation location;

        private ResourceLocation location() {
            if (location == null) location = new ResourceLocation(texture);
            return location;
        }

        private void validate() {
            location();
            if (firstId < 0 || count < 1 || count > 1000 || rows < 1 || rows > 100 || stateColumns < 1 || stateColumns > 100 || cellWidth < 1 || cellHeight < 1 || width < 1 || height < 1 || width > 4096 || height > 4096
                    || (long) ((count + rows - 1) / rows) * stateColumns * cellWidth > width || (long) rows * cellHeight > height)
                throw new IllegalArgumentException("Invalid icon atlas: " + texture);
            for (String state : List.of("normal", "hover", "disabled", "maximum", "refund", "unaffordable")) {
                Integer column = states.get(state);
                if (column == null || column < 0 || column >= stateColumns) throw new IllegalArgumentException("Invalid icon state: " + state);
            }
        }
    }

    public static final class Layout {
        public int columns, padding, headerHeight, footerHeight, buttonSize, iconSize, columnGap, rowGap, levelGap;
        public int headerInset, headerTop, headerBarHeight, titleOffset;
        public int footerBottom, footerBarHeight, xpOffset, hintBottom;
        public int tooltipPadding, tooltipMaxWidth, tooltipMargin;

        private void validate() {
            if (columns < 1 || columns > 16 || padding < 1 || padding > 64 || buttonSize < 16 || buttonSize > 64 || iconSize < 1 || iconSize > buttonSize || columnGap < 0 || columnGap > 64 || rowGap < 0 || rowGap > 64 || levelGap < 0 || levelGap > 16
                    || headerHeight < 16 || headerHeight > 128 || footerHeight < 24 || footerHeight > 128 || headerInset < 0 || headerInset > padding || headerTop < 0 || headerBarHeight < 9 || headerTop + headerBarHeight > headerHeight
                    || titleOffset < 0 || titleOffset + 9 > headerBarHeight || footerBottom < 0 || footerBottom + footerBarHeight > footerHeight || footerBarHeight < 9 || xpOffset < 0 || xpOffset + 9 > footerBarHeight || hintBottom < 9 || hintBottom > footerBottom
                    || tooltipPadding < 1 || tooltipPadding > 32 || tooltipMaxWidth < 32 || tooltipMaxWidth > 1024 || tooltipMargin < 0 || tooltipMargin > 32)
                throw new IllegalArgumentException("Invalid UI layout dimensions");
        }
    }
}
