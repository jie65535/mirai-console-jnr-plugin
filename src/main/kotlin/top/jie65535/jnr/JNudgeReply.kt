package top.jie65535.jnr

import net.mamoe.mirai.console.command.CommandManager.INSTANCE.register
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin
import net.mamoe.mirai.contact.*
import net.mamoe.mirai.event.EventPriority
import net.mamoe.mirai.event.events.NudgeEvent
import net.mamoe.mirai.event.globalEventChannel
import net.mamoe.mirai.message.code.MiraiCode.deserializeMiraiCode
import net.mamoe.mirai.message.data.*
import net.mamoe.mirai.message.data.Image.Key.isUploaded
import net.mamoe.mirai.utils.ExternalResource.Companion.toExternalResource
import net.mamoe.mirai.utils.ExternalResource.Companion.uploadAsImage
import net.mamoe.mirai.utils.info
import java.time.LocalDateTime
import kotlin.random.Random
import kotlin.time.DurationUnit
import kotlin.time.toDuration

object JNudgeReply : KotlinPlugin(
    JvmPluginDescription(
        id = "me.jie65535.mirai-console-jnr-plugin",
        name = "J Nudge Reply",
        version = "1.4.0",
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
                        replyList = replyList.filter { !it.message.startsWith("#group.mute\\:") }
                    }
                }

                // 判断间隔
                val isIgnored = if (isReply) {
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
                    false
                } else {
                    logger.info("正在CD中，本次已忽略")
                    true
                }

                // 拦截事件
                if (JNRPluginConfig.priority != EventPriority.MONITOR && JNRPluginConfig.isIntercept
                ) {
                    // 在被忽略的情况下判断是否拦截
                    if (!isIgnored || JNRPluginConfig.interceptAtInterval)
                        intercept()
                }
            }
        }

        logger.info { "Plugin loaded. https://github.com/jie65535/mirai-console-jnr-plugin" }
    }

    private suspend fun doReply(reply: ReplyMessage, event: NudgeEvent) {
        val replyMessageChain = reply.message.deserializeMiraiCode()
        val replyMessage = replyMessageChain.content
        if (replyMessage.startsWith("#")) {
            when {
                // 戳回去
                replyMessage.startsWith("#nudge") -> {
                    event.from.nudge().sendTo(event.subject)
                    val replyMsg = replyMessage.substring("#nudge".length).removePrefix(":")
                    if (replyMsg.isNotBlank()) {
                        sendRecordMessage(event.subject, messageChainOf(PlainText(replyMsg.trim())))
                        logger.info("已尝试戳回发送者并回复消息")
                    } else {
                        logger.info("已尝试戳回发送者")
                    }
                }

                // 禁言
                replyMessage.startsWith("#group.mute:") -> {
                    val args = replyMessage.substring("#group.mute:".length).split(':')
                    val durationS = if (args.isNotEmpty()) args[0].toIntOrNull() else 0
                    if (durationS == null || durationS < 1) {
                        logger.warning("戳一戳禁言失败：\"${replyMessage}\" 格式不正确")
                    } else {
                        val member: Member = event.from as Member
                        try {
                            member.mute(durationS)
                            val duration = durationS.toDuration(DurationUnit.SECONDS)
                            if (args.size > 1 && args[1].isNotBlank()) {
                                val replyMsg = args[1].trim()
//                                    .replace("{duration}", duration.toString())
//              如果禁言时间是在消息中设置的，那么用户也可以同时设置回复的内容里带时间，因此无需添加格式化，除非支持随机禁言时间，可以再考虑
                                sendRecordMessage(event.subject, messageChainOf(PlainText(replyMsg)))
                            }
                            logger.info("戳一戳禁言目标 ${member.nameCardOrNick}(${member.id}) $duration")
                        } catch (e: Throwable) {
                            logger.warning("戳一戳禁言失败", e)
                        }
                    }
                }

                // 忽略
                replyMessage == "#ignore" -> {
                    logger.info("已忽略本次戳一戳回复")
                }

                // 音频回复
                replyMessage.startsWith("#audio:") -> {
                    val filename = replyMessage.substring("#audio:".length)
                    val audioFile = resolveDataFile("audios/$filename").toExternalResource()
                    if (event.subject is AudioSupported) {
                        logger.info("上传并回复语音 $filename")
                        val messageTemp = (event.subject as AudioSupported).uploadAudio(audioFile)
                        sendRecordMessage(event.subject, messageTemp.toMessageChain())
                    } else {
                        logger.warning("当前上下文不支持回复语音")
                        sendRecordMessage(event.subject, messageChainOf(PlainText("[语音消息] 当前上下文不支持")))
                    }
                }

                // 其它
                else -> sendRecordMessage(event.subject, replyMessageChain)
            }
        } else {
            sendRecordMessage(event.subject, replyMessageChain)
        }
    }

    private suspend fun sendRecordMessage(subject: Contact, message: MessageChain) {
        for (it in message) {
            if (it is Image) {
                if (!it.isUploaded(subject.bot)) {
                    val imgFile = resolveDataFile("images/" + it.imageId)
                    if (imgFile.exists()) {
                        imgFile.uploadAsImage(subject)
                    } else {
                        logger.warning(
                            "图片的服务器缓存已失效，本地缓存已丢失，请重新设置该消息内的图片！" +
                                    "消息内容：" + message.serializeToMiraiCode()
                        )
                    }
                }
            }
        }
        subject.sendMessage(message)
    }
}
