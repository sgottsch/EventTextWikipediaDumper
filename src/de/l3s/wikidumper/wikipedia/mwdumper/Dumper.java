package de.l3s.wikidumper.wikipedia.mwdumper;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.sql.Connection;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.Random;

import org.mediawiki.dumper.ProgressFilter;
import org.mediawiki.importer.DumpWriter;
import org.mediawiki.importer.MultiWriter;
import org.mediawiki.importer.XmlDumpReader;

import de.l3s.wikidumper.meta.Language;
import de.l3s.wikidumper.pipeline.Config;
import de.l3s.wikidumper.source.dbpedia.DBpediaToWikidataMap;
import de.l3s.wikidumper.source.dbpedia.DBpediaTypesMap;
import de.l3s.wikidumper.source.dbpedia.DBpediaTypesMapWDExtended;
import de.l3s.wikidumper.source.dbpedia.RedirectsMap;
import de.l3s.wikidumper.source.wikidata.WikidataEventsDict;
import de.l3s.wikidumper.source.wikidata.WikidataTypesMap;
import de.l3s.wikidumper.source.wikipedia.mwdumper.articleprocessing.ArticleTextExtractor;
import de.l3s.wikidumper.wikipedia.WikiWords;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;

class Dumper {

	private static final int IN_BUF_SZ = 1024 * 1024;

	public static void main(String[] args) throws IOException, ParseException {

		if (args.length != 2)
			return;

		Config.init(args[0]);
		MultiWriter writers = new MultiWriter();

		Language language = Language.getLanguage(args[1]);

		List<Language> languages = new ArrayList<Language>();
		languages.add(language);
		WikiWords.getInstance().init(languages);

		RedirectsMap redirectsMap = new RedirectsMap();
		redirectsMap.load();
		DBpediaTypesMap dbpediaTypesMap = new DBpediaTypesMap();
		dbpediaTypesMap.load();
		WikidataTypesMap wikidataTypesMap = new WikidataTypesMap();
		wikidataTypesMap.load();
		DBpediaToWikidataMap dbpediaToWikidataMap = new DBpediaToWikidataMap();
		dbpediaToWikidataMap.load();
		DBpediaTypesMapWDExtended dbpediaTypesMapWDExtended = new DBpediaTypesMapWDExtended(
				dbpediaTypesMap.getDbpediaTransitiveTypes(), wikidataTypesMap);
		dbpediaTypesMapWDExtended.load();

		//Properties props = new Properties();
		//props.setProperty("annotators", "tokenize,pos,lemma,ner");
		StanfordCoreNLP pipeline = null;//new StanfordCoreNLP(props);

		WikidataEventsDict wikidataEventsDict = new WikidataEventsDict();
		wikidataEventsDict.load();

		DumpWriter sink = getEventTextExtractor(language, redirectsMap, dbpediaTypesMap, wikidataTypesMap,
				dbpediaToWikidataMap, dbpediaTypesMapWDExtended, pipeline, wikidataEventsDict);
		InputStream input = openStandardInput();

		writers.add(sink);

		int progressInterval = 1000;
		DumpWriter outputSink = (progressInterval > 0) ? (DumpWriter) new ProgressFilter(writers, progressInterval)
				: (DumpWriter) writers;

		XmlDumpReader reader = new XmlDumpReader(input, outputSink);
		reader.readDump();

		System.out.println("Dumper done.");
	}

	static InputStream openStandardInput() throws IOException {
		return new BufferedInputStream(System.in, IN_BUF_SZ);
	}

	static class OutputWrapper {
		private OutputStream fileStream = null;
		private Connection sqlConnection = null;

		OutputWrapper(OutputStream aFileStream) {
			fileStream = aFileStream;
		}

		OutputWrapper(Connection anSqlConnection) {
			sqlConnection = anSqlConnection;
		}

		OutputStream getFileStream() {
			if (fileStream != null)
				return fileStream;
			if (sqlConnection != null)
				throw new IllegalArgumentException("Expected file stream, got SQL connection?");
			throw new IllegalArgumentException("Have neither file nor SQL connection. Very confused!");
		}

	}

	private static ArticleTextExtractor getEventTextExtractor(Language language, RedirectsMap redirectsMap,
			DBpediaTypesMap dbpediaTypesMap, WikidataTypesMap wikidataTypesMap,
			DBpediaToWikidataMap dbpediaToWikidataMap, DBpediaTypesMapWDExtended dbpediaTypesMapWDExtended,
			StanfordCoreNLP pipeline, WikidataEventsDict wikidataEventsDict) {
		BufferedWriter fileEvents = null;

		try {
			Date currentDate = new Date();
			Random random = new Random();
			long LOWER_RANGE = 0L;
			long UPPER_RANGE = 999999999L;
			long randomValue = LOWER_RANGE + (long) (random.nextDouble() * (UPPER_RANGE - LOWER_RANGE));
			String pathSuffix = language + "_" + new SimpleDateFormat("yyyyMMddhhmm").format(new Date()) + "_"
					+ currentDate.getTime() + "_" + String.valueOf(randomValue) + ".ndjson";
			String path1 = Config.getValue("data_folder") + "articles-" + pathSuffix;
			System.out.println(path1);
			fileEvents = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(path1, false), "UTF-8"));
		} catch (Exception e) {
			e.printStackTrace();
		}

		// Set<String> events = null; // typesMap.getResourcesOfType("Event");
		// System.out.println("Number of events: " + events.size() + ".");

		return new ArticleTextExtractor("-1", language, fileEvents, redirectsMap, dbpediaTypesMap, wikidataTypesMap,
				dbpediaToWikidataMap, dbpediaTypesMapWDExtended, pipeline, wikidataEventsDict);
	}

}
