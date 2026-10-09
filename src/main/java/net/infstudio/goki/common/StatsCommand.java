package net.infstudio.goki.common;

import net.infstudio.goki.common.network.GokiPacketHandler;
import net.infstudio.goki.common.network.message.S2COpenGui;
import net.infstudio.goki.common.utils.Reference;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber(modid = Reference.MODID)
public class StatsCommand {
    @SubscribeEvent
    public static void register(RegisterCommandsEvent event) {
        var command = event.getDispatcher().register(Commands.literal(Reference.MODID)
                .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                .then(Commands.literal("reload").executes(context -> {
                    context.getSource().sendFailure(Component.translatable("commands.gokistats_reborn.reload.unavailable"));
                    return 0;
                }))
                .then(Commands.literal("gui")
                        .executes(context -> {
                            var player = context.getSource().getPlayerOrException();
                            PacketDistributor.sendToPlayer(player, new S2COpenGui());
                            return 1;
                        })
                        .then(Commands.argument("target", EntityArgument.player()).executes(context -> {
                            var player = EntityArgument.getPlayer(context, "target");
                            PacketDistributor.sendToPlayer(player, new S2COpenGui());
                            return 1;
                        }))));
        event.getDispatcher().register(Commands.literal(Reference.LEGACY_MODID)
                .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS)).redirect(command));
    }
}
