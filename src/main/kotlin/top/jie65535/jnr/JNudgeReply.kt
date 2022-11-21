package top.jie65535.jnr

import net.mamoe.mirai.console.command.CommandManager.INSTANCE.register
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.contact.Member
import net.mamoe.mirai.contact.nameCardOrNick
import net.mamoe.mirai.event.EventPriority
import net.mamoe.mirai.event.events.NudgeEvent
import net.mamoe.mirai.event.globalEventChannel
import net.mamoe.mirai.message.code.MiraiCode.deserializeMiraiCode
import net.mamoe.mirai.utils.info
import java.time.LocalDateTime
import kotlin.random.Random

object JNudgeReply : KotlinPlugin(
    JvmPluginDescription(
        id = "me.jie65535.mirai-console-jnr-plugin",
        name = "J Nudge Reply",
        version = "1.2.0",
    ) {
        author("jie65535")
        info("""自定义戳一戳回复插件""")
    }
) {
    private val groupLastReply = mutableMapOf<Long, LocalDateTime>()
    private val userLastReply = mutableMapOf<Long, LocalDateTime>()

    override fun onEnable() {
        JNRPluginConfig.reload()
        JNRCommand.register()

        globalEventChannel().subscribeAlways<NudgeEvent>(priority = JNRPluginConfig.priority) {
            if (target.id == bot.id && target.id != from.id && JNRPluginConfig.replyMessageList.isNotEmpty()) {
                var replyList: List<ReplyMessage> = JNRPluginConfig.replyMessageList
                val now = LocalDateTime.now()
                var isReply = true
                if (subject !is Group) {
                    if (JNRPluginConfig.userInterval > 0) {
                        val t = userLastReply[subject.id]
                        if (t == null || t.plusSeconds(JNRPluginConfig.userInterval) < now) {
                            userLastReply[subject.id] = now
                        } else {
                            isReply = false
                        }
                    }
                    replyList = replyList.filter { !it.message.startsWith("#group") }
                } else {
                    if (JNRPluginConfig.groupInterval > 0) {
                        val t = groupLastReply[subject.id]
                        if (t == null || t.plusSeconds(JNRPluginConfig.groupInterval) < now) {
                            groupLastReply[subject.id] = now
                        } else {
                            isReply = false
                        }
                    }
                    if ((from as Member).permission.level >= (subject as Group).botPermission.level) {
                        replyList = replyList.filter { !it.message.startsWith("#group.mute:") }
                    }
                }

                // 判断间隔
                if (isReply) {
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
                } else {
                    logger.info("正在CD中，本次已忽略")
                }

                // 拦截事件
                if (JNRPluginConfig.priority != EventPriority.MONITOR && JNRPluginConfig.isIntercept) {
                    intercept()
                }
            }
        }

        logger.info { "Plugin loaded" }
    }

    private suspend fun doReply(message: ReplyMessage, event: NudgeEvent) {
        if (message.message.startsWith("#")) {
            when {
                // 戳回去
                message.message == "#nudge" -> {
                    event.from.nudge().sendTo(event.subject)
                    logger.info("已尝试戳回发送者")
                }

                // 禁言
                message.message.startsWith("#group.mute:") -> {
                    val duration = message.message.substringAfter(':').toIntOrNull()
                    if (duration == null) {
                        logger.warning("戳一戳禁言失败：\"${message.message}\" 格式不正确")
                    } else {
                        val member: Member = event.from as Member
                        try {
                            member.mute(duration)
                            logger.info("戳一戳禁言目标 ${member.nameCardOrNick}(${member.id}) $duration 秒")
                        } catch (e: Throwable) {
                            logger.warning("戳一戳禁言失败", e)
                        }
                    }
                }

                // 忽略
                message.message == "#ignore" -> {
                    logger.info("已忽略本次戳一戳回复")
                }

                // 其它
                else -> event.subject.sendMessage(message.message.deserializeMiraiCode())
            }
        } else {
            event.subject.sendMessage(message.message.deserializeMiraiCode())
        }
    }
}
