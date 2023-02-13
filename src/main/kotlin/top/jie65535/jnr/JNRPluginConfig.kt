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