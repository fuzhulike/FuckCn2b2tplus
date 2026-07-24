package com.windsor.fuckcn2b2tplusfabric;

import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class InteractionListener {

    private final ViolationManager violationManager;
    private final PluginConfig config;

    // 记录铁砧界面中原始物品，用于还原
    private final ConcurrentHashMap<UUID, String> originalItemNames = new ConcurrentHashMap<>();

    public InteractionListener(ViolationManager violationManager, PluginConfig config) {
        this.violationManager = violationManager;
        this.config = config;
    }

    /**
     * 处理告示牌交互
     */
    public ActionResult onSignInteract(PlayerEntity player, World world, Hand hand, BlockHitResult hitResult) {
        if (!(player instanceof ServerPlayerEntity serverPlayer)) {
            return ActionResult.PASS;
        }

        if (!config.isInterceptSign()) {
            return ActionResult.PASS;
        }

        if (!violationManager.isMuted(serverPlayer)) {
            return ActionResult.PASS;
        }

        // 检查是否是告示牌
        var blockState = world.getBlockState(hitResult.getBlockPos());
        var block = blockState.getBlock();

        if (block instanceof net.minecraft.block.SignBlock || block instanceof net.minecraft.block.WallSignBlock) {
            // 玩家处于禁言状态，拦截告示牌编辑
            String logMsg = String.format("[聊天管制-告示牌拦截] 玩家 %s 在隐形禁言期间尝试编辑告示牌",
                    serverPlayer.getName().getString());
            FuckCn2b2tplusFabric.LOGGER.info(logMsg);
            notifyOps(String.format("玩家 %s 在隐形禁言期间尝试编辑告示牌", serverPlayer.getName().getString()));
            return ActionResult.FAIL;
        }

        return ActionResult.PASS;
    }

    /**
     * 处理铁砧交互（简化版）
     */
    public void onAnvilUse(ServerPlayerEntity player, String newName) {
        if (!config.isInterceptAnvil()) return;
        if (!violationManager.isMuted(player)) return;

        String logMsg = String.format("[聊天管制-铁砧拦截] 玩家 %s 在隐形禁言期间尝试将物品重命名为: %s",
                player.getName().getString(), newName);
        FuckCn2b2tplusFabric.LOGGER.info(logMsg);
        notifyOps(String.format("玩家 %s 在隐形禁言期间尝试将物品重命名为: %s",
                player.getName().getString(), newName));
    }

    /**
     * 处理书与笔编辑（简化版）
     */
    public void onBookEdit(ServerPlayerEntity player, String bookContent) {
        if (!config.isInterceptBook()) return;
        if (!violationManager.isMuted(player)) return;

        String logMsg = String.format("[聊天管制-书与笔拦截] 玩家 %s 在隐形禁言期间尝试编辑书与笔，内容:\n%s",
                player.getName().getString(), bookContent);
        FuckCn2b2tplusFabric.LOGGER.info(logMsg);
        notifyOps(String.format("玩家 %s 在隐形禁言期间尝试编辑书与笔", player.getName().getString()));
    }

    /**
     * 辅助方法：通知在线OP
     */
    private void notifyOps(String message) {
        if (!config.isNotifyOp()) return;

        Text notification = Text.literal("[聊天管制] " + message);
        if (FuckCn2b2tplusFabric.getServer() != null) {
            for (ServerPlayerEntity online : FuckCn2b2tplusFabric.getServer().getPlayerManager().getPlayerList()) {
                if (online.hasPermissionLevel(2)) {
                    online.sendMessage(notification, false);
                }
            }
        }
    }
}