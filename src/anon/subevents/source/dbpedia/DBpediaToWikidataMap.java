package anon.subevents.source.dbpedia;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;

import anon.subevents.pipeline.Config;
import anon.subevents.util.LogUtil;

public class DBpediaToWikidataMap {

	private Map<String, String> dbpediaToWikidataMap = new HashMap<String, String>();

	public void load() {

		LogUtil logUtil = new LogUtil();
		System.out.println(logUtil.printTimeDiff() + "\tLoad DBpedia to Wikidata map: Start. (" + LogUtil.printMemory()
				+ " bytes).");

		String fileName = Config.getValue("data_folder") + "sameas-enwiki.ttl";

		LineIterator it = null;
		try {
			it = FileUtils.lineIterator(new File(fileName), "UTF-8");
			while (it.hasNext()) {
				String line = it.nextLine();
				String[] parts = line.split(" ");
				String wikidata = parts[0];
				String dbpedia = parts[1].replace(" ", "_");
				dbpediaToWikidataMap.put(dbpedia, wikidata);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				it.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		System.out.println(logUtil.printTimeDiff() + "\tLoad DBpedia to Wikidata: Done (" + dbpediaToWikidataMap.size()
				+ " entries). (" + LogUtil.printMemory() + " bytes).");
	}

	public String getWikidataID(String dbpediaId) {

		dbpediaId = dbpediaId.replace(" ", "_");

		return this.dbpediaToWikidataMap.get(dbpediaId); // can be null
	}

}
