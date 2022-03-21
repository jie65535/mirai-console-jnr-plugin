package top.jie65535.jnr

/**
 * 回复的消息
 * @param message 消息内容
 * @param weight 回复该消息的权重
 */
@kotlinx.serialization.Serializable
data class ReplyMessage(
    val message: String,
    val weight: Int
)