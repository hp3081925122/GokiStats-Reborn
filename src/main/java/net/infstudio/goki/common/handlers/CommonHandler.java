package net.infstudio.goki.common.handlers;

import net.infstudio.goki.api.capability.CapabilityStat;
import net.infstudio.goki.api.stat.StatBase;
import net.infstudio.goki.api.stat.StatSpecial;
import net.infstudio.goki.api.stat.StatStorage;
import net.infstudio.goki.api.stat.Stats;
import net.infstudio.goki.common.config.GokiConfig;
import net.infstudio.goki.common.init.GokiSounds;
import net.infstudio.goki.common.utils.DataHelper;
import net.infstudio.goki.common.utils.Reference;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.event.entity.living.LivingEvent;
import net.neoforged.neoforge.event.entity.living.LivingFallEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.common.EventBusSubscriber;
import net.minecraft.tags.DamageTypeTags;

@EventBusSubscriber(modid = Reference.MODID)
public class CommonHandler {

    @SubscribeEvent
    public static void playerFall(LivingFallEvent event) {
        if (event.getEntity() instanceof Player) {
            var player = (Player) event.getEntity();
            var featherFallLevel = DataHelper.getPlayerStatLevel(player,
                    Stats.STAT_FEATHER_FALL);
            if (Stats.STAT_FEATHER_FALL.isEnabled() && event.getDistance() < 3.0D + featherFallLevel * 0.1D) {
                event.setDistance(0.0F);
            }
        }
    }

    /**
     * https://github.com/TheIllusiveC4/Curios/blob/1.18.x/src/main/java/top/theillusivec4/curios/common/event/CuriosEventHandler.java#L251
     * https://github.com/gigaherz/ToolBelt/blob/master/src/main/java/dev/gigaherz/toolbelt/slot/BeltExtensionSlot.java#L220-L253
     */
    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        var original = event.getOriginal().getData(CapabilityStat.STAT);
        var copy = event.getEntity().getData(CapabilityStat.STAT);
        copy.stateMap.clear();
        original.stateMap.forEach((stat, state) -> copy.stateMap.put(stat,
                new net.infstudio.goki.api.stat.StatState(stat, state.level, state.revertedLevel)));
        net.infstudio.goki.GokiStats.log.debug("Copied {} skill states for player respawn, death={}", copy.stateMap.size(), event.isWasDeath());
    }

    @SubscribeEvent
    public static void playerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        var player = event.getEntity();
        if (!player.level().isClientSide()) {
            if (!event.isEndConquered() && GokiConfig.SERVER.loseStatsOnDeath.get()) {
                for (var stat = 0; stat < StatBase.stats.size(); stat++) {
                    DataHelper.multiplyPlayerStatLevel(player,
                            StatBase.stats.get(stat),
                            level -> level - (int) (GokiConfig.SERVER.loseStatsMultiplier.get() * level));
                }
            }
            SyncEventHandler.syncPlayerData(player);
            player.heal(player.getMaxHealth());
        }
    }

    @SubscribeEvent
    public static void playerBreakSpeed(PlayerEvent.BreakSpeed event) {
        var heldItem = event.getEntity().getMainHandItem();
        var player = event.getEntity();

        double multiplier = 1.0F;

        if (Stats.MINING.isEffectiveOn(heldItem,
                event.getPosition().orElse(player.blockPosition()),
                player.level())) {
            multiplier += Stats.MINING.getBonus(player);
        }
        if (Stats.DIGGING.isEffectiveOn(heldItem,
                event.getPosition().orElse(player.blockPosition()),
                player.level())) {
            multiplier += Stats.DIGGING.getBonus(player);
        }
        if (Stats.CHOPPING.isEffectiveOn(heldItem,
                event.getPosition().orElse(player.blockPosition()),
                player.level())) {
            multiplier += Stats.CHOPPING.getBonus(player);
        }
        if (Stats.TRIMMING.isEffectiveOn(heldItem,
                event.getPosition().orElse(player.blockPosition()),
                player.level())) {
            multiplier += Stats.TRIMMING.getBonus(player);
        }

        event.setNewSpeed((float) (event.getOriginalSpeed() * multiplier));
    }

    @SubscribeEvent
    public static void playerJump(LivingEvent.LivingJumpEvent event) {
        if (event.getEntity() instanceof Player player) {
            if (player.isSprinting()) {
                player.setDeltaMovement(player.getDeltaMovement().multiply(1.0 + Stats.LEAPER_H.getBonus(player), 1.0 + Stats.LEAPER_V.getBonus(player), 1.0 + Stats.LEAPER_H.getBonus(player)));
            }
        }
    }

    @SubscribeEvent
    public static void entityHurt(LivingIncomingDamageEvent event) {
        var source = event.getSource();

        var victim = event.getEntity();

        if (victim instanceof Player player) {
            if (!source.is(DamageTypeTags.IS_FIRE) && !source.is(DamageTypeTags.BYPASSES_EFFECTS)) {
                if (player.level().random.nextFloat() >= 1.0f - Stats.ROLL.getBonus(player)) {
                    // Avoid damage
                    event.setCanceled(true);

                    player.addEffect(
                            new MobEffectInstance(MobEffects.DAMAGE_BOOST, 20, 2)
                    );

                    victim.addTag("knockback");
//                    player.sendMessage(new TranslationTextComponent("skill.gokistats_reborn.roll.message"));

                    return;
                }
            }

            double damageMultiplier = 1.0F - (Stats.PROTECTION.getAppliedBonus(player,
                    source) + Stats.TOUGH_SKIN.getAppliedBonus(player,
                    source) + Stats.STAT_FEATHER_FALL.getAppliedBonus(player,
                    source) + Stats.TEMPERING.getAppliedBonus(player,
                    source));

            event.setAmount((float) (event.getAmount() * Math.max(0, damageMultiplier)));
        }

        Entity src = source.getEntity();

        if (src instanceof Player player) {
            var heldItem = player.getMainHandItem();
            var damage = event.getAmount();
            var bonus = 0f;

            if (!heldItem.isEmpty()) {
                if (Stats.SWORDSMANSHIP.isItemSupported(heldItem)) {
                    bonus = Math.round(damage * Stats.SWORDSMANSHIP.getAppliedBonus(player, heldItem));
                } else if (Stats.BOWMANSHIP.isItemSupported(heldItem)) {
                    bonus = Math.round(damage * Stats.BOWMANSHIP.getAppliedBonus(player, heldItem));
                } else {
                    if (!DataHelper.hasDamageModifier(heldItem))
                        // This is not a item providing damage, apply pugilism
                        bonus = Math.round(Stats.PUGILISM.getBonus(player));
                }
            } else {
//				bonus = Math.round(damage + (StatBase.PUGILISM.getBonus(DataHelper.getPlayerStatLevel(	player, StatBase.PUGILISM))));
                bonus = Math.round(Stats.PUGILISM.getBonus(player));
            }
            event.setAmount(bonus + damage);

            if (Stats.REAPER.isEffectiveOn(victim)) {
                double reap = Stats.REAPER.getBonus(player);
                double reapBonus = 0;
                if (Stats.STEALTH.isEffectiveOn(player))
                    reapBonus = reap * ((StatSpecial) Stats.STEALTH).getSecondaryBonus(player) / 100.0F;
                double reapChance = reap + reapBonus;
                if (player.getRandom().nextFloat() <= reapChance) {
                    player.crit(victim);
                    player.level().playSound(player, event.getEntity().blockPosition(), GokiSounds.REAPER, SoundSource.MASTER, 1.0f, 1.0f);
                    event.setAmount(100000.0F);
                }
            }
        }
    }

    /*
    @SubscribeEvent
    public void configChanged(ConfigChangedEvent.OnConfigChangedEvent event) {
        if (event.getModID().equals(Reference.MODID)) {
            ConfigManager.sync(Reference.MODID, Config.Type.INSTANCE);
            LootConfigDeserializer.reloadAll();
        }
    }*/
}
