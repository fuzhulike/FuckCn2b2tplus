# FuckCn2b2t Fabric

> 新玩家聊天行为管制、反广告刷屏模组 —— Fabric 服务端版

## 简介

这是 FuckCn2b2t 插件的 Fabric 模组版本，专门解决离线服务器容易遭遇的新玩家广告/违规消息刷屏问题。

## 功能

- 新玩家判定（活跃度积分 / 在线时长）
- 聊天严管（超长消息、链接、频繁消息、过多数字检测）
- 阿瓦隆隐形禁言系统
- 违规处置（警告、踢出、封禁）
- 禁言期间拦截（私聊、告示牌、铁砧、书与笔）

## 安装

1. 下载最新版本的 JAR 文件
2. 放入 Fabric 服务端的 `mods/` 目录
3. 确保已安装 Fabric API
4. 启动服务器

## 编译

```bash
./gradlew build
```

编译产物位于 `build/libs/fuckcn2b2t-fabric-*.jar`。

## 配置

配置文件位于 `config/fuckcn2b2t.properties`，首次启动自动生成。

## 命令

| 命令 | 说明 | 权限 |
|------|------|------|
| `/fkcn2b2t mute <玩家> <分钟>` | 隐形禁言 | OP (权限等级 2+) |
| `/fkcn2b2t unmute <玩家>` | 解除隐形禁言 | OP |
| `/fkcn2b2t stat [玩家]` | 查看玩家活跃数据 | OP |
| `/fkcn2b2t reload` | 重载配置 | OP |
| `/shadowban <玩家> <分钟>` | 平替禁言命令 | OP |
| `/newplayerstat [玩家]` | 平替统计命令 | OP |

## 许可证

GNU General Public License v3.0