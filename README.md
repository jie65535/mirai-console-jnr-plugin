# mirai-console-jnr-plugin

MiraiConsolePlugin 自定义戳一戳回复消息

## 指令列表

```bash
/jnr add [weight]    # 添加回复消息（权重默认为1）
/jnr add <message> [weight]    # 添加简单回复消息（权重默认为1）
/jnr clear    # 清空回复消息列表
/jnr list [page] [pageSize]   # 列出当前回复消息列表，参数可翻页
/jnr remove <index>    # 删除指定索引的回复消息
/jnr reload  # 重载配置
```

## 特殊消息

设置回复消息为以下内容，代表特殊含义

- `#nudge` 戳回去
- `#nudge:戳我干嘛！` 戳回去，并且回复一条消息
- `#group.mute:30` 禁言30s, 可以自定义禁言时间, 单位秒
- `#group.mute:60:生气了！禁言你1分钟` 同上，并且回复一条消息
- `#ignore` 忽略本次戳一戳
- `#audio:XXX.amr` 回复音频，参数通常为 XXX.amr，服务器要求文件名后缀必须为 ".amr"，但其编码方式也有可能是非
  AudioCodec.AMR。
  音频文件保存在 `data/me.jie65535.mirai-console-jnr-plugin/` 目录下，理论上你也可以手工设置音频文件。

## 配置文件

文件位置：`config/me.jie65535.mirai-console-jnr-plugin/jnr.yml`

```yaml
# 戳一戳回复的消息
replyMessageList: 
  - message: 'Hello world'
    weight: 1
# 事件优先级 从高到低可选 HIGHEST, HIGH, NORMAL, LOW, LOWEST, MONITOR
# 设置后需要重启插件生效
priority: HIGH
# 是否拦截事件 回复后可阻止其它插件响应戳一戳事件 优先级为MONITOR时拦截无效
isIntercept: true
# 群回复间隔（秒），0表示无限制
groupInterval: 0
# 用户私聊回复间隔（秒），0表示无限制
userInterval: 0
```

## 用例

![Use example image](doc/example.png)
