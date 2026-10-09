package net.infstudio.goki.servertest;

import com.mojang.authlib.GameProfile;
import net.infstudio.goki.GokiStats;
import net.infstudio.goki.api.capability.CapabilityStat;
import net.infstudio.goki.api.stat.StatBase;
import net.infstudio.goki.api.stat.Stats;
import net.infstudio.goki.common.config.GokiConfig;
import net.infstudio.goki.common.handlers.TickHandler;
import net.infstudio.goki.common.handlers.SyncEventHandler;
import net.infstudio.goki.common.network.handler.PacketSyncHandler;
import net.infstudio.goki.common.network.message.C2SStatSync;
import net.infstudio.goki.common.network.message.S2CSyncAll;
import net.infstudio.goki.common.utils.DataHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.event.entity.living.LivingEvent;
import net.neoforged.neoforge.event.entity.living.LivingFallEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import net.neoforged.neoforge.network.handling.ServerPayloadContext;
import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import io.netty.buffer.Unpooled;
import java.util.UUID;

@GameTestHolder("gokistats_reborn")
@PrefixGameTestTemplate(false)
public class GokiServerTests {
    private static int passed;
    private static int failed;

    @FunctionalInterface
    interface TestCase { void run() throws Exception; }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }

    private static void near(double actual, double expected, String message) {
        check(Math.abs(actual - expected) < 0.0001, message + ": actual=" + actual + ", expected=" + expected);
    }

    private static class TestPlayer extends FakePlayer {
        final java.util.List<byte[]> snapshots = new java.util.ArrayList<>();
        TestPlayer(ServerLevel level, GameProfile profile) {
            super(level, profile);
            var connection = new Connection(PacketFlow.SERVERBOUND);
            new io.netty.channel.embedded.EmbeddedChannel(connection);
            new net.minecraft.server.network.ServerGamePacketListenerImpl(level.getServer(), connection, this,
                    net.minecraft.server.network.CommonListenerCookie.createInitial(profile, false)) {
                @Override public void send(net.minecraft.network.protocol.Packet<?> packet) {
                    if (packet instanceof net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket custom
                            && custom.payload() instanceof net.infstudio.goki.common.network.message.IMessage message) {
                        var bytes = new FriendlyByteBuf(Unpooled.buffer());
                        try {
                            bytes.writeVarInt(message instanceof S2CSyncAll ? 1 : 2);
                            message.toBytes(bytes);
                            byte[] payload = new byte[bytes.readableBytes()];
                            bytes.readBytes(payload);
                            snapshots.add(payload);
                        } finally { bytes.release(); }
                    }
                }
                @Override public void send(net.minecraft.network.protocol.Packet<?> packet, net.minecraft.network.PacketSendListener listener) { send(packet); }
            };
        }

        void finishShieldDelay() { useItemRemaining -= 5; }
    }

    private static TestPlayer player(ServerLevel level) {
        return new TestPlayer(level, new GameProfile(UUID.randomUUID(), "GokiTest"));
    }

    private static void test(String name, TestCase body) {
        try {
            body.run();
            passed++;
            GokiStats.log.info("GOKI_SERVER_TEST PASS {}", name);
        } catch (Throwable error) {
            failed++;
            GokiStats.log.error("GOKI_SERVER_TEST FAIL " + name, error);
        }
    }

    private static void request(FakePlayer player, int stat, int amount) throws Exception {
        var context = new ServerPayloadContext(player.connection, C2SStatSync.TYPE.id());
        PacketSyncHandler.acceptC2S(new C2SStatSync(stat, amount), context);
    }

    @GameTest(template = "empty", timeoutTicks = 1200)
    public static void functionality(GameTestHelper helper) {
        passed = 0;
        failed = 0;
        var level = helper.getLevel();
        var serverConfig = com.electronwill.nightconfig.core.CommentedConfig.inMemory();
        GokiConfig.serverSpec.correct(serverConfig);
        for (var stat : StatBase.stats) {
            var config = com.electronwill.nightconfig.core.CommentedConfig.inMemory();
            stat.configSpec.correct(config);
        }
        test("renamed_identity_and_skill_ids", () -> {
            check(net.neoforged.fml.ModList.get().isLoaded("gokistats_reborn"), "Renamed mod ID not loaded");
            check(!net.neoforged.fml.ModList.get().isLoaded("gokistats"), "Old mod ID still loaded");
            check(net.infstudio.goki.common.utils.Reference.MOD_NAME.equals("GokiStats Reborn"), "Display name not renamed");
            check(StatBase.REGISTRY_NAME.toString().equals("gokistats_reborn:stats"), "Registry namespace not renamed");
            for (var stat : StatBase.stats) check(stat.getRegistryName().getNamespace().equals("gokistats_reborn"), "Legacy skill registry entry remains");
        });
        test("legacy_skill_nbt_migration", () -> {
            var p = player(level); p.giveExperiencePoints(72);
            DataHelper.setPlayerStatLevel(p, Stats.MINING, 9); DataHelper.setPlayerRevertStatLevel(p, Stats.MINING, 2);
            DataHelper.setPlayerStatLevel(p, Stats.MAX_HEALTH, 4);
            var saved = p.saveWithoutId(new CompoundTag());
            var caps = saved.getCompound("neoforge:attachments");
            check(caps.contains("gokistats_reborn:stat_storage"), "Attachment outer storage key lost");
            var current = caps.getCompound("gokistats_reborn:stat_storage"); var legacy = new CompoundTag();
            for (var key : current.getAllKeys()) legacy.put(key.replace("gokistats_reborn:", "gokistats:"), current.get(key).copy());
            legacy.put("invalid legacy key", new CompoundTag());
            var forgeCaps = new CompoundTag();
            forgeCaps.put("gokistats:stat_storage", legacy);
            saved.remove("neoforge:attachments");
            saved.put("ForgeCaps", forgeCaps);
            var copy = player(level); copy.load(saved);
            check(DataHelper.getPlayerStatLevel(copy, Stats.MINING) == 9, "Legacy mining level lost");
            check(DataHelper.getPlayerRevertStatLevel(copy, Stats.MINING) == 2, "Legacy refund count lost");
            check(DataHelper.getPlayerStatLevel(copy, Stats.MAX_HEALTH) == 4, "Legacy health level lost");
            check(DataHelper.getXPTotal(copy) == 72, "Player XP changed during migration");
            var rewritten = copy.saveWithoutId(new CompoundTag()).getCompound("neoforge:attachments").getCompound("gokistats_reborn:stat_storage");
            check(rewritten.contains("gokistats_reborn:mining") && !rewritten.contains("gokistats:mining"), "Migrated skill IDs not saved under new namespace");
        });
        test("registry_and_tags", () -> {
            check(StatBase.stats.size() == 21, "Skill count");
            check(StatBase.REGISTRY.size() == 21, "Registry count");
            for (var stat : StatBase.stats) check(StatBase.REGISTRY.get(stat.getRegistryName()) == stat, "Skill registry identity");
            check(BuiltInRegistries.SOUND_EVENT.containsKey(ResourceLocation.parse("gokistats_reborn:treasure")), "Sound missing");
            check(NeoForgeRegistries.GLOBAL_LOOT_MODIFIER_SERIALIZERS.containsKey(ResourceLocation.parse("gokistats_reborn:loot")), "Loot codec missing");
            check(Stats.MINING.isItemSupported(new ItemStack(Items.DIAMOND_PICKAXE)), "Mining tag missing");
            check(Stats.DIGGING.isItemSupported(new ItemStack(Items.DIAMOND_SHOVEL)), "Digging tag missing");
            check(Stats.CHOPPING.isItemSupported(new ItemStack(Items.DIAMOND_AXE)), "Chopping tag missing");
            check(Stats.TRIMMING.isItemSupported(new ItemStack(Items.SHEARS)), "Shears tag missing");
            check(Stats.SWORDSMANSHIP.isItemSupported(new ItemStack(Items.DIAMOND_SWORD)), "Sword tag missing");
            check(Stats.BOWMANSHIP.isItemSupported(new ItemStack(Items.BOW)), "Bow tag missing");
        });
        test("capability_registered", () -> check(NeoForgeRegistries.ATTACHMENT_TYPES.containsKey(ResourceLocation.parse("gokistats_reborn:stat_storage")), "Capability registration event missing"));
        test("capability_attached", () -> {
            var p = player(level);
            var storage = p.getData(CapabilityStat.STAT);
            check(storage.stateMap.size() == 21, "Capability skills missing");
        });
        test("upgrade_refund_and_experience", () -> {
            var p = player(level);
            p.giveExperiencePoints(1000);
            int start = DataHelper.getXPTotal(p);
            int index = StatBase.stats.indexOf(Stats.MINING);
            int cost = Stats.MINING.getCost(0);
            request(p, index, 1);
            check(DataHelper.getPlayerStatLevel(p, Stats.MINING) == 1, "Upgrade did not apply");
            check(DataHelper.getXPTotal(p) == start - cost, "Wrong upgrade cost");
            request(p, index, -1);
            check(DataHelper.getPlayerStatLevel(p, Stats.MINING) == 0, "Refund did not apply");
            check(DataHelper.getPlayerRevertStatLevel(p, Stats.MINING) == 1, "Refund counter");
            check(DataHelper.getXPTotal(p) == start - cost + (int)(cost * GokiConfig.SERVER.globalRevertFactor.get()), "Wrong refund");
        });
        test("invalid_requests_and_limits", () -> {
            var p = player(level);
            p.giveExperiencePoints(1000);
            int index = StatBase.stats.indexOf(Stats.MINING);
            int start = DataHelper.getXPTotal(p);
            request(p, -1, 1); request(p, 9999, 1); request(p, index, 0); request(p, index, 2); request(p, index, Integer.MIN_VALUE); request(p, index, -1);
            check(DataHelper.getPlayerStatLevel(p, Stats.MINING) == 0 && DataHelper.getXPTotal(p) == start, "Invalid request changed player");
            DataHelper.setPlayerStatLevel(p, Stats.MINING, Stats.MINING.getLimit());
            request(p, index, 1);
            check(DataHelper.getPlayerStatLevel(p, Stats.MINING) == Stats.MINING.getLimit(), "Limit bypass");
            var poor = player(level);
            request(poor, index, 1);
            check(DataHelper.getPlayerStatLevel(poor, Stats.MINING) == 0, "Upgrade without XP");
        });
        test("refund_limit_and_disabled_skill", () -> {
            var p = player(level);
            var oldRevert = GokiConfig.SERVER.globalMaxRevertLevel.get();
            var oldMax = Stats.MINING.config.maxLevel.get();
            try {
                DataHelper.setPlayerStatLevel(p, Stats.MINING, 2);
                GokiConfig.SERVER.globalMaxRevertLevel.set(1);
                int index = StatBase.stats.indexOf(Stats.MINING);
                request(p, index, -1); request(p, index, -1);
                check(DataHelper.getPlayerStatLevel(p, Stats.MINING) == 1, "Refund limit bypass");
                Stats.MINING.config.maxLevel.set(0);
                p.giveExperiencePoints(1000);
                request(p, index, 1);
                check(DataHelper.getPlayerStatLevel(p, Stats.MINING) == 1, "Disabled skill upgrade");
                near(Stats.MINING.getBonus(p), 0, "Disabled skill effect");
            } finally { GokiConfig.SERVER.globalMaxRevertLevel.set(oldRevert); Stats.MINING.config.maxLevel.set(oldMax); }
        });
        test("player_nbt_save_load", () -> {
            var p = player(level);
            DataHelper.setPlayerStatLevel(p, Stats.MINING, 7);
            DataHelper.setPlayerRevertStatLevel(p, Stats.MINING, 3);
            CompoundTag saved = p.saveWithoutId(new CompoundTag());
            check(saved.getCompound("neoforge:attachments").contains("gokistats_reborn:stat_storage"), "Skill data absent in saved player NBT");
            var copy = player(level);
            copy.load(saved);
            check(DataHelper.getPlayerStatLevel(copy, Stats.MINING) == 7, "Saved level lost");
            check(DataHelper.getPlayerRevertStatLevel(copy, Stats.MINING) == 3, "Saved refund count lost");
        });
        test("clone_copies_independent_storage", () -> {
            var old = player(level); var copy = player(level);
            DataHelper.setPlayerStatLevel(old, Stats.MINING, 6);
            DataHelper.setPlayerRevertStatLevel(old, Stats.MINING, 2);
            NeoForge.EVENT_BUS.post(new PlayerEvent.Clone(copy, old, true));
            check(DataHelper.getPlayerStatLevel(copy, Stats.MINING) == 6, "Clone level missing");
            check(DataHelper.getPlayerRevertStatLevel(copy, Stats.MINING) == 2, "Clone refunds missing");
            DataHelper.setPlayerStatLevel(copy, Stats.MINING, 1);
            check(DataHelper.getPlayerStatLevel(old, Stats.MINING) == 6, "Clone shares state");
        });
        test("health_preserves_other_modifiers", () -> {
            var p = player(level); var attribute = p.getAttribute(Attributes.MAX_HEALTH);
            var id = ResourceLocation.fromNamespaceAndPath("goki_test", "other_health");
            attribute.addTransientModifier(new AttributeModifier(id, 7, AttributeModifier.Operation.ADD_VALUE));
            DataHelper.setPlayerStatLevel(p, Stats.MAX_HEALTH, 4);
            DataHelper.resetMaxHealth(p); DataHelper.resetMaxHealth(p);
            near(p.getMaxHealth(), 31, "Health bonus or duplicate modifier");
            check(attribute.getModifier(id) != null, "Other mod modifier removed");
            DataHelper.setPlayerStatLevel(p, Stats.MAX_HEALTH, 1);
            DataHelper.resetMaxHealth(p);
            near(p.getMaxHealth(), 28, "Health downgrade");
            SyncEventHandler.syncPlayerData(p);
            near(p.getMaxHealth(), 28, "Health sync");
        });
        test("death_loss_and_end_return", () -> {
            var p = player(level);
            var oldLoss = GokiConfig.SERVER.loseStatsOnDeath.get(); var oldRate = GokiConfig.SERVER.loseStatsMultiplier.get();
            try {
                GokiConfig.SERVER.loseStatsOnDeath.set(true); GokiConfig.SERVER.loseStatsMultiplier.set(0.5);
                DataHelper.setPlayerStatLevel(p, Stats.MINING, 10);
                NeoForge.EVENT_BUS.post(new PlayerEvent.PlayerRespawnEvent(p, false));
                check(DataHelper.getPlayerStatLevel(p, Stats.MINING) == 5, "Death loss wrong");
                NeoForge.EVENT_BUS.post(new PlayerEvent.PlayerRespawnEvent(p, true));
                check(DataHelper.getPlayerStatLevel(p, Stats.MINING) == 5, "End return removed skills");
                GokiConfig.SERVER.loseStatsOnDeath.set(false);
                NeoForge.EVENT_BUS.post(new PlayerEvent.PlayerRespawnEvent(p, false));
                check(DataHelper.getPlayerStatLevel(p, Stats.MINING) == 5, "Disabled death loss");
            } finally { GokiConfig.SERVER.loseStatsOnDeath.set(oldLoss); GokiConfig.SERVER.loseStatsMultiplier.set(oldRate); }
        });
        test("tool_speed_isolation", () -> {
            var p = player(level);
            DataHelper.setPlayerStatLevel(p, Stats.MINING, 5); DataHelper.setPlayerStatLevel(p, Stats.DIGGING, 5);
            DataHelper.setPlayerStatLevel(p, Stats.CHOPPING, 5); DataHelper.setPlayerStatLevel(p, Stats.TRIMMING, 5);
            p.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.DIAMOND_PICKAXE));
            var event = new PlayerEvent.BreakSpeed(p, Blocks.STONE.defaultBlockState(), 10, BlockPos.ZERO);
            NeoForge.EVENT_BUS.post(event);
            near(event.getNewSpeed(), 10 * (1 + Stats.MINING.getBonus(p)), "Other tool bonuses stacked");
        });
        test("combat_pugilism_sword_and_reaper", () -> {
            var p = player(level); var target = EntityType.COW.create(level);
            var event = new LivingIncomingDamageEvent(target, new net.neoforged.neoforge.common.damagesource.DamageContainer(level.damageSources().playerAttack(p), 10));
            NeoForge.EVENT_BUS.post(event); near(event.getAmount(), 10, "Level-zero pugilism doubled base damage");
            DataHelper.setPlayerStatLevel(p, Stats.PUGILISM, 5);
            event = new LivingIncomingDamageEvent(target, new net.neoforged.neoforge.common.damagesource.DamageContainer(level.damageSources().playerAttack(p), 10));
            NeoForge.EVENT_BUS.post(event); near(event.getAmount(), 10 + Math.round(Stats.PUGILISM.getBonus(p)), "Pugilism bonus");
            p.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.DIAMOND_SWORD));
            DataHelper.setPlayerStatLevel(p, Stats.SWORDSMANSHIP, 5);
            event = new LivingIncomingDamageEvent(target, new net.neoforged.neoforge.common.damagesource.DamageContainer(level.damageSources().playerAttack(p), 10));
            NeoForge.EVENT_BUS.post(event); near(event.getAmount(), 10 + Math.round(10 * Stats.SWORDSMANSHIP.getBonus(p)), "Sword bonus");
            var old = Stats.REAPER.config.bonusMultiplier.get();
            try {
                Stats.REAPER.config.bonusMultiplier.set(10000.0); DataHelper.setPlayerStatLevel(p, Stats.REAPER, 1);
                event = new LivingIncomingDamageEvent(target, new net.neoforged.neoforge.common.damagesource.DamageContainer(level.damageSources().playerAttack(p), 10));
                NeoForge.EVENT_BUS.post(event); near(event.getAmount(), 100000, "Reaper did not trigger");
            } finally { Stats.REAPER.config.bonusMultiplier.set(old); }
        });
        test("fire_fall_explosion_and_mob_protection", () -> {
            var p = player(level);
            DataHelper.setPlayerStatLevel(p, Stats.TEMPERING, 5); DataHelper.setPlayerStatLevel(p, Stats.STAT_FEATHER_FALL, 5);
            DataHelper.setPlayerStatLevel(p, Stats.TOUGH_SKIN, 5); DataHelper.setPlayerStatLevel(p, Stats.PROTECTION, 5);
            var sources = new net.minecraft.world.damagesource.DamageSource[]{level.damageSources().lava(), level.damageSources().fall(), level.damageSources().explosion(null, null), level.damageSources().mobAttack(EntityType.ZOMBIE.create(level))};
            var stats = new StatBase<?>[]{Stats.TEMPERING, Stats.STAT_FEATHER_FALL, Stats.TOUGH_SKIN, Stats.PROTECTION};
            for (int i = 0; i < sources.length; i++) {
                var event = new LivingIncomingDamageEvent(p, new net.neoforged.neoforge.common.damagesource.DamageContainer(sources[i], 10));
                NeoForge.EVENT_BUS.post(event);
                near(event.getAmount(), 10 * (1 - stats[i].getBonus(p)), "Protection " + sources[i].getMsgId());
            }
            var fall = new LivingFallEvent(p, 3.4F, 1);
            NeoForge.EVENT_BUS.post(fall); near(fall.getDistance(), 0, "Feather fall threshold");
        });
        test("jump_axes_and_sneak_modifier", () -> {
            var p = player(level);
            DataHelper.setPlayerStatLevel(p, Stats.LEAPER_H, 2); DataHelper.setPlayerStatLevel(p, Stats.LEAPER_V, 7);
            p.setSprinting(true); p.setDeltaMovement(1, 1, 1);
            NeoForge.EVENT_BUS.post(new LivingEvent.LivingJumpEvent(p));
            near(p.getDeltaMovement().x, 1 + Stats.LEAPER_H.getBonus(p), "Horizontal leap");
            near(p.getDeltaMovement().y, 1 + Stats.LEAPER_V.getBonus(p), "Vertical leap");
            DataHelper.setPlayerStatLevel(p, Stats.STEALTH, 3); p.setShiftKeyDown(true); p.setPose(net.minecraft.world.entity.Pose.CROUCHING);
            NeoForge.EVENT_BUS.post(new PlayerTickEvent.Pre(p));
            var speed = p.getAttribute(Attributes.MOVEMENT_SPEED);
            check(speed.getModifier(TickHandler.stealthSpeedID) != null, "Sneak modifier missing");
            DataHelper.setPlayerStatLevel(p, Stats.STEALTH, 8);
            NeoForge.EVENT_BUS.post(new PlayerTickEvent.Pre(p));
            near(speed.getModifier(TickHandler.stealthSpeedID).amount(), Stats.STEALTH.getBonus(p) / 100, "Sneak modifier did not update");
            p.setShiftKeyDown(false); p.setPose(net.minecraft.world.entity.Pose.STANDING); NeoForge.EVENT_BUS.post(new PlayerTickEvent.Pre(p));
            check(speed.getModifier(TickHandler.stealthSpeedID) == null, "Sneak modifier stayed active");
        });
        test("bow_arrow_damage", () -> {
            var p = player(level); var target = EntityType.COW.create(level);
            p.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.BOW));
            DataHelper.setPlayerStatLevel(p, Stats.BOWMANSHIP, 5);
            var arrow = new net.minecraft.world.entity.projectile.Arrow(level, p, new ItemStack(Items.ARROW), null);
            var event = new LivingIncomingDamageEvent(target, new net.neoforged.neoforge.common.damagesource.DamageContainer(level.damageSources().arrow(arrow, p), 10));
            NeoForge.EVENT_BUS.post(event);
            near(event.getAmount(), 10 + Math.round(10 * Stats.BOWMANSHIP.getBonus(p)), "Arrow bonus");
        });
        test("shield_knockback_resistance", () -> {
            var p = player(level); DataHelper.setPlayerStatLevel(p, Stats.STEADY_GUARD, 5);
            p.setItemSlot(EquipmentSlot.OFFHAND, new ItemStack(Items.SHIELD));
            p.startUsingItem(InteractionHand.OFF_HAND); p.finishShieldDelay();
            check(p.isBlocking(), "Shield setup failed");
            NeoForge.EVENT_BUS.post(new PlayerTickEvent.Pre(p));
            var attribute = p.getAttribute(Attributes.KNOCKBACK_RESISTANCE);
            check(attribute.getModifier(TickHandler.knockbackResistanceID) != null, "Shield modifier missing");
            near(attribute.getModifier(TickHandler.knockbackResistanceID).amount(), Stats.STEADY_GUARD.getBonus(p), "Shield resistance");
            DataHelper.setPlayerStatLevel(p, Stats.STEADY_GUARD, 9);
            NeoForge.EVENT_BUS.post(new PlayerTickEvent.Pre(p));
            near(attribute.getModifier(TickHandler.knockbackResistanceID).amount(), Stats.STEADY_GUARD.getBonus(p), "Shield upgrade not refreshed");
            p.stopUsingItem(); NeoForge.EVENT_BUS.post(new PlayerTickEvent.Pre(p));
            check(attribute.getModifier(TickHandler.knockbackResistanceID) == null, "Shield modifier persisted");
        });
        test("roll_and_excluded_damage", () -> {
            var p = player(level); var old = Stats.ROLL.config.bonusMultiplier.get();
            try {
                Stats.ROLL.config.bonusMultiplier.set(10000.0); DataHelper.setPlayerStatLevel(p, Stats.ROLL, 1);
                var event = new LivingIncomingDamageEvent(p, new net.neoforged.neoforge.common.damagesource.DamageContainer(level.damageSources().mobAttack(EntityType.ZOMBIE.create(level)), 10));
                NeoForge.EVENT_BUS.post(event);
                check(event.isCanceled(), "Roll did not cancel hit");
                check(p.hasEffect(net.minecraft.world.effect.MobEffects.DAMAGE_BOOST), "Roll effect missing");
                check(p.getTags().contains("knockback"), "Roll knockback tag missing");
                event = new LivingIncomingDamageEvent(p, new net.neoforged.neoforge.common.damagesource.DamageContainer(level.damageSources().lava(), 10));
                NeoForge.EVENT_BUS.post(event); check(!event.isCanceled(), "Roll blocked fire damage");
                var damageRegistry = level.registryAccess().registryOrThrow(net.minecraft.core.registries.Registries.DAMAGE_TYPE);
                var excluded = damageRegistry.getTag(net.minecraft.tags.DamageTypeTags.BYPASSES_EFFECTS).orElseThrow();
                check(excluded.size() > 0, "No bypass-effects damage type in runtime");
                for (var type : excluded) {
                    var source = new net.minecraft.world.damagesource.DamageSource(type);
                    event = new LivingIncomingDamageEvent(p, new net.neoforged.neoforge.common.damagesource.DamageContainer(source, 10));
                    NeoForge.EVENT_BUS.post(event); check(!event.isCanceled(), "Roll blocked bypass-effects damage: " + source.getMsgId());
                }
            } finally { Stats.ROLL.config.bonusMultiplier.set(old); }
        });
        test("climbing_vertical_only_and_sneak", () -> {
            var p = player(level); var pos = helper.absolutePos(new BlockPos(2, 2, 2));
            level.setBlockAndUpdate(pos.south(), Blocks.STONE.defaultBlockState());
            level.setBlockAndUpdate(pos, Blocks.LADDER.defaultBlockState());
            p.setPos(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5); p.noPhysics = true;
            check(p.onClimbable(), "Ladder setup failed");
            DataHelper.setPlayerStatLevel(p, Stats.CLIMBING, 5); p.setDeltaMovement(0.05, 0.2, 0.05);
            var before = p.position();
            NeoForge.EVENT_BUS.post(new PlayerTickEvent.Pre(p));
            near(p.getX(), before.x, "Climbing duplicated horizontal X movement");
            near(p.getZ(), before.z, "Climbing duplicated horizontal Z movement");
            near(p.getY() - before.y, 0.2 * Stats.CLIMBING.getBonus(p), "Climbing vertical bonus");
            p.setShiftKeyDown(true); before = p.position();
            NeoForge.EVENT_BUS.post(new PlayerTickEvent.Pre(p));
            near(p.getY(), before.y, "Sneaking still climbed");
        });
        test("death_clone_respawn_health_chain", () -> {
            var old = player(level); var copy = player(level);
            DataHelper.setPlayerStatLevel(old, Stats.MINING, 8); DataHelper.setPlayerRevertStatLevel(old, Stats.MINING, 2);
            DataHelper.setPlayerStatLevel(old, Stats.MAX_HEALTH, 4);
            NeoForge.EVENT_BUS.post(new PlayerEvent.Clone(copy, old, true));
            var loss = GokiConfig.SERVER.loseStatsOnDeath.get(); var rate = GokiConfig.SERVER.loseStatsMultiplier.get();
            try {
                GokiConfig.SERVER.loseStatsOnDeath.set(true); GokiConfig.SERVER.loseStatsMultiplier.set(0.5);
                copy.setHealth(1); NeoForge.EVENT_BUS.post(new PlayerEvent.PlayerRespawnEvent(copy, false));
                check(DataHelper.getPlayerStatLevel(copy, Stats.MINING) == 4, "Cloned level loss");
                check(DataHelper.getPlayerRevertStatLevel(copy, Stats.MINING) == 2, "Cloned refunds lost");
                near(copy.getMaxHealth(), 22, "Respawn max health"); near(copy.getHealth(), 22, "Respawn full heal");
            } finally { GokiConfig.SERVER.loseStatsOnDeath.set(loss); GokiConfig.SERVER.loseStatsMultiplier.set(rate); }
        });
        test("login_dimension_and_multiplayer_sync", () -> {
            var a = player(level); var b = player(level);
            DataHelper.setPlayerStatLevel(a, Stats.MINING, 3); DataHelper.setPlayerStatLevel(b, Stats.MINING, 7);
            NeoForge.EVENT_BUS.post(new PlayerEvent.PlayerLoggedInEvent(a));
            NeoForge.EVENT_BUS.post(new PlayerEvent.PlayerChangedDimensionEvent(b, net.minecraft.world.level.Level.OVERWORLD, net.minecraft.world.level.Level.NETHER));
            check(a.snapshots.size() == 1 && b.snapshots.size() == 1, "Login or dimension snapshot missing: a=" + a.snapshots.size() + ", b=" + b.snapshots.size());
            for (var p : new TestPlayer[]{a, b}) {
                var bytes = new FriendlyByteBuf(Unpooled.wrappedBuffer(p.snapshots.get(0)));
                try {
                    check(bytes.readVarInt() == 1, "Wrong S2C message id");
                    var snapshot = new S2CSyncAll(); snapshot.fromBytes(bytes);
                    check(snapshot.statLevels[0] == (p == a ? 3 : 7), "Cross-player snapshot data leak");
                } finally { bytes.release(); }
            }
            a.giveExperiencePoints(1000); request(a, StatBase.stats.indexOf(Stats.MINING), 1);
            check(DataHelper.getPlayerStatLevel(a, Stats.MINING) == 4 && DataHelper.getPlayerStatLevel(b, Stats.MINING) == 7, "Upgrade affected another player");
            check(a.snapshots.size() == 2 && b.snapshots.size() == 1, "Upgrade reply sent to wrong player");
        });
        test("network_snapshot_roundtrip", () -> {
            var p = player(level); DataHelper.setPlayerStatLevel(p, Stats.MINING, 4); DataHelper.setPlayerRevertStatLevel(p, Stats.MINING, 2);
            var message = new S2CSyncAll(p); var buffer = new FriendlyByteBuf(Unpooled.buffer());
            try {
                message.toBytes(buffer); var copy = new S2CSyncAll(); copy.fromBytes(buffer);
                check(copy.statLevels.length == 21 && copy.revertedStatLevels.length == 21, "Snapshot size");
                check(copy.statLevels[0] == 4 && copy.revertedStatLevels[0] == 2, "Snapshot content");
            } finally { buffer.release(); }
        });
        test("real_block_loot_treasure_and_magician", () -> {
            var p = player(level); var position = helper.absolutePos(new BlockPos(1, 1, 1));
            DataHelper.setPlayerStatLevel(p, Stats.TREASURE_FINDER, 3);
            boolean treasure = false;
            for (int i = 0; i < 500; i++) {
                var drops = Block.getDrops(Blocks.DIRT.defaultBlockState(), level, position, null, p, new ItemStack(Items.DIAMOND_SHOVEL));
                if (drops.stream().anyMatch(stack -> !stack.is(Items.DIRT))) treasure = true;
                check(drops.size() < 20, "Recursive treasure loot");
            }
            check(treasure, "No treasure after 500 real dirt loot rolls");
            DataHelper.setPlayerStatLevel(p, Stats.MINING_MAGICIAN, 1);
            var old = Stats.MINING_MAGICIAN.config.bonusMultiplier.get();
            try {
                Stats.MINING_MAGICIAN.config.bonusMultiplier.set(10000.0);
                var drops = Block.getDrops(Blocks.DIAMOND_ORE.defaultBlockState(), level, position, null, p, new ItemStack(Items.DIAMOND_PICKAXE));
                check(drops.size() >= 2, "Mining magician missing additional drops");
                check(drops.size() < 20, "Unbounded mining magician loot");
            } finally { Stats.MINING_MAGICIAN.config.bonusMultiplier.set(old); }
        });
        test("periodic_sync_tick_handler", () -> {
            var old = GokiConfig.SERVER.syncTicks.get(); var timer = TickHandler.tickTimer.get();
            try {
                GokiConfig.SERVER.syncTicks.set(2); TickHandler.tickTimer.set(0);
                java.util.function.Supplier<ServerTickEvent.Post> tick = () -> new ServerTickEvent.Post(() -> true, level.getServer());
                NeoForge.EVENT_BUS.post(tick.get());
                check(TickHandler.tickTimer.get() == 1, "Periodic handler missing");
                NeoForge.EVENT_BUS.post(tick.get()); NeoForge.EVENT_BUS.post(tick.get());
                check(TickHandler.tickTimer.get() == 0, "Periodic sync timer did not reset");
                GokiConfig.SERVER.syncTicks.set(0); NeoForge.EVENT_BUS.post(tick.get());
                check(TickHandler.tickTimer.get() == 0, "Disabled periodic sync advanced");
            } finally { GokiConfig.SERVER.syncTicks.set(old); TickHandler.tickTimer.set(timer); }
        });
        test("commands_registered", () -> {
            var dispatcher = level.getServer().getCommands().getDispatcher();
            var root = dispatcher.getRoot().getChild("gokistats_reborn");
            check(root != null && root.getChild("gui") != null && root.getChild("reload") != null, "Commands missing");
            check(root.getChild("gui").getChild("target") != null, "GUI target command missing");
            var legacy = dispatcher.getRoot().getChild("gokistats");
            check(legacy != null && legacy.getRedirect() == root, "Legacy command alias missing");
            check(dispatcher.execute("gokistats reload", level.getServer().createCommandSourceStack()) == 0, "Legacy command alias execution failed");
        });
        GokiStats.log.info("GOKI_SERVER_TEST RESULT passed={} failed={}", passed, failed);
        helper.assertTrue(failed == 0, "GokiStats server checks failed: " + failed + " (passed=" + passed + ")");
        helper.succeed();
    }
}
