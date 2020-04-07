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

/**
 * @author shuklaalok (alok@clay.fish)
 * @since v0.1.1 2020-04-05 9:52 AM IST
 */
data class ClientConfig(
        /**
         * Obtained from rev.ai
         */
        val accessToken: String,
        val contentType: AudioContentType,
        /**
         * Handle the response obtained from rev.ai
         */
        val callback: (RevAiResponse) -> Unit,

        /**
         * Size of the buffer in bytes to read from the input-stream passed to [RevAi.stream].
         */
        val bufferSize: Int = 391680,

        /**
         * Needed only when [contentType] is [AudioContentType.RAW].
         */
        val params: RawParameters? = null,

        /**
         * Needed only when [contentType] is [AudioContentType.RAW].
         */
        val metadata: String? = null,

        /**
         * Any pre-existing custom-vocabulary to be used in transcription.
         */
        val customVocabularyId: String? = null,

        /**
         * Default is `false`. Profane words will use asterisks.
         */
        val filterProfanity: Boolean = false
)

/**
 * See, [rev.ai Docs](https://www.rev.ai/docs/streaming#section/WebSocket-Endpoint/Content-Type)
 * @author shuklaalok7 (alok@clay.fish)
 * @since v0.1.0 2020-03-29 06:51 PM IST
 */
data class RawParameters(
        val interleaved: Boolean,

        /**
         * 8000-48000 Hz, inclusive range
         */
        val rate: Int,

        /**
         * See [allowed formats](https://gstreamer.freedesktop.org/documentation/additional/design/mediatype-audio-raw.html?gi-language=c#formats)
         */
        val format: String,

        /**
         * 1-10, inclusive range
         */
        val channels: Int
)

/**
 * @author shuklaalok7 (alok@clay.fish)
 * @since v0.1.0 2020-03-29 07:54 PM IST
 */
data class RevAiResponse(
        val id: String?,
        val type: String?,
        val elements: List<Element>?,
        val ts: Double?,
        val endTs: Double?
) {
    companion object {
        /**
         * @author shuklaalok7 (alok@clay.fish)
         * @since v0.1.0 2020-03-29 09:06 PM IST
         */
        data class Element(
                val type: String?,
                val value: String,
                val ts: Double?,
                val endTs: Double?,
                val confidence: Double?
        )
    }
}
