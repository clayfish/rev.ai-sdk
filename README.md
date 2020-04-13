# rev.ai API JVM SDK

Currently supports rev.ai streaming/websocket API.

# How to use?

Import it in your POM based project.

* Maven

```xml
<dependencies>
    <dependency>
        <groupId>in.clayfish</groupId>
        <artifactId>rev-ai-api</artifactId>
        <version>0.1.1</version>
    </dependency>
    
    <!-- Other dependencies -->
    <dependency>
        <groupId>org.jetbrains.kotlin</groupId>
        <artifactId>kotlin-stdlib-jdk8</artifactId>
        <version>${kotlin.version}</version>
    </dependency>
    
    <dependency>
        <groupId>org.slf4j</groupId>
        <artifactId>slf4j-api</artifactId>
        <version>1.7.30</version>
    </dependency>

    <dependency>
        <groupId>com.google.code.gson</groupId>
        <artifactId>gson</artifactId>
        <version>2.8.6</version>
    </dependency>
</dependencies>
```

* Gradle

```
implementation 'in.clayfish:rev-ai-api:0.1.1'

implementation `org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.3.71`
implementation `org.slf4j:slf4j-api:1.7.30`
implementation `com.google.code.gson:gson:2.8.6`
```

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
