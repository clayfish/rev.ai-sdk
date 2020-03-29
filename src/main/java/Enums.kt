package ai.rev.streaming

/**
 * @author shuklaalok7 (alok@clay.fish)
 * @since v0.1.0 2020-03-29 06:48 PM IST
 */
enum class AudioContentType(val mime: String) {
    RAW("audio/x-raw"),
    FLAC("audio/x-flac"),
    WAV("audio/x-wav")
}
