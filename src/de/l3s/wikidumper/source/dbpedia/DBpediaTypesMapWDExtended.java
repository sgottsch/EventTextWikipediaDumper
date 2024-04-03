package de.l3s.wikidumper.source.dbpedia;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;

import de.l3s.wikidumper.pipeline.Config;
import de.l3s.wikidumper.source.wikidata.WikidataTypesMap;
import de.l3s.wikidumper.util.LogUtil;

public class DBpediaTypesMapWDExtended {

	private DBPediaTransitiveTypes dbpediaTransitiveTypes;
	private WikidataTypesMap wikidataTypesMap;
	private Map<String, String> dbToWDMap = new HashMap<String, String>();

	private Map<String, Set<String>> wdTypesMap = new HashMap<String, Set<String>>(); // cache for efficiency
	private Map<String, Set<String>> wdTransitiveTypesMap = new HashMap<String, Set<String>>(); // cache for efficiency

	public DBpediaTypesMapWDExtended(DBPediaTransitiveTypes dbpediaTransitiveTypes, WikidataTypesMap wikidataTypesMap) {
		this.dbpediaTransitiveTypes = dbpediaTransitiveTypes;
		this.wikidataTypesMap = wikidataTypesMap;
	}

	public void load() {

		LogUtil logUtil = new LogUtil();
		System.out.println(logUtil.printTimeDiff() + "\tLoad DBpediaTypesMapWDExtended: Start. ("
				+ LogUtil.printMemory() + " bytes).");

		String fileName = Config.getValue("data_folder") + "wd_to_dbp_class.tsv";

		LineIterator it = null;
		try {
			it = FileUtils.lineIterator(new File(fileName), "UTF-8");
			while (it.hasNext()) {
				String line = it.nextLine();
				String[] parts = line.split("\t");
				String wd = parts[0];
				String dbp = parts[1].replace(" ", "_");
				dbToWDMap.put(wd, dbp);
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

		System.out.println(logUtil.printTimeDiff() + "\tLoad DBpediaTypesMapWDExtended: Done. (" + LogUtil.printMemory()
				+ " bytes).");

		System.out.println("\t\tExample -  1970 United States Census (Q1730452): " + getTypes("Q1730452"));
		System.out.println("\t\tExample -  1970 United States Census (Q1730452): " + getTransitiveTypes("Q1730452"));
	}

	public Set<String> getTypes(String wikidataId) {

		if (wdTypesMap.containsKey(wikidataId))
			return wdTypesMap.get(wikidataId);

		Set<String> wikidataTypes = wikidataTypesMap.getTypes(wikidataId);

		Set<String> dbpediaTypes = new HashSet<String>();

		for (String wdType : wikidataTypes) {
			String dbpediaType = dbToWDMap.get(wdType);
			if (dbpediaType != null) {
				dbpediaTypes.add(dbpediaType);
			}
		}

		wdTypesMap.put(wikidataId, dbpediaTypes);

		return dbpediaTypes;
	}

	public Set<String> getTransitiveTypes(String wikidataId) {

		if (wdTransitiveTypesMap.containsKey(wikidataId))
			return wdTransitiveTypesMap.get(wikidataId);

		Set<String> wikidataTypes = wikidataTypesMap.getTransitiveTypes(wikidataId);

		Set<String> dbpediaTypes = new HashSet<String>();

		for (String wdType : wikidataTypes) {
			String dbpediaType = dbToWDMap.get(wdType);
			if (dbpediaType != null) {
				dbpediaTypes.add(dbpediaType);
				dbpediaTypes.addAll(dbpediaTransitiveTypes.getTransitiveParentClasses(dbpediaType));
			}
		}

		wdTransitiveTypesMap.put(wikidataId, dbpediaTypes);

		return dbpediaTypes;
	}

}
