/*
   Copyright 2020 ClayFish Technologies

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */

package ai.rev.streaming

import ai.rev.streaming.models.AudioContentType
import ai.rev.streaming.models.ClientConfig
import ai.rev.streaming.models.RevAiResponse
import com.google.gson.FieldNamingPolicy
import com.google.gson.Gson
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

    /**
     * @param sessionHandler
     * @param config
     */
    fun handshake(sessionHandler: SessionHandler, config: ClientConfig) {
        val client: WebSocketClient = StandardWebSocketClient()

        val uri = URI(createUrl(config))
        val headers = HttpHeaders()
        headers["host"] = uri.host
        headers["upgrade"] = "websocket"
        headers["connection"] = "upgrade"

        client.doHandshake(sessionHandler, WebSocketHttpHeaders(headers), uri).addCallback({
            logger.info("Handshake successful. Waiting for the server to get ready.")
            sessionHandler.initialSession = it
        }, {
            // todo Maybe retry
            logger.error("Error in handshake. Please retry connecting to rev.ai again", it)
        })
    }

    /**
     *
     */
    private fun createUrl(config: ClientConfig): String {

        var fullContentType = config.contentType.mime
        if (config.contentType == AudioContentType.RAW) {
            if (config.params == null) throw IllegalArgumentException("params cannot be null with $fullContentType.")
            fullContentType += ";layout=${if (config.params.interleaved) "interleaved" else "non-interleaved"};rate=${config.params.rate};format=${config.params.format};channels=${config.params.channels}"
        }

        var url = "wss://${config.baseUrl}/stream?access_token=${config.accessToken}&content_type=$fullContentType&filter_profanity=${config.filterProfanity}"
        if (config.metadata != null) url += "&metadata=${config.metadata}"
        if (config.customVocabularyId != null) url += "&custom_vocabulary_id=${config.customVocabularyId}"

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

    val gson: Gson = GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create()

    /**
     * @param message   [TextMessage] received from rev.ai streaming API over the websocket
     * @return Response that this library generates
     */
    fun convertToRevAiResponse(message: TextMessage): RevAiResponse = gson.fromJson(message.payload, RevAiResponse::class.java)
}
