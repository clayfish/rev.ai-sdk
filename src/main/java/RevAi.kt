package ai.rev.streaming

/**
 * Entry-point for the client projects. Handles one connection at a time. To get multiple streams, create more instances
 * of it.
 *
 * @author shuklaalok7 (alok@clay.fish)
 * @since v0.1.0 2020-03-29 18:20 IST
 */
class RevAi(private val accessToken: String, private val contentType: AudioContentType,
            private val params: RawParameters? = null, private val metadata: String? = null,
            private val customVocabularyId: String? = null, private val filterProfanity: Boolean = false) : AutoCloseable {
    //    private var sessionHandlers = ConcurrentHashMap<String, SessionHandler>()
    private var sessionHandler: SessionHandler? = null

    /**
     * Establishes initial connection
     */
    fun connect(callback: (RevAiResponse) -> Unit) {
        sessionHandler = SessionHandler(callback)
        NetworkUtils.handshake(sessionHandler!!, accessToken, contentType, params, metadata, customVocabularyId, filterProfanity)
    }

    /**
     * Stream audio data
     */
    fun stream(audio: ByteArray) {
        if (sessionHandler == null) logger.error("Session is closed, please call RevAi.connect() again.")
        else sessionHandler?.sendAudio(audio)
    }

    /**
     * Closes connection gracefully
     */
    override fun close() {
        // close session/connection
        sessionHandler?.close()
        sessionHandler = null
    }

    companion object {
        private val logger = AppUtils.getLogger<RevAi>()
    }

}
