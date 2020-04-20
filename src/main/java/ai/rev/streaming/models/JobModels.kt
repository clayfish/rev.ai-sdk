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

import java.time.LocalDateTime

/**
 * See [rev.ai Docs](https://www.rev.ai/docs#operation/SubmitTranscriptionJob)
 * @author shuklaalok7 (alok@clay.fish)
 * @since v0.1.2 2020-04-19 08:11 PM IST
 */
data class Job(
        /**
         * Id of the job
         */
        val id: String,

        /**
         * Current status of the job
         */
        val status: JobStatus,

        /**
         * The date and time the job was created in ISO-8601 UTC form
         */
        val createdOn: LocalDateTime,

        /**
         * The date and time the job was completed, whether successfully or failing, in ISO-8601 UTC form
         */
        val completedOn: LocalDateTime?,

        /**
         * Optional metadata that was provided during job submission.
         */
        val metadata: String?,

        /**
         * Name of the file provided. Present when the file name is available
         */
        val name: String?,

        /**
         * Optional callback url to invoke on job completion
         */
        val callbackUrl: String?,

        /**
         * Duration of the file in seconds. Null if the file could not be retrieved or there was not a valid media file.
         */
        val durationSeconds: Double?,

        /**
         * Media url provided by the job submission. Null if the job was provided using a local file
         */
        val mediaUrl: String?,

        /**
         * Simple reason of why the transcription job failed. Check failure_detail for specific details and solutions
         */
        val failure: JobFailure?,

        /**
         * Human-readable reason why the job failed
         */
        val failureDetail: String?,

        /**
         * Type of speech recognition performed. Currently the only supported values are 'async' for asynchronous jobs
         * and 'stream' for streaming jobs.
         */
        val type: JobType
)

/**
 * See [rev.ai Docs](https://www.rev.ai/docs#operation/SubmitTranscriptionJob)
 * @author shuklaalok7 (alok@clay.fish)
 * @since v0.1.2 2020-04-19 07:58 PM IST
 */
data class JobRequest(
        /**
         * <= 2048 characters
         *
         * Direct download media url. Ignored if submitting job from file.
         */
        val mediaUrl: String,

        /**
         * Specify if speaker diarization will be skipped by the speech engine
         */
        val skipDiarization: Boolean? = false,

        /**
         * Specify if "punct" type elements will be skipped by the speech engine. For JSON outputs, this includes
         * removing spaces. For text outputs, words will still be delimited by a space
         */
        val skipPunctuation: Boolean? = false,

        /**
         * Currently we only define disfluencies as 'ums' and 'uhs'. When set to true, disfluencies will be not appear
         * in the transcript.
         */
        val removeDisfluencies: Boolean? = false,

        /**
         * Enabling this option will filter for approx. 600 profanities, which cover most use cases. If a transcribed
         * word matches a word on this list, then all the characters of that word will be replaced by asterisks except
         * for the first and last character.
         */
        val filterProfanity: Boolean? = false,

        /**
         * Integer 1 to 8, including boundaries. Use to specify the total number of unique speaker channels in the audio.
         *
         * Given the number of audio channels provided, each channel will be transcribed separately and the channel id
         * assigned to the speaker label. The final output will be a combination of all individual channel outputs.
         * Overlapping monologues will have ordering broken by the order in which the first spoken element of each
         * monologue occurs. If speaker_channels_count is greater than the actual channels in the audio, the job will
         * fail with invalid_media.
         *
         * **Note:** The amount charged will be the duration of the file multiplied by the number of channels specified.
         */
        val speakerChannelsCount: Int?,

        /**
         * Optional callback url to invoke when processing is complete
         */
        val callbackUrl: String?,

        /**
         * Array of objects [ 1 .. 50 ] items
         * metadata
         * string <= 512 characters Nullable
         * Optional metadata that was provided during submission
         */
        val customVocabularies: Array<String>?
) {

}
