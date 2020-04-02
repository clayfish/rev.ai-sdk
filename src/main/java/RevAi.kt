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
     *
     * @param callback Handle the response obtained from rev.ai
     */
    fun connect(callback: (RevAiResponse) -> Unit) {
        sessionHandler = SessionHandler(callback)
        NetworkUtils.handshake(sessionHandler!!, accessToken, contentType, params, metadata, customVocabularyId, filterProfanity)
    }

    /**
     * @param audio The audio data to send to rev.ai
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
