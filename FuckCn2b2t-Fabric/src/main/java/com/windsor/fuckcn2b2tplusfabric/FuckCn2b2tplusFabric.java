package com.windsor.fuckcn2b2tplusfabric;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.message.v1.ServerMessageEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.MinecraftServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FuckCn2b2tplusFabric implements ModInitializer {

    public static final String MOD_ID = "fuckcn2b2tplus-fabric";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private static MinecraftServer server;
    private static PluginConfig config;
    private static NewPlayerManager newPlayerManager;
    private static ViolationManager violationManager;
    private static ChatListener chatListener;
    private static InteractionListener interactionListener;
    private static long tickCount = 0;

    @Override
    public void onInitialize() {
        LOGGER.info("FuckCn2b2t Fabric 正在初始化...");

        // 加载配置
        config = new PluginConfig();
        config.load();

        // 初始化管理器
        newPlayerManager = new NewPlayerManager(config);
        violationManager = new ViolationManager(config);
        chatListener = new ChatListener(newPlayerManager, violationManager, config);
        interactionListener = new InteractionListener(violationManager, config);

        // 注册事件
        registerEvents();

        LOGGER.info("FuckCn2b2t Fabric 已启用");
    }

    private void registerEvents() {
        // 服务器启动事件
        ServerLifecycleEvents.SERVER_STARTED.register(serverInstance -> {
            server = serverInstance;
            LOGGER.info("FuckCn2b2t Fabric 服务器已启动");
        });

        // 服务器关闭事件
        ServerLifecycleEvents.SERVER_STOPPING.register(serverInstance -> {
            if (violationManager != null) {
                violationManager.saveData();
            }
        });

        // 玩家加入事件
        ServerPlayConnectionEvents.JOIN.register((handler, sender, serverInstance) -> {
            if (config.isLoginMessageEnabled()) {
                serverInstance.execute(() -> {
                    newPlayerManager.checkAndRemindPlayer(handler.getPlayer());
                });
            }
        });

        // 聊天消息事件 - 允许取消消息
        ServerMessageEvents.ALLOW_CHAT_MESSAGE.register((message, sender, params) -> {
            return chatListener.onPlayerChat(message, sender);
        });

        // 定时提醒任务
        ServerTickEvents.END_SERVER_TICK.register(serverInstance -> {
            tickCount++;
            if (config.isPeriodicReminderEnabled() && 
                tickCount % config.getReminderIntervalTicks() == 0) {
                newPlayerManager.checkAndRemindNewPlayers();
            }
        });

        // 注册命令
        Fkcn2b2tCommand.register();
    }

    public static MinecraftServer getServer() {
        return server;
    }

    public static PluginConfig getConfig() {
        return config;
    }

    public static NewPlayerManager getNewPlayerManager() {
        return newPlayerManager;
    }

    public static ViolationManager getViolationManager() {
        return violationManager;
    }
}