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
import de.l3s.wikidumper.util.LogUtil;

public class DBPediaTransitiveTypes {

	public static void main(String[] args) {
		DBPediaTransitiveTypes dtt = new DBPediaTransitiveTypes();
		Config.init("config.txt");
		dtt.load();
	}

	private Map<String, Set<String>> parentClasses = new HashMap<String, Set<String>>();

	public void load() {

		LogUtil logUtil = new LogUtil();
		System.out.println(logUtil.printTimeDiff() + "\tLoad TypesMap: Start. (" + LogUtil.printMemory() + " bytes).");

		/*
		 * select distinct ?a ?c WHERE { ?a rdfs:subClassOf ?c . FILTER
		 * (strstarts(str(?a), "http://dbpedia.org/ontology/")) . FILTER
		 * (strstarts(str(?c), "http://dbpedia.org/ontology/")) . }
		 * 
		 */

		String fileName = Config.getValue("data_folder") + "dbpedia_subclasses.csv";

		LineIterator it = null;
		try {
			it = FileUtils.lineIterator(new File(fileName), "UTF-8");
			while (it.hasNext()) {
				String line = it.nextLine();
				String[] parts = line.split(" ");
				String child = parts[0];
				String parent = parts[1];

				if (!parentClasses.containsKey(child)) {
					parentClasses.put(child, new HashSet<String>());
					parentClasses.get(child).add(child);
				}

				parentClasses.get(child).add(parent);
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

		makeTransitive();

		System.out.println(logUtil.printTimeDiff() + "\tLoad DBpedia subclasses: Done. (" + LogUtil.printMemory() + " bytes).");
		System.out.println("\t\tExample, transitive parents of City: " + getTransitiveParentClasses("City"));
//		System.out
//				.println("Example, transitive parents of TennisPlayer: " + getTransitiveParentClasses("TennisPlayer"));
		System.out.println("\t\tExample, transitive children of Race: " + getTransitiveChildClasses("Race"));

	}

	private void makeTransitive() {

		boolean changed = true;

		while (changed) {
			changed = false;
			for (String child : parentClasses.keySet()) {
				Set<String> newParents = new HashSet<String>();
				newParents.add(child);
				for (String parent : parentClasses.get(child)) {
					newParents.add(parent);
					if (parentClasses.containsKey(parent))
						newParents.addAll(parentClasses.get(parent));
				}
				if (this.parentClasses.get(child).size() != newParents.size()) {
					changed = true;
					parentClasses.get(child).addAll(newParents);
				}
			}
		}

	}

	public Set<String> getTransitiveParentClasses(String childClass) {
		if (parentClasses.containsKey(childClass))
			return parentClasses.get(childClass);
		else {
			// create a set just with the class itself
			Set<String> parents = new HashSet<String>();
			parents.add(childClass);
			parentClasses.put(childClass, parents);
			return parents;
		}
	}

	public Set<String> getTransitiveChildClasses(String parentClass) {
		Set<String> children = new HashSet<String>();
		children.add(parentClass);
		for (String child : this.parentClasses.keySet()) {
			if (getTransitiveParentClasses(child).contains(parentClass))
				children.add(child);
		}
		return children;
	}

}
