# mirai-console-jnr-plugin

MiraiConsolePlugin 自定义戳一戳回复消息

## 指令列表

```bash
/jnr add [weight]    # 添加回复消息（权重默认为1）
/jnr add <message> [weight]    # 添加简单回复消息（权重默认为1）
/jnr clear    # 清空回复消息列表
/jnr list    # 列出当前回复消息列表
/jnr remove <index>    # 删除指定索引的回复消息
```

## 特殊消息

设置回复消息为以下内容，代表特殊含义

- `#nudge` 戳回去
- `#group.mute:30` 禁言30s, 可以自定义禁言时间, 单位秒

## 用例

![Use example image](doc/example.png)
