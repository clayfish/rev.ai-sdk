# rev.ai API JVM SDK

Currently supports rev.ai streaming/websocket API.

# How to use?

```kotlin
val listener: (RevAiResponse) -> Unit = {
                // The text received from revai can be partial or final, currently we will discard the partials and
                // write the finals only.
                if (it.type == "partial") {
                    // todo handle partial text
                    logger.warn("Partial text received from rev.ai, discarding it.")
                } else {
                    // todo handle final text
                    logger.info("Final text received from rev.ai, writing it to Google Doc.")
                }
            }
val inputStream = FileInputStream("media/file1.flac")
val revAi = RevAi(YOUR_REVAI_ACCESS_TOKEN, AudioContentType.FLAC, listener)
revAi.stream(inputStream) // can be called any number of times.
```

Once you are done with it, you need to close it.

```kotlin
revAi.close()
```

Or `RevAi` instance can be used with try-with-resources.

```kotlin
val inputStream = FileInputStream("media/file1.flac")
RevAi(ClientConfig(appConfig.revaiAccessToken!!, AudioContentType.FLAC, listener)).use {
    it.stream(inputStream)
}
```
