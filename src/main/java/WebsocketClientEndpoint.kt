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

import ai.rev.streaming.WebsocketManager.State
import ai.rev.streaming.models.ClientConfig
import ai.rev.streaming.models.StreamingResponse
import org.springframework.web.socket.TextMessage
import java.nio.ByteBuffer
import java.util.*
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference
import javax.websocket.*

/**
 * Taken from [StackOverflow](https://stackoverflow.com/a/26454417/6860171)
 *
 * @author shuklaalok7 (alok@clay.fish)
 * @since v0.2.1 2020-05-05 05:22 AM IST
 */
@ClientEndpoint
class WebsocketClientEndpoint(private val config: ClientConfig) : WebsocketManager {
    private var state: AtomicReference<State> = AtomicReference(State.IDLE)
    private val audioQueue: Queue<ByteArray> = ConcurrentLinkedQueue()
    private val callbacks = arrayListOf(config.callback)
    private val executor = Executors.newSingleThreadExecutor()
    var session: Session? = null

    private fun connect() {
        if (state.get() != State.CLOSING && state.get() != State.CLOSED)
            state.set(State.CONNECTING)
        try {
            val container = ContainerProvider.getWebSocketContainer()
            session = container.connectToServer(this, NetworkUtils.createURI(config))
            startExecutor()
        } catch (e: Exception) {
            logger.error("Could not connect to Rev.ai websocket.", e)
        }
    }

    private val task = Runnable {
        logger.debug("Current state: $state")
        when (state.get()!!) {
            State.IDLE, State.DISCONNECTED -> {
                logger.debug("Trying to connect/reconnect with rev.ai...")
                connect()
                startExecutor()
            }

            State.CONNECTING, State.CONNECTED -> {
                logger.debug("Will start streaming soon...")
                Thread.sleep(500)
                startExecutor()
            }

            State.READY -> while (audioQueue.isNotEmpty()) {
                val audio = audioQueue.peek()
                if (session?.isOpen == true) {
                    logger.debug("Session is open, streaming audio...")
                    session?.asyncRemote?.sendBinary(ByteBuffer.wrap(audio))
                    // Remove this element from the queue
                    audioQueue.poll()
                } else {
                    logger.error("Session closed. Retrying...")
                    if (state.get() != State.CLOSING && state.get() != State.CLOSED)
                        state.set(State.DISCONNECTED)
                    startExecutor()
                    return@Runnable
                }

                // fixme see if it should break when the state is CLOSING or CLOSED
                if (Thread.currentThread().isInterrupted) {
                    logger.debug("Thread streaming data to rev.ai is interrupted.")
                    break
                }
            }

            State.CLOSING -> if (audioQueue.isEmpty()) {
                logger.debug("Audio queue is empty. All the data has been streamed to rev.ai.")
                if (session?.isOpen == true) session?.asyncRemote?.sendText("EOS")
                state.set(State.CLOSED)
                startExecutor()
            }

            State.CLOSED -> return@Runnable
        }
    }

    /**
     * Callback hook for Connection open events.
     *
     * @param userSession the userSession which is opened.
     */
    @OnOpen
    fun onOpen(userSession: Session?) {
        println("opening websocket")
        session = userSession
        if (state.get() != State.CLOSING && state.get() != State.CLOSED)
            state.set(State.CONNECTED)
    }

    /**
     * Callback hook for Connection close events.
     *
     * @param userSession the userSession which is getting closed.
     * @param reason the reason for connection close
     */
    @OnClose
    fun onClose(userSession: Session?, reason: CloseReason?) {
        println("closing websocket: ${reason?.reasonPhrase}")
        session = null
    }

    /**
     * Callback hook for Message Events. This method will be invoked when a client send a message.
     *
     * @param message The text message
     */
    @OnMessage
    fun onMessage(message: String?, userSession: Session?) {
        logger.info("Text message received\n$message")
        this.session = userSession
        if (!message.isNullOrBlank()) {
            val data = AppUtils.convertToStreamingResponse(TextMessage(message))
            if (state.get() == State.READY)
                callbacks.forEach { it.invoke(data) }
            else if (data.type == "connected" && state.get() != State.CLOSING
                    && state.get() != State.CLOSED)
                state.set(State.READY)
        }
    }

    override fun addCallback(callback: (StreamingResponse) -> Unit) = callbacks.add(callback)

    override fun clearCallbacks() = callbacks.clear()

    override fun sendAudio(audio: ByteArray) {
        if (state.get() != State.CLOSING && state.get() != State.CLOSED) {
            logger.debug("Adding given audio bytes to the queue...")
            audioQueue.offer(audio)
            startExecutor()
        } else logger.warn("RevAi client is closing down, cannot stream any more audio-data.")
    }

//    override fun sendAudio(audio: ByteArray) {
//        session?.asyncRemote?.sendBinary(ByteBuffer.wrap(audio))
//    }

    override fun close() {
        state.set(State.CLOSING)

        Thread {
            while (state.get() != State.CLOSED) Thread.sleep(2000)

            executor.shutdown()
            try {
                if (!executor.awaitTermination(TIMEOUT, TimeUnit.MINUTES)) executor.shutdownNow()
            } catch (e: InterruptedException) {
                executor.shutdownNow()
            }
        }.start()
    }

    private fun startExecutor(): Unit = executor.execute(task)

    companion object {
        private val logger = AppUtils.getLogger<WebsocketClientEndpoint>()
        private const val TIMEOUT = 2L // in minutes
    }

}
