package de.l3s.wikidumper.source.wikipedia.mwdumper.articleprocessing;

import java.io.BufferedWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.json.JSONObject;
import org.mediawiki.importer.DumpWriter;
import org.mediawiki.importer.Page;
import org.mediawiki.importer.Revision;
import org.mediawiki.importer.Siteinfo;
import org.mediawiki.importer.Wikiinfo;

import de.l3s.wikidumper.meta.Language;
import de.l3s.wikidumper.source.dbpedia.DBpediaToWikidataMap;
import de.l3s.wikidumper.source.dbpedia.DBpediaTypesMap;
import de.l3s.wikidumper.source.dbpedia.DBpediaTypesMapWDExtended;
import de.l3s.wikidumper.source.dbpedia.RedirectsMap;
import de.l3s.wikidumper.source.wikidata.WikidataEventsDict;
import de.l3s.wikidumper.source.wikidata.WikidataTypesMap;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;

public class ArticleTextExtractor implements DumpWriter {

	String pageTitle = "";
	int _targetPageId;
	int _pageId;
	boolean debug = false;
	String _page = "";
	boolean empty = true;
	String path;
	boolean pageIsMainArticle = false;
	// Set<Integer> _pageIdsWithEvents;
	BufferedWriter fileEvents;
	private int pageCount = 0;

	private Language language;
	private RedirectsMap redirectsMap;
	private DBpediaTypesMap dbpediaTypesMap;
	private WikidataTypesMap wikidataTypesMap;
	private DBpediaToWikidataMap dbpediaToWikidataMap;
	private StanfordCoreNLP nlpPipeline;
	private WikidataEventsDict wikidataEventsDict;
	private DBpediaTypesMapWDExtended dbpediaTypesMapWDExtended;
	private Date previousDate;

	// private Set<String> events;

	protected static final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss.S");

	public void close() throws IOException {
	}

	public ArticleTextExtractor(String pageId, Language language, BufferedWriter fileEvents, RedirectsMap redirectsMap,
			DBpediaTypesMap dbpediaTypesMap, WikidataTypesMap wikidataTypesMap,
			DBpediaToWikidataMap dbpediaToWikidataMap, DBpediaTypesMapWDExtended dbpediaTypesMapWDExtended,
			StanfordCoreNLP nlpPipeline, WikidataEventsDict wikidataEventsDict) {
		this._targetPageId = Integer.parseInt(pageId);
		this.language = language;
		this.fileEvents = fileEvents;

		this.redirectsMap = redirectsMap;
		this.dbpediaTypesMap = dbpediaTypesMap;
		this.wikidataTypesMap = wikidataTypesMap;
		this.dbpediaToWikidataMap = dbpediaToWikidataMap;
		this.dbpediaTypesMapWDExtended = dbpediaTypesMapWDExtended;

		this.nlpPipeline = nlpPipeline;
		this.wikidataEventsDict = wikidataEventsDict;

		// this.events = events;

		// this._pageIdsWithEvents = pageIdsWithEvents;
	}

	public void writeStartWiki(Wikiinfo info) throws IOException {
	}

	public void writeEndWiki() throws IOException {
	}

	public void writeSiteinfo(Siteinfo info) throws IOException {
	}

	public void writeStartPage(Page page) throws IOException {
		this.empty = true;
		this._pageId = page.Id;
		this.pageTitle = page.Title.Text;
		if (page.Ns == 0 && !page.isRedirect) {
			this.pageIsMainArticle = true;
		}
	}

	public void writeEndPage() throws IOException {
		if (this._pageId == this._targetPageId || this._targetPageId == -1) {
			this.fileEvents.flush();
		}
		this.pageIsMainArticle = false;
	}

	public void writeRevision(Revision revision) throws IOException {

		if (this.pageIsMainArticle) {

			if (this.pageCount % 500 == 0) {
				Date currentDate = new Date();

				if (this.previousDate == null) {
					this.previousDate = currentDate;
					System.out.println("Time:\t" + currentDate);
				} else {

					long ms = (currentDate.getTime() - this.previousDate.getTime());
					System.out.println("Time diff:\t" + ms + "ms");

					this.previousDate = currentDate;
				}
			}

			this.pageCount += 1;

			// if (this.events.contains(pageTitle.replace(" ", "_"))) {

			// System.out.println("Wiki Page: " + String.valueOf(this._pageId) + ": " +
			// this.pageTitle);

			TextExtractorNew extractor = new TextExtractorNew(revision.Text, this._pageId, true, language,
					this.pageTitle, this.redirectsMap, this.dbpediaTypesMap, this.wikidataTypesMap,
					this.dbpediaToWikidataMap, this.dbpediaTypesMapWDExtended, this.nlpPipeline,
					this.wikidataEventsDict);
			try {
				extractor.extractLinks();
			} catch (Exception e) {
				System.err.println("Error (a) with " + this._pageId + ": " + this.pageTitle);
				System.err.println(e.getMessage() + "\n" + e.getStackTrace());
			}

			// String prefix = this._pageId + "\t" + this.pageTitle + "\t";

			JSONObject result = extractor.getArticleJSON();
			this.fileEvents.append(result.toString() + "\n");

//				Output output = extractor.getOutput();

			// extract events

//				EventExtractorFromYearPages eventsExtractor = new EventExtractorFromYearPages(revision.Text,
//						this._pageId, this.pageTitle, language, redirects);
//				if (eventsExtractor.isYearOrDayPage()) {
//					System.out.println("Date page: " + String.valueOf(this._pageId) + ": " + this.pageTitle);
//					eventsExtractor.extractEvents();
//					if (!eventsExtractor.getEventsOutput().isEmpty()) {
//						this.fileEvents.append(eventsExtractor.getEventsOutput() + "\n");
//					}
//				}

			// }
		}
	}
}
