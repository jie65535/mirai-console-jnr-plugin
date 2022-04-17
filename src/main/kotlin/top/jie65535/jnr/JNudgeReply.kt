package top.jie65535.jnr

import net.mamoe.mirai.console.command.CommandManager.INSTANCE.register
import net.mamoe.mirai.console.command.descriptor.ExperimentalCommandDescriptors
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin
import net.mamoe.mirai.console.util.ConsoleExperimentalApi
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.contact.Member
import net.mamoe.mirai.contact.PermissionDeniedException
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
            if (target.id == bot.id && target.id != from.id && JNRPluginConfig.replyMessageList.isNotEmpty()) {
                var replyList = JNRPluginConfig.replyMessageList
                if(subject !is Group){
                    replyList = replyList.filter { !it.message.startsWith("#group") }.toMutableList()
                }else{
                    if((from as Member).permission.level >= (subject as Group).botPermission.level){
                        replyList = replyList.filter { !it.message.startsWith("#group.mute") }.toMutableList()
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

    @OptIn(ExperimentalCommandDescriptors::class, ConsoleExperimentalApi::class)
    suspend fun doReply(message: ReplyMessage, event: NudgeEvent) {
        val mutePattern = Regex("(?<=#group.mute(\\\\)?:)\\d+")
        if(message.message.startsWith("#")) {
            when{
                message.message == "#nudge" -> {
                    event.from.nudge().sendTo(event.subject)
                }
                mutePattern.find(message.message) != null -> {
                    val duration = mutePattern.find(message.message)?.value?.toLong()!!
                    val member: Member = event.from as Member
                    try {
                        member.mute(duration.toInt())
                    }catch (e: PermissionDeniedException){
                        logger.warning("权限不足，无法进行禁言")
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
