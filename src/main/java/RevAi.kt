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

import java.io.IOException
import java.io.InputStream
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

/**
 * Entry-point for the client projects. Handles one connection at a time. To get multiple streams, create more instances
 * of it.
 *
 * @param clientConfig Configuration
 *
 * @author shuklaalok7 (alok@clay.fish)
 * @since v0.1.0 2020-03-29 18:20 IST
 */
class RevAi constructor(private val clientConfig: ClientConfig) : AutoCloseable {
    //    private var sessionHandlers = ConcurrentHashMap<String, SessionHandler>()
    private val sessionHandler: SessionHandler = SessionHandler(clientConfig)
    private val executor = Executors.newSingleThreadExecutor()

    /**
     * @param audio The audio data to send to rev.ai
     */
    fun stream(audio: ByteArray) = sessionHandler.sendAudio(audio)

    /**
     * @param audioStream
     * @see ClientConfig.bufferSize
     */
    fun stream(audioStream: InputStream) {
        logger.debug("Creating a separate thread to handle the audio inputStream...")

        executor.execute {
            val buffer = ByteArray(clientConfig.bufferSize)
            var bytesRead = 0
            while (true) {
//                val availableBytes = audioStream.available()
//                if (availableBytes > 0) {
//                    logger.debug("Available bytes in the given audioStream: $availableBytes")
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
//                }
                // We need analyze whether someone decided to interrupt the infinite loop.
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

    /**
     * Closes connection gracefully
     */
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
        private val logger = AppUtils.getLogger<RevAi>()
        private const val TIMEOUT = 60L // in seconds
    }
}
