# rev.ai API JVM SDK

Currently supports rev.ai streaming/websocket API.

# How to use?

[![Maven Central](https://img.shields.io/maven-central/v/in.clayfish/rev-ai-api.svg?label=Maven%20Central)](https://search.maven.org/search?q=g:%22in.clayfish%22%20AND%20a:%22rev-ai-api%22)

To import in various build-tools, use the import statements provided on [Maven Central](https://search.maven.org/artifact/in.clayfish/rev-ai-api/0.1.1/jar).

In your Kotlin code, use it like following.

```kotlin
val callback: (RevAiResponse) -> Unit = {
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
val revAi = RevAi(ClientConfig(YOUR_REVAI_ACCESS_TOKEN, AudioContentType.FLAC, callback))
revAi.stream(inputStream) // Connect with the file. The file that is being read can be simultaneously written by some 
// other thread/process.
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
