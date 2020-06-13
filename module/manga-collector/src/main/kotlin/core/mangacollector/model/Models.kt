package core.mangacollector.model

import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.util.*
import kotlin.collections.LinkedHashSet


@Document
data class LatestMangaUpdate(
        @Id
        var id: String = ObjectId().toHexString(),
        var mangaName: String,
        var cover: String,
        var description: String,
        var mangaUrl: String,
        var latestChapter: String,
        var viewCount: Long,
        var lastUpdated: Date = Date(),
        var lastChapterUpdated: Date = Date(),
        var isChapterUpdated: Boolean = false)

@Document
data class Manga(
        @Id
        var id: String = ObjectId().toHexString(),
        var mangaName: String,
        var cover: String,
        var description: String,
        var mangaUrl: String,
        var latestChapter: String,
        var viewCount: Long,
        var authors: LinkedHashSet<String>,
        var status: String,
        var genres: LinkedHashSet<String>,
        var rating: Double?,
        var chapters: LinkedHashSet<Chapter>,
        var mangaLastUpdated: String,
        var lastUpdated: Date = Date(),
        var lastChapterUpdated: Date = Date())

@Document
data class Chapter(
        @Id
        var id: String = ObjectId().toHexString(),
        var chapterName: String,
        var chapterViewCount: Long = 0L,
        var chapterImages: LinkedHashSet<String> = LinkedHashSet(),
        var lastUpdated: Date = Date(),
        var chapterAdded: String = "",
        var chapterLink: String
)