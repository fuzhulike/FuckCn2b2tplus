# FuckCn2b2tplus

> 新玩家聊天行为管制、反广告刷屏插件/模组 —— 去你妈的cn2b2t广告，准备放飞刷屏狗的马！

## 致谢

本项目基于 [WindsorMacmillan/FuckCn2b2t](https://github.com/WindsorMacmillan/FuckCn2b2t) 开发，在此对原作者 Windsor Macmillan 表示衷心感谢。

## 简介
基于 FuckCn2b2t 插件，FuckCn2b2tplus 是一个 **Paper/Folia 插件** 和 **Fabric 模组**，专门解决离线服务器容易遭遇的新玩家广告/违规消息刷屏问题。  
与市面上的常见关键词审查/AI检查聊天内容的插件不同，这个插件只针对服务器新玩家——他们往往是广告刷屏骚扰、敏感信息爆破的重灾区。  
插件按照可配置的玩家活跃统计数据判断其是否为新玩家，并严管新玩家发言行为。老玩家不会受此插件管控，你可以将此插件与其他任意聊天检查插件不冲突地同时使用。  
新玩家发言严管期间不可发送超长消息/链接/长数字、频繁发送消息，违规则会被插件自动拦截并抄送管理员/控制台。   
开发者并不喜欢"文字狱"式地让关键字识别与违规消息造字规避审查进行日复一日的军备竞赛，此插件设计出来的目的就是优雅且不留痕迹地防范广告消息于无形，
从而降低被违规者试探发现消息审查规律的概率。***为此精心设计的阿瓦隆隐形禁言系统，可以让违规玩家对自己的发言被拦截毫不知情。***

## 功能细节

### 新玩家判定（模式二选一）

- **活跃度积分判定**（推荐）：通过统计玩家各项游戏行为（击杀、挖矿、附魔等）并加权计算积分，达到阈值即解除管制。可防止玩家通过挂机闲逛、消极游戏混时长熬过严管期再进行广告刷屏。
- **在线时长判定**（回退模式）：简单以游玩时长判定新玩家，达到指定时长即解除管制。  
判定规则可配置，支持PAPI占位符拓展统计维度（Fabric 版本使用内置统计系统）。

### 新玩家聊天严管

- **超长消息检测**：新玩家发送的消息（剔除掉所有颜色格式的纯文本）字符数量超出限制，则视为违规并拦截。
- **链接检测**：新玩家发送的信息包含链接，则视为违规并拦截。支持检测 `域名:端口` 格式，支持抗干扰，即玩家在链接之间插入无意义字符来规避检测（如：b哈a哈i d～u .～c哈o哈m，会被识别到链接baidu.com）
- **频繁消息检测**：新玩家在规定时间窗口内发言超过次数限制，则视为违规并拦截。
- **过多数字检测**：新玩家发送的消息中数字字符总量超限，则视为违规并拦截（防开盒发手机号/身份证）。
- **InteractiveChat 兼容**：自动移除聊天组件标记，避免消息超长被误判（Paper 版本）。

每项检查均可独立开关。

### 违规处置

- **阿瓦隆模式**（推荐）：取消消息发送，但向发言者发送一条格式化的"成功"假消息，使其发送消息仅自己可见，使违规者难以察觉其发言被吞掉！
- **消息抄送OP**：玩家违规聊天内容可抄送给在线OP。
- **累积违规禁言**：每次违规累计积分，达到阈值自动禁言，禁言时长随次数递增。
- **禁言期间其他拦截**：玩家禁言期间私聊命令、告示牌、铁砧重命名、书与笔均会被静默拦截，并将内容抄送给管理员。
- **警告/踢出/封禁**：各级处罚的阈值和消息完全可配置

<img width="1909" height="1037" alt="发言管制1" src="https://github.com/user-attachments/assets/b7686c3d-cb51-4f8b-831d-348387db1afe" />
<img width="1912" height="1031" alt="发言管制2" src="https://github.com/user-attachments/assets/e7bc46f8-8369-4ada-bb01-89f254fe9f9c" />
<img width="1919" height="1029" alt="发言管制3" src="https://github.com/user-attachments/assets/f6856205-32d6-4eef-a478-3686c465432d" />
<img width="1919" height="1035" alt="发言管制4" src="https://github.com/user-attachments/assets/0693d1c2-9d4a-46f3-aebd-e5eb3917c4b3" />


### **版本兼容性**

| 服务端/模组加载器 | 支持状态 | 说明 |
|------------------|----------|------|
| ✅ **Paper / Folia** | 支持 | 需要服务端版本 1.21.3+（使用了高版本铁砧相关API） |
| ✅ **Fabric** | 支持 | Minecraft 1.21.1，需要 Fabric API |
| ❌ **Bukkit / Spigot** | 不支持 | 使用了 Paper API 的 AsyncChatEvent |
| ❌ **Forge / NeoForge** | 不支持 | 需要单独开发 |

## 命令

### Paper/Folia 插件版本

| 命令                         | 说明        | 权限 | 平替命令                   |
|----------------------------|-----------|----|------------------------|
| `/fkcn2b2tplus mute <玩家> <分钟>` | 隐形禁言，单位分钟 | OP | `/shadowban <玩家> <分钟>` |
| `/fkcn2b2tplus mute <玩家> off`  | 解除隐形禁言    | OP | `/shadowban <玩家> off`  |
| `/fkcn2b2tplus unmute <玩家>`    | 解除隐形禁言    | OP | —                      |
| `/fkcn2b2tplus stat [玩家]`      | 查看玩家活跃数据  | OP | `/newplayerstat [玩家]`  |
| `/fkcn2b2tplus reload`         | 重载配置文件    | OP | —                      |

### Fabric 模组版本

| 命令                         | 说明        | 权限 | 平替命令                   |
|----------------------------|-----------|----|------------------------|
| `/fkcn2b2tplus mute <玩家> <分钟>` | 隐形禁言，单位分钟 | OP (权限等级 2+) | `/shadowban <玩家> <分钟>` |
| `/fkcn2b2tplus unmute <玩家>`    | 解除隐形禁言    | OP | —                      |
| `/fkcn2b2tplus stat [玩家]`      | 查看玩家活跃数据  | OP | `/newplayerstat [玩家]`  |
| `/fkcn2b2tplus reload`         | 重载配置文件    | OP | —                      |

所有命令均支持 Tab 补全（玩家名、常用分钟数）。

## 权限

### Paper/Folia 版本

| 权限节点                | 默认值 | 说明      |
|---------------------|-----|---------|
| `fuckcn2b2t.reload` | op  | 允许重载配置  |
| `fuckcn2b2t.bypass` | op  | 跳过新玩家判定 |

### Fabric 版本

Fabric 版本使用 Minecraft 原生权限系统，需要 OP 权限等级 2+ 才能执行管理命令。

## 配置文件

### Paper/Folia 版本

插件首次加载时自动生成 `plugins/FuckCn2b2t/config.yml`，包含四个板块：

```
一、新玩家聊天检查功能类    → new-player-chat-check
  ├── 超长消息检测
  ├── InteractiveChat 兼容
  ├── 链接检测（含域名+端口）
  ├── 频繁消息检测
  └── 过多数字检测

二、聊天违规处置措施类      → violation-penalties
  ├── 静默模式 / OP通知
  ├── 禁言阈值与时长
  ├── 警告/踢出/封禁消息
  └── 禁言期间拦截项（私聊/告示牌/铁砧/书笔）

三、新玩家判定规则类        → new-player-detection
  ├── 活跃度积分判定（统计项与权重）
  └── 在线时长判定（回退模式）

四、新玩家提醒              → new-player-reminder
  ├── 登录提醒 / 定时提醒
  └── 提醒内容
```

### Fabric 模组版本

模组首次加载时自动生成 `config/fuckcn2b2t.properties`，配置项与 Paper 版本类似，使用 properties 格式。

所有阈值、开关、消息文本均可在配置文件中修改，修改后执行 `/fkcn2b2t reload` 即时生效。

## 安装

### Paper/Folia 插件版本

1. 下载最新版本 JAR（`fuckcn2b2t-0.1.jar`）
2. 放入 `plugins/` 目录
3. 重启服务器或使用 `/reload` 加载
4. 建议安装 [PlaceholderAPI](https://modrinth.com/plugin/placeholderapi)
5. 安装PAPI扩展：Player 和 Statistic。（`/papi ecloud download Player` 和 `/papi ecloud download Statistic`）

### Fabric 模组版本

1. 下载最新版本 JAR（`fuckcn2b2t-fabric-0.1.0.jar`）
2. 放入 `mods/` 目录
3. 确保已安装 **Fabric API**
4. 重启服务器
5. 配置文件自动生成在 `config/fuckcn2b2t.properties`

## 编译

### Paper/Folia 插件版本

```bash
cd FuckCn2b2t-master
mvn clean package
```  
编译产物位于 `target/fuckcn2b2t-0.1.jar`。

### Fabric 模组版本

```bash
cd FuckCn2b2t-Fabric
gradle build
```  
编译产物位于 `build/libs/fuckcn2b2t-fabric-0.1.0.jar`。

## 许可证

[GNU General Public License v3.0](LICENSE)

### Paper/Folia 版本依赖
- **Paper API** — MIT 许可证（兼容 GPL-3.0）
- **PlaceholderAPI** — GPL-3.0 许可证（本项目因此采用 GPL-3.0）

### Fabric 版本依赖
- **Fabric Loader** — Apache-2.0 许可证
- **Fabric API** — Apache-2.0 许可证
- **Minecraft** — Mojang EULA