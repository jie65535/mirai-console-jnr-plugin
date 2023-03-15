package top.jie65535.jnr

import net.mamoe.mirai.console.data.AutoSavePluginConfig
import net.mamoe.mirai.console.data.ValueDescription
import net.mamoe.mirai.console.data.value
import net.mamoe.mirai.event.EventPriority
import net.mamoe.mirai.message.data.Message

/**
 * 插件配置
 */
object JNRPluginConfig : AutoSavePluginConfig("jnr") {
    /**
     * 回复的消息
     * @see Message
     */
    @ValueDescription("戳一戳回复的消息")
    var replyMessageList: MutableList<ReplyMessage> by value()

    /**
     * 优先级 默认为高
     * @see EventPriority
     */
    @ValueDescription(
        "事件优先级 从高到低可选 HIGHEST, HIGH, NORMAL, LOW, LOWEST, MONITOR\n" +
                "设置后需要重启插件生效"
    )
    var priority: EventPriority by value(EventPriority.HIGH)

    /**
     * 是否拦截事件 为true时优先级较低的
     * @see EventPriority
     */
    @ValueDescription("是否拦截事件 回复后可阻止其它插件响应戳一戳事件 优先级为MONITOR时拦截无效")
    var isIntercept: Boolean by value(true)

    /**
     * 群间隔时间（单位秒）
     * 0 表示无限制
     */
    @ValueDescription("群回复间隔（秒），0表示无限制")
    var groupInterval: Long by value(0L)

    /**
     * 群共享冷却时间
     * 本设定优先级低于群间隔时间
     * 如需使用请将群间隔时间设置为0
     * 请不要一起打开
     * */
    @ValueDescription("群共享冷却时间上界（分钟），0表示无限制")
    var groupCoolDownTimeUpperBound: Long by value(0L)
    @ValueDescription("群共享冷却时间下界（分钟）")
    var groupCoolDownTimeLowerBound: Long by value(0L)
    @ValueDescription("冷却触发发送语句,%s为占位符，可不加，用来在消息中显示冷却时长")
    var replyMessageForRest: String by value("呜呜，被戳傻了。休息%s分钟")
    @ValueDescription("群共享冷却默认触发最低次数")
    var groupCoolDownTriggerCountMin: Long by value(6L)
    @ValueDescription("群共享冷却默认触发最高次数，到此次数必定触发")
    var groupCoolDownTriggerCountMax: Long by value(12L)
    @ValueDescription("达到最低次数后的触发概率,1~100，按百分比触发")
    var groupCoolDownTriggerProbability: Int by value(50)
    /**
     * 用户间隔（单位秒）
     * 0 表示无限制
     */
    @ValueDescription("用户私聊回复间隔（秒），0表示无限制")
    var userInterval: Long by value(0L)

    /**
     * 是否在间隔期间依然拦截事件，与 [isIntercept] 有关
     */
    var interceptAtInterval: Boolean by value(true)
}
