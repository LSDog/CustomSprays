![CustomSprays](banner.png)

# <b>CustomSprays</b> | 花样喷漆 🎉

> ![logo](logo64.png)
> ![use](https://s1.ax1x.com/2022/04/18/Ldo6SK.gif)

- " 就像起源游戏的喷漆一样! "
- " Just like source games' spraying! "

## 📢 介绍 | Introduce
___
>
> |*Name*|*Content*|
> |:---:|---|
> |名字 <br> Name    |CustomSprays ~ 花样喷漆|
> |作者 <br> Author  |LSDog|
> |版本 <br> Version |1.5.1|
> |支持 <br> Support | Bukkit \ Spigot \ Paper <b>1.8 ~ 1.18</b> |
> |介绍 <br> Description |让玩家在服务器"喷涂"自定义图片！<br>Let players spray theirs image in the server!|
> |指令 <br> Cmd     |`/cspray ...` - 主命令 <br>`/spray` - 喷漆<br>`/spray big` - 喷大喷漆|
> |权限 <br> Perm    |请见 / see "config.yml"|
> 
> Mcbbs >> https://www.mcbbs.net/thread-1289391-1-1.html
>
> Spigot >> https://www.spigotmc.org/resources/customsprays-upload-image-and-spray-it-on-the-wall.98979/

## ⏬ <b>下载 | Download</b>: [Releases](https://gitee.com/pixelmc/CustomSprays/releases) !!
### [Releases](https://gitee.com/pixelmc/CustomSprays/releases) !! [Releases](https://gitee.com/pixelmc/CustomSprays/releases) !!

<b>⭐ 1.8 ~ 1.18 适配完毕 ！ fully supported ! ⭐</b>

<b>⭐ English message config is also in config.yml (commented out by #) ⭐</b>



## ✨ 用法 | Usage
___
### 步骤 | steps:

    1 | 将 CustomSprays.jar 放入 plugins 文件夹，重启服务器
      |
      | (现在去复制图片链接)
      |
    2 | 输入指令: /cspray upload <url>  -  上传图片
    3 | 输入指令: /spray                -  对着墙喷漆
      |
    4 | 🎇 如果服务器版本 ≥ 1.9，【快速双击F键】也可以喷漆！🎇


    1 | Put "CustomSprays.jar" in "plugins" folder，restart the server.
      |
      | (now get ur image link)
      |
    2 | Send command: /cspray upload <url>  -  Upload image
    3 | Send command: /spray                -  Spray at wall
      |
    4 | 🎇 If server version ≥ 1.9，【Quick double click "F"】can also do the spray！🎇

![QuickUse](QuickUse.gif "糊到爆炸的示意图")

<b>📡双击 "F" 喷漆! </b>(交换主副手的快捷键)

<b>📡 Double click "F" to spray! </b>(the key exchanges items in you both hand)


### 💻 更多指令发送 "/cspray" |  More commands send "/cspray"
### 💻 更多设置请见 "config.yml" | More configuration see "config.yml"

## 🎮 版本支持 | Version Support
___
> ⭕ <b>经 *测试* 后支持</b> : 1.8 ~ 1.18
> 
> ⭕ <b>Support after *TEST* </b> : 1.8 ~ 1.18

> 📏 支持的"领域"插件: <br>
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
- 📦 All packet sending to achieve【no interact with spray | no generating "map_xxx.dat"】


- 📐 限制喷漆的世界或区域！
- 📐 Disable spraying in specific worlds or regions!


- ☕ 无需前置API，发包纯手搓（
- ☕ No API required ，making NMS sh-t by my self（


- 🔗 玩家上传图片链接 >> 文本存储图片 (image->base64) >> 喷漆后自动消失
- 🔗Player uploads link >> Storage image (image->base64) >> spray auto disappear

<br>

... *[已完善]* <sub>(~~仅支持 128\*128px 图片~~)</sub> 保存数据时直接压缩图片到 384*384 大小...

... *[Fixed]* <sub>(~~Only support 128\*128px image~~)</sub> Save the image directly in 384*384 ...



## ⚡ 注意事项
___

> 已知问题 <b>*[仅限版本1.8~1.12]*</b>：当某一世界地图数量 >32467 时 <sub>(世界地图最多为32767)</sub> 喷漆会导致某些原生地图被暂时替换
>
>> <b>原因:</b> 插件使用 MapView id [32467~32767] 对喷漆进行标识，在喷漆时可能会一并被发包上色


> Known problems <b>*[Only in version 1.8~1.12]*</b>: When the map count is >32467 in a world <sub>(max 32767 in a world)</sub>, spraying will make some original map replaced by spray image temporarily.
>
>> <b>Reason:</b> Plugin use MapView id [32467~32767] to target the spray ...So map with that id will be replaced.



## ✍ TODO
___
- 空 (笑)



## 💖 & DONATE
___
本项目由 [PixelMC](http://pixelmc.cn/) 团队提供长期支持与维护

![logo](banner_logo.png)

如果您有能力，可以考虑捐献我们，帮助我们做得更好！

微信：
[图片]
支付宝：
[图片]

### 爱发电 | Aifadian  https://afdian.net/@LSDog