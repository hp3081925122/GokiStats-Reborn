package net.infstudio.goki.client.network.handler;

import net.infstudio.goki.api.stat.StatBase;
import net.infstudio.goki.api.stat.Stats;
import net.infstudio.goki.common.network.message.S2CStatSync;
import net.infstudio.goki.common.network.message.S2CSyncAll;
import net.infstudio.goki.common.utils.DataHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.function.Supplier;

public class PacketSyncClientHandler {
    public static void acceptSync(S2CStatSync message, IPayloadContext context) {
        var ctx = context;
        ctx.enqueueWork(() -> acceptSyncInternal(message));
    }

    public static void acceptSyncAll(S2CSyncAll message, IPayloadContext context) {
        var ctx = context;
        ctx.enqueueWork(() -> acceptSyncAllInternal(message));
    }
    public static void acceptSyncInternal(S2CStatSync message) {
        var player = Minecraft.getInstance().player;
        if (player == null)
            return;
        if (message.stat < 0 || message.stat >= StatBase.stats.size()) return;
        var stat = StatBase.stats.get(message.stat);
        DataHelper.setPlayerRevertStatLevel(player, stat, message.reverted);
        DataHelper.setPlayerStatLevel(player, stat, message.amount);
        if (stat == Stats.MAX_HEALTH) DataHelper.resetMaxHealth(player);
    }
    public static void acceptSyncAllInternal(S2CSyncAll message) {
        var player = Minecraft.getInstance().player;
        if (player == null)
            return;
        for (var i = 0; i < Math.min(message.statLevels.length, StatBase.stats.size()); i++) {
            DataHelper.setPlayerStatLevel(player,
                    StatBase.stats.get(i),
                    message.statLevels[i]);
        }
        for (var i = 0; i < Math.min(message.revertedStatLevels.length, StatBase.stats.size()); i++) {
            DataHelper.setPlayerRevertStatLevel(player, StatBase.stats.get(i), message.revertedStatLevels[i]);
        }
        DataHelper.resetMaxHealth(player);
    }
}
