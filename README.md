********![CustomSprays](banner.png)


# <b>CustomSprays</b>™ 

## 介绍
___
允许玩家提供 图片链接 在服务器中展示喷涂

因兼容需要，率先开发 *[≤1.12]* 版本 。（原因：1.13及以上的版本将允许展示框在方块上下两端放置）



## 用法
___
[此处应该有gif图]

    | 将 CustomSprays.jar 放入 plugins 文件夹，重启服务器
    | /cspary upload <url>  -  上传图片
    | /spary   -   对着墙喷漆

> 如果服务器版本 ≥ 1.9，<b>【快速双击F键】</b>也可以喷漆！

> 更多设置请见 config.yml

## 版本支持
___
> <b>经测试后支持</b> : 1.12

> 理论支持<sup>\**需要测试*\*</sup> : | 1.8 | 1.9 | 1.10 | 1.11 |

> <b>尚未支持</b> : ≥1.13



## 特性
___

- 无 Bungeecord 端插件，所有数据皆由 MySQL/yml 进行存储

- 纯发包实现喷漆【玩家无法交互、不产生任何废料 (map_xxx.dat)】 / 无需前置API

- 玩家上传图片链接 >> 文本存储图片 (image->base64) >> 喷漆(地图)自动消失

- *[已完善]* ~~仅支持 128\*128px 图片~~  保存数据时自动压缩图片到128*128大小



## 注意事项
___

> 已知问题：当某一世界地图数量 >32467 时<sub>(世界地图最大为32767)</sub>，喷漆会导致某些原生地图被暂时替换
>
>> <b>原因:</b> 插件使用 MapView id [32467~32767] 对喷漆进行标识，在喷漆时可能会一并被发包上色
>
> *(风险较小 , 尚未找到解决方法)*



## TODO
___
- Nothing......



## 支持与捐赠
___
本项目由[PixelMC](http://pixelmc.cn/)团队提供长期支持与维护
<img src=“https://gitee.com/pixelmc/CustomSprays/raw/master/fav.png” width=200px>
<img src=“https://gitee.com/pixelmc/CustomSprays/raw/master/banner_logo.png” width=400px>

如果您有能力，可以考虑捐献我们，帮助我们做的更好！

微信：
[图片]
支付宝：
[图片]