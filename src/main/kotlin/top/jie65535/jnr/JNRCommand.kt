package top.jie65535.jnr

import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withTimeout
import net.mamoe.mirai.console.command.CommandSender
import net.mamoe.mirai.console.command.CompositeCommand
import net.mamoe.mirai.console.command.fold
import net.mamoe.mirai.console.command.isUser
import net.mamoe.mirai.event.EventPriority
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.event.globalEventChannel
import net.mamoe.mirai.event.nextEvent
import net.mamoe.mirai.message.code.MiraiCode.deserializeMiraiCode
import net.mamoe.mirai.message.data.PlainText
import net.mamoe.mirai.message.data.buildForwardMessage
import net.mamoe.mirai.message.data.isContentBlank
import top.jie65535.jnr.JNudgeReply.reload

object JNRCommand : CompositeCommand(
    JNudgeReply, "jnr",
    description = "戳一戳自动回复"
) {
    private const val WAIT_REPLY_TIMEOUT_MS = 30000L

    @SubCommand
    @Description("添加简单回复消息（权重默认为1）")
    suspend fun CommandSender.add(message: PlainText, weight: Int = 1) {
        if (weight < 1) {
            sendMessage("请设置正确的权重值")
            return
        }
        if (message.isContentBlank()) {
            sendMessage("消息内容不能为空")
            return
        }
        JNRPluginConfig.replyMessageList.add(ReplyMessage(message.serializeToMiraiCode(), weight))
        sendMessage("已添加一条消息，权重为$weight")
    }

    @SubCommand
    @Description("添加回复消息（权重默认为1）")
    suspend fun CommandSender.add(weight: Int = 1) {
        if (weight < 1) {
            sendMessage("请设置正确的权重值")
            return
        }
        if (isUser()) {
            try {
                sendMessage("请在${WAIT_REPLY_TIMEOUT_MS / 1000}秒内发送要添加的消息内容，你可以发送空白消息来取消。")
                val msg = withTimeout(WAIT_REPLY_TIMEOUT_MS) {
                    subject.globalEventChannel().nextEvent<MessageEvent>(EventPriority.MONITOR) { it.sender == user }
                }
                if (msg.message.isContentBlank()) {
                    sendMessage("已取消")
                } else {
                    JNRPluginConfig.replyMessageList.add(ReplyMessage(msg.message.serializeToMiraiCode(), weight))
                    sendMessage("已添加一条消息，权重为$weight")
                }
            } catch (e: TimeoutCancellationException) {
                sendMessage("已取消")
            }
        } else {
            sendMessage("必须使用聊天环境执行该命令")
        }
    }

    @SubCommand
    @Description("删除指定索引的回复消息")
    suspend fun CommandSender.remove(index: Int) {
        if (index < 0 || index >= JNRPluginConfig.replyMessageList.size) {
            sendMessage("目标索引超出范围")
        } else {
            JNRPluginConfig.replyMessageList.removeAt(index)
            sendMessage("OK")
        }
    }

    @SubCommand
    @Description("清空回复消息列表")
    suspend fun CommandSender.clear() {
        JNRPluginConfig.replyMessageList.clear()
        sendMessage("OK")
    }

    @SubCommand
    @Description("列出当前回复消息列表")
    suspend fun CommandSender.list() {
        val list = JNRPluginConfig.replyMessageList
        if (list.isEmpty()) {
            sendMessage("当前列表为空")
        } else {
            this.fold({
                val sb = StringBuilder()
                for (i in list.indices) {
                    sb.appendLine(" - [$i] (${list[i].weight}) \"${list[i].message}\"")
                }
                sendMessage(sb.toString())
            }, {
                if (list.size > 1) {
                    sendMessage(buildForwardMessage(subject) {
                        for (i in list.indices) {
                            bot named "[$i] (${list[i].weight})" says list[i].message.deserializeMiraiCode()
                        }
                    })
                } else {
                    sendMessage(list[0].message.deserializeMiraiCode())
                }
            })
        }
    }

    @SubCommand
    @Description("重载配置")
    suspend fun CommandSender.reload() {
        JNRPluginConfig.reload()
        sendMessage("OK")
    }
}