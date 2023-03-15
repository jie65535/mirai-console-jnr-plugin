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
- `#group.mute:30` 禁言30s, 可以自定义禁言时间, 单位秒
- `#ignore` 忽略

Tips：可以在#nudge之后直接添加消息以在回戳时同时回复消息
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
# 群共享冷却时间上界（分钟），0表示无限制
# 请不要和群回复间隔一起打开，避免出现问题
groupCoolDownTimeUpperBound: 0
# 群共享冷却时间下界（分钟）
# 以下界<=x<=上界为范围产生随机数随机休息时间
groupCoolDownTimeLowerBound: 0
# 冷却触发发送语句
# %s为占位符，可不加，用来在消息中显示冷却时长
replyMessageForRest: 呜呜，被戳傻了。休息%s分钟
# 群共享冷却默认触发最低次数
groupCoolDownTriggerCountMin: 6
# 群共享冷却默认触发最高次数，到此次数必定触发
groupCoolDownTriggerCountMax: 12
# 达到最低次数后的触发概率,1~100，按百分比触发
groupCoolDownTriggerProbability: 50
# 用户私聊回复间隔（秒），0表示无限制
userInterval: 0
```

## 用例

![Use example image](doc/example.png)
