# 花样喷漆 || **CustomSprays**

> **用法展示 (Demonstration)**
> ![use](https://s1.ax1x.com/2022/04/18/Ldo6SK.gif)


## 介绍 | Info
___

|          *Name*           | *Content*                                                                                        |
|:-------------------------:|--------------------------------------------------------------------------------------------------|
| ![logo](media/logo64.png) | 花样喷漆 (CustomSprays)                                                                              |
|        作者 (Author)        | LSDog                                                                                            |
|       版本 (Version)        | 1.5.23                                                                                           |
|       支持 (Support)        | **1.7.10 ~ 26.1** (Bukkit Spigot Paper etc.)                                                     |
|       指令 (Command)        | `/sprays` - 主命令 (main help command) <br>`/spray` - 喷漆 (spray)<br>`/spray big` - 喷大喷漆 (big spray) |
|         权限 (Perm)         | 请见 `config.yml` (please see `config.yml`)                                                        |
> 
> 其他发布页面 (other release links) :
> | [Modrinth](https://modrinth.com/plugin/customsprays)
> | [Spigot](https://www.spigotmc.org/resources/customsprays-upload-image-and-spray-it-on-the-wall.98979/)
> | [PlanetMinecraft](https://www.planetminecraft.com/mod/customsprays-spray-your-image-like-in-source-games/)
> | [Gitee](https://gitee.com/pixelmc/CustomSprays)
> |

## **下载 | Download**
### [发布页 | Releases](https://github.com/LSDog/CustomSprays/releases)
(上面Github打不开? → 打开[Gitee发布页](https://gitee.com/pixelmc/CustomSprays/releases))

## 其他资源 | Other Resources

**[喷漆音效资源包](https://github.com/LSDog/CustomSprays/blob/master/spray_sound_pack.zip?raw=true)
| [Spray sound resource pack](https://github.com/LSDog/CustomSprays/blob/master/spray_sound_pack.zip?raw=true)**

**[ItemsAdder 支持](https://github.com/LSDog/CustomSprays/tree/itemsadder)
| [ItemsAdder Support](https://github.com/LSDog/CustomSprays/tree/itemsadder)**

**[oraxen 支持](https://github.com/LSDog/CustomSprays/tree/oraxen)
| [oraxen Support](https://github.com/LSDog/CustomSprays/tree/oraxen)**

**Hey! 💬 English messages are at the bottom of config.yml, you can replace it with chinese part, and now u have english messages!**

## 用法 | Usage
___
### 步骤

- 将 CustomSprays.jar 放入 plugins 文件夹，重启服务器
- 
- (现在去复制图片链接)
- 输入指令: /sprays               -  查看所有可用指令
- 输入指令: /sprays upload <url>  -  上传图片
- 输入指令: /spray                -  对着墙喷漆

### Steps:

- Put "CustomSprays.jar" in "plugins" folder，restart the server.
- 
- (now get ur image link)
- Send command: /sprays               -  See all available commands
- Send command: /sprays upload <url>  -  Upload image
- Send command: /spray                -  Spray at wall

## 配置 | Config

详见 `config.yml`

See `config.yml` for details.


## 版本支持 | Version Support
___

📏 支持的 **"领域"** 插件 | Supported **"region"** plugins:
- [x] Residence .......... [ *?* ~ 5.0.1.3+ ]
- [x] WorldGuard ......... [ 6.*?* ~ 7.*x* ]
- [x] GriefDefender ...... [ *?* ~ 2.1.4+ ]

(理论上支持更新的版本 | newer versions should be ok)

## 特性 | Features
___

- 🙅‍ 无 Bungeecord 端插件，所有数据皆由 MySQL / yml 进行存储   
- 📦 使用发包实现喷漆
- 📐 限制喷漆的世界或区域
- 📺 可自定义喷漆物品或音效   
- 👋 复制别人的喷漆，或者禁止别人喷漆！
- ☕ 无需前置API
- 💴 或者使用 Vault 经济花钱喷漆！


- 🙅‍ NO Bungeecord plugin，all the data storage in MySQL / yml  
- 📦 Generate sprays by sending packets
- 📐 Disable spraying in specific worlds or regions
- 📺 Custom spray item / sound effect！
- 👋 Copy others spray or prevent others from copying yours!
- ☕ No other lib required
- 💴 Or use Vault to cost players money when spraying！

## 工作原理 | How it works
1. 插件启动时，插件会依据不同的服务端游戏版本，通过反射获取所有所需的 NMS 类和方法
2. 当玩家使用喷漆指令上传图片时，插件会将图片转换成地图数据
3. 这些数据将被存储在 MySQL 或 yml 文件中，一个玩家只有一个喷漆数据，一定冷却时间后可更改
4. 玩家喷漆时，插件通过发包将喷漆的展示框、地图和图案发送给所有玩家：
   1. 喷漆图案会以无边框地图的形式显示在墙上
   2. 经过一段时间后，喷漆图案会被自动移除
   3. 如果玩家在喷漆时按住了 `Shift` 键，喷漆图案会被放大显示
   4. 当拥有 CustomSprays 管理权限的玩家左键喷漆时，喷漆图案会被删除，并会提示喷漆者的名称


1. On plugin startup, the plugin uses reflection to obtain all required NMS classes and methods for the server version.
2. When a player uploads an image using the spray command, the plugin converts the image into map data.
3. The data is stored in MySQL or a yml file; each player can have only one spray, which can be changed after a cooldown.
4. When a player sprays, the plugin sends packets containing the spray frame, map, and image to all players:
   1. The spray image is displayed on the wall as a borderless map.
   2. The spray image is automatically removed after a period of time.
   3. If the player holds `Shift` while spraying, the spray image is shown enlarged.
   4. When a player with CustomSprays admin permission left-clicks a spray, the spray is removed and the spray owner's name is notified.


## 注意事项 | Warning
___

> 🔺 已知问题 ***[仅限版本1.7-1.12]***：当某一世界地图数量 >32467 时 (最多为32767) 喷漆会导致某些原生地图被暂时替换  
> 🔺 Known problem ***[Only in version 1.7-1.12]***: When the map count is >32467 in a world (max 32767), spraying will make some original map replaced by spray image temporarily.
>
>> **原因:** 这些版本中插件使用 id [32467-32767] 对地图图案(MapView)进行标识，喷漆时对图案会被替换  
>> **Reason:** In these versions plugin uses id [32467-32767] to target the Mapview, so map with that id will be replaced.

## 💻 开发 | for Developers
___
### 你只需要...
- **1.9+ 服务端** 作为库 (推荐1.12.2)
- 直接构建！构建结果可用于全部适用版本
### You only need...
- **1.9+ server jar** as the library (1.12.2 recommended)
- Build the jar! the build results can be used for all applicable versions

## ✍ TODO
___
- [ ] 不同世界和位置的管理
- [ ] 根据喷漆位置动态调整发包的玩家范围
- [x] 左键喷漆删除喷漆 (因为不想用protocol lib所以需要研究手写pack listener)
- [ ] 自动获取NMS中用到的混淆名称，这样就不用随版本更新手动添加case了
- [x] 喷漆动画 (粒子效果；颜色可以与图像颜色稍微对应←貌似这一点做不到欸)
- [x] folia 支持 (需要更多研究)
- [ ] 粒子喷漆 (用粒子效果组成图像)
- [ ] gif 动图支持 (需要更多研究)
- [ ] SQLite 支持
- [ ] 商店版 (购买预制的喷漆)
- [ ] 喷漆仓库，多文件存储



##  💖 DONATE ME 💖 喜欢我就给我发电！！
___

### 打赏 | Donate:
> [爱发电 | Aifadian](https://afdian.com/@LSDog) or [PayPal](https://paypal.me/LSDog)

![bstats](https://bstats.org/signatures/bukkit/CustomSprays.svg)

## (Visits)
![Visits](https://count.getloli.com/get/@CustomSprays?darkmode=0)  
