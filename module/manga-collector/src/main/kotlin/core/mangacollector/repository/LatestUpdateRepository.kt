package core.mangacollector.repository

import core.mangacollector.model.LatestMangaUpdate
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

@Repository
interface LatestUpdateRepository : MongoRepository<LatestMangaUpdate, String> {
    fun findByMangaName(name: String): LatestMangaUpdate?
    fun findAllByOrderByMangaName(): List<LatestMangaUpdate>
}