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

package ai.rev.streaming.models

/**
 * @author shuklaalok7 (alok@clay.fish)
 * @since v0.1.0 2020-04-19 08:42 PM IST
 */
data class Account(
        val email: String,
        val balanceSeconds: Long
)

/**
 * @param start The timestamp of the beginning of the segment relative to the beginning of the audio in seconds
 * with centisecond precision.
 * @param end The timestamp of the end of the segment relative to the beginning of the audio in seconds with
 * centisecond precision.
 *
 * @author shuklaalok7 (alok@clay.fish)
 * @since v0.2.2 2021-12-26 10:12 PM IST
 */
data class Segment (val start: Float, val end: Float)
