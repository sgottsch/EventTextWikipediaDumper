package de.l3s.wikidumper.source.wikidata;

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

public class WikidataTypesMap {

	private Map<String, Set<String>> types = new HashMap<String, Set<String>>();
	private WikidataTransitiveTypes wikidataTransitiveTypes;

	// for efficiency: dictionary of trans. parent types which is filled as soon as
	// transitive parents are computed for an entity
	private Map<String, Set<String>> transitiveParentTypes = new HashMap<String, Set<String>>();

	public void load() {

		wikidataTransitiveTypes = new WikidataTransitiveTypes();
		wikidataTransitiveTypes.load();

		LogUtil logUtil = new LogUtil();
		System.out.println(logUtil.printTimeDiff() + "\tLoad WikidataTypesMap: Start. (" + LogUtil.printMemory() + " bytes).");

		String fileName = Config.getValue("data_folder") + "instanceof-data-relevant.tsv";

		LineIterator it = null;
		try {
			it = FileUtils.lineIterator(new File(fileName), "UTF-8");
			while (it.hasNext()) {
				String line = it.nextLine();
				String[] parts = line.split(" ");
				String entityWikidataId = parts[0];
				String type = parts[1];
				if (!this.types.containsKey(entityWikidataId))
					this.types.put(entityWikidataId, new HashSet<String>());
				types.get(entityWikidataId).add(type);
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

		System.out.println(logUtil.printTimeDiff() + "\tLoad WikidataTypesMap: Done. (" + LogUtil.printMemory() + " bytes).");
	}

	public Set<String> getTypes(String entityWikidataId) {
		Set<String> types = this.types.get(entityWikidataId);
		if (types == null)
			return new HashSet<String>();
		else
			return types;
	}

	public Set<String> getTransitiveTypes(String entityWikidataId) {

		if (transitiveParentTypes.containsKey(entityWikidataId))
			return transitiveParentTypes.get(entityWikidataId);

		Set<String> parents = new HashSet<String>();

		Set<String> transitiveEntityTypes = this.types.get(entityWikidataId);
		if (transitiveEntityTypes != null) {
			for (String targetType : transitiveEntityTypes) {
				parents.addAll(wikidataTransitiveTypes.getTransitiveParentClasses(targetType));
			}
		}
		transitiveParentTypes.put(entityWikidataId, parents);
		return parents;
	}

//	public Set<String> getResourcesOfType(String type) {
//		Set<String> resources = new HashSet<String>();
//		for (String resource : this.types.keySet()) {
//			if (this.types.get(resource).contains(type))
//				resources.add(resource);
//		}
//		return resources;
//	}

}
