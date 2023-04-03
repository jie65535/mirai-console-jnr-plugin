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
import net.mamoe.mirai.message.data.*
import net.mamoe.mirai.message.data.Image.Key.queryUrl
import top.jie65535.jnr.JNudgeReply.reload
import kotlin.math.min

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
                val nextEvent = withTimeout(WAIT_REPLY_TIMEOUT_MS) {
                    subject.globalEventChannel().nextEvent<MessageEvent>(EventPriority.MONITOR) { it.sender == user }
                }
                if (nextEvent.message.isContentBlank()) {
                    sendMessage("已取消")
                } else {
                    if (nextEvent.message.contains(OnlineAudio.Key)) {
                        saveResources(nextEvent.message)
                        for (it in nextEvent.message){
                            if (it is OnlineAudio){
                                JNRPluginConfig.replyMessageList.add(ReplyMessage("#Audio:${it.filename}", weight))
                            }
                        }
                    } else {
                        saveResources(nextEvent.message)
                        JNRPluginConfig.replyMessageList.add(ReplyMessage(nextEvent.message.serializeToMiraiCode(), weight))
                    }
                    sendMessage("已添加一条消息，权重为$weight")
                }
            } catch (e: TimeoutCancellationException) {
                sendMessage("已取消")
            }
        } else {
            sendMessage("必须使用聊天环境执行该命令")
        }
    }

    /**
     * 保存消息中的图片和音频
     */
    private suspend fun saveResources(message: MessageChain) {
        for (it in message) {
            if (it is Image) {
                val imgDir = JNudgeReply.resolveDataFile("images")
                if (!imgDir.exists()) {
                    imgDir.mkdir()
                }
                val imgFile = imgDir.resolve(it.imageId)
                if (!imgFile.exists()) {
                    JNudgeReply.logger.info("下载图片 ${it.imageId}")
                    HttpUtil.download(it.queryUrl(), imgFile)
                }
            } else if (it is OnlineAudio) {
                val audioDir = JNudgeReply.resolveDataFile("audios")
                if (!audioDir.exists()) {
                    audioDir.mkdir()
                }
                val audioFile = audioDir.resolve(it.filename)
                if (!audioFile.exists()) {
                    JNudgeReply.logger.info("下载语音 ${it.filename}")
                    HttpUtil.download(it.urlForDownload, audioFile)
                }
            }
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
    suspend fun CommandSender.list(page: Int = 0, pageSize: Int = 50) {
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
                    val begin = page * pageSize
                    val end = min(list.size, (page + 1) * pageSize)
                    if (begin < 0 || end <= begin) {
                        sendMessage("翻页参数错误")
                    } else {
                        sendMessage(buildForwardMessage(subject) {
                            for (i in begin until end) {
                                bot named "[$i] (${list[i].weight})" says list[i].message.deserializeMiraiCode()
                            }
                            if (end < list.size) {
                                bot says "当前显示 $begin~$end 共 ${list.size}"
                            }
                        })
                    }
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