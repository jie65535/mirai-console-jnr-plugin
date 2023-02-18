package top.jie65535.jnr

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.request.*
import java.io.File

object HttpUtil {
    private val httpClient = HttpClient(OkHttp)

    suspend fun download(url: String, file: File): ByteArray {
        val data = httpClient.get(url).body<ByteArray>()
        file.writeBytes(data)
        return data
    }
}