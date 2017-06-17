package com.jvm_bloggers.core.rss.json

import com.jvm_bloggers.utils.NowProvider
import com.rometools.rome.feed.synd.SyndContent
import com.rometools.rome.feed.synd.SyndEntry
import com.rometools.rome.feed.synd.SyndFeed
import groovy.json.JsonSlurper
import spock.lang.Specification
import spock.lang.Subject

import java.time.LocalDateTime

import static com.jvm_bloggers.utils.DateTimeUtilities.toDate

class RssToJsonConverterSpec extends Specification {

    static final String BASE_URL = "http://localhost"
    static final String FEED_TITLE = "JvmBloggers"
    static final LocalDateTime DATE = new NowProvider().now()

    static final String AUTHOR_1 = "author1"
    static final String AUTHOR_2 = "author2"
    static final String DESCRIPTION_1 = "description1"
    static final String TITLE_1 = "title1"
    static final String TITLE_2 = "title2"

    @Subject
    RssToJsonConverter converter = new RssToJsonConverter(BASE_URL)

    SyndFeed feed = Mock() {
        SyndEntry entry1 = stubFeedEntry(AUTHOR_1, BASE_URL, DESCRIPTION_1, TITLE_1, DATE)
        SyndEntry entry2 = stubFeedEntry(AUTHOR_2, BASE_URL, null, TITLE_2, DATE)

        getTitle() >> FEED_TITLE
        getLink() >> BASE_URL
        getGenerator() >> BASE_URL

        getEntries() >> [entry1, entry2]
    }

    def "Should convert RSS feed to a JSON content"() {
        when:
            String jsonString = converter.convert(feed).toString()
            Object json = new JsonSlurper().parseText(jsonString)

        then:
            with(json) {
                title == FEED_TITLE
                link == BASE_URL + "/pl/rss"
                generator == BASE_URL

                entries != null
                entries.size() == 2
            }

        and:
            with(json.entries[0]) {
                author == AUTHOR_1
                link == BASE_URL
                description == DESCRIPTION_1
                title == TITLE_1
                date == RssToJsonConverter.DATE_FORMATTER.format(DATE)
            }

        and:
            with(json.entries[1]) {
                author == AUTHOR_2
                link == BASE_URL
                description == null
                title == TITLE_2
                date == RssToJsonConverter.DATE_FORMATTER.format(DATE)
            }
    }

    def stubFeedEntry(String author, String link, String description, String title, LocalDateTime publishedDate) {
        SyndContent content = Stub() {
            getValue() >> description
        }

        return Stub(SyndEntry) {
            getAuthor() >> author
            getDescription() >> content
            getTitle() >> title
            getLink() >> link
            getPublishedDate() >> toDate(publishedDate)
        }
    }
}