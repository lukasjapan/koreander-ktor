# koreander-ktor

Ktor support for the [Koreander](https://github.com/lukasjapan/koreander) template engine.

## Usage

Gradle:

```groovy
repositories {
    jcenter()

    maven {
        url  "https://dl.bintray.com/lukasjapan/de.cvguy.kotlin"
    }
}

dependencies {
    compile "org.jetbrains.kotlin:kotlin-stdlib-jdk8:$kotlin_version"
    compile "io.ktor:ktor-server-netty:$ktor_version"
    compile 'de.cvguy.kotlin:koreander-ktor:0.+'
}
```

Code:

```kotlin
data class ViewModel(val hello: String)

val viewModel = ViewModel("World")

fun main() {
    val server = embeddedServer(Netty, port = 8080) {
        install(KoreanderFeature)
        routing {
            get("/") { call.respondKorRaw("%p Hello \$hello", viewModel) }
            get("/java-resource") { call.respondKorRes("/index.kor", viewModel) }
            get("/file") { call.respondKor("index.kor", viewModel) }
            get("/file-explicit") { call.respondKor(File("index.kor"), viewModel) }
            get("/input-stream") { call.respondKor(File("index.kor").inputStream(), viewModel) }
            get("/url") { call.respondKor(URL("file://index.kor"), viewModel) }
        }
    }
    server.start(wait = true)
}
```