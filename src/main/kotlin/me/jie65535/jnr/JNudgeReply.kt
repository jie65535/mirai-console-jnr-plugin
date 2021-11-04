package me.jie65535.jnr

import net.mamoe.mirai.console.command.CommandManager.INSTANCE.register
import net.mamoe.mirai.console.command.CommandManager.INSTANCE.unregister
import net.mamoe.mirai.console.data.AutoSavePluginConfig
import net.mamoe.mirai.console.data.value
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin
import net.mamoe.mirai.event.EventPriority
import net.mamoe.mirai.event.events.NudgeEvent
import net.mamoe.mirai.event.globalEventChannel
import net.mamoe.mirai.message.code.MiraiCode.deserializeMiraiCode
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.message.data.isContentBlank
import net.mamoe.mirai.utils.info

object JNudgeReply : KotlinPlugin(
    JvmPluginDescription(
        id = "me.jie65535.mirai-console-jnr-plugin",
        name = "J Nudge Reply",
        version = "0.1.1",
    ) {
        author("jie65535")
        info("""自定义戳一戳回复插件""")
    }
) {
    override fun onEnable() {
        JNRPluginConfig.reload()
        JNRCommand.register()

        globalEventChannel().subscribeAlways<NudgeEvent>(priority = JNRPluginConfig.priority) {
            if (target.id == bot.id && JNRPluginConfig.replyMessage.isNotBlank()) {
                subject.sendMessage(JNRPluginConfig.replyMessage.deserializeMiraiCode())
                if (JNRPluginConfig.priority != EventPriority.MONITOR && JNRPluginConfig.isIntercept)
                    intercept()
            }
        }

        logger.info { "Plugin loaded" }
    }
}
