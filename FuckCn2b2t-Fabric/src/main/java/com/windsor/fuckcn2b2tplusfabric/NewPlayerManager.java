package com.windsor.fuckcn2b2tplusfabric;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.stat.Stats;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;

public class NewPlayerManager {

    private final PluginConfig config;
    private List<PluginConfig.StatWeight> statWeights = new ArrayList<>();

    public NewPlayerManager(PluginConfig config) {
        this.config = config;
        loadStatWeights();
    }

    private void loadStatWeights() {
        statWeights = new ArrayList<>(config.getStatWeights());
    }

    /**
     * 判断玩家是否为新玩家
     */
    public boolean isNewPlayer(ServerPlayerEntity player) {
        // 调试模式：所有玩家视为新玩家
        if (config.isDebugMode()) {
            return true;
        }

        // OP 权限：跳过新玩家判定
        if (player.hasPermissionLevel(4)) {
            return false;
        }

        // 启用活跃度检查且配置有效
        if (config.isActiveScoreEnabled() && !statWeights.isEmpty()) {
            double score = calculateActiveScore(player);
            return score < config.getActiveScoreThreshold();
        }

        // 回退：使用在线时长判定
        int playedSeconds = getPlaytimeInSeconds(player);
        return playedSeconds < config.getPlaytimeThresholdSeconds();
    }

    /**
     * 计算玩家的活跃度积分
     */
    public double calculateActiveScore(ServerPlayerEntity player) {
        double total = 0.0;
        var statHandler = player.getStatHandler();
        
        for (PluginConfig.StatWeight sw : statWeights) {
            try {
                Identifier statId = Identifier.tryParse(sw.getStatName().replace("stat.", "minecraft:"));
                var stat = Stats.CUSTOM.getOrCreateStat(statId);
                int value = statHandler.getStat(stat);
                total += value * sw.getWeight();
            } catch (Exception e) {
                // 忽略解析失败的项
            }
        }
        return total;
    }

    /**
     * 获取玩家的游玩时间（秒）
     */
    private int getPlaytimeInSeconds(ServerPlayerEntity player) {
        return player.getStatHandler().getStat(Stats.CUSTOM.getOrCreateStat(Identifier.tryParse("minecraft:play_time"))) / 20;
    }

    /**
     * 向玩家发送聊天管制提醒
     */
    public void sendReminderMessage(ServerPlayerEntity player) {
        String rawMessage = config.getReminderMessage();
        player.sendMessage(Text.literal(rawMessage), false);
    }

    /**
     * 遍历所有在线玩家，对每个符合条件的玩家发送定时提醒
     */
    public void checkAndRemindNewPlayers() {
        if (FuckCn2b2tplusFabric.getServer() == null) return;

        for (ServerPlayerEntity player : FuckCn2b2tplusFabric.getServer().getPlayerManager().getPlayerList()) {
            checkAndRemindPlayer(player);
        }
    }

    /**
     * 判断单个玩家是否需要发送提醒，并执行发送
     */
    public void checkAndRemindPlayer(ServerPlayerEntity player) {
        if (isNewPlayer(player)) {
            sendReminderMessage(player);
        }
    }

    public boolean isActiveCheckEnabled() {
        return config.isActiveScoreEnabled() && !statWeights.isEmpty();
    }

    public double getActiveThreshold() {
        return config.getActiveScoreThreshold();
    }
}