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
import ai.rev.streaming.models.StreamingResponse
import com.google.gson.FieldNamingPolicy
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.net.URI
import java.util.*
import javax.ws.rs.client.Client
import javax.ws.rs.client.Invocation
import javax.ws.rs.core.MediaType


/**
 * @author shuklaalok7 (alok@clay.fish)
 * @since v0.1.0 2020-03-29 06:26 PM IST
 */
internal object NetworkUtils {

    /**
     *
     */
    fun createURI(config: ClientConfig): URI {
        var fullContentType = config.contentType.mime
        if (config.contentType == AudioContentType.RAW) {
            if (config.params == null) throw IllegalArgumentException("params cannot be null with $fullContentType.")
            fullContentType += ";layout=${if (config.params.interleaved) "interleaved" else "non-interleaved"};rate=${config.params.rate};format=${config.params.format};channels=${config.params.channels}"
        }

        var url = "wss://${config.baseUrl}/stream?access_token=${config.accessToken}&content_type=$fullContentType&filter_profanity=${config.filterProfanity}"
        if (config.metadata != null) url += "&metadata=${config.metadata}"
        if (config.customVocabularyId != null) url += "&custom_vocabulary_id=${config.customVocabularyId}"

        logger.info("Full URL: $url")
        return URI(url)
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
     *
     */
    fun createInvocation(client: Client, uri: String, config: ClientConfig, vararg queryParam: Pair<String, String>): Invocation.Builder =
            client.target("https://${config.baseUrl}$uri").apply {
                queryParam.forEach { this.queryParam(it.first, it.second) }
            }.request(MediaType.APPLICATION_JSON_TYPE)
                    .accept(MediaType.APPLICATION_JSON_TYPE)
                    .acceptEncoding("gzip", "deflate", "sdch", "br")
                    .acceptLanguage(Locale.US)
                    .header("Authorization", "Bearer ${config.accessToken}")
                    .header("Accept-Language", "en-GB,en;q=0.8,en-US;q=0.6,hi;q=0.4")
                    .header("Connection", "keep-alive")
                    .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/81.0.4044.113 Safari/537.36")

    /**
     * @param message   Received from rev.ai streaming API over the websocket
     * @return Response that this library generates
     */
    fun convertToStreamingResponse(message: String): StreamingResponse = gson.fromJson(message, StreamingResponse::class.java)
}
