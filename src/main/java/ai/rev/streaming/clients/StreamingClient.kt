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
package ai.rev.streaming.clients

import ai.rev.streaming.AppUtils
import ai.rev.streaming.WebsocketClientEndpoint
import ai.rev.streaming.WebsocketManager
import ai.rev.streaming.models.ClientConfig
import java.io.IOException
import java.io.InputStream
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

/**
 * @author shuklaalok7 (alok@clay.fish)
 * @since v0.2.0 2020-04-21 03:04 AM IST
 */
interface StreamingClient : AutoCloseable {
    /**
     * @param audio The audio data to send to rev.ai
     */
    fun stream(audio: ByteArray)

    /**
     * [ClientConfig.streamIdleTime] and [ClientConfig.streamStartTime] are used to control the behaviour when the
     * audio-stream stops getting appended. If these properties are not set, it is necessary to stop the transcription
     * by calling [close].
     *
     * @param audioStream
     * @param blocking If `false`, this function returns immediately and the transcription is done in a separate thread.
     * If `true`, the function does block the current thread.
     * the current thread.
     *
     * @see ClientConfig.bufferSize
     * @see ClientConfig.streamStartTime
     * @see ClientConfig.streamIdleTime
     */
    @Throws(InterruptedException::class)
    fun stream(audioStream: InputStream, blocking: Boolean = false)
}

/**
 * @author shuklaalok7 (alok@clay.fish)
 * @since v0.2.0 2020-04-19 08:45 PM IST
 */
internal class StreamingClientImpl(private val clientConfig: ClientConfig) : StreamingClient {
    //    private var sessionHandlers = ConcurrentHashMap<String, SessionHandler>()
    private val websocket: WebsocketManager = WebsocketClientEndpoint(clientConfig)
    private val executor = Executors.newSingleThreadExecutor()

    override fun stream(audio: ByteArray) = websocket.sendAudio(audio)

    @Throws(InterruptedException::class)
    override fun stream(audioStream: InputStream, blocking: Boolean) = if (blocking)
        startStreaming(audioStream)
    else {
        logger.debug("Creating a separate thread to handle the audio inputStream...")
        executor.execute { startStreaming(audioStream) }
    }

    override fun close() {
        websocket.close()
        executor.shutdown()
        try {
            if (!executor.awaitTermination(TIMEOUT, TimeUnit.SECONDS)) executor.shutdownNow()
        } catch (e: InterruptedException) {
            executor.shutdownNow()
            Thread.currentThread().interrupt()
        }
    }

    /**
     * @param audioStream
     */
    @Throws(InterruptedException::class)
    private fun startStreaming(audioStream: InputStream) {
        val buffer = ByteArray(clientConfig.bufferSize)
        var bytesRead = 0
        var hasStartedStreaming = false
        var idleTime = 0L
        try {
            while (true) {
                try {
                    val length = audioStream.read(buffer, 0, clientConfig.bufferSize)
                    if (length > 0) {
                        hasStartedStreaming = true
                        bytesRead += length
                        logger.debug("Read bytes from audio input-stream: $length\t Total bytes read: $bytesRead")
                        stream(buffer.copyOfRange(0, length))
                        idleTime = 0
                    } else {
                        // No audio has been generated, wait a little
                        Thread.sleep(1000)
                        idleTime += 1000
                    }
                } catch (e: IndexOutOfBoundsException) {
                    logger.debug("File is not populated yet, waiting for it", e)
                    Thread.sleep(1000)
                    idleTime += 1000
                }

                if (toClose(idleTime, hasStartedStreaming)) {
                    // Audio Stream has stopped, clean it up
                    audioStream.close()
                    close()
                    break
                }
            }
        } catch (e: InterruptedException) {
            logger.warn("Terminated reading from the audio inputStream.")
            try {
                audioStream.close()
            } catch (e: IOException) {
                logger.error("Error when closing the audio inputStream.", e)
            }
            Thread.currentThread().interrupt()
        }
    }

    /**
     * Makes the decision to close this transcription based on the config values [ClientConfig.streamStartTime] and
     * [ClientConfig.streamIdleTime] and current state of the audio-stream.
     *
     * @param idleTime              Current idle-time in milliseconds
     * @param hasStartedStreaming   If any bytes have been read from the audio-stream
     */
    private fun toClose(idleTime: Long, hasStartedStreaming: Boolean): Boolean {
        if (idleTime == 0L) return false

        var idleStreamTime = clientConfig.streamIdleTime ?: 0L
        var streamStartTime = clientConfig.streamStartTime ?: 0L

        return when {
            idleStreamTime > 0 && streamStartTime > 0 ->
                if (hasStartedStreaming) idleTime > idleStreamTime else idleTime > streamStartTime

            idleStreamTime > 0 -> {
                streamStartTime = idleStreamTime * START_TIME_MULTIPLIER
                if (hasStartedStreaming) idleTime > idleStreamTime else idleTime > streamStartTime
            }

            streamStartTime > 0 -> {
                idleStreamTime = streamStartTime / START_TIME_MULTIPLIER
                if (hasStartedStreaming) idleTime > idleStreamTime else idleTime > streamStartTime
            }

            else -> false
        }
    }

    companion object {
        private val logger = AppUtils.getLogger<StreamingClient>()
        private const val TIMEOUT = 60L // in seconds
        private const val START_TIME_MULTIPLIER = 6
    }
}
