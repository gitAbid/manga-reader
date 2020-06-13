package core.mangacollector.repository

import core.mangacollector.model.Manga
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

@Repository
interface MangaRepository : MongoRepository<Manga, String> {
    fun findByMangaName(name: String): Manga?
}