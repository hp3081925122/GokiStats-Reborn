package net.infstudio.goki.common.utils;

import net.minecraft.resources.ResourceLocation;

public class Reference {
    public static final String MODID = "gokistats_reborn";
    public static final String LEGACY_MODID = "gokistats";
    public static final String MOD_NAME = "GokiStats Reborn";
    public static final ResourceLocation STORAGE_ID = ResourceLocation.fromNamespaceAndPath(LEGACY_MODID, "stat_storage");
    public static final ResourceLocation RPG_ICON_TEXTURE_LOCATION = ResourceLocation.fromNamespaceAndPath(MODID, "textures/rpg_icons.png");
    public static final ResourceLocation RPG_ICON_2_TEXTURE_LOCATION = ResourceLocation.fromNamespaceAndPath(MODID, "textures/rpg_icons_2.png");
    public static final ResourceLocation PARTICLES_TEXTURE = ResourceLocation.fromNamespaceAndPath(MODID, "textures/particles.png");
    public static final String STAT_TAG = LEGACY_MODID + "_Stats";
    public static boolean isPlayerAPILoaded = false;
}
