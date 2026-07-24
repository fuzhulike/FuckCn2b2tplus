package com.windsor.fuckcn2b2tplusfabric;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public class Fkcn2b2tCommand {

    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            // 注册主命令
            dispatcher.register(CommandManager.literal("fkcn2b2t")
                    .requires(source -> source.hasPermissionLevel(2))
                    .then(CommandManager.literal("mute")
                            .then(CommandManager.argument("player", EntityArgumentType.player())
                                    .then(CommandManager.argument("minutes", IntegerArgumentType.integer(1))
                                            .executes(Fkcn2b2tCommand::mutePlayer)
                                    )
                                    .then(CommandManager.literal("off")
                                            .executes(Fkcn2b2tCommand::unmutePlayer)
                                    )
                            )
                    )
                    .then(CommandManager.literal("unmute")
                            .then(CommandManager.argument("player", EntityArgumentType.player())
                                    .executes(Fkcn2b2tCommand::unmutePlayer)
                            )
                    )
                    .then(CommandManager.literal("stat")
                            .then(CommandManager.argument("player", EntityArgumentType.player())
                                    .executes(Fkcn2b2tCommand::showStat)
                            )
                            .executes(Fkcn2b2tCommand::showStatSelf)
                    )
                    .then(CommandManager.literal("reload")
                            .executes(Fkcn2b2tCommand::reloadConfig)
                    )
            );

            // 注册 shadowban 命令
            dispatcher.register(CommandManager.literal("shadowban")
                    .requires(source -> source.hasPermissionLevel(2))
                    .then(CommandManager.argument("player", EntityArgumentType.player())
                            .then(CommandManager.argument("minutes", IntegerArgumentType.integer(1))
                                    .executes(Fkcn2b2tCommand::mutePlayer)
                            )
                            .then(CommandManager.literal("off")
                                    .executes(Fkcn2b2tCommand::unmutePlayer)
                            )
                    )
            );

            // 注册 newplayerstat 命令
            dispatcher.register(CommandManager.literal("newplayerstat")
                    .requires(source -> source.hasPermissionLevel(2))
                    .then(CommandManager.argument("player", EntityArgumentType.player())
                            .executes(Fkcn2b2tCommand::showStat)
                    )
                    .executes(Fkcn2b2tCommand::showStatSelf)
            );
        });
    }

    private static int mutePlayer(CommandContext<ServerCommandSource> context) {
        try {
            ServerPlayerEntity target = EntityArgumentType.getPlayer(context, "player");
            int minutes = IntegerArgumentType.getInteger(context, "minutes");

            ViolationManager violationManager = FuckCn2b2tplusFabric.getViolationManager();
            violationManager.addMuteTime(target, minutes);

            context.getSource().sendFeedback(() -> Text.literal(
                    String.format("已将玩家 %s 隐形禁言 %d 分钟", target.getName().getString(), minutes)
            ), true);

            return 1;
        } catch (Exception e) {
            context.getSource().sendError(Text.literal("执行命令时出错: " + e.getMessage()));
            return 0;
        }
    }

    private static int unmutePlayer(CommandContext<ServerCommandSource> context) {
        try {
            ServerPlayerEntity target = EntityArgumentType.getPlayer(context, "player");

            ViolationManager violationManager = FuckCn2b2tplusFabric.getViolationManager();
            violationManager.removeMute(target);

            context.getSource().sendFeedback(() -> Text.literal(
                    String.format("已解除玩家 %s 的隐形禁言", target.getName().getString())
            ), true);

            return 1;
        } catch (Exception e) {
            context.getSource().sendError(Text.literal("执行命令时出错: " + e.getMessage()));
            return 0;
        }
    }

    private static int showStat(CommandContext<ServerCommandSource> context) {
        try {
            ServerPlayerEntity target = EntityArgumentType.getPlayer(context, "player");
            NewPlayerManager newPlayerManager = FuckCn2b2tplusFabric.getNewPlayerManager();

            double score = newPlayerManager.calculateActiveScore(target);
            boolean isNew = newPlayerManager.isNewPlayer(target);

            context.getSource().sendFeedback(() -> Text.literal(
                    String.format("玩家 %s 的活跃积分: %.2f, 是否为新玩家: %s",
                            target.getName().getString(), score, isNew ? "是" : "否")
            ), false);

            return 1;
        } catch (Exception e) {
            context.getSource().sendError(Text.literal("执行命令时出错: " + e.getMessage()));
            return 0;
        }
    }

    private static int showStatSelf(CommandContext<ServerCommandSource> context) {
        try {
            ServerPlayerEntity player = context.getSource().getPlayer();
            if (player == null) {
                context.getSource().sendError(Text.literal("此命令只能由玩家执行"));
                return 0;
            }

            NewPlayerManager newPlayerManager = FuckCn2b2tplusFabric.getNewPlayerManager();
            double score = newPlayerManager.calculateActiveScore(player);
            boolean isNew = newPlayerManager.isNewPlayer(player);

            context.getSource().sendFeedback(() -> Text.literal(
                    String.format("你的活跃积分: %.2f, 是否为新玩家: %s", score, isNew ? "是" : "否")
            ), false);

            return 1;
        } catch (Exception e) {
            context.getSource().sendError(Text.literal("执行命令时出错: " + e.getMessage()));
            return 0;
        }
    }

    private static int reloadConfig(CommandContext<ServerCommandSource> context) {
        try {
            PluginConfig config = FuckCn2b2tplusFabric.getConfig();
            config.load();

            context.getSource().sendFeedback(() -> Text.literal("配置已重载"), true);
            return 1;
        } catch (Exception e) {
            context.getSource().sendError(Text.literal("重载配置时出错: " + e.getMessage()));
            return 0;
        }
    }
}