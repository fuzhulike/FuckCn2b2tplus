package com.windsor.fuckcn2b2tplusfabric;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Pattern;

public class PluginConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger("PluginConfig");

    // 配置文件路径
    private Path configPath;
    private Properties config;

    // 调试模式
    private boolean debugMode;

    // 聊天检查
    private boolean chatCheckEnabled;
    private boolean longMessageEnabled;
    private int maxMessageLength;
    private boolean linkDetectionEnabled;
    private Pattern urlPattern;
    private Pattern validUrlChars;
    private Pattern domainPortPattern;
    private boolean spamDetectionEnabled;
    private int spamTimeWindowSeconds;
    private int spamMaxMessages;
    private boolean excessiveDigitsEnabled;
    private int excessiveDigitCount;

    // 违规处罚
    private boolean penaltiesEnabled;
    private boolean notifyOp;
    private boolean silentMode;
    private String silentChatFormat;
    private int muteThresholdPoints;
    private int muteBaseDurationMinutes;
    private int muteAdditionalDurationMinutes;
    private int warningPointsMultiple;
    private String warningMessage;
    private int kickPoints;
    private String kickMessage;
    private int banPoints;
    private String banMessage;
    private boolean interceptPrivateMessage;
    private List<String> privateMessageCommands;
    private boolean interceptSign;
    private boolean interceptAnvil;
    private boolean interceptBook;

    // 新玩家判定
    private boolean activeScoreEnabled;
    private double activeScoreThreshold;
    private List<StatWeight> statWeights;
    private int playtimeThresholdSeconds;

    // 新玩家提醒
    private boolean loginMessageEnabled;
    private boolean periodicReminderEnabled;
    private int reminderIntervalTicks;
    private String reminderMessage;

    public PluginConfig() {
        this.statWeights = new ArrayList<>();
        this.config = new Properties();
    }

    public void load() {
        configPath = FabricLoader.getInstance().getConfigDir().resolve("fuckcn2b2tplus.properties");

        // 如果配置文件不存在，创建默认配置
        if (!Files.exists(configPath)) {
            createDefaultConfig();
        }

        // 加载配置
        try (InputStream in = Files.newInputStream(configPath)) {
            config.load(in);
        } catch (IOException e) {
            LOGGER.error("加载配置文件失败: {}", e.getMessage());
            createDefaultConfig();
        }

        // 解析配置
        parseConfig();
        LOGGER.info("配置已加载: {}", configPath);
    }

    private void createDefaultConfig() {
        try {
            config.setProperty("debug-mode", "false");
            config.setProperty("chat-check.enabled", "true");
            config.setProperty("chat-check.long-message.enabled", "true");
            config.setProperty("chat-check.long-message.max-length", "30");
            config.setProperty("chat-check.link-detection.enabled", "true");
            config.setProperty("chat-check.spam-detection.enabled", "true");
            config.setProperty("chat-check.spam-detection.time-window-seconds", "15");
            config.setProperty("chat-check.spam-detection.max-messages", "3");
            config.setProperty("chat-check.excessive-digits.enabled", "true");
            config.setProperty("chat-check.excessive-digits.max-digit-count", "9");
            config.setProperty("violation-penalties.enabled", "true");
            config.setProperty("violation-penalties.notify-op", "true");
            config.setProperty("violation-penalties.silent-mode", "true");
            config.setProperty("violation-penalties.mute.threshold-points", "5");
            config.setProperty("violation-penalties.mute.base-duration-minutes", "5");
            config.setProperty("violation-penalties.mute.additional-duration-minutes", "1");
            config.setProperty("violation-penalties.warning.points-multiple", "10");
            config.setProperty("violation-penalties.kick.points", "41");
            config.setProperty("violation-penalties.ban.points", "61");
            config.setProperty("violation-penalties.intercept.private-message", "true");
            config.setProperty("violation-penalties.intercept.sign", "true");
            config.setProperty("violation-penalties.intercept.anvil", "true");
            config.setProperty("violation-penalties.intercept.book", "true");
            config.setProperty("new-player-detection.active-score.enabled", "true");
            config.setProperty("new-player-detection.active-score.threshold", "30000.0");
            config.setProperty("new-player-detection.playtime.threshold-seconds", "86400");
            config.setProperty("new-player-reminder.login-message", "true");
            config.setProperty("new-player-reminder.periodic-reminder.enabled", "true");
            config.setProperty("new-player-reminder.periodic-reminder.interval-ticks", "18000");
            config.setProperty("new-player-reminder.message", "[聊天管制] 新玩家有一段时间的严管聊天行为，期间不允许频繁发送消息、发送长消息、发送链接。正常游玩即可结束聊天严管。");

            Files.createDirectories(configPath.getParent());
            try (OutputStream out = Files.newOutputStream(configPath)) {
                config.store(out, "FuckCn2b2t Fabric Configuration");
            }
        } catch (IOException e) {
            LOGGER.error("创建默认配置失败: {}", e.getMessage());
        }
    }

    private void parseConfig() {
        debugMode = Boolean.parseBoolean(config.getProperty("debug-mode", "false"));

        chatCheckEnabled = Boolean.parseBoolean(config.getProperty("chat-check.enabled", "true"));
        longMessageEnabled = Boolean.parseBoolean(config.getProperty("chat-check.long-message.enabled", "true"));
        maxMessageLength = Integer.parseInt(config.getProperty("chat-check.long-message.max-length", "30"));
        linkDetectionEnabled = Boolean.parseBoolean(config.getProperty("chat-check.link-detection.enabled", "true"));
        urlPattern = Pattern.compile(config.getProperty("chat-check.link-detection.url-pattern",
                "(?i)(?:https?://|ftp://|www\\.)[a-zA-Z0-9\\-.]+\\.(?:[a-zA-Z]{2,}(?:/\\S*)?)"));
        validUrlChars = Pattern.compile(config.getProperty("chat-check.link-detection.valid-url-chars", "[^a-zA-Z0-9.\\-/:?&=#]"));
        domainPortPattern = Pattern.compile(config.getProperty("chat-check.link-detection.domain-port-pattern",
                "[a-zA-Z0-9-]+(?:\\.[a-zA-Z0-9-]+)+\\s*[:：]\\s*\\d+"));
        spamDetectionEnabled = Boolean.parseBoolean(config.getProperty("chat-check.spam-detection.enabled", "true"));
        spamTimeWindowSeconds = Integer.parseInt(config.getProperty("chat-check.spam-detection.time-window-seconds", "15"));
        spamMaxMessages = Integer.parseInt(config.getProperty("chat-check.spam-detection.max-messages", "3"));
        excessiveDigitsEnabled = Boolean.parseBoolean(config.getProperty("chat-check.excessive-digits.enabled", "true"));
        excessiveDigitCount = Integer.parseInt(config.getProperty("chat-check.excessive-digits.max-digit-count", "9"));

        penaltiesEnabled = Boolean.parseBoolean(config.getProperty("violation-penalties.enabled", "true"));
        notifyOp = Boolean.parseBoolean(config.getProperty("violation-penalties.notify-op", "true"));
        silentMode = Boolean.parseBoolean(config.getProperty("violation-penalties.silent-mode", "true"));
        silentChatFormat = config.getProperty("violation-penalties.silent-chat-format", "[{player}]: {message}");
        muteThresholdPoints = Integer.parseInt(config.getProperty("violation-penalties.mute.threshold-points", "5"));
        muteBaseDurationMinutes = Integer.parseInt(config.getProperty("violation-penalties.mute.base-duration-minutes", "5"));
        muteAdditionalDurationMinutes = Integer.parseInt(config.getProperty("violation-penalties.mute.additional-duration-minutes", "1"));
        warningPointsMultiple = Integer.parseInt(config.getProperty("violation-penalties.warning.points-multiple", "10"));
        warningMessage = config.getProperty("violation-penalties.warning.message", "[聊天管制] 你聊天管制期内违规行为较多，如违规严重会导致踢出或封禁。");
        kickPoints = Integer.parseInt(config.getProperty("violation-penalties.kick.points", "41"));
        kickMessage = config.getProperty("violation-penalties.kick.message", "严管期内聊天频繁违规，继续违规会导致封禁！");
        banPoints = Integer.parseInt(config.getProperty("violation-penalties.ban.points", "61"));
        banMessage = config.getProperty("violation-penalties.ban.message", "严管期内聊天严重违规，详情请联系腐竹");
        interceptPrivateMessage = Boolean.parseBoolean(config.getProperty("violation-penalties.intercept.private-message", "true"));
        privateMessageCommands = Arrays.asList(config.getProperty("violation-penalties.intercept.private-message-commands", "tell,msg,w,message,whisper,m").split(","));
        interceptSign = Boolean.parseBoolean(config.getProperty("violation-penalties.intercept.sign", "true"));
        interceptAnvil = Boolean.parseBoolean(config.getProperty("violation-penalties.intercept.anvil", "true"));
        interceptBook = Boolean.parseBoolean(config.getProperty("violation-penalties.intercept.book", "true"));

        activeScoreEnabled = Boolean.parseBoolean(config.getProperty("new-player-detection.active-score.enabled", "true"));
        activeScoreThreshold = Double.parseDouble(config.getProperty("new-player-detection.active-score.threshold", "30000.0"));
        statWeights.clear();
        statWeights.add(new StatWeight("stat.mob_kills", 1.0));
        statWeights.add(new StatWeight("stat.mine_block", 0.5));
        statWeights.add(new StatWeight("stat.use_item", 0.2));
        statWeights.add(new StatWeight("stat.break_item", 10.0));
        statWeights.add(new StatWeight("stat.craft_item", 0.2));
        statWeights.add(new StatWeight("stat.damage_dealt", 0.2));
        statWeights.add(new StatWeight("stat.damage_taken", 0.5));
        statWeights.add(new StatWeight("stat.deaths", 10.0));
        statWeights.add(new StatWeight("stat.item_enchanted", 10.0));
        statWeights.add(new StatWeight("stat.fish_caught", 5.0));
        statWeights.add(new StatWeight("stat.traded_with_villager", 1.0));
        playtimeThresholdSeconds = Integer.parseInt(config.getProperty("new-player-detection.playtime.threshold-seconds", "86400"));

        loginMessageEnabled = Boolean.parseBoolean(config.getProperty("new-player-reminder.login-message", "true"));
        periodicReminderEnabled = Boolean.parseBoolean(config.getProperty("new-player-reminder.periodic-reminder.enabled", "true"));
        reminderIntervalTicks = Integer.parseInt(config.getProperty("new-player-reminder.periodic-reminder.interval-ticks", "18000"));
        reminderMessage = config.getProperty("new-player-reminder.message", "[聊天管制] 新玩家有一段时间的严管聊天行为，期间不允许频繁发送消息、发送长消息、发送链接。正常游玩即可结束聊天严管。");
    }

    // Getters
    public boolean isDebugMode() { return debugMode; }
    public boolean isChatCheckEnabled() { return chatCheckEnabled; }
    public boolean isLongMessageEnabled() { return longMessageEnabled; }
    public int getMaxMessageLength() { return maxMessageLength; }
    public boolean isLinkDetectionEnabled() { return linkDetectionEnabled; }
    public Pattern getUrlPattern() { return urlPattern; }
    public Pattern getValidUrlChars() { return validUrlChars; }
    public Pattern getDomainPortPattern() { return domainPortPattern; }
    public boolean isSpamDetectionEnabled() { return spamDetectionEnabled; }
    public int getSpamTimeWindowSeconds() { return spamTimeWindowSeconds; }
    public int getSpamMaxMessages() { return spamMaxMessages; }
    public boolean isExcessiveDigitsEnabled() { return excessiveDigitsEnabled; }
    public int getExcessiveDigitCount() { return excessiveDigitCount; }
    public boolean isPenaltiesEnabled() { return penaltiesEnabled; }
    public boolean isNotifyOp() { return notifyOp; }
    public boolean isSilentMode() { return silentMode; }
    public String getSilentChatFormat() { return silentChatFormat; }
    public int getMuteThresholdPoints() { return muteThresholdPoints; }
    public int getMuteBaseDurationMinutes() { return muteBaseDurationMinutes; }
    public int getMuteAdditionalDurationMinutes() { return muteAdditionalDurationMinutes; }
    public int getWarningPointsMultiple() { return warningPointsMultiple; }
    public String getWarningMessage() { return warningMessage; }
    public int getKickPoints() { return kickPoints; }
    public String getKickMessage() { return kickMessage; }
    public int getBanPoints() { return banPoints; }
    public String getBanMessage() { return banMessage; }
    public boolean isInterceptPrivateMessage() { return interceptPrivateMessage; }
    public List<String> getPrivateMessageCommands() { return privateMessageCommands; }
    public boolean isInterceptSign() { return interceptSign; }
    public boolean isInterceptAnvil() { return interceptAnvil; }
    public boolean isInterceptBook() { return interceptBook; }
    public boolean isActiveScoreEnabled() { return activeScoreEnabled; }
    public double getActiveScoreThreshold() { return activeScoreThreshold; }
    public List<StatWeight> getStatWeights() { return statWeights; }
    public int getPlaytimeThresholdSeconds() { return playtimeThresholdSeconds; }
    public boolean isLoginMessageEnabled() { return loginMessageEnabled; }
    public boolean isPeriodicReminderEnabled() { return periodicReminderEnabled; }
    public int getReminderIntervalTicks() { return reminderIntervalTicks; }
    public String getReminderMessage() { return reminderMessage; }

    public static class StatWeight {
        private final String statName;
        private final double weight;

        public StatWeight(String statName, double weight) {
            this.statName = statName;
            this.weight = weight;
        }

        public String getStatName() { return statName; }
        public double getWeight() { return weight; }
    }
}