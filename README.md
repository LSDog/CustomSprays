![CustomSprays](banner.png)

# **CustomSprays** | 花样喷漆 🎉

> ![logo](logo64.png)👈这事Logo 👇**大概是一些用法** 👉这是右手
> ![use](https://s1.ax1x.com/2022/04/18/Ldo6SK.gif)

- ***" 就像起源游戏的喷漆一样！ "***
- ***" Just like source games' spraying! "***

## 📢 介绍 | Introduce
___
>
> |*Name*|*Content*|
> |:---:|---|
> |名字 <br> Name    |CustomSprays ~ 花样喷漆|
> |作者 <br> Author  |LSDog|
> |版本 <br> Version |1.6.0|
> |支持 <br> Support | Bukkit \ Spigot \ Paper **1.8 ~ 1.19** |
> |介绍 <br> Description |让玩家在服务器"喷涂"自定义图片！<br>Let players spray theirs image in the server!|
> |指令 <br> Cmd     |`/cspray ...` - 主命令 <br>`/spray` - 喷漆<br>`/spray big` - 喷大喷漆|
> |权限 <br> Perm    |请见 / see "config.yml"|
> 
> Mcbbs >> https://www.mcbbs.net/thread-1289391-1-1.html
>
> Spigot >> https://www.spigotmc.org/resources/customsprays-upload-image-and-spray-it-on-the-wall.98979/

## ⏬ **下载 | Download**: [Releases](https://gitee.com/pixelmc/CustomSprays/releases) !!
### [Releases](https://gitee.com/pixelmc/CustomSprays/releases) !! [Releases](https://gitee.com/pixelmc/CustomSprays/releases) !!


**⭐ [Spray音效资源包](https://gitee.com/pixelmc/CustomSprays/raw/master/spray_sound_pack.zip) ！ [Spray sound resource pack](https://gitee.com/pixelmc/CustomSprays/raw/master/spray_sound_pack.zip) ! ⭐**

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



### 💻 更多指令发送 "/cspray" |  Send "/cspray" for more commands  

### 💻 更多设置请见 "config.yml" | See "config.yml" for more configuration

## 🎮 版本支持 | Version Support
___
> ⭕ **1.8 ~ 1.19**

> 📏 支持的"领域"插件:  
> 📏 Supported "region" plugins:
>    * Residence - [ *x* ~ 5.0.1.3+ ]
>    * WorldGuard - [ 6.*x* ~ 7.*x* ]
>    * GriefDefender - [ *x* ~ 2.1.4+ ]
> 
> (理论上能支持更新的版本 | newer versions should be ok)

## 🏆 特性
___

- 🙅‍ 无 Bungeecord 端插件，所有数据皆由 MySQL / yml 进行存储   
- 🙅‍ NO Bungeecord-side plugin，all the data storage in MySQL / yml  


- 📦 纯发包实现喷漆【玩家无法交互 | 不生成map_xxx.dat】    
- 📦 All packet sending to achieve【never interact with spray | no generating "map_xxx.dat"】  


- 📐 限制喷漆的世界或区域！ 
- 📐 Disable spraying in specific worlds or regions! 


- 📺 自定义喷漆物品/喷漆音效！   
- 📺 Custom spray item / spray sound effect！ 


- 👋 复制别人的喷漆，或者禁止别人喷漆！
- 👋 Copy others spray or prevent others copying yours!


- ☕ 无需前置API，发包纯手搓（  
- ☕ No API required ，making NMS sh-t by my self（    


- 🔗 玩家上传图片链接 >> 文本存储图片 (image->base64) >> 喷漆后自动消失   
- 🔗Player uploads link >> Storage image (image->base64) >> spray auto disappear 


## ⚡ 注意事项
___

> 已知问题 ***[仅限版本1.8~1.12]***：当某一世界地图数量 >32467 时 (最多为32767) 喷漆会导致某些原生地图被暂时替换
>
>> **原因:** 插件使用 MapView id [32467~32767] 对喷漆进行标识，在喷漆时可能会一并被发包上色


> Known problems ***[Only in version 1.8~1.12]***: When the map count is >32467 in a world (max 32767), spraying will make some original map replaced by spray image temporarily.
>
>> **Reason:** Plugin use MapView id [32467~32767] to target the spray ...So map with that id will be replaced.



## ✍ TODO
___
- SQLite 支持
- 有时间做一个商店版的... 就是需要买预制的喷漆的那种插件
- 喷漆仓库，实际上跟上面这个设想有相通的地方，问题是如果单个玩家仓库过大加载单个玩家yaml时间会很长，所以应该要搞成分文件的，直接把384*384转成的bytearray存到奇怪后缀的文件里，sql就不管了，该扩展扩展



## 💖 & DONATE
___
本项目由 [PixelMC](http://pixelmc.cn/) 团队提供长期支持与维护

![logo](banner_logo.png)


### 爱发电 | Aifadian  https://afdian.net/@LSDog

![who use? I use!](https://bstats.org/signatures/bukkit/CustomSprays.svg)