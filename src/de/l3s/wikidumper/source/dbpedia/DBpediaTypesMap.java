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

public class DBpediaTypesMap {

	private Map<String, Set<String>> types = new HashMap<String, Set<String>>();
	private DBPediaTransitiveTypes dbpediaTransitiveTypes;

	public void load() {

		this.dbpediaTransitiveTypes = new DBPediaTransitiveTypes();
		this.dbpediaTransitiveTypes.load();

		LogUtil logUtil = new LogUtil();
		System.out.println(logUtil.printTimeDiff() + "\tLoad TypesMap: Start. (" + LogUtil.printMemory() + " bytes).");

		String fileName = Config.getValue("data_folder") + "dbpedia_types_specific.ttl";

		LineIterator it = null;
		try {
			it = FileUtils.lineIterator(new File(fileName), "UTF-8");
			while (it.hasNext()) {
				String line = it.nextLine();
				String[] parts = line.split(" ");
				String entity = parts[0].replace(" ", "_");
				;
				String type = parts[1];
				if (!this.types.containsKey(entity))
					this.types.put(entity, new HashSet<String>());
				types.get(entity).add(type);
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

		System.out.println(logUtil.printTimeDiff() + "\tLoad TypesMap: Done. (" + LogUtil.printMemory() + " bytes).");
	}

	public Set<String> getTypes(String entity) {
		entity = entity.replace(" ", "_");

		Set<String> entityTypes = this.types.get(entity);
		if (entityTypes == null)
			return new HashSet<String>();
		else
			return entityTypes;
	}

	public Set<String> getTransitiveTypes(String entity) {
		entity = entity.replace(" ", "_");

		Set<String> entityTypes = this.types.get(entity);
		if (entityTypes == null)
			return new HashSet<String>();
		else {
			Set<String> parents = new HashSet<String>();
			for (String entityType : entityTypes) {
				parents.addAll(dbpediaTransitiveTypes.getTransitiveParentClasses(entityType));
			}
			return parents;
		}
	}

	public DBPediaTransitiveTypes getDbpediaTransitiveTypes() {
		return dbpediaTransitiveTypes;
	}

//	public Set<String> getEntitiesOfType(String type) {
//		Set<String> entities = new HashSet<String>();
//		for (String resource : this.types.keySet()) {
//			if (this.types.get(resource).contains(type))
//				entities.add(resource);
//		}
//		return entities;
//	}

}
