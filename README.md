# rev.ai API JVM SDK

Currently adding support for streaming/websocket API.

# How to use?

```
val revAi = RevAi(YOUR_REVAI_ACCESS_TOKEN, AudioContentType.FLAC)
revAi.connect {
    // The text received from revai can be partial or final, currently we will discard the partials and write the finals only.
    if (it.type == "partial") {
        logger.warn("Partial text received from rev.ai, discarding it.")
        logger.warn(AppUtils.toText(it))
    } else {
        logger.warn("Final text received from rev.ai, writing it to Google Doc.")
        // todo put that text in a Google Docs
        logger.info(AppUtils.toText(it))
    }
}
revAi.stream(audioData) // can be called any number of times.
```
