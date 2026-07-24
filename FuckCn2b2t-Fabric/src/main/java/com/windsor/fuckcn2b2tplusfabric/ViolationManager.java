package com.windsor.fuckcn2b2tplusfabric;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.server.PlayerManager;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ViolationManager {

    private final PluginConfig config;
    private final Map<UUID, Integer> violationPoints = new ConcurrentHashMap<>();
    private final Map<UUID, Long> muteEndTime = new ConcurrentHashMap<>();
    private Path dataPath;

    public ViolationManager(PluginConfig config) {
        this.config = config;
        this.dataPath = Path.of("config", "fuckcn2b2t-data.properties");
        loadData();
    }

    private void loadData() {
        if (!Files.exists(dataPath)) {
            return;
        }

        try (InputStream in = Files.newInputStream(dataPath)) {
            java.util.Properties data = new java.util.Properties();
            data.load(in);

            for (String key : data.stringPropertyNames()) {
                if (key.startsWith("mute.")) {
                    try {
                        UUID uuid = UUID.fromString(key.substring(5));
                        long endTime = Long.parseLong(data.getProperty(key));
                        if (endTime > System.currentTimeMillis()) {
                            muteEndTime.put(uuid, endTime);
                        }
                    } catch (Exception ignored) {}
                } else {
                    try {
                        UUID uuid = UUID.fromString(key);
                        int points = Integer.parseInt(data.getProperty(key));
                        violationPoints.put(uuid, points);
                    } catch (Exception ignored) {}
                }
            }

            FuckCn2b2tplusFabric.LOGGER.info("已加载 " + violationPoints.size() + " 个玩家的违规积分，" +
                    muteEndTime.size() + " 个玩家的禁言数据");
        } catch (IOException e) {
            FuckCn2b2tplusFabric.LOGGER.error("加载违规数据失败: {}", e.getMessage());
        }
    }

    public void saveData() {
        try {
            java.util.Properties data = new java.util.Properties();

            for (Map.Entry<UUID, Integer> entry : violationPoints.entrySet()) {
                data.setProperty(entry.getKey().toString(), String.valueOf(entry.getValue()));
            }

            for (Map.Entry<UUID, Long> entry : muteEndTime.entrySet()) {
                if (entry.getValue() > System.currentTimeMillis()) {
                    data.setProperty("mute." + entry.getKey().toString(), String.valueOf(entry.getValue()));
                }
            }

            Files.createDirectories(dataPath.getParent());
            try (OutputStream out = Files.newOutputStream(dataPath)) {
                data.store(out, "FuckCn2b2t Violation Data");
            }
        } catch (IOException e) {
            FuckCn2b2tplusFabric.LOGGER.error("保存违规数据失败: {}", e.getMessage());
        }
    }

    public boolean isMuted(ServerPlayerEntity player) {
        UUID uuid = player.getUuid();
        Long end = muteEndTime.get(uuid);
        if (end == null) return false;
        if (end > System.currentTimeMillis()) {
            return true;
        } else {
            muteEndTime.remove(uuid);
            saveData();
            checkPointsOnMuteEnd(player);
            return false;
        }
    }

    private void checkPointsOnMuteEnd(ServerPlayerEntity player) {
        int points = getPoints(player);
        if (points >= config.getBanPoints()) {
            String reason = config.getBanMessage();
            MinecraftServer server = FuckCn2b2tplusFabric.getServer();
            if (server != null) {
                server.getPlayerManager().getUserBanList().add(
                        new net.minecraft.server.BannedPlayerEntry(player.getGameProfile())
                );
            }
            player.networkHandler.disconnect(Text.literal(reason));
        } else if (points >= config.getKickPoints()) {
            String reason = config.getKickMessage();
            player.networkHandler.disconnect(Text.literal(reason));
        }
    }

    public void addMuteTime(ServerPlayerEntity player, long minutes) {
        UUID uuid = player.getUuid();
        long currentEnd = muteEndTime.getOrDefault(uuid, System.currentTimeMillis());
        long newEnd = currentEnd + minutes * 60 * 1000;
        muteEndTime.put(uuid, newEnd);
        FuckCn2b2tplusFabric.LOGGER.info("玩家 {} 隐形禁言时间增加 {} 分钟，结束时间: {}",
                player.getName().getString(), minutes, newEnd);
        saveData();
    }

    public void removeMute(ServerPlayerEntity player) {
        UUID uuid = player.getUuid();
        if (muteEndTime.remove(uuid) != null) {
            saveData();
            FuckCn2b2tplusFabric.LOGGER.info("玩家 {} 的隐形禁言已被管理员手动解除", player.getName().getString());
        }
    }

    /**
     * 增加玩家的违规积分，并返回新的总分
     */
    public int addViolation(ServerPlayerEntity player, String violationReason, String messageContent) {
        UUID uuid = player.getUuid();
        int oldPoints = violationPoints.getOrDefault(uuid, 0);
        int newPoints = oldPoints + 1;
        violationPoints.put(uuid, newPoints);

        saveData();

        // 控制台日志
        FuckCn2b2tplusFabric.LOGGER.warn("玩家 {} 尝试{}（违规次数{}），被隐形拦截：{}",
                player.getName().getString(), violationReason, newPoints, messageContent);

        // OP 通知（根据配置）
        if (config.isNotifyOp()) {
            Text notification = Text.literal(String.format(
                    "[聊天管制] 玩家 %s 尝试%s（违规次数%d），被隐形拦截：%s",
                    player.getName().getString(), violationReason, newPoints, messageContent
            ));
            MinecraftServer server = FuckCn2b2tplusFabric.getServer();
            if (server != null) {
                for (ServerPlayerEntity online : server.getPlayerManager().getPlayerList()) {
                    if (online.hasPermissionLevel(2)) {
                        online.sendMessage(notification, false);
                    }
                }
            }
        }

        // 禁言检查
        if (config.getMuteThresholdPoints() > 0 && newPoints % config.getMuteThresholdPoints() == 0) {
            long muteMinutes = (long) config.getMuteBaseDurationMinutes() * (newPoints / config.getMuteThresholdPoints());
            addMuteTime(player, muteMinutes);
        }

        // 警告消息（达到 points-multiple 倍数时发送）
        if (newPoints % config.getWarningPointsMultiple() == 0) {
            String warnMsg = config.getWarningMessage();
            player.sendMessage(Text.literal(warnMsg), false);
        }

        // 踢出/封禁检查
        int finalNewPoints = newPoints;
        MinecraftServer server = FuckCn2b2tplusFabric.getServer();
        if (server != null) {
            server.execute(() -> {
                if (finalNewPoints >= config.getBanPoints()) {
                    String reason = config.getBanMessage();
                    server.getPlayerManager().getUserBanList().add(
                            new net.minecraft.server.BannedPlayerEntry(player.getGameProfile())
                    );
                    player.networkHandler.disconnect(Text.literal(reason));
                    muteEndTime.remove(uuid);
                } else if (finalNewPoints >= config.getKickPoints()) {
                    String reason = config.getKickMessage();
                    player.networkHandler.disconnect(Text.literal(reason));
                    muteEndTime.remove(uuid);
                }
                saveData();
            });
        }

        return newPoints;
    }

    public int getPoints(ServerPlayerEntity player) {
        return violationPoints.getOrDefault(player.getUuid(), 0);
    }
}