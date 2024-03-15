package anon.subevents.source.wikidata;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.apache.commons.lang3.StringUtils;

import anon.subevents.pipeline.Config;
import anon.subevents.util.LogUtil;

public class WikidataTransitiveTypes {

	private Map<String, Set<String>> parentClassesForWriting;
	private Map<String, String> parentClasses;

	public static void main(String[] args) {
		WikidataTransitiveTypes dtt = new WikidataTransitiveTypes();
		Config.init("config.txt");
		dtt.load(true);
		System.out.println("Example, transitive parents of world war: " + dtt.parentClasses.get("Q103495"));

		// dtt.writeTransitiveTypes();
	}

	private void writeTransitiveTypes() {
		load(false);

		System.out.println("Example, parents of world war: " + parentClasses.get("Q103495"));

		System.out.println("Make transitive");
		makeTransitive();

		System.out.println("Load Wikidata subclasses: Done.");

		String fileName = Config.getValue("data_folder") + "wikidata_subclasses_transitive.csv";
		PrintWriter printWriter = null;
		try {
			printWriter = new PrintWriter(new File(fileName), "UTF-8");
			for (String child : parentClassesForWriting.keySet()) {
				printWriter.write(child + "\t" + StringUtils.join(parentClassesForWriting.get(child), ",") + "\n");
			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} finally {
			printWriter.close();
		}

	}

	public void load() {
		load(true);
	}

	public void load(boolean loadTransitiveFromFile) {

		LogUtil logUtil = new LogUtil();
		System.out.println(logUtil.printTimeDiff() + "\tLoad WikidataTransitiveTypes (" + loadTransitiveFromFile
				+ "): Start. (" + LogUtil.printMemory() + " bytes).");
		String fileName = Config.getValue("data_folder");

		if (loadTransitiveFromFile) {
			fileName += "wikidata_subclasses_transitive.csv";
			parentClasses = new HashMap<String, String>();
		} else {
			fileName += "wikidata_subclasses.tsv";
			parentClassesForWriting = new HashMap<String, Set<String>>();
		}

		LineIterator it = null;
		try {
			it = FileUtils.lineIterator(new File(fileName), "UTF-8");
			while (it.hasNext()) {

				String line = it.nextLine();
				String[] parts = line.split("\t");
				String child = parts[0];
				String parentClassesOfEntity = parts[1];

				if (loadTransitiveFromFile)
					parentClasses.put(child, parentClassesOfEntity);
				else {
					if (!parentClassesForWriting.containsKey(child)) {
						parentClassesForWriting.put(child, new HashSet<String>());
					}
					parentClassesForWriting.get(child).add(parentClassesOfEntity);
				}
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

		System.out.println(logUtil.printTimeDiff() + "\tLoad WikidataTransitiveTypes (" + loadTransitiveFromFile
				+ "): Done. (" + LogUtil.printMemory() + " bytes).");

		if (loadTransitiveFromFile)
			System.out.println("\t\tExample, transitive parents of social phenomenon: " + parentClasses.get("Q602884"));
	}

	private void makeTransitive() {

		Map<String, Set<String>> newParentClasses = new HashMap<String, Set<String>>();

		for (String child : parentClassesForWriting.keySet()) {
			Set<String> parents = new HashSet<>();
			Set<String> done = new HashSet<>();
			addParents(child, parents, done);
			parents.add(child);
			newParentClasses.put(child, parents);
		}

		this.parentClassesForWriting = newParentClasses;

	}

	private void addParents(String child, Set<String> parents, Set<String> done) {
		if (done.contains(child))
			return;
		done.add(child);
		Set<String> newParents = new HashSet<String>();
		if (parentClassesForWriting.containsKey(child)) {
			for (String parent : parentClassesForWriting.get(child)) {
				newParents.add(parent);
				addParents(parent, parents, done);
			}
		}
		parents.addAll(newParents);
	}

	public Set<String> getTransitiveParentClasses(String child) {
		Set<String> parents = new HashSet<String>();
		if (parentClasses.containsKey(child)) {
			for (String parent : parentClasses.get(child).split(","))
				parents.add(parent);
			return parents;
		} else {
			parents.add(child);
			return parents;
		}
	}

}
