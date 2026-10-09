package net.infstudio.goki.common.network.handler;

import net.infstudio.goki.GokiStats;
import net.infstudio.goki.api.stat.StatBase;
import net.infstudio.goki.api.stat.Stats;
import net.infstudio.goki.common.config.GokiConfig;
import net.infstudio.goki.common.network.GokiPacketHandler;
import net.infstudio.goki.common.network.message.C2SRequestStatSync;
import net.infstudio.goki.common.network.message.C2SStatSync;
import net.infstudio.goki.common.network.message.S2CStatSync;
import net.infstudio.goki.common.network.message.S2CSyncAll;
import net.infstudio.goki.common.utils.DataHelper;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

import java.util.function.Supplier;

public class PacketSyncHandler {
    public static void acceptC2S(C2SStatSync message, Supplier<NetworkEvent.Context> context) {
        var ctx = context.get();
        ctx.enqueueWork(() -> {
            var player = ctx.getSender();
            if (player == null || message.stat < 0 || message.stat >= StatBase.stats.size()
                    || (message.amount != 1 && message.amount != -1)) {
                GokiStats.log.debug("Rejected invalid skill request: stat={}, amount={}", message.stat, message.amount);
                return;
            }
            var stat = StatBase.stats.get(message.stat);
            if (!stat.isEnabled()) return;
            int level = DataHelper.getPlayerStatLevel(player, stat);
            int reverted = DataHelper.getPlayerRevertStatLevel(player, stat);
            if (message.amount == -1) {
                if (level <= 0 || !DataHelper.canPlayerRevertStat(player, stat)) return;
                int refund = (int) (stat.getCost(level - 1) * GokiConfig.SERVER.globalRevertFactor.get());
                DataHelper.setPlayerRevertStatLevel(player, stat, ++reverted);
                DataHelper.setPlayerStatLevel(player, stat, level - 1);
                player.giveExperiencePoints(refund);
            } else {
                if (level >= stat.getLimit()) return;
                int cost = stat.getCost(level);
                if (DataHelper.getXPTotal(player) < cost) return;
                player.giveExperiencePoints(-cost);
                DataHelper.setPlayerStatLevel(player, stat, level + 1);
            }
            if (stat == Stats.MAX_HEALTH) DataHelper.resetMaxHealth(player);
            GokiStats.log.debug("Updated skill {} to level {} (reverted={})", stat.getRegistryName(), level + message.amount, reverted);
            GokiPacketHandler.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player),
                    new S2CStatSync(message.stat, level + message.amount, reverted));
        });
        ctx.setPacketHandled(true);
    }

    public static void onRequestSyncAll(C2SRequestStatSync message, Supplier<NetworkEvent.Context> context) {
        var ctx = context.get();
        ctx.enqueueWork(() -> {
            var player = ctx.getSender();
            if (player != null) {
                GokiPacketHandler.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new S2CSyncAll(player));
            }
        });
        ctx.setPacketHandled(true);
    }
}
