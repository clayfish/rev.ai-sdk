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

import ai.rev.streaming.WebsocketManager.State.*
import ai.rev.streaming.models.ClientConfig
import ai.rev.streaming.models.StreamingResponse
import org.springframework.web.socket.BinaryMessage
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.handler.TextWebSocketHandler
import java.io.IOException
import java.util.*
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference

/**
 * Depends on Spring Websocket
 *
 * @author shuklaalok7 (alok@clay.fish)
 * @since v0.1.0 2020-03-29 07:18 PM IST
 */
internal class SessionHandler(private val config: ClientConfig) : TextWebSocketHandler(), WebsocketManager {
    /**
     * Once the handshake done, rev.ai sends a text-message saying "connected", only then it's ready to take the streams
     * from client.
     */
    private var state: AtomicReference<WebsocketManager.State> = AtomicReference(IDLE)
    private val audioQueue: Queue<ByteArray> = ConcurrentLinkedQueue()
    private val callbacks = arrayListOf(config.callback)
    private val executor = Executors.newSingleThreadExecutor()

    // Experimental
    var initialSession: WebSocketSession? = null
    private var session: WebSocketSession? = null

    private val task = Runnable {
        logger.debug("Current state: $state")
        when (state.get()!!) {
            IDLE, DISCONNECTED -> {
                logger.debug("Trying to connect/reconnect with rev.ai...")
                connect()
                startExecutor()
            }

            CONNECTING, CONNECTED -> {
                logger.debug("Will start streaming soon...")
                Thread.sleep(500)
                startExecutor()
            }

            READY -> while (audioQueue.isNotEmpty()) {
                val audio = audioQueue.peek()
                if (session?.isOpen == true) {
                    logger.debug("Session is open, streaming audio...")
                    session?.sendMessage(BinaryMessage(audio))
                    // Remove this element from the queue
                    audioQueue.poll()
                } else {
                    logger.error("Session closed. Retrying...")

                    if (initialSession?.isOpen == true) {
                        logger.debug("Session is open, streaming audio...")
                        initialSession?.sendMessage(BinaryMessage(audio))
                    } else {
                        logger.error("Initial session is also closed. Retrying...")
                        if (state.get() != CLOSING && state.get() != CLOSED)
                            state.set(DISCONNECTED)
                        startExecutor()
                        return@Runnable
                    }
                }

                // fixme see if it should break when the state is CLOSING or CLOSED
                if (Thread.currentThread().isInterrupted) {
                    logger.debug("Thread streaming data to rev.ai is interrupted.")
                    break
                }
            }

            CLOSING -> if (audioQueue.isEmpty()) {
                logger.debug("Audio queue is empty. All the data has been streamed to rev.ai.")
                when {
                    session?.isOpen == true -> session?.sendMessage(TextMessage("EOS"))
                    initialSession?.isOpen == true -> initialSession?.sendMessage(TextMessage("EOS"))
                }
                state.set(CLOSED)
                startExecutor()
            }

            CLOSED -> return@Runnable
        }
    }

    override fun afterConnectionEstablished(session: WebSocketSession) {
        logger.info("Connection established")
        if (state.get() != CLOSING && state.get() != CLOSED)
            state.set(CONNECTED)
        this.session = session
    }

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
        val data = AppUtils.convertToStreamingResponse(message)
        if (state.get() == READY) callbacks.forEach { it.invoke(data) }
        else if (data.type == "connected" && state.get() != CLOSING && state.get() != CLOSED) state.set(READY)
    }

    override fun addCallback(callback: (StreamingResponse) -> Unit) = callbacks.add(callback)

    override fun clearCallbacks() = callbacks.clear()

    override fun sendAudio(audio: ByteArray) {
        if (state.get() != CLOSING && state.get() != CLOSED) {
            logger.debug("Adding given audio bytes to the queue...")
            audioQueue.offer(audio)
            startExecutor()
        } else logger.warn("RevAi client is closing down, cannot stream any more audio-data.")
    }

    private fun startExecutor(): Unit = executor.execute(task)

    private fun connect() {
        if (state.get() != CLOSING && state.get() != CLOSED)
            state.set(CONNECTING)
        NetworkUtils.handshake(this, config)
        startExecutor()
    }

    @Throws(IOException::class)
    override fun close() {
        state.set(CLOSING)

        Thread {
            while (state.get() != CLOSED) Thread.sleep(2000)

            executor.shutdown()
            try {
                if (!executor.awaitTermination(TIMEOUT, TimeUnit.MINUTES)) executor.shutdownNow()
                state.set(CLOSED)
            } catch (e: InterruptedException) {
                executor.shutdownNow()
            }
        }.start()
    }

    companion object {
        private val logger = AppUtils.getLogger<SessionHandler>()
        private const val TIMEOUT = 2L // in minutes
    }

}
