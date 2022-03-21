package top.jie65535.jnr

import net.mamoe.mirai.console.command.CommandManager.INSTANCE.register
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin
import net.mamoe.mirai.event.EventPriority
import net.mamoe.mirai.event.events.NudgeEvent
import net.mamoe.mirai.event.globalEventChannel
import net.mamoe.mirai.message.code.MiraiCode.deserializeMiraiCode
import net.mamoe.mirai.utils.info
import kotlin.random.Random

object JNudgeReply : KotlinPlugin(
    JvmPluginDescription(
        id = "me.jie65535.mirai-console-jnr-plugin",
        name = "J Nudge Reply",
        version = "1.0.0",
    ) {
        author("jie65535")
        info("""自定义戳一戳回复插件""")
    }
) {
    override fun onEnable() {
        JNRPluginConfig.reload()
        JNRCommand.register()
        Random.nextInt()
        globalEventChannel().subscribeAlways<NudgeEvent>(priority = JNRPluginConfig.priority) {
            if (target.id == bot.id && JNRPluginConfig.replyMessageList.isNotEmpty()) {
                val totalWeight = JNRPluginConfig.replyMessageList.sumOf { it.weight }
                var w = Random.nextInt(totalWeight)
                for (msg in JNRPluginConfig.replyMessageList) {
                    if (w < msg.weight) {
                        subject.sendMessage(msg.message.deserializeMiraiCode())
                        break
                    } else {
                        w -= msg.weight
                    }
                }
                if (JNRPluginConfig.priority != EventPriority.MONITOR && JNRPluginConfig.isIntercept)
                    intercept()
            }
        }

        logger.info { "Plugin loaded" }
    }
}
