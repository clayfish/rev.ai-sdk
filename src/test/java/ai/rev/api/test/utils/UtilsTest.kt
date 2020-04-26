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

package ai.rev.api.test.utils

import ai.rev.streaming.AppUtils
import ai.rev.streaming.models.StreamingResponse
import org.junit.Test
import org.springframework.web.socket.TextMessage
import kotlin.test.assertEquals

/**
 * @author shuklaalok7 (alok@clay.fish)
 * @since v0.2.0 2020-04-21 05:13 AM IST
 */
class UtilsTest {

    @Test
    fun testCreateInvocation() {

    }

    @Test
    fun testConvertToStreamingResponse() {
        val expectedResponse = StreamingResponse("qwe", "async",
                listOf(StreamingResponse.Element("final", "This is element 1.", 0.0, 1.0, 50.0),
                        StreamingResponse.Element("partial", "This is element 2.", 1.1, 2.0, 60.0),
                        StreamingResponse.Element("final", "This is element 1.", 2.1, 3.0, 70.0)),
                0.0, 3.0)

        val response = AppUtils.convertToStreamingResponse(TextMessage(AppUtils.gson.toJson(expectedResponse), true))

        assertEquals(expectedResponse, response)
    }

    @Test
    fun handshake() {
    }

}
