![CustomSprays](banner.png)

# **CustomSprays** | 花样喷漆 🎉

> 👇**大概是一些用法 | bla bla usages**
> ![use](https://s1.ax1x.com/2022/04/18/Ldo6SK.gif)

## （＾∀＾●）ﾉｼ
![Visits](https://count.getloli.com/get/@CustomSprays)  
*[~ 这是一个猫猫计数器 ^ↀᴥↀ^ **MoeCounter** !](https://moe-counter.glitch.me/)*

## 📢 介绍 | Introduce
___

|       *Name*        | *Content*                                                    |
|:-------------------:|--------------------------------------------------------------|
| ![logo](logo64.png) | CustomSprays <br> 花样喷漆                                       |
|   作者 <br> Author    | LSDog                                                        |
|   版本 <br> Version   | 1.5.8                                                        |
|   支持 <br> Support   | **1.8 ~ 1.20** (Bukkit Spigot Paper CatServer Mohist...)     |
|     指令 <br> Cmd     | `/cspray ...` - 主命令 <br>`/spray` - 喷漆<br>`/spray big` - 喷大喷漆 |
|    权限 <br> Perm     | 请见 "config.yml" / see "config.yml"                           |
> 
> Links:
> [Mcbbs](https://www.mcbbs.net/thread-1289391-1-1.html)
> | [Spigot](https://www.spigotmc.org/resources/customsprays-upload-image-and-spray-it-on-the-wall.98979/)
> | [PlanetMinecraft](https://www.planetminecraft.com/mod/customsprays-spray-your-image-like-in-source-games/)

## ⏬ **下载 | Download** → [发布页](https://github.com/LSDogX/CustomSprays/releases) | [Releases](https://github.com/LSDogX/CustomSprays/releases) !!
(上面Github打不开? → [Gitee发布页](https://gitee.com/pixelmc/CustomSprays/releases))


**⭐ [喷漆音效资源包](https://github.com/LSDogX/CustomSprays/blob/master/spray_sound_pack.zip?raw=true) / [Spray sound resource pack](https://github.com/LSDogX/CustomSprays/blob/master/spray_sound_pack.zip?raw=true) ! ⭐**

**⭐ English message config is also in config.yml (commented out by #) ⭐**



## ✨ 用法 | Usage
___
### 步骤

    1 | 将 CustomSprays.jar 放入 plugins 文件夹，重启服务器
      |
      | (现在去复制图片链接)
      |
    2 | 输入指令: /cspray upload <url>  -  上传图片
    3 | 输入指令: /spray                -  对着墙喷漆
      |
    4 | 🎇 如果服务器版本 ≥ 1.9，【快速双击F键】也可以喷漆！🎇

### Steps:

    1 | Put "CustomSprays.jar" in "plugins" folder，restart the server.
      |
      | (now get ur image link)
      |
    2 | Send command: /cspray upload <url>  -  Upload image
    3 | Send command: /spray                -  Spray at wall
      |
    4 | 🎇 If server version ≥ 1.9，【Quick double click "F"】can also spray！🎇



### 💻 更多指令发送 "/sprays" |  Send "/sprays" for more commands  

### 💻 更多设置请见 "config.yml" | See "config.yml" for more configurations


## 🎮 版本支持 | Version Support
___
> 🔷 **1.8 ~ 1.20**

> 📏 支持的 **"领域"** 插件 | Supported **"region"** plugins:
> - [x] Residence .......... [ *?* ~ 5.0.1.3+ ]
> - [x] WorldGuard ......... [ 6.*?* ~ 7.*x* ]
> - [x] GriefDefender ...... [ *?* ~ 2.1.4+ ]
> 
> (理论上支持更新的版本 | newer versions should be ok)

## 🏆 特性 | Features
___

- 🙅‍ 无 Bungeecord 端插件，所有数据皆由 MySQL / yml 进行存储   
- 🙅‍ NO Bungeecord-side plugin，all the data storage in MySQL / yml  


- 📦 纯发包实现喷漆【避免交互 | 不生成垃圾】    
- 📦 100% use packets【no interact with spray | no junk generated】  


- 📐 限制喷漆的世界或区域！ 
- 📐 Disable spraying in specific worlds or regions! 


- 📺 自定义喷漆物品/音效！   
- 📺 Custom spray item / sound effect！ 


- 👋 复制别人的喷漆，或者禁止别人喷漆！
- 👋 Copy others spray or prevent others from copying yours!


- ☕ 无需前置API，发包纯手搓
- ☕ No API required ，making NMS sh-t by myself


- 🔗 玩家上传图片链接 >> 压缩存储图片 >> 喷漆后自动消失   
- 🔗 Player uploads link >> Zip & save image >> Spray self-destruct 


- 💴 或者使用 Vault 经济花钱喷漆！
- 💴 Or use Vault cost players money for spraying！


## ⚡ 注意事项 | Warning
___

> 🔺 已知问题 ***[仅限版本1.8~1.12]***：当某一世界地图数量 >32467 时 (最多为32767) 喷漆会导致某些原生地图被暂时替换  
> 🔺 Known problem ***[Only in version 1.8~1.12]***: When the map count is >32467 in a world (max 32767), spraying will make some original map replaced by spray image temporarily.
>
>> **原因:** 这些版本中插件使用 id [32467~32767] 对地图图案(MapView)进行标识，喷漆时对图案会被替换  
>> **Reason:** In these versions plugin uses id [32467~32767] to target the Mapview, so map with that id will be replaced.



## ✍ TODO
___
- [ ] SQLite 支持
- [ ] 有时间做一个商店版的... 就是需要买预制的喷漆的那种插件
- [ ] 喷漆仓库，实际上跟上面这个设想有相通的地方，问题是如果单个玩家仓库过大加载单个玩家yaml时间会很长，所以应该要搞成分文件的，直接把384*384转成的bytearray存到奇怪后缀的文件里，sql就不管了，该扩展扩展



##  💖 DONATE ME 💖 喜欢我就给我发电！！
___

### 打赏 | Donate: [爱发电 | Aifadian](https://afdian.net/@LSDog)

![who use? I use!](https://bstats.org/signatures/bukkit/CustomSprays.svg)

本项目由 [PixelMC](http://pixelmc.cn/) 团队提供长期支持与维护

![logo](banner_logo.png)
