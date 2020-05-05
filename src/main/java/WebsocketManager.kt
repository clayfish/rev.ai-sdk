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

import ai.rev.streaming.models.StreamingResponse

/**
 * @author shuklaalok7 (alok@clay.fish)
 * @since v0.2.1 2020-05-05 05:39 AM IST
 */
internal interface WebsocketManager: AutoCloseable {

    fun addCallback(callback: (StreamingResponse) -> Unit): Boolean
    fun clearCallbacks()
    fun sendAudio(audio: ByteArray)

    /**
     * @author shuklaalok7 (alok@clay.fish)
     * @since v0.1.31 2020-04-05 2:00 PM IST
     */
    enum class State {
        /**
         * Cannot send message
         */
        IDLE,

        /**
         * Connecting to the remote
         */
        CONNECTING,

        /**
         * Connected to the remote
         */
        CONNECTED,

        /**
         * Ready to transmit data
         */
        READY,

        /**
         * The session has expired, needs to be reconnected for further communication
         */
        DISCONNECTED,

        /**
         * No further action can be done, resources should be cleaned up soon.
         */
        CLOSING,

        /**
         * The connection with rev.ai is terminated, audio-stream can no longer be processed, not further action.
         */
        CLOSED
    }
}

/**
 * @author shuklaalok7 (alok@clay.fish)
 * @since v0.2.1 2020-05-05 06:02 AM IST
 */
internal abstract class AbstractWebsocketManager: WebsocketManager {

    override fun addCallback(callback: (StreamingResponse) -> Unit): Boolean {
        TODO("Not yet implemented")
    }

    override fun clearCallbacks() {
        TODO("Not yet implemented")
    }

    override fun close() {
        TODO("Not yet implemented")
    }

}
