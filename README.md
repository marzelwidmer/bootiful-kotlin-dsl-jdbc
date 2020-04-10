# Spring Tips: Bootiful Kotlin Redux
https://youtu.be/P3yI_YhG5pk


# Kotlin SQL Framework
https://github.com/JetBrains/Exposed
https://github.com/JetBrains/Exposed/wiki/Getting-Started#download

Add Maven Repository
```xml
 <repositories>
        <repository>
            <id>jcenter</id>
            <name>jcenter</name>
            <url>https://jcenter.bintray.com</url>
        </repository>
 </repositories>
```
Add Dependencies
```xml
<dependency>
    <groupId>org.jetbrains.exposed</groupId>
    <artifactId>exposed</artifactId>
    <version>0.17.7</version>
</dependency>
<dependency>
    <groupId>org.jetbrains.exposed</groupId>
    <artifactId>spring-transaction</artifactId>
    <version>0.11.2</version>
</dependency>
```


# Bean Definition
Bean definition with Kotlin DSL.
 
```kotlin
fun main(args: Array<String>) {
    runApplication<BootifulKotlinDslApplication>(*args) {
        val log = LoggerFactory.getLogger("Main")

        val context = beans {
            bean {
                ApplicationRunner {
                    val customerService = ref<CustomerService>()
                    listOf("John", "Jane", "Jack")
                        .map { Customer(name = it) }
                        .forEach { customerService.insert(it) }

                    customerService.all()
                        .forEach { log.info("--> $it") }
                }
            }
        }
        addInitializers(context)
    }
}
```
