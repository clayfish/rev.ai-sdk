package ai.rev.streaming

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
        val end_ts: Double?
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
                val end_ts: Double?,
                val confidence: Double?
        )
    }
}
