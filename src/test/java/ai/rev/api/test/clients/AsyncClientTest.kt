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
import ai.rev.streaming.clients.AsyncClient
import ai.rev.streaming.models.AudioContentType
import ai.rev.streaming.models.ClientConfig
import ai.rev.streaming.models.JobRequest
import org.junit.Test
import kotlin.test.BeforeTest
import kotlin.test.assertNotNull

/**
 * @author shuklaalok7 (alok@clay.fish)
 * @since v0.2.0 2020-04-21 04:53 AM IST
 */
class AsyncClientTest {

    private lateinit var asyncClient: AsyncClient

    @BeforeTest
    fun initialize() {
        asyncClient = RevAi(ClientConfig("", AudioContentType.FLAC, {})).asyncClient
    }

    @Test
    fun testGetAccount() {

    }

    @Test
    fun testGetCaptions() {

    }

    @Test
    fun testGetTranscript() {

    }

    @Test
    fun testGet() {

    }

    @Test
    fun testDelete() {

    }

    @Test
    fun testGetJobs() {

    }

    @Test
    fun testSubmitJob() {
        assertNotNull(asyncClient.submitJob(JobRequest("", speakerChannelsCount = null, callbackUrl = null,
                customVocabularies = null)))
    }

}
