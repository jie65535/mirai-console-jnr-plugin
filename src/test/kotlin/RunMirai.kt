import net.mamoe.mirai.console.MiraiConsole
import net.mamoe.mirai.console.plugin.PluginManager.INSTANCE.enable
import net.mamoe.mirai.console.plugin.PluginManager.INSTANCE.load
import net.mamoe.mirai.console.terminal.MiraiConsoleTerminalLoader
import net.mamoe.mirai.console.util.ConsoleExperimentalApi
import top.jie65535.jnr.JNudgeReply

@OptIn(ConsoleExperimentalApi::class)
suspend fun main(){
    MiraiConsoleTerminalLoader.startAsDaemon()
    JNudgeReply.load()
    JNudgeReply.enable()
    MiraiConsole.job.join()
}
