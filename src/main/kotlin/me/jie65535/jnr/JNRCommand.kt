package me.jie65535.jnr

import kotlinx.coroutines.TimeoutCancellationException
import net.mamoe.mirai.console.command.CommandSender
import net.mamoe.mirai.console.command.RawCommand
import net.mamoe.mirai.console.command.isUser
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.event.nextEventAsync
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.message.data.isContentBlank
import net.mamoe.mirai.utils.MiraiExperimentalApi

object JNRCommand : RawCommand(
    JNudgeReply, "jnr", "setPokeReply", "setNudgeReply",
    usage = "/jnr|setPokeReply|setNudgeReply <message>  # 设置戳一戳回复消息",
    description = "设置戳一戳回复消息"
) {
    private const val WAIT_REPLY_TIMEOUT_MS = 30000L

    @OptIn(MiraiExperimentalApi::class)
    override suspend fun CommandSender.onCommand(args: MessageChain) {
        if (args.isContentBlank()) {
            if (this.isUser()) {
                try {
                    sendMessage("请在${WAIT_REPLY_TIMEOUT_MS / 1000}秒内发送要回复的消息内容，你可以发送空白消息来清空预设回复。")
                    val msg = subject.nextEventAsync<MessageEvent>(
                        WAIT_REPLY_TIMEOUT_MS,
                        coroutineContext = this.coroutineContext
                    ) { it.sender == user } .await()
                    if (msg.message.isContentBlank()) {
                        setReplyMessage(null)
                    } else {
                        setReplyMessage(msg.message)
                    }
                    sendMessage("OK")
                } catch (e: TimeoutCancellationException) {
                    sendMessage("已取消")
                }
            } else {
                setReplyMessage(null)
                sendMessage("已清空预设回复")
            }
        } else {
            setReplyMessage(args)
            sendMessage("OK")
        }
    }

    private fun setReplyMessage(message: MessageChain?) {
        JNRPluginConfig.replyMessage = message?.serializeToMiraiCode() ?: ""
        JNudgeReply.logger.info("已设置戳一戳回复内容 \"${JNRPluginConfig.replyMessage}\"")
    }
}