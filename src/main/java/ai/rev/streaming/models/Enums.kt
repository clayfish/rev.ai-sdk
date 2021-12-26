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
 * @since v0.1.0 2020-03-29 06:48 PM IST
 */
enum class AudioContentType(val mime: String) {
    RAW("audio/x-raw"),
    FLAC("audio/x-flac"),
    WAV("audio/x-wav")
}

/**
 * @author shuklaalok7 (alok@clay.fish)
 * @since v0.1.2 2020-04-19 08:12 PM IST
 */
enum class JobStatus {
    in_progress, transcribed, failed
}

/**
 * @author shuklaalok7 (alok@clay.fish)
 * @since v0.1.2 2020-04-19 08:17 PM IST
 */
enum class JobFailure {
    internal_processing,
    download_failure,
    duration_exceeded,
    duration_too_short,
    invalid_media,
    empty_media,
    transcription,
    insufficient_balance,
    invoicing_limit_exceeded
}

/**
 * @author shuklaalok7 (alok@clay.fish)
 * @since v0.1.2 2020-04-19 08:22 PM IST
 */
enum class JobType {
    async, stream
}

/**
 * @author shuklaalok7 (alok@clay.fish)
 * @since v0.2.0 2020-04-19 08:34 PM IST
 */
enum class CaptionFormat(val mime: String) {
    SRT("application/x-subrip"), VTT("text/vtt")
}

/**
 * @author shuklaalok7 (alok@clay.fish)
 * @since v0.2.0 2020-04-19 11:36 PM IST
 */
enum class TranscriptFormat(val mime: String) {
    JSON("application/vnd.rev.transcript.v1.0+json"), TEXT("text/plain")
}

/**
 * @author shuklaalok7 (alok@clay.fish)
 * @since v0.2.2 2021-12-26 09:44 PM IST
 */
enum class Transcriber {
    MACHINE, HUMAN
}

/**
 * See [documentation](https://www.rev.ai/docs#operation/SubmitTranscriptionJob)
 *
 * @param code ISO 639 Language code. Language parameter is provided as a
 * [ISO 639-1 language code](https://en.wikipedia.org/wiki/List_of_ISO_639-1_codes), except [MANDARIN] which is
 * supplied as an [ISO 639-3 language code](https://en.wikipedia.org/wiki/ISO_639-3).
 *
 * @author shuklaalok7 (alok@clay.fish)
 * @since v0.2.2 2021-12-26 09:57 PM IST
 */
enum class Language(val code: String) {
    ARABIC("ar"),
    BULGARIAN("bg"),
    CATALAN("ca"),
    CROATIAN("hr"),
    CZECH("cs"),
    DANISH("da"),
    DUTCH("nl"),
    ENGLISH("en"),
    FINNISH("fi"),
    FRENCH("fr"),
    GERMAN("de"),
    GREEK("el"),
    HINDI("hi"),
    HUNGARIAN("hu"),
    ITALIAN("it"),
    JAPANESE("ja"),
    KOREAN("ko"),
    LITHUANIAN("lt"),
    LATVIAN("lv"),
    MALAY("ms"),
    MANDARIN("cmn"),
    NORWEGIAN("no"),
    POLISH("pl"),
    PORTUGUESE("pt"),
    ROMANIAN("ro"),
    RUSSIAN("ru"),
    SLOVAK("sk"),
    SLOVENIAN("sl"),
    SPANISH("es"),
    SWEDISH("sv"),
    TURKISH("tr")
}
