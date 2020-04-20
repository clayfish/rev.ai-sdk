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

import ai.rev.streaming.ai.rev.streaming.clients.AsyncClient
import ai.rev.streaming.ai.rev.streaming.clients.AsyncClientImpl
import ai.rev.streaming.clients.StreamingClient
import ai.rev.streaming.clients.StreamingClientImpl
import ai.rev.streaming.models.ClientConfig

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
    val asyncClient: AsyncClient = AsyncClientImpl(clientConfig)
    val streamingClient: StreamingClient = StreamingClientImpl(clientConfig)

    override fun close() {
        streamingClient.close()
    }
}
