package de.l3s.wikidumper.source.wikidata;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;

import de.l3s.wikidumper.pipeline.Config;

public class WikidataLinksReducer {

	public static void main(String[] args) {
		
		Config.init(args[0]);

		Set<String> wikidataIds = new HashSet<String>();

		System.out.println("Load relevant Wikidata IDs: Start.");

		String fileName = Config.getValue("data_folder") + "sameas-enwiki.ttl";

		LineIterator it = null;
		try {
			it = FileUtils.lineIterator(new File(fileName), "UTF-8");
			while (it.hasNext()) {
				String line = it.nextLine();
				String[] parts = line.split(" ");
				String wikidata = parts[0];
				wikidataIds.add(wikidata);
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

		System.out.println("Load relevant Wikidata IDs: Done (" + wikidataIds.size() + " entries).");

		System.out.println("Load WikidataLinksReducer: Start.");

		String fileName2 = Config.getValue("data_folder") + "instanceof-data.tsv";

		String fileNameOut = Config.getValue("data_folder") + "instanceof-data-relevant.tsv";
		PrintWriter printWriter = null;
		try {
			printWriter = new PrintWriter(new File(fileNameOut), "UTF-8");

			LineIterator it2 = null;
			try {
				it2 = FileUtils.lineIterator(new File(fileName2), "UTF-8");
				while (it2.hasNext()) {
					String line = it2.nextLine();
					String[] parts = line.split("\t");
					String resource = parts[0];

					if (wikidataIds.contains(resource))
						printWriter.print(parts[0] + " " + parts[2] + "\n");
				}
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				try {
					it2.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		} catch (FileNotFoundException | UnsupportedEncodingException e1) {
			e1.printStackTrace();
		} finally {
			printWriter.close();
		}

		System.out.println("Load WikidataLinksReducer: Done.");

	}

}
