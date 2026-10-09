package net.infstudio.goki.common;

import net.infstudio.goki.common.network.GokiPacketHandler;
import net.infstudio.goki.common.network.message.S2COpenGui;
import net.infstudio.goki.common.utils.Reference;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;

@Mod.EventBusSubscriber(modid = Reference.MODID)
public class StatsCommand {
    @SubscribeEvent
    public static void register(RegisterCommandsEvent event) {
        var command = event.getDispatcher().register(Commands.literal(Reference.MODID)
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("reload").executes(context -> {
                    context.getSource().sendFailure(Component.translatable("commands.gokistats_reborn.reload.unavailable"));
                    return 0;
                }))
                .then(Commands.literal("gui")
                        .executes(context -> {
                            var player = context.getSource().getPlayerOrException();
                            GokiPacketHandler.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new S2COpenGui());
                            return 1;
                        })
                        .then(Commands.argument("target", EntityArgument.player()).executes(context -> {
                            var player = EntityArgument.getPlayer(context, "target");
                            GokiPacketHandler.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new S2COpenGui());
                            return 1;
                        }))));
        event.getDispatcher().register(Commands.literal(Reference.LEGACY_MODID)
                .requires(source -> source.hasPermission(2)).redirect(command));
    }
}
