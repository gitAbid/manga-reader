package core.mangacollector

import core.mangacollector.repository.LatestUpdateRepository
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableScheduling
class MangaCollectorApplication {

    @Bean
    fun run(repo: LatestUpdateRepository) = CommandLineRunner {

    }
}

fun main(args: Array<String>) {
    runApplication<MangaCollectorApplication>(*args)
}
