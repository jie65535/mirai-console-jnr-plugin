package me.jie65535.jnr

import net.mamoe.mirai.console.command.CommandSender
import net.mamoe.mirai.console.command.RawCommand
import net.mamoe.mirai.message.data.MessageChain

object JNRCommand : RawCommand(
    JNudgeReply, "jnr", "setPokeReply", "setNudgeReply",
    usage = "/jnr|setPokeReply|setNudgeReply <message>  # 设置戳一戳回复消息",
    description = "设置戳一戳回复消息"
) {
    override suspend fun CommandSender.onCommand(args: MessageChain) {
        JNRPluginConfig.replyMessage = args.serializeToMiraiCode()
        sendMessage("OK")
    }
}