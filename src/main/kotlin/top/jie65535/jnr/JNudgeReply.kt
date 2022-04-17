package top.jie65535.jnr

import net.mamoe.mirai.console.command.CommandManager.INSTANCE.register
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.contact.Member
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
        version = "1.1.0",
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
            if (target.id == bot.id && target.id != from.id && JNRPluginConfig.replyMessageList.isNotEmpty()) {
                var replyList: List<ReplyMessage> = JNRPluginConfig.replyMessageList
                if(subject !is Group){
                    replyList = replyList.filter { !it.message.startsWith("#group") }
                }else{
                    if((from as Member).permission.level >= (subject as Group).botPermission.level){
                        replyList = replyList.filter { !it.message.startsWith("#group.mute:") }
                    }
                }
                val totalWeight = replyList.sumOf { it.weight }
                var w = Random.nextInt(totalWeight)
                for (msg in replyList) {
                    if (w < msg.weight) {
                        doReply(msg, this)
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

    private suspend fun doReply(message: ReplyMessage, event: NudgeEvent) {
        if (message.message.startsWith("#")) {
            when {
                message.message == "#nudge" -> {
                    event.from.nudge().sendTo(event.subject)
                }
                message.message.startsWith("#group.mute:") -> {
                    val duration = message.message.substringAfter(':').toIntOrNull()
                    if (duration == null) {
                        logger.warning("戳一戳禁言失败：\"${message.message}\" 格式不正确")
                    } else {
                        val member: Member = event.from as Member
                        try {
                            member.mute(duration)
                        } catch (e: Throwable) {
                            logger.warning("戳一戳禁言失败", e)
                        }
                    }
                }
                else -> {
                    event.subject.sendMessage(message.message.deserializeMiraiCode())
                }
            }
        } else {
            event.subject.sendMessage(message.message.deserializeMiraiCode())
        }
    }
}
