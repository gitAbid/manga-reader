package core.mangacollector.service

import core.mangacollector.model.Chapter
import core.mangacollector.model.LatestMangaUpdate
import core.mangacollector.model.Manga
import core.mangacollector.repository.ChapterRepository
import core.mangacollector.repository.LatestUpdateRepository
import core.mangacollector.repository.MangaRepository
import org.jsoup.Jsoup
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.util.*
import javax.annotation.PostConstruct
import kotlin.collections.HashSet
import kotlin.collections.LinkedHashSet

@Service
class Collector(var luRepo: LatestUpdateRepository,
                var mangaRepository: MangaRepository,
                var chapterRepository: ChapterRepository) {
    val logger = LoggerFactory.getLogger(Collector::class.java)

    @PostConstruct
    fun init() {
        logger.info("Collector initialized")
    }

   // @Scheduled(fixedRate = 900000)
    fun latestUpdate() {
        logger.info("Running latest manga updates collector")
        val initDoc = Jsoup.connect("https://mangakakalot.com/manga_list?type=latest&category=all&state=all&page=1").get()
        val lastPageString = initDoc.select(".page_last").text()
                .toString()
                .replace(Regex("[A-z]"), "")
                .replace(Regex("\\("), "")
                .replace(Regex("\\)"), "")

        val firstPage = 1
        val lastPage = lastPageString.toInt()

        timeElapsedToExecute {
            for (page in firstPage..lastPage) {
                val doc = Jsoup.connect("https://mangakakalot.com/manga_list?type=latest&category=all&state=all&page=$page").get()
                logger.info("$page")
                val elements = doc.select(".list-truyen-item-wrap")
                var name: String
                var mangaUrl: String
                var description: String
                var latestChapterUrl: String
                var coverImageUrl: String
                var viewCount: Long
                elements.forEach {
                    name = it.select("h3").text();
                    mangaUrl = it.select("a[href]").first().attr("abs:href")
                    description = it.select("p").text()
                    name = it.select("h3").text();
                    viewCount = it.select(".aye_icon").text().replace(Regex(","), "")?.toLong()
                    val link = it.select(".list-story-item-wrap-chapter").select("a[href]").first();
                    latestChapterUrl = link.attr("abs:href")
                    coverImageUrl = it.select("img").first().attr("abs:src")

                    luRepo.findByMangaName(name)?.let { it ->
                        it.mangaName = name
                        it.description = description
                        it.mangaUrl = mangaUrl
                        it.cover = coverImageUrl
                        if (it.latestChapter != latestChapterUrl) {
                            it.lastChapterUpdated = Date()
                            it.isChapterUpdated = true
                        }
                        it.viewCount = viewCount
                        it.latestChapter = latestChapterUrl
                        luRepo.save(it)

                    } ?: run {
                        luRepo.save(LatestMangaUpdate(
                                mangaName = name,
                                description = description,
                                mangaUrl = mangaUrl,
                                cover = coverImageUrl,
                                latestChapter = latestChapterUrl,
                                viewCount = viewCount,
                                isChapterUpdated = true
                        ))
                    }

                }
            }
        }
        logger.info("Finished running latest manga updates collector")
    }


    @Scheduled(fixedRate = 300000)
    fun mangaDetailsCollector() {
        logger.info("Running manga details collector")
        val mangas = luRepo.findAll()
        mangas.filter { latestMangaUpdate -> latestMangaUpdate.isChapterUpdated }.forEach {
            collectMangaDetail(it);
            it.isChapterUpdated = false
            luRepo.save(it)
        }
        logger.info("Finished running manga details collector")

    }

    private fun collectMangaDetail(latestManga: LatestMangaUpdate) {
        val doc = Jsoup.connect(latestManga.mangaUrl).get()
        val cover: String
        val authors = HashSet<String>()
        val genres = HashSet<String>()
        var status = ""
        var mangaLastUpdated = ""
        var rating: Double? = 0.0
        val description: String
        val chapters = HashSet<Chapter>()

        if (latestManga.mangaUrl.startsWith("https://mangakakalot.com/")) {
            cover = doc.select(".manga-info-pic").select("img").first().attr("abs:src")
            val elements = doc.select(".manga-info-text").select("li")
            description = doc.select("#noidungm").text();
            elements?.forEach {
                if (it.text().contains("Author")) {
                    val authorLinks = it.select("a[href]")
                    authorLinks?.forEach { author ->
                        authors.add(author.text())
                    }
                } else if (it.text().contains("Status")) {
                    val statusArr = it.text().split(":")
                    status = statusArr[1]?.trim()
                } else if (it.text().contains("Last updated")) {
                    val updateArr = it.text().split(":")
                    mangaLastUpdated = updateArr[1]?.trim()
                } else if (it.text().contains("Genres")) {
                    val genresLink = it.select("a[href]")
                    genresLink?.forEach { author ->
                        genres.add(author.text())
                    }
                }
            }
            rating = doc.select("#rate_row_cmd")?.text()?.split("/")?.get(0)?.split(":")?.get(1)?.trim()?.toDouble()
            doc.select(".chapter-list").select("a[href]")
            val chapterLinks = doc.select(".chapter-list").select("a[href]");
            chapterLinks.forEach {
                val chapterLink = it.attr("abs:href")
                val chapterName = it.text()
                val chapterImages = collectChapterImagesFromUrl(chapterLink)
                chapters.add(Chapter(
                        chapterName = chapterName,
                        chapterLink = chapterLink,
                        chapterImages = chapterImages
                ))
            }
            logger.info(cover)
            logger.info(authors.toString())
            logger.info(genres.toString())
            logger.info(status)
            logger.info(mangaLastUpdated)
            logger.info(rating.toString())
            logger.info(chapters.toString())

            mangaRepository.findByMangaName(latestManga.mangaName)?.let {
                it.mangaName = latestManga.mangaName
                it.mangaUrl = latestManga.mangaUrl
                it.viewCount = latestManga.viewCount
                it.latestChapter = latestManga.latestChapter
                it.description = description
                it.cover = cover
                it.authors = authors
                it.genres = genres
                it.status = status
                it.mangaLastUpdated = mangaLastUpdated
                it.rating = rating
                it.chapters = chapters
                it.lastUpdated = Date()
                mangaRepository.save(it)

            } ?: run {
                mangaRepository.save(Manga(
                        mangaName = latestManga.mangaName,
                        mangaUrl = latestManga.mangaUrl,
                        viewCount = latestManga.viewCount,
                        latestChapter = latestManga.latestChapter,
                        description = description,
                        cover = cover,
                        authors = authors,
                        genres = genres,
                        status = status,
                        mangaLastUpdated = mangaLastUpdated,
                        rating = rating,
                        chapters = chapters
                ))
            }


        } else {
            logger.error(latestManga.mangaUrl)
        }
    }

    private fun collectChapterImagesFromUrl(chapterLink: String?): LinkedHashSet<String> {
        val chapterImages = LinkedHashSet<String>()
        val doc = Jsoup.connect(chapterLink).get()
        val images = doc.select(".vung-doc").select("img")
        images?.forEach {
            val imageLink = it.attr("abs:src")
            chapterImages.add(imageLink)
        }
        return chapterImages
    }

    fun timeElapsedToExecute(function: () -> Unit) {
        logger.info("Started ${Date()}")
        function.invoke()
        logger.info("Finished ${Date()}")
    }
}