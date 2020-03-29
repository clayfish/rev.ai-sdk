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
    private lateinit var session: WebSocketSession

    override fun afterConnectionEstablished(session: WebSocketSession) {
        logger.info("Connection established")
        this.session = session
    }

    override fun handleBinaryMessage(session: WebSocketSession, message: BinaryMessage) {
        logger.info("Binary message received")
    }

    override fun handleTextMessage(session: WebSocketSession, message: TextMessage) {
        logger.info("Text message received\n$message")
        val data = AppUtils.convertToRevAiResponse(message)
        if (connected) callback.invoke(data)
        else connected = data.type == "connected"
    }

    fun sendAudio(audio: ByteArray) {
        if (session.isOpen) session.sendMessage(BinaryMessage(audio))
        else logger.error("Session closed. Cannot send message.")
    }

    @Throws(IOException::class)
    override fun close() {
        if (session.isOpen) session.sendMessage(TextMessage("EOS"))
        else logger.error("Session closed. Cannot send message.")
    }

    companion object {
        private val logger = AppUtils.getLogger<SessionHandler>()
    }

}
