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

package ai.rev.api.test.clients

import ai.rev.streaming.RevAi
import ai.rev.streaming.clients.StreamingClient
import ai.rev.streaming.models.AudioContentType
import ai.rev.streaming.models.ClientConfig
import org.junit.Test
import kotlin.test.AfterTest
import kotlin.test.BeforeTest

/**
 * @author shuklaalok7 (alok@clay.fish)
 * @since v0.2.0 2020-04-21 05:07 AM IST
 */
class StreamingClientTest {

    private lateinit var streamingClient: StreamingClient

    @BeforeTest
    fun initialize() {
        streamingClient = RevAi(ClientConfig("", AudioContentType.FLAC, {})).streamingClient
    }

    @AfterTest
    fun cleanup() {
        streamingClient.close()
    }

    @Test
    fun testStream1() {

    }

    @Test
    fun testStream2() {

    }

    @Test
    fun testClose() {

    }

}
