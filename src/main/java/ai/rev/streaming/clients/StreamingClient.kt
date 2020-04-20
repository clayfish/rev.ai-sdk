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
import ai.rev.streaming.SessionHandler
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
     * @param audioStream
     * @see ClientConfig.bufferSize
     */
    fun stream(audioStream: InputStream)
}

/**
 * @author shuklaalok7 (alok@clay.fish)
 * @since v0.2.0 2020-04-19 08:45 PM IST
 */
internal class StreamingClientImpl(private val clientConfig: ClientConfig) : StreamingClient {
    //    private var sessionHandlers = ConcurrentHashMap<String, SessionHandler>()
    private val sessionHandler: SessionHandler = SessionHandler(clientConfig)
    private val executor = Executors.newSingleThreadExecutor()

    override fun stream(audio: ByteArray) = sessionHandler.sendAudio(audio)

    override fun stream(audioStream: InputStream) {
        logger.debug("Creating a separate thread to handle the audio inputStream...")

        executor.execute {
            val buffer = ByteArray(clientConfig.bufferSize)
            var bytesRead = 0
            while (true) {
                try {
                    val length = audioStream.read(buffer, 0, clientConfig.bufferSize)
                    if (length > 0) {
                        bytesRead += length
                        logger.debug("Read bytes from audio input-stream: $length\t Total bytes read: $bytesRead")
                        stream(buffer.copyOfRange(0, length))
                    }
                } catch (e: IndexOutOfBoundsException) {
                    logger.debug("File is not populated yet, waiting for it", e)
                    Thread.sleep(1000)
                }

                if (Thread.interrupted()) {
                    logger.warn("Terminated reading from the audio inputStream.")
                    try {
                        audioStream.close()
                    } catch (e: IOException) {
                        logger.error("Error when closing the audio inputStream.", e)
                    }
                }
            }
        }
    }

    override fun close() {
        sessionHandler.close()
        executor.shutdown()
        try {
            if (!executor.awaitTermination(TIMEOUT, TimeUnit.SECONDS)) executor.shutdownNow()
        } catch (e: InterruptedException) {
            executor.shutdownNow()
        }
    }

    companion object {
        private val logger = AppUtils.getLogger<StreamingClient>()
        private const val TIMEOUT = 60L // in seconds
    }
}
