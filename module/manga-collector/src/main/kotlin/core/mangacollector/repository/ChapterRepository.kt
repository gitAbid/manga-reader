package core.mangacollector.repository

import core.mangacollector.model.Chapter
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

@Repository
interface ChapterRepository : MongoRepository<Chapter, String> {
}