package de.l3s.wikidumper.source.wikidata;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.apache.commons.lang3.StringUtils;

import de.l3s.wikidumper.pipeline.Config;
import de.l3s.wikidumper.util.LogUtil;

public class WikidataEventsDict {

	private int NUMBER_OF_EVENTS = 10000;

	Map<String, String> eventsDict = new HashMap<String, String>();
	private List<String> eventDictSortedByLengthDecreasing;

	public static void main(String[] args) {
		Config.init("config.txt");
		WikidataEventsDict wed = new WikidataEventsDict();
		wed.load();
	}

	public void load() {

		LogUtil logUtil = new LogUtil();
		System.out.println(logUtil.printTimeDiff() + "\tLoad EventDictionary: Start.");

		String fileName = Config.getValue("data_folder") + "most_linked_wikidata_events.tsv";

		LineIterator it = null;
		try {
			it = FileUtils.lineIterator(new File(fileName), "UTF-8");
			while (it.hasNext()) {
				String line = it.nextLine();
				String[] parts = line.split("\t");
				eventsDict.put(StringUtils.strip(parts[0], "\""),
						StringUtils.strip(parts[1], "\"").replace("http://www.wikidata.org/entity/", ""));
				if (eventsDict.keySet().size() > NUMBER_OF_EVENTS)
					break;
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

		System.out.println("\t\tNumber of events in global dictionary: " + this.eventsDict.keySet().size());

		this.eventDictSortedByLengthDecreasing = new ArrayList<String>();
		this.eventDictSortedByLengthDecreasing.addAll(this.eventsDict.keySet());
		Collections.sort(this.eventDictSortedByLengthDecreasing, (a, b) -> Integer.compare(b.length(), a.length()));

		System.out.println("\\t\\tLongest event: " + this.eventDictSortedByLengthDecreasing.get(0) + " -> "
				+ this.eventsDict.get(this.eventDictSortedByLengthDecreasing.get(0)));

		System.out.println(logUtil.printTimeDiff() + "\tLoad EventDictionary: Done.");

	}

	public Map<String, String> getEventsDict() {
		return eventsDict;
	}

	public List<String> getEventDictSortedByLengthDecreasing() {
		return eventDictSortedByLengthDecreasing;
	}

}
