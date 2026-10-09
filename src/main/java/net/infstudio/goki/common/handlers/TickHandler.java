package net.infstudio.goki.common.handlers;

import net.infstudio.goki.api.stat.Stats;
import net.infstudio.goki.common.config.GokiConfig;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.util.ObfuscationReflectionHelper;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import net.minecraft.resources.ResourceLocation;
import net.infstudio.goki.common.utils.Reference;
import java.util.concurrent.atomic.AtomicInteger;

import static net.infstudio.goki.common.utils.Reference.MODID;

@EventBusSubscriber(modid = MODID)
public class TickHandler {
    public static final ResourceLocation knockbackResistanceID = ResourceLocation.fromNamespaceAndPath(Reference.MODID, "knockbackresistanceid");
    public static final ResourceLocation stealthSpeedID = ResourceLocation.fromNamespaceAndPath(Reference.MODID, "stealthspeedid");
    public static final ResourceLocation swimSpeedID = ResourceLocation.fromNamespaceAndPath(Reference.MODID, "swimspeedid");

    public static AtomicInteger tickTimer = new AtomicInteger();

    @SubscribeEvent
    public static void playerTick(PlayerTickEvent.Pre event) {
        if (!event.getEntity().level().isClientSide()) { // Due to issue #32
            var player = event.getEntity();

            handleTaskPlayerAPI(player);
            if (!player.isAlive()) return;
            var attributeInstance = player.getAttribute(Attributes.MOVEMENT_SPEED);
            if (attributeInstance == null) return;
            var modifier = new AttributeModifier(stealthSpeedID, Stats.STEALTH.getBonus(player) / 100.0F, AttributeModifier.Operation.ADD_MULTIPLIED_BASE);
            if (player.isCrouching()) {
                var existing = attributeInstance.getModifier(stealthSpeedID);
                if (existing == null || Double.compare(existing.amount(), modifier.amount()) != 0) {
                    attributeInstance.removeModifier(stealthSpeedID);
                    attributeInstance.addTransientModifier(modifier);
                }
            } else if (attributeInstance.getModifier(stealthSpeedID) != null) {
                attributeInstance.removeModifier(modifier);
            }

            attributeInstance = player.getAttribute(Attributes.KNOCKBACK_RESISTANCE);
            if (attributeInstance == null) return;
            modifier = new AttributeModifier(knockbackResistanceID, Stats.STEADY_GUARD.getBonus(player), AttributeModifier.Operation.ADD_VALUE);
            if (player.isBlocking()) {
                var existing = attributeInstance.getModifier(knockbackResistanceID);
                if (existing == null || Double.compare(existing.amount(), modifier.amount()) != 0) {
                    attributeInstance.removeModifier(knockbackResistanceID);
                    attributeInstance.addTransientModifier(modifier);
                }
            } else if (attributeInstance.getModifier(knockbackResistanceID) != null) {
                attributeInstance.removeModifier(modifier);
            }
        }
    }

    @SubscribeEvent
    public static void serverTick(ServerTickEvent.Post event) {
        if (GokiConfig.SERVER.syncTicks.get() > 0) {
            if (tickTimer.get() == GokiConfig.SERVER.syncTicks.get()) {
                tickTimer.lazySet(0);
                for (var player : ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayers()) {
                    SyncEventHandler.syncPlayerData(player);
                }
            } else {
                tickTimer.getAndIncrement();
            }
        }
    }

    public static boolean isJumping(LivingEntity livingBase) {
        return Boolean.TRUE.equals(ObfuscationReflectionHelper.getPrivateValue(LivingEntity.class, livingBase, "jumping"));
    }

    private static void handleTaskPlayerAPI(Player player) {
        if (!player.isLocalPlayer() || player.canRiderInteract())
            /*if (player.isSwimming()) {
                var multiplier = Math.max(0.0F,
                        Stats.SWIMMING.getBonus(player));
                if (isJumping(player)) {
//                    player += multiplier;
                } else if (multiplier > 0) {
                    //			player.moveEntity(	player.motionX * multiplier,
                    //								player.motionY * multiplier,
                    //								player.motionZ * multiplier);

                    // Copied from LivingEntity
                    var d0 = player.getY();
                    var f1 = 0.8f;
                    var f2 = 0.02F;

                    f1 += (0.54600006F - f1) * multiplier;
                    f2 += (player.getSpeed() - f2) * multiplier;

                    player.moveRelative(f2, player.getDeltaMovement());
                    player.move(MoverType.SELF, player.getDeltaMovement());
                    player.setDeltaMovement(player.getDeltaMovement().multiply(f1, 0.800000011920929D, f1));

                    if (!player.isNoGravity()) {
                        player.setDeltaMovement(player.getDeltaMovement().add(0, 0.02, 0));
                    }

                    var offset = player.getDeltaMovement().add(0, 0.6000000238418579D - player.getY() + d0, 0);
//                    if (player.horizontalCollision && player.isOffsetPositionInLiquid(offset.x, offset.y, offset.z)) {
//                        player.setMotion(new Vector3d(player.getMotion().x, 0.30000001192092896D, player.getMotion().z));
//                    }


//                    player.move(MoverType.SELF, new Vector3d(player.moveStrafing * multiplier, player.moveVertical * multiplier, 0.02f));
                }
            }*/

        if (player.onClimbable() && !player.isShiftKeyDown()) {
            var multiplier = Stats.CLIMBING.getBonus(player);
            player.move(MoverType.SELF, player.getDeltaMovement().multiply(0, multiplier, 0));
        }
    }

}
