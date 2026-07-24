package com.windsor.fuckcn2b2tplusfabric;

import net.minecraft.network.message.SignedMessage;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.ArrayDeque;
import java.util.Map;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

public class ChatListener {

    private final NewPlayerManager newPlayerManager;
    private final ViolationManager violationManager;
    private final PluginConfig config;
    private final Map<UUID, Queue<Long>> messageTimestamps = new ConcurrentHashMap<>();

    public ChatListener(NewPlayerManager newPlayerManager, ViolationManager violationManager, PluginConfig config) {
        this.newPlayerManager = newPlayerManager;
        this.violationManager = violationManager;
        this.config = config;
    }

    /**
     * 处理玩家聊天消息，返回 true 表示允许发送，false 表示拦截
     */
    public boolean onPlayerChat(SignedMessage message, ServerPlayerEntity sender) {
        String plainMessage = message.getContent().getString();

        // 优先处理禁言状态（无论新老玩家）
        if (violationManager.isMuted(sender)) {
            handleMutedPlayerChat(sender, plainMessage);
            return false;
        }

        // 非新玩家，放行
        if (!newPlayerManager.isNewPlayer(sender)) {
            return true;
        }

        // 聊天检查功能总开关
        if (!config.isChatCheckEnabled()) {
            recordMessage(sender);
            return true;
        }

        // 检查违规（包括频率）
        String reason = getViolationReason(plainMessage, sender);
        if (reason == null) {
            // 合法消息：记录时间戳，放行
            recordMessage(sender);
            return true;
        }

        // 违规处理
        violationManager.addViolation(sender, reason, plainMessage);

        // 仅自己可见模式（silent-mode）则向玩家发送假消息
        if (config.isSilentMode()) {
            sendFormattedMessageToPlayer(sender, plainMessage);
        }

        return false;
    }

    /**
     * 处理已禁言玩家的聊天消息
     */
    private void handleMutedPlayerChat(ServerPlayerEntity player, String plainMessage) {
        // 检查内容违规（忽略频率）
        if (config.isChatCheckEnabled()) {
            String reason = getViolationReasonForMuted(plainMessage);
            if (reason != null) {
                violationManager.addViolation(player, reason, plainMessage);
                // 禁言期间每次额外增加禁言时长
                if (config.getMuteAdditionalDurationMinutes() > 0) {
                    violationManager.addMuteTime(player, config.getMuteAdditionalDurationMinutes());
                }
            }
        }

        String logMsg = String.format("玩家 %s 在隐形禁言期间尝试发送：%s", player.getName().getString(), plainMessage);
        FuckCn2b2tplusFabric.LOGGER.info(logMsg);

        // OP 通知
        if (config.isNotifyOp()) {
            Text opMsg = Text.literal(String.format(
                    "[聊天管制] 玩家 %s 在隐形禁言期间尝试发送：%s",
                    player.getName().getString(), plainMessage
            ));
            if (FuckCn2b2tplusFabric.getServer() != null) {
                for (ServerPlayerEntity online : FuckCn2b2tplusFabric.getServer().getPlayerManager().getPlayerList()) {
                    if (online.hasPermissionLevel(2)) {
                        online.sendMessage(opMsg, false);
                    }
                }
            }
        }

        // 仅自己可见模式
        if (config.isSilentMode()) {
            sendFormattedMessageToPlayer(player, plainMessage);
        }
    }

    /**
     * 获取违规原因，若不违规返回 null
     * 用于新玩家（含频率检测）
     */
    private String getViolationReason(String plainMessage, ServerPlayerEntity player) {
        if (config.isLongMessageEnabled() && plainMessage.length() > config.getMaxMessageLength()) {
            return "发送超长消息";
        }
        if (config.isSpamDetectionEnabled() && isSpamming(player)) {
            return "频繁发送消息";
        }
        if (config.isLinkDetectionEnabled() && containsUrl(plainMessage)) {
            return "发送链接";
        }
        if (config.isExcessiveDigitsEnabled() && containsExcessiveDigits(plainMessage)) {
            return "发送过多数字";
        }
        return null;
    }

    /**
     * 获取违规原因（禁言版本，忽略频率）
     */
    private String getViolationReasonForMuted(String plainMessage) {
        if (config.isLongMessageEnabled() && plainMessage.length() > config.getMaxMessageLength()) {
            return "发送超长消息";
        }
        if (config.isLinkDetectionEnabled() && containsUrl(plainMessage)) {
            return "发送链接";
        }
        if (config.isExcessiveDigitsEnabled() && containsExcessiveDigits(plainMessage)) {
            return "发送过多数字";
        }
        return null;
    }

    // ==================================================================
    // 消息发送
    // ==================================================================

    private void sendFormattedMessageToPlayer(ServerPlayerEntity player, String messageContent) {
        String format = config.getSilentChatFormat()
                .replace("{player}", player.getName().getString())
                .replace("{message}", messageContent);
        player.sendMessage(Text.literal(format), false);
    }

    // ==================================================================
    // 检测方法
    // ==================================================================

    private boolean isSpamming(ServerPlayerEntity player) {
        UUID uuid = player.getUuid();
        Queue<Long> timestamps = messageTimestamps.computeIfAbsent(uuid, k -> new ArrayDeque<>());
        long now = System.currentTimeMillis();
        long cutoff = now - config.getSpamTimeWindowSeconds() * 1000;
        while (!timestamps.isEmpty() && timestamps.peek() < cutoff) {
            timestamps.poll();
        }
        return timestamps.size() >= config.getSpamMaxMessages();
    }

    private boolean containsUrl(String text) {
        if (text == null || text.isEmpty()) return false;

        // 域名+端口号检测
        if (config.getDomainPortPattern().matcher(text).find()) {
            return true;
        }

        // 净化后匹配标准 URL
        String sanitized = config.getValidUrlChars().matcher(text).replaceAll("");
        if (sanitized.length() < 5) return false;
        return config.getUrlPattern().matcher(sanitized).find();
    }

    private boolean containsExcessiveDigits(String text) {
        if (text == null || text.isEmpty()) return false;
        int digitCount = 0;
        int maxDigits = config.getExcessiveDigitCount();
        for (char c : text.toCharArray()) {
            if (c >= '0' && c <= '9') {
                digitCount++;
                if (digitCount >= maxDigits) {
                    return true;
                }
            }
        }
        return false;
    }

    private void recordMessage(ServerPlayerEntity player) {
        UUID uuid = player.getUuid();
        Queue<Long> timestamps = messageTimestamps.computeIfAbsent(uuid, k -> new ArrayDeque<>());
        timestamps.offer(System.currentTimeMillis());
    }
}