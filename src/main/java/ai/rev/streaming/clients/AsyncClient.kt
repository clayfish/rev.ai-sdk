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
import ai.rev.streaming.models.*
import org.json.JSONObject
import java.util.concurrent.TimeUnit
import javax.ws.rs.client.Client
import javax.ws.rs.client.ClientBuilder
import javax.ws.rs.client.Entity
import javax.ws.rs.core.GenericType
import javax.ws.rs.core.MediaType

/**
 * @author shuklaalok7 (alok@clay.fish)
 * @since v0.2.0 2020-04-21 03:09 AM IST
 */
interface AsyncClient {

    /**
     * [Documentation](https://www.rev.ai/docs#tag/Account)
     *
     * Get the developer's account information.
     *
     * @return Details about the developer's account
     */
    fun getAccount(): Account?

    /**
     * [Documentation](https://www.rev.ai/docs#operation/GetCaptions)
     *
     * Returns the caption output for a transcription job. We currently support SubRip (SRT) and Web Video Text Tracks
     * (VTT) output. Caption output format can be specified in the Accept header. Returns SRT by default.
     *
     * **Note:** For streaming jobs, transient failure of our storage during a live session may prevent the final
     * hypothesis elements from saving properly, resulting in an incomplete caption file. This is rare, but not
     * impossible.
     *
     * @param id                Rev.ai API Job Id
     * @param speakerChannel    Identifies which channel of the job output to caption. Default is `null` which works
     * only for jobs with no `speaker_channels_count` provided during job submission.
     * @param format            MIME type specifying the caption output format
     * @return String in the specified format
     */
    fun getCaptions(id: String, speakerChannel: Int? = null, format: CaptionFormat = CaptionFormat.VTT): String?

    /**
     * [Documentation](https://www.rev.ai/docs#tag/Transcript)
     *
     * Returns the transcript for a completed transcription job. Transcript can be returned as either JSON or plaintext
     * format. Transcript output format can be specified in the `Accept` header. Returns JSON by default.
     *
     * **Note:** For streaming jobs, transient failure of our storage during a live session may prevent the final
     * hypothesis elements from saving properly, resulting in an incomplete transcript. This is rare, but not
     * impossible. To guarantee 100% completeness, we recommend capturing all final hypothesis when you receive them on
     * the client.
     *
     * @param id        Rev.ai API Job Id
     * @param format    MIME type specifying the transcription output format
     * @return The transcript // todo the transcript has JSON structure, try to put it in POJO
     */
    fun getTranscript(id: String, format: TranscriptFormat = TranscriptFormat.JSON): JSONObject?

    /**
     * [Documentation](https://www.rev.ai/docs#operation/GetJobById)
     *
     * Returns information about a transcription job
     *
     * @param id    Rev.ai API Job Id
     */
    operator fun get(id: String): Job?

    /**
     * [Documentation](https://www.rev.ai/docs#operation/DeleteJobById)
     *
     * Deletes a transcription job. All data related to the job, such as input media and transcript, will be permanently
     * deleted. A job can only be deleted once it's completed (either with success or failure).
     *
     * @param id    Rev.ai API Job Id
     */
    fun delete(id: String): Boolean

    /**
     * [Documentation](https://www.rev.ai/docs#operation/GetListOfJobs)
     *
     * Gets a list of transcription jobs submitted within the last 30 days in reverse chronological order up to the
     * provided `limit` number of jobs per call.
     *
     * **Note:** Jobs older than 30 days will not be listed. Pagination is supported via passing the last job `id` from
     * a previous call into `starting_after`.
     *
     * @param limit         Limits the number of jobs returned, default is 100, max is 1000.
     * @param startingAfter If specified, returns transcription jobs submitted before the job with this id, exclusive
     * (job with this id is not included)
     */
    fun getJobs(limit: Int = 100, startingAfter: String? = null): List<Job>

    /**
     * [Documentation](https://www.rev.ai/docs#operation/SubmitTranscriptionJob)
     *
     * Starts an asynchronous job to transcribe speech-to-text for a media file. Media files can be specified in two
     * ways, either by including a public url to the media in the transcription job options or by uploading a local file
     * as part of a multipart/form request.
     *
     * @param jobRequest    All the data to create a job
     */
    fun submitJob(jobRequest: JobRequest): Job?

}

/**
 * @author shuklaalok7 (alok@clay.fish)
 * @since v0.2.0 2020-04-19 08:46 PM IST
 */
internal class AsyncClientImpl(private val config: ClientConfig) : AsyncClient {

    private val client: Client = ClientBuilder.newBuilder().connectTimeout(config.timeout, TimeUnit.SECONDS)
            .readTimeout(config.timeout, TimeUnit.SECONDS).build()

    override fun getAccount(): Account? = try {
        AppUtils.createInvocation(client, "/account", config).get(Account::class.java)
    } catch (e: Exception) {
        // todo inspect
        logger.error("Error occurred in getting account", e)
        null
    }

    override fun getCaptions(id: String, speakerChannel: Int?, format: CaptionFormat): String? = try {
        val invocationBuilder = if (speakerChannel == null) AppUtils.createInvocation(client, "/jobs/$id/captions", config)
        else AppUtils.createInvocation(client, "/jobs/$id/captions", config, Pair("speaker_channel", "$speakerChannel"))

        invocationBuilder.accept(format.mime).get().entity.toString()
    } catch (e: Exception) {
        // todo inspect
        logger.error("Error occurred in getting captions for job $id", e)
        null
    }

    override fun getTranscript(id: String, format: TranscriptFormat): JSONObject? = try {
        JSONObject(AppUtils.createInvocation(client, "/jobs/$id/transcript", config).accept(format.mime)
                .get().entity.toString())
    } catch (e: Exception) {
        // todo inspect
        logger.error("Error occurred in getting transcript for job $id", e)
        null
    }

    override fun get(id: String): Job? = try {
        AppUtils.createInvocation(client, "/jobs/$id", config).get(Job::class.java)
    } catch (e: Exception) {
        // todo inspect
        logger.error("Error occurred in getting job with $id", e)
        null
    }

    override fun delete(id: String): Boolean = try {
        val status = AppUtils.createInvocation(client, "/jobs/$id", config).delete().status
        status in 200..299
    } catch (e: Exception) {
        // todo inspect
        logger.error("Error occurred in deleting job with $id", e)
        false
    }

    override fun getJobs(limit: Int, startingAfter: String?): List<Job> {
        var limit1 = limit.coerceAtMost(1000)
        limit1 = limit1.coerceAtLeast(0)
        if (limit1 == 0) return emptyList()

        // todo implement
        return try {
            val queryParams = arrayListOf(Pair("limit", "$limit1"))
            if (startingAfter != null) queryParams.add(Pair("starting_after", startingAfter))

            // May need to use Jackson instead of JaxB: https://stackoverflow.com/questions/9627170/cannot-unmarshal-a-json-array-of-objects-using-jersey-client
            AppUtils.createInvocation(client, "/jobs", config, *queryParams.toTypedArray()).get(object : GenericType<List<Job>>() {})
        } catch (e: Exception) {
            // todo inspect
            logger.error("Error occurred in getting jobs", e)
            emptyList()
        }
    }

    override fun submitJob(jobRequest: JobRequest): Job? = try {
        AppUtils.createInvocation(client, "/jobs", config)
                .post(Entity.entity(jobRequest, MediaType.APPLICATION_JSON_TYPE), Job::class.java)
    } catch (e: Exception) {
        // todo inspect
        logger.error("Error occurred in submitting job", e)
        null
    }

    companion object {
        private val logger = AppUtils.getLogger<AsyncClientImpl>()
    }

}
