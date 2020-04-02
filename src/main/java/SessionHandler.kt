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

import org.springframework.web.socket.BinaryMessage
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.handler.TextWebSocketHandler
import java.io.IOException

/**
 * @author shuklaalok7 (alok@clay.fish)
 * @since v0.1.0 2020-03-29 07:18 PM IST
 */
internal class SessionHandler(private val callback: (RevAiResponse) -> Unit) : TextWebSocketHandler(), AutoCloseable {
    /**
     * Once the handshake done, rev.ai sends a text-message saying "connected", only then it's ready to take the streams
     * from client. This boolean field is to keep track of that "connected" message.
     */
    private var connected = false

    // Experimental
    var initialSession: WebSocketSession? = null
    private var session: WebSocketSession? = null

    override fun afterConnectionEstablished(session: WebSocketSession) {
        logger.info("Connection established")
        this.session = session
    }

    override fun supportsPartialMessages() = true

    override fun handleTransportError(session: WebSocketSession, exception: Throwable) {
        logger.error("Error occurred in socket transport", exception)
        this.session = session
    }

    override fun handleBinaryMessage(session: WebSocketSession, message: BinaryMessage) {
        logger.info("Binary message received")
        this.session = session
    }

    override fun handleTextMessage(session: WebSocketSession, message: TextMessage) {
        logger.info("Text message received\n$message")
        this.session = session
        val data = AppUtils.convertToRevAiResponse(message)
        if (connected) callback.invoke(data)
        else connected = data.type == "connected"
    }

    fun sendAudio(audio: ByteArray) {
        if (session?.isOpen == true) session?.sendMessage(BinaryMessage(audio))
        else {
            logger.error("Session closed. Cannot send message.")

            if (initialSession?.isOpen == true) initialSession?.sendMessage(BinaryMessage(audio))
            else logger.error("Initial session is closed.")
        }
    }

    @Throws(IOException::class)
    override fun close() {
        if (session?.isOpen == true) session?.sendMessage(TextMessage("EOS"))
        else logger.error("Session closed. Cannot send message.")

        if (initialSession?.isOpen == true) initialSession?.sendMessage(TextMessage("EOS"))
        else logger.error("Initial session closed. Cannot send message.")
    }

    companion object {
        private val logger = AppUtils.getLogger<SessionHandler>()
    }

}
