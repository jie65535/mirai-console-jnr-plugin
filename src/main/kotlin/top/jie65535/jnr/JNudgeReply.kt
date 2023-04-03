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
import java.util.regex.Matcher
import java.util.regex.Pattern
import kotlin.random.Random

object JNudgeReply : KotlinPlugin(
    JvmPluginDescription(
        id = "me.jie65535.mirai-console-jnr-plugin",
        name = "J Nudge Reply",
        version = "1.3.0",
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

    private suspend fun doReply(message: ReplyMessage, event: NudgeEvent) {
        if (message.message.startsWith("#")) {
            when {
                // 戳回去
                message.message.startsWith("#nudge") -> {
                    event.from.nudge().sendTo(event.subject)
                    if (message.message.length > 6) {
                        val messageTemp = message.message.substring(6)
                        sendRecordMessage(event.subject, messageTemp.deserializeMiraiCode())
                    }
                    logger.info("已尝试戳回发送者")
                }

                // 禁言
                message.message.startsWith("#group.mute\\:") -> {
                    val duration = RegexMatches.main(message.message)
                    if (duration == 0) {
                        logger.warning("戳一戳禁言失败：\"${message.message}\" 格式不正确")
                    } else {
                        val member: Member = event.from as Member
                        try {
                            member.mute(duration)
                            val s = duration.toString()
                            if (message.message.length > (13 + s.length)){
                                val messageTemp = String.format(message.message.substring(13 + s.length),s)
                                sendRecordMessage(event.subject, messageTemp.deserializeMiraiCode())
                            }
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

                message.message.startsWith("#Audio:") -> {
                    val audioFile = resolveDataFile("audios/" + message.message.substring(7)).toExternalResource()
                    if (event.subject is Group){
                        val messageTemp = (event.subject as Group).uploadAudio(audioFile)
                        sendRecordMessage(event.subject, messageTemp.toMessageChain())
                    } else {
                        val messageTemp = "暂不支持私聊发送语音"
                        sendRecordMessage(event.subject, messageTemp.deserializeMiraiCode())
                    }
                }

                // 其它
                else -> sendRecordMessage(event.subject, message.message.deserializeMiraiCode())
            }
        } else {
            sendRecordMessage(event.subject, message.message.deserializeMiraiCode())
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

    object RegexMatches {
        @JvmStatic
        fun main(str: String): Int {
            val pattern = "[0-9]+"
            val r: Pattern = Pattern.compile(pattern)
            val m: Matcher = r.matcher(str)
            return if (m.find()){
                m.group(0).toInt()
            }else{
                0
            }
        }
    }

}
