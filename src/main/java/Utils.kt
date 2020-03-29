package ai.rev.streaming

import com.google.gson.GsonBuilder
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpHeaders
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketHttpHeaders
import org.springframework.web.socket.client.WebSocketClient
import org.springframework.web.socket.client.standard.StandardWebSocketClient
import java.net.URI


/**
 * @author shuklaalok7 (alok@clay.fish)
 * @since v0.1.0 2020-03-29 06:26 PM IST
 */
internal object NetworkUtils {
    private const val BASE_URL = "wss://api.rev.ai/speechtotext/v1/stream"

    fun handshake(sessionHandler: SessionHandler, accessToken: String, contentType: AudioContentType,
                  params: RawParameters?, metadata: String?, customVocabularyId: String? = null,
                  filterProfanity: Boolean = false) {
        val client: WebSocketClient = StandardWebSocketClient()

        val uri = URI(createUrl(accessToken, contentType, params, metadata, customVocabularyId, filterProfanity))
        val headers = HttpHeaders()
        headers["host"] = uri.host
        headers["upgrade"] = "websocket"
        headers["connection"] = "upgrade"

        client.doHandshake(sessionHandler, WebSocketHttpHeaders(headers), uri).addCallback({
            logger.info("Handshake successful. Waiting for the server to get ready.")
        }, {
            // todo Maybe retry
            logger.error("Error in handshake. Please retry connecting to rev.ai again", it)
        })
    }

    /**
     *
     */
    private fun createUrl(accessToken: String, contentType: AudioContentType, params: RawParameters? = null, metadata: String? = null,
                          customVocabularyId: String? = null, filterProfanity: Boolean = false): String {

        var fullContentType = contentType.mime
        if (contentType == AudioContentType.RAW) {
            if (params == null) throw IllegalArgumentException("params cannot be null with $fullContentType.")
            fullContentType += ";layout=${if (params.interleaved) "interleaved" else "non-interleaved"};rate=${params.rate};format=${params.format};channels=${params.channels}"
        }

        var url = "$BASE_URL?access_token=$accessToken&content_type=$fullContentType&filter_profanity=${filterProfanity}"
        if (metadata != null) url += "&metadata=$metadata"
        if (customVocabularyId != null) url += "&custom_vocabulary_id=$customVocabularyId"

        logger.info("Full URL: $url")
        return url
    }

    private val logger = AppUtils.getLogger<NetworkUtils>()
}

/**
 * @author shuklaalok7 (alok@clay.fish)
 * @since v0.1.0 2020-03-29 07:25 PM IST
 */
internal object AppUtils {
    inline fun <reified T> getLogger(): Logger = LoggerFactory.getLogger(T::class.java)

    // todo see what options can be tweaked
    val gson = GsonBuilder().create()

    fun convertToRevAiResponse(message: TextMessage): RevAiResponse {
        // todo test it thoroughly
        return gson.fromJson(message.payload, RevAiResponse::class.java)
    }
}
