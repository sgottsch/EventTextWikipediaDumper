package anon.subevents.source.wikipedia.mwdumper.articleprocessing;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import anon.subevents.meta.Language;
import anon.subevents.nlp.NLPUtils;
import anon.subevents.nlp.OpenNLPOrBreakIteratorNLPUtils;
import anon.subevents.pipeline.Config;
import anon.subevents.source.dbpedia.DBpediaToWikidataMap;
import anon.subevents.source.dbpedia.DBpediaTypesMap;
import anon.subevents.source.dbpedia.DBpediaTypesMapWDExtended;
import anon.subevents.source.dbpedia.RedirectsMap;
import anon.subevents.source.wikidata.WikidataEventsDict;
import anon.subevents.source.wikidata.WikidataTypesMap;
import anon.subevents.source.wikipedia.mwdumper.model.Annotation;
import anon.subevents.source.wikipedia.mwdumper.model.Link;
import anon.subevents.source.wikipedia.mwdumper.model.Paragraph;
import anon.subevents.source.wikipedia.mwdumper.model.Reference;
import anon.subevents.source.wikipedia.mwdumper.model.Sentence;
import anon.subevents.wikipedia.WikiWords;
import edu.stanford.nlp.ling.CoreAnnotations.NormalizedNamedEntityTagAnnotation;
import edu.stanford.nlp.pipeline.CoreDocument;
import edu.stanford.nlp.pipeline.CoreEntityMention;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import opennlp.tools.util.Span;

public class TextExtractorNew {

	private static final String LINE_START_TO_REMOVE = "#!#LINE_START_TO_REMOVE#!#";
	private static final boolean ONLY_MATCH_COMPLETE_WORDS_AS_LINKS = true;
	private String text;
	private int pageId;
	private String pageTitle;
	private Paragraph articleParagraph;
	// private PageIdFinder linkPageIdFinder;

	private int textParagraphId = 0;
	private int textParagraphStartPosition = 0;
//	private Paragraph topParagraph;
//	private boolean splitTextIntoSections = true;
	private NLPUtils nlpUtils;

	private RedirectsMap redirectsMap;
	private DBpediaTypesMap dbpediaTypesMap;
	private WikidataTypesMap wikidataTypesMap;
	private DBpediaToWikidataMap dbpediaToWikidataMap;

	private List<Sentence> sentences = new ArrayList<Sentence>();

	private Output output = new Output();

	private Map<String, Map<String, Integer>> anchorTextsToLinkNames = new HashMap<String, Map<String, Integer>>();

	private Map<String, String> resolvedLinkMap = new HashMap<String, String>();
	private List<String> anchorTextSortedByLengthDecreasing;

	private WikidataEventsDict wikidataEventsDict;

	private Pattern patternRoundBrackets = Pattern.compile("\\(.*?\\)");
	private Pattern patternSquareBrackets = Pattern.compile("\\[.*?\\]");
	private Language language;
	private StanfordCoreNLP nlpPipeline;
	private DBpediaTypesMapWDExtended dbpediaTypesMapWDExtended;

	public static void main(String[] args) throws IOException {

		// int id = 26913444;

		// int id = 25433;
		// int id = 1833593;
		// int id = 44499465;

		// int id = 737; // Afghanistan

		Properties props = new Properties();
		props.setProperty("annotators", "tokenize,pos,lemma,ner");
		StanfordCoreNLP pipeline = new StanfordCoreNLP(props);

		Config.init("config.txt");
		List<Language> languages = new ArrayList<Language>();
		languages.add(Language.EN);
		WikiWords.getInstance().init(languages);

		Map<Language, Map<Integer, String>> exampleTexts = new HashMap<Language, Map<Integer, String>>();

		Map<Integer, String> enMap = new HashMap<Integer, String>();
		enMap.put(72578, "Joab");
		enMap.put(737, "Afghanistan");
		enMap.put(58478, "Airborne forces");
		enMap.put(41915, "Primary market");
		enMap.put(76216, "List of regions of Quebec");
		enMap.put(79266, "Armor-piercing shell");
		enMap.put(53347099, "Green Light (Lorde song)");
		enMap.put(2175, "Cuisine of the United States");
		enMap.put(2604, "Abated");
		enMap.put(18224, "Known Space");
		enMap.put(18749714, "International rankings of Costa Rica");
		enMap.put(1223569, "William Griffiths (VC)");
		enMap.put(26913444, "Ajloun National Private University");
		enMap.put(2102605, "Jun Akiyama");
		enMap.put(66612294, "2021 Myanmar protests");
		enMap.put(17743204, "2010 FIFA Club World Cup");
		enMap.put(55237622, "1978 United States Senate election in Louisiana");
		enMap.put(311608, "Battle of Latakia");
		enMap.put(255296, "Battle of Xiaoting");

		exampleTexts.put(Language.EN, enMap);

//		Map<Integer, String> deMap = new HashMap<Integer, String>();
//		deMap.put(83, "Al Pacino");
//		deMap.put(5909, "1957");
//		deMap.put(302, "Assoziativgesetz");
//		deMap.put(163, "AikidÃ…ï¿½");
//		deMap.put(83544, "Schlacht um Verdun");
//		deMap.put(1082774, "Skulpturenmeile (Hannover)");
//		deMap.put(1624, "Freie Demokratische Partei");
//		exampleTexts.put(Language.DE, deMap);
//
//		Map<Integer, String> ruMap = new HashMap<Integer, String>();
//		ruMap.put(46992, "Ã�Å¸Ã�Â°Ã‘â€¡Ã�Â¸Ã�Â½Ã�Â¾, Ã�ï¿½Ã�Â»Ã‘Å’");
//		ruMap.put(2647, "1957 Ã�Â³Ã�Â¾Ã�Â´");
//		exampleTexts.put(Language.RU, ruMap);
//
//		Map<Integer, String> frMap = new HashMap<Integer, String>();
//		frMap.put(32864, "Ronald Reagan");
//		frMap.put(3506, "1957");
//		exampleTexts.put(Language.FR, frMap);
//
//		Map<Integer, String> ptMap = new HashMap<Integer, String>();
//		ptMap.put(13541, "Ronald Reagan");
//		ptMap.put(24532, "2007");
//		ptMap.put(327699, "Lista de filmes de terror");
//		exampleTexts.put(Language.PT, ptMap);
//
//		Map<Integer, String> itMap = new HashMap<Integer, String>();
//		itMap.put(4714697, "Barack Obama");
//		itMap.put(3862, "Seconda guerra mondiale");
//		exampleTexts.put(Language.IT, itMap);
//
//		Map<Integer, String> daMap = new HashMap<Integer, String>();
//		daMap.put(71, "GNU Free Documentation License");
//		exampleTexts.put(Language.DA, daMap);

		WikidataEventsDict wikidataEventsDict = new WikidataEventsDict();
		wikidataEventsDict.load();

		Language language = Language.EN;
		int id = 311608;

		languages.clear();
		languages.add(language);
		WikiWords.getInstance().init(languages);

		String text = IOUtils.toString(
				TextExtractorNew.class.getResourceAsStream("/resource/wikipage/" + language.getLanguage() + "/" + id),
				"UTF-8");
		// PageIdFinderTrove linkPageIdFinder = new PageIdFinderTrove();

		RedirectsMap redirectsMap = new RedirectsMap();
		DBpediaTypesMap dbpediaTypesMap = new DBpediaTypesMap();
		WikidataTypesMap wikidataTypesMap = new WikidataTypesMap();
		DBpediaToWikidataMap dbpediaToWikidataMap = new DBpediaToWikidataMap();
		DBpediaTypesMapWDExtended dbpediaTypesMapWDExtended = new DBpediaTypesMapWDExtended(null, wikidataTypesMap);

		TextExtractorNew extr = new TextExtractorNew(text, id, true, language, exampleTexts.get(language).get(id),
				redirectsMap, dbpediaTypesMap, wikidataTypesMap, dbpediaToWikidataMap, dbpediaTypesMapWDExtended,
				pipeline, wikidataEventsDict);

		languages.add(language);
		WikiWords.getInstance().init(languages);
		// String title = "Barack Obama";
		id = 0;
		// String text = EventExtractorFromYearPages.getWikipediaArticleText(title,
		// language);
//		TextExtractorNew extr = new TextExtractorNew(text, id, true, language, title,
//				RedirectsTableCreator.getRedirectsDummy(language));

		extr.extractLinks();

		// System.out.println(extr.output.getFirstSentence());
		// System.out.println(extr.output.getLinkSetsInLines());
		// for (Set<Link> links : extr.output.getLinksInCommonSentences()) {
		// Set<String> linkNames = new HashSet<String>();
		// for (Link link : links)
		// linkNames.add(link.getName());
		// System.out.println(StringUtils.join(linkNames, " "));
		// }

//		for (Sentence sentence : extr.sentences) {
//			System.out.println(sentence.getText());
//			for (Link link : sentence.getLinks()) {
//				// System.out.println(
//				// "\t" + link.getName() + " | " +
//				// sentence.getText().substring(link.getStart(),
//				// link.getEnd()));
//				System.out.println("\t" + link.getName());
//			}
//		}

		JSONObject resJSON = extr.getArticleJSON();

		System.out.println(resJSON);

//		int high = 0;
//		String str = null;
//		for (String s : extr.output.getEnrichedCounts().keySet()) {
//			int cnt = extr.output.getEnrichedCounts().get(s);
//			if (cnt > high) {
//				str = s;
//				high = cnt;
//			}
//		}
//
//		System.out.println("Most frequent: " + str + " -> " + high);
	}

	public JSONObject getArticleJSON() {

		JSONObject articleJSON = new JSONObject();

		JSONArray typesJSON = new JSONArray();
		for (String type : dbpediaTypesMap.getTypes(pageTitle)) {
			typesJSON.put(type);
		}
		articleJSON.put("dbpedia_types", typesJSON);

		JSONArray allParentTypesJSON = new JSONArray();
		for (String type : dbpediaTypesMap.getTransitiveTypes(pageTitle)) {
			allParentTypesJSON.put(type);
		}
		articleJSON.put("all_dbpedia_parent_types", allParentTypesJSON);

		articleJSON.put("name", this.pageTitle);
		articleJSON.put("dbpedia_id", this.pageTitle.replace(" ", "_"));

		articleJSON.put("id", this.pageId);
		articleJSON.put("language", this.language);

		String wikidataId = dbpediaToWikidataMap.getWikidataID(this.pageTitle);
		if (wikidataId != null) {
			articleJSON.put("wikidata_id", wikidataId);

			if (this.wikidataTypesMap.getTypes(wikidataId) != null) {
				articleJSON.put("wikidata_types", this.wikidataTypesMap.getTypes(wikidataId));
				articleJSON.put("all_wikidata_parent_types", this.wikidataTypesMap.getTransitiveTypes(wikidataId));
			}

			articleJSON.put("dbpedia_wikidata_enriched_types", dbpediaTypesMapWDExtended.getTypes(wikidataId));
			articleJSON.put("all_dbpedia_wikidata_enriched_parent_types",
					dbpediaTypesMapWDExtended.getTransitiveTypes(wikidataId));
		}

		articleJSON.put("paragraphs", printParagraph(articleParagraph, ""));

		return articleJSON;
	}

	public JSONObject printParagraph(Paragraph p, String indent) {

		JSONObject paragraphJSON = new JSONObject();
		paragraphJSON.put("title", p.getTitle());
		paragraphJSON.put("completeTitle", p.getCompleteTitle());

		if (!p.getSentences().isEmpty()) {
			JSONArray sentencesJSON = new JSONArray();
			paragraphJSON.put("sentences", sentencesJSON);

			for (Sentence sentence : p.getSentences()) {
				JSONObject sentenceJSON = new JSONObject();
				sentenceJSON.put("text", sentence.getText());
				sentencesJSON.put(sentenceJSON);

				JSONArray linksJSON = new JSONArray();
				sentenceJSON.put("links", linksJSON);

				for (Link link : sentence.getLinks()) {

					// System.out.println(link.isEnriched()+"->"+sentence.getText().substring(link.getStart(),link.getEnd()));

					JSONObject linkJSON = new JSONObject();
					linkJSON.put("start", link.getStart());
					linkJSON.put("end", link.getEnd());
					linkJSON.put("anchor_text", link.getAnchorText());

					linkJSON.put("enriched", link.isEnriched());

					if (this.wikidataEventsDict != null)
						linkJSON.put("enriched_globally", link.isEnrichedGlobally());

					String redirectedLink = redirectsMap.getRedirectedPage(link.getName());
					linkJSON.put("dbpedia_id", redirectedLink);

					JSONArray typesJSON = new JSONArray();
					for (String type : dbpediaTypesMap.getTypes(redirectedLink)) {
						typesJSON.put(type);
					}
					linkJSON.put("dbpedia_types", typesJSON);

					JSONArray allParentTypesJSON = new JSONArray();
					for (String type : dbpediaTypesMap.getTransitiveTypes(redirectedLink)) {
						allParentTypesJSON.put(type);
					}
					linkJSON.put("all_dbpedia_parent_types", allParentTypesJSON);

					String wikidataId = dbpediaToWikidataMap.getWikidataID(redirectedLink);
					if (wikidataId != null) {
						linkJSON.put("wikidata_id", wikidataId);

						if (this.wikidataTypesMap.getTypes(wikidataId) != null) {
							linkJSON.put("wikidata_types", this.wikidataTypesMap.getTypes(wikidataId));
							linkJSON.put("all_wikidata_parent_types",
									this.wikidataTypesMap.getTransitiveTypes(wikidataId));
						}

						linkJSON.put("dbpedia_wikidata_enriched_types", dbpediaTypesMapWDExtended.getTypes(wikidataId));
						linkJSON.put("all_dbpedia_wikidata_enriched_parent_types",
								dbpediaTypesMapWDExtended.getTransitiveTypes(wikidataId));

					}

					linksJSON.put(linkJSON);
				}

				if (this.nlpPipeline != null) {
					JSONArray annotationsJSON = new JSONArray();
					sentenceJSON.put("annotations", annotationsJSON);
					for (Annotation annotation : sentence.getAnnotations()) {
						JSONObject annotationJSON = new JSONObject();
						annotationJSON.put("start", annotation.getStart());
						annotationJSON.put("end", annotation.getEnd());
						annotationJSON.put("text", annotation.getText());
						annotationJSON.put("type", annotation.getType());
						annotationJSON.put("normalised_value", annotation.getNormalisedValue());
						annotationsJSON.put(annotationJSON);
					}
				}

			}
		}

		if (!p.getSubParagraphs().isEmpty()) {
			JSONArray subParagraphsJSON = new JSONArray();
			paragraphJSON.put("paragraphs", subParagraphsJSON);

			for (Paragraph pSub : p.getSubParagraphs())
				subParagraphsJSON.put(printParagraph(pSub, indent + " "));
		}

		return paragraphJSON;
	}

	public TextExtractorNew(String text, int pageId, boolean splitTextIntoSections, Language language, String pageTitle,
			RedirectsMap redirectsMap, DBpediaTypesMap dbpediaTypesMap, WikidataTypesMap wikidataTypesMap,
			DBpediaToWikidataMap dbpediaToWikidataMap, DBpediaTypesMapWDExtended dbpediaTypesMapWDExtended,
			StanfordCoreNLP nlpPipeline, WikidataEventsDict wikidataEventsDict) {

		this.redirectsMap = redirectsMap;
		this.dbpediaTypesMap = dbpediaTypesMap;
		this.wikidataTypesMap = wikidataTypesMap;
		this.dbpediaToWikidataMap = dbpediaToWikidataMap;
		this.dbpediaTypesMapWDExtended = dbpediaTypesMapWDExtended;

		this.nlpPipeline = nlpPipeline;
		this.wikidataEventsDict = wikidataEventsDict;

		text = "== START ==\n" + text;
		text = StringEscapeUtils.unescapeHtml4(text);

		try {
			this.nlpUtils = new OpenNLPOrBreakIteratorNLPUtils(language);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		this.text = text;
		this.pageId = pageId;
		this.pageTitle = pageTitle;
		this.language = language;
		// this.splitTextIntoSections = splitTextIntoSections;
	}

	public void extractLinks() {
		this.removeFurtherLinks();

		this.text = this.text.replace("{{nbs}}", " ");

		this.removeTemplates();

		// ":''" expressions are often unneeded lines like ":''"Primary market"
		// may also refer ''111'' to"
		this.text = this.text.replaceAll(":''", LINE_START_TO_REMOVE);

		this.text = this.text.replaceAll("'''", "");
		this.text = this.text.replaceAll("''", "");
		this.text = this.text.replaceAll("__NOTOC__", "").trim();
		this.text = this.text.replaceAll("&nbsp;", " ");
		this.text = this.text.replaceAll("&ndash;", "Ã¢â‚¬â€œ");
		this.text = this.text.replaceAll("&mdash;", "Ã¢â‚¬â€�");

		// this.mergeLines();
		this.divideIntoParagraphs();
		this.removeReferencesFromParagraphs(this.articleParagraph);

		this.extractLinksFromParagraphs(this.articleParagraph);
		// this.createCSVString(this.articleParagraph);
		resolveLinkMapStrict();
		extractSentencesAndLinks();
	}

	// private String createText(String text, Paragraph paragraph) {
	//
	// if (paragraph.getText() != null) {
	// if (!paragraph.getText().startsWith("[[" +
	// WikiWords2.getInstance().getFileLabel(this.language) + ":"))
	// text += paragraph.getText() + "\n";
	// paragraph.getLinks().clear();
	// }
	//
	// for (Paragraph sub : paragraph.getSubParagraphs()) {
	// text = createText(text, sub);
	// }
	//
	// return text;
	// }

	private void removeReferencesFromParagraphs(Paragraph paragraph) {
		if (paragraph.getText() != null) {
			this.removeReferencesFromParagraph(paragraph);
		}
		for (Paragraph subParagraph : paragraph.getSubParagraphs()) {
			this.removeReferencesFromParagraphs(subParagraph);
		}
	}

	private void removeReferencesFromParagraph(Paragraph paragraph) {
		paragraph.setText(
				ReferenceAndTemplateRemover.getInstance(language).removeReferences(paragraph, paragraph.getText()));
	}

	private void removeTemplates() {
		this.text = ReferenceAndTemplateRemover.getInstance(language).removeTemplates(this.text);
	}

	private void removeFurtherLinks() {
		Pattern p = ReferenceAndTemplateRemover.getInstance(language).getFurtherPagesPattern();
		try {
			Matcher m = p.matcher(this.text);
			StringBuffer sb = new StringBuffer();
			while (m.find()) {
				String replaceMent = "";
				m.appendReplacement(sb, Matcher.quoteReplacement(replaceMent));
			}
			m.appendTail(sb);
			this.text = sb.toString();
		} catch (IndexOutOfBoundsException e) {
			System.err.println("Error for page " + this.pageId + ": " + e.getMessage());
			e.printStackTrace();
		}
	}

	// private void mergeLines() {
	// ArrayList<String> lines = new ArrayList<String>();
	// String currentLine = "";
	// for (String line : this.text.split("\n")) {
	// if (line.startsWith(LINE_START_TO_REMOVE))
	// continue;
	// if ((line = line.replaceAll("\n", "").replaceAll("\r",
	// "")).startsWith("==") && line.endsWith("==")) {
	// if (!currentLine.replaceAll("\n", "").replaceAll("\r",
	// "").trim().isEmpty()) {
	// lines.add(currentLine);
	// }
	// currentLine = "";
	// lines.add(line);
	// } else if (line.isEmpty()) {
	// if (!currentLine.replaceAll("\n", "").replaceAll("\r",
	// "").trim().isEmpty()) {
	// lines.add(currentLine);
	// }
	// currentLine = "";
	// } else {
	// currentLine = line.endsWith("}}") || line.endsWith("|}") ||
	// line.endsWith("]]")
	// ? String.valueOf(currentLine) + line + "\n" : String.valueOf(currentLine)
	// + line;
	// }
	// }
	// lines.add(currentLine);
	// this.text = StringUtils.join(lines, "\n\n");
	// }

	private void divideIntoParagraphs() {
		int id = 0;
		this.articleParagraph = new Paragraph(null, "A", id);
		id += 1;
		this.articleParagraph.setLevel(1);
		Paragraph currentParagraph = this.articleParagraph;
		int levelBefore = 0;

		for (String line : this.text.split("\n")) {
			line = line.trim();

			if (line.startsWith(LINE_START_TO_REMOVE))
				continue;
			line = line.replaceAll(LINE_START_TO_REMOVE, "");

			if (!line.isEmpty()) {
				if (line.trim().startsWith("==") && line.endsWith("==")) {
					String title = line.replaceAll("^=+", "");
					int level = line.length() - title.length() - 1;
					if (level > levelBefore + 1) {
						level = levelBefore + 1;
					}
					levelBefore = level;
					title = title.replaceAll("=+$", "").trim();
					if (WikiWords.getInstance().getTitlesOfParagraphsNotToRead(language).contains(title)) {
						break;
					}
					// if (title.equals("Images"))
					// continue;
					Paragraph paragraph = new Paragraph(null, title, id);
					id += 1;
					paragraph.setLevel(level + 1);
					try {
						paragraph.setTopParagraph(currentParagraph.getTopParagraphAtLevel(level));
					} catch (NullPointerException e) {
						System.err.println("Error (b) with page " + this.pageId + ": " + title);
						System.err.println(e.getMessage() + "\n" + e.getStackTrace());
					}
					paragraph.getTopParagraph().addSubParagraph(paragraph);
					currentParagraph = paragraph;
				} else {
					if (line.startsWith("{{quote|") && line.endsWith("}}")) {
						line = line.substring("{{quote|".length(), line.length() - 2);
					}
					if (line.startsWith("[[" + WikiWords.getInstance().getCategoryLabel(language) + ":")) {
						continue;
					}
					if (line.startsWith("[[" + WikiWords.getInstance().getTemplateLabel(language) + ":"))
						continue;

					// Lists should consistently start with "* "
					if (line.length() > 1 && line.startsWith("*") && line.charAt(1) != ' ')
						line = "* " + line.substring(1);
					if (line.length() > 1 && line.startsWith("#") && line.charAt(1) != ' ')
						line = "# " + line.substring(1);
					if (line.length() > 1 && line.startsWith(";") && line.charAt(1) != ' ')
						line = "; " + line.substring(1);

					Paragraph pParagraph = new Paragraph(line, "P", id);
					id += 1;
					currentParagraph.addSubParagraph(pParagraph);
					pParagraph.setTopParagraph(currentParagraph);
					pParagraph.setLevel(currentParagraph.getLevel() + 1);
				}
			}
		}
	}

	private void extractLinksFromParagraphs(Paragraph paragraph) {
		if (paragraph.getText() != null) {
			this.extractLinksFromParagraph(paragraph);
		}
		for (Paragraph subParagraph : paragraph.getSubParagraphs()) {
			this.extractLinksFromParagraphs(subParagraph);
		}
	}

	private void extractLinksFromParagraph(Paragraph paragraph) {

		boolean changed = true;
		while (changed) {
			changed = false;
			int offset = 0;
			Pattern p = ReferenceAndTemplateRemover.getInstance(language).getLinksFindPattern();
			Matcher m = p.matcher(paragraph.getText());

			StringBuffer sb = new StringBuffer();
			while (m.find()) {
				String linkName;
				changed = true;
				String anchorText = linkName = m.group().substring(2, m.group().length() - 2);

				boolean starts = false;
				for (String label : WikiWords.getInstance().getFileLabel(language))
					if (linkName.startsWith(label + ":"))
						starts = true;
				for (String label : WikiWords.getInstance().getImageLabels(language))
					if (linkName.startsWith(label + ":"))
						starts = true;

				if (starts) {
					m.appendReplacement(sb, "");
					continue;
				}
				if (linkName.contains("|")) {
					anchorText = linkName.substring(linkName.indexOf("|") + 1, linkName.length());
					linkName = linkName.substring(0, linkName.indexOf("|"));
				}

				String insertedAnchorText = Matcher.quoteReplacement(anchorText);
				m.appendReplacement(sb, insertedAnchorText);
				if (linkName.equals("#"))
					continue;

				linkName = resolveRedirects(linkName);
				if (linkName == null)
					continue;

				// don't consider links of the page to itself
				if (linkName.replaceAll(" ", "_").equals(this.pageTitle.replaceAll(" ", "_")))
					continue;

				int lengthOfRemovedPart = insertedAnchorText.length() - m.group().length();

				Link link = null;
				try {
					link = new Link(linkName, m.start() + offset, m.start() + offset + insertedAnchorText.length(),
							false, false);

					link.setAnchorText(insertedAnchorText);

					if (!link.getName().contains("#"))
						paragraph.addLink(link);
				} catch (Exception e) {
					System.out.println("Ignore problem with " + pageId + ", " + pageTitle + ".");
					continue;
				}

				if (!anchorTextsToLinkNames.containsKey(link.getAnchorText())) {
					anchorTextsToLinkNames.put(link.getAnchorText(), new HashMap<String, Integer>());
					anchorTextsToLinkNames.get(link.getAnchorText()).put(linkName, 1);
				} else {
					if (!anchorTextsToLinkNames.get(link.getAnchorText()).containsKey(linkName)) {
						anchorTextsToLinkNames.get(link.getAnchorText()).put(linkName, 1);
					} else {
						anchorTextsToLinkNames.get(link.getAnchorText()).put(linkName,
								anchorTextsToLinkNames.get(link.getAnchorText()).get(linkName) + 1);
					}
				}

				offset += lengthOfRemovedPart;
				this.updateReferences(m.end(), lengthOfRemovedPart, paragraph);

			}
			m.appendTail(sb);
			String paragraphText = sb.toString();
			paragraph.setText(paragraphText);

		}
		if (paragraph.getTitle().equals("P") && !paragraph.getText().isEmpty()) {
			paragraph.setTextParagraphId(this.textParagraphId);
			paragraph.setStartPosition(this.textParagraphStartPosition);
			this.textParagraphId += 1;
			this.textParagraphStartPosition += paragraph.getText().length() + 1;
		}

	}

	private String resolveRedirects(String linkName) {
		linkName = linkName.replaceAll(" ", "_");

		if (linkName.isEmpty())
			return null;

		linkName = StringUtils.capitalize(linkName);
		linkName = linkName.replaceAll(Config.TAB, "_");

		linkName = this.redirectsMap.getRedirectedPage(linkName);

		return linkName;
	}

	public void extractSentencesAndLinks(Paragraph paragraph) {

		if (paragraph.getText() != null && !paragraph.getText().isEmpty()) {

			List<Span> spans = splitIntoSentenceSpans(paragraph.getText());

			int offset = 0;
			for (Span span : spans) {

				String text = span.getCoveredText(paragraph.getText()).toString();

				Set<Link> linksInSentence = new LinkedHashSet<Link>();
				Sentence sentence = new Sentence(text);
				sentences.add(sentence);
				paragraph.addSentence(sentence);

				for (Iterator<Link> it = paragraph.getLinks().iterator(); it.hasNext();) {
					Link link = it.next();
					if (link.getStart() >= span.getStart() && link.getEnd() <= span.getEnd()) {
						sentence.addLink(link);
						link.setStart(link.getStart() + offset);
						link.setEnd(link.getEnd() + offset);
						it.remove();
						linksInSentence.add(link);
					}
				}

				Set<Link> enrichedLinks = findEnrichedLinks(text, linksInSentence,
						this.anchorTextSortedByLengthDecreasing, this.resolvedLinkMap, true, false);

				sentence.getLinks().addAll(enrichedLinks);

				if (this.wikidataEventsDict != null) {
					linksInSentence.addAll(enrichedLinks);
					Set<Link> enrichedLinksGlobally = findEnrichedLinks(text, linksInSentence,
							this.wikidataEventsDict.getEventDictSortedByLengthDecreasing(),
							this.wikidataEventsDict.getEventsDict(), false, true);
					sentence.getLinks().addAll(enrichedLinksGlobally);
				}

				if (linksInSentence.size() > 1)
					output.addLinksInSentenceSet(linksInSentence);
				for (Link link : linksInSentence)
					output.addLink(link.getName());

				// add enriched links
				linksInSentence.addAll(enrichedLinks);
				if (linksInSentence.size() > 1)
					output.addLinksInSentenceSetEnriched(linksInSentence);

				for (Link link : linksInSentence)
					output.addLinkEnriched(link.getName());

				offset -= text.length() + 1;
				// offset=-span.getStart();
				// System.out.println(offset+"//"+span.getStart()+"//"+span.getEnd());

				if (this.nlpPipeline != null)
					sentence.getAnnotations().addAll(extractsNumbersAndDates(text));
			}

			// TODO: Merge sentences for each link that is still in
			// paragraph.getLinks()?
		}
		for (Paragraph sub : paragraph.getSubParagraphs()) {
			extractSentencesAndLinks(sub);
		}
	}

	private List<Annotation> extractsNumbersAndDates(String text) {
		List<Annotation> annotations = new ArrayList<Annotation>();
		CoreDocument doc = new CoreDocument(text);
		nlpPipeline.annotate(doc);

		for (CoreEntityMention em : doc.entityMentions()) {
			if (em.entityType().equals("NUMBER") || em.entityType().equals("DATE") || em.entityType().equals("DURATION")
					|| em.entityType().equals("TIME") || em.entityType().equals("ORDINAL")
					|| em.entityType().equals("SET") || em.entityType().equals("MONEY")
					|| em.entityType().equals("PERCENT") || em.entityType().equals("URL")) {
				annotations.add(new Annotation(em.charOffsets().first(), em.charOffsets().second(), em.text(),
						em.coreMap().get(NormalizedNamedEntityTagAnnotation.class),
						StringUtils.capitalize(em.entityType().toLowerCase())));
			}

		}

		return annotations;
	}

	private Set<Link> findEnrichedLinks(String text, Set<Link> realLinks, List<String> textSortedByLengthDecreasing,
			Map<String, String> textToLinkMap, boolean localEnrich, boolean globalEnrich) {
		Set<Link> enrichedLinks = new LinkedHashSet<Link>();
		Set<Integer> forbiddenPositions = new HashSet<Integer>();
		// System.out.println("\nEnrich links of " + text);

		for (Link realLink : realLinks) {
			// System.out.println(" " + realLink.getAnchorText());
			for (int pos = realLink.getStart(); pos <= realLink.getEnd(); pos++)
				forbiddenPositions.add(pos);
		}

		// System.out.println("Forbidden: "+forbiddenPositions);

		List<String> allowedPreviousLetters = Arrays.asList(new String[] { null, " ", ",", ".", "\"" });
		List<String> allowedNextLetters = Arrays.asList(new String[] { null, " ", "'", ",", ".", "\"" });

		for (String anchorText : textSortedByLengthDecreasing) {
			boolean found = true;
			int startIdx = 0;
			while (found) {
				int idx = text.indexOf(anchorText, startIdx);

				// There is a bug (?) where idx < startIdx...Page 737.
				if (idx != -1 && idx > startIdx) {

					int start = idx;
					int end = idx + anchorText.length();

					// in the next round search for the achor text AFTER the
					// current occurrence
					startIdx = end + 1;

					Set<Integer> positionsOfThisLink = new HashSet<Integer>();
					for (int pos = start; pos <= end; pos++)
						positionsOfThisLink.add(pos);

					if (!Collections.disjoint(forbiddenPositions, positionsOfThisLink)) {
						continue;
					}

					if (ONLY_MATCH_COMPLETE_WORDS_AS_LINKS) {
						String previousLetter = null;
						if (idx > 0)
							previousLetter = text.substring(idx - 1, idx);

						String nextLetter = null;
						if (idx + anchorText.length() < text.length())
							nextLetter = text.substring(idx + anchorText.length(), idx + anchorText.length() + 1);

						if (!allowedPreviousLetters.contains(previousLetter))
							continue;
						if (!allowedNextLetters.contains(nextLetter))
							continue;
						// System.out.println(
						// "New link: " + anchorText + ", " + ", " + previousLetter + ", " + nextLetter
						// + "|");
					}

					Link link = new Link(textToLinkMap.get(anchorText), start, end, localEnrich, globalEnrich);
					link.setAnchorText(anchorText);
					forbiddenPositions.addAll(positionsOfThisLink);

					enrichedLinks.add(link);
				} else
					found = false;
			}
		}

		return enrichedLinks;
	}

	public void resolveLinkMapStrict() {

		// Stricter version of the link resolver.
		// Only add anchor_text -> link, if
		// anchor_text = link
		// no ambiguity & anchor_text != link & [anchor_text|link] appears more
		// than once
		// ambiguity & anchor_text != link & [anchor_text|link] appears more
		// often than [anchor_text|other_link]

		resolvedLinkMap = new HashMap<String, String>();

		anchorTextsToLinkNames.remove(this.pageTitle);

		for (String anchorText : anchorTextsToLinkNames.keySet()) {
			if (anchorTextsToLinkNames.get(anchorText).size() > 1) {
				Integer highestCount = null;
				boolean strictlyHigher = false;
				String linkNameWithHighestCount = null;
				for (String linkName : anchorTextsToLinkNames.get(anchorText).keySet()) {
					if (linkName.equals(anchorText.replaceAll(" ", "_"))) {
						// anchor text = entity label
						resolvedLinkMap.put(anchorText, linkName);
						break;
					}
					if (highestCount == null || anchorTextsToLinkNames.get(anchorText).get(linkName) > highestCount) {
						if (highestCount != null)
							strictlyHigher = false;
						highestCount = anchorTextsToLinkNames.get(anchorText).get(linkName);
						linkNameWithHighestCount = linkName;
					}
				}

				// in case of break above -> don't put here
				if (!resolvedLinkMap.containsKey(anchorText)) {
					if (strictlyHigher)
						resolvedLinkMap.put(anchorText, linkNameWithHighestCount);
				}
			} else {
				// only one element
				for (String linkName : anchorTextsToLinkNames.get(anchorText).keySet()) {

					// to add some trust: if the anchor text appears only once
					// and anchor and link text are not the same: ignore it
					if (anchorTextsToLinkNames.get(anchorText).get(linkName) == 1
							&& !linkName.equals(anchorText.replaceAll(" ", "_")))
						continue;

					resolvedLinkMap.put(anchorText, linkName);
				}
			}
		}

		anchorTextSortedByLengthDecreasing = new ArrayList<String>();
		anchorTextSortedByLengthDecreasing.addAll(resolvedLinkMap.keySet());
		Collections.sort(anchorTextSortedByLengthDecreasing, new Comparator<String>() {

			@Override
			public int compare(String o1, String o2) {
				if (o1.length() > o2.length())
					return -1;
				else if (o1.length() < o2.length())
					return 1;
				return 0;
			}

		});

	}

	public void resolveLinkMap() {

		// first prio: anchor text = entity label
		// 2nd: highest count
		// 3rd: shortest label

		resolvedLinkMap = new HashMap<String, String>();

		anchorTextsToLinkNames.remove(this.pageTitle);

		for (String anchorText : anchorTextsToLinkNames.keySet()) {
			if (anchorTextsToLinkNames.get(anchorText).size() > 1) {
				Integer highestCount = null;
				boolean strictlyHigher = false;
				String linkNameWithHighestCount = null;
				String shortestLinkName = null;
				for (String linkName : anchorTextsToLinkNames.get(anchorText).keySet()) {
					if (linkName.equals(anchorText.replaceAll(" ", "_"))) {
						// anchor text = entity label
						resolvedLinkMap.put(anchorText, linkName);
						break;
					}
					if (highestCount == null || anchorTextsToLinkNames.get(anchorText).get(linkName) > highestCount) {
						if (highestCount != null)
							strictlyHigher = false;
						highestCount = anchorTextsToLinkNames.get(anchorText).get(linkName);
						linkNameWithHighestCount = linkName;
					}
					if (shortestLinkName == null || linkName.length() < shortestLinkName.length())
						shortestLinkName = linkName;
				}

				// in case of break above -> don't put here
				if (!resolvedLinkMap.containsKey(anchorText)) {
					if (strictlyHigher)
						resolvedLinkMap.put(anchorText, linkNameWithHighestCount);
					else {
						resolvedLinkMap.put(anchorText, shortestLinkName);
					}
				}
			} else {
				// only one element
				for (String linkName : anchorTextsToLinkNames.get(anchorText).keySet())
					resolvedLinkMap.put(anchorText, linkName);
			}
		}

		anchorTextSortedByLengthDecreasing = new ArrayList<String>();
		anchorTextSortedByLengthDecreasing.addAll(resolvedLinkMap.keySet());
		Collections.sort(anchorTextSortedByLengthDecreasing, new Comparator<String>() {

			@Override
			public int compare(String o1, String o2) {
				if (o1.length() > o2.length())
					return -1;
				else if (o1.length() < o2.length())
					return 1;
				return 0;
			}

		});

	}

	public void extractSentencesAndLinks() {

		extractSentencesAndLinks(this.articleParagraph);

		if (sentences.isEmpty()) {
			// System.out.println("Did not find sentences for " + this.pageId +
			// ", " + this.pageTitle + ".");
			return;
		}

		// if first sentence is too small -> combine it with second one
		while (sentences.get(0).getText().length() < 15 && sentences.size() > 1) {
			Sentence mergedSentence = mergeSentences(sentences.get(0), sentences.get(1));
			sentences.set(0, mergedSentence);
			for (int i = 1; i < sentences.size() - 1; i++) {
				sentences.set(i, sentences.get(i + 1));
			}
			sentences.remove(sentences.size() - 1);
		}

		// merge first and second sentence if first has an unclosed "("
		if (sentences.get(0).getText().contains("(") && !sentences.get(0).getText().contains(")")
				&& sentences.size() > 1 && sentences.get(1).getText().contains(")")) {
			Sentence mergedSentence = mergeSentences(sentences.get(0), sentences.get(1));
			sentences.set(0, mergedSentence);
			for (int i = 1; i < sentences.size() - 1; i++) {
				sentences.set(i, sentences.get(i + 1));
			}
			sentences.remove(sentences.size() - 1);
		}

		String textOfFirstSentence = sentences.get(0).getText();
		textOfFirstSentence = removeBrackets(textOfFirstSentence, sentences.get(0)).trim();

		// remove any " " in the first sentence
		boolean changed = true;
		while (changed) {
			String newText = textOfFirstSentence.replaceAll(" ", " ");
			if (!newText.equals(textOfFirstSentence)) {
				changed = true;
				textOfFirstSentence = newText;
			} else
				changed = false;
		}
		textOfFirstSentence = textOfFirstSentence.replaceAll(" ,", ",");
		sentences.get(0).setText(textOfFirstSentence);

		output.setFirstSentence(sentences.get(0).getText());
	}

	private String removeBrackets(String text, Sentence sentence) {

		// remove all brackets and their content (e.g. "(abc)" from text. But
		// don't remove it if it is part of the page title!

		Set<Integer> forbiddenPositions = new HashSet<Integer>();

		int index = text.indexOf(this.pageTitle);
		while (index >= 0) {
			for (int i = index; i < index + this.pageTitle.length(); i++)
				forbiddenPositions.add(i);
			index = text.indexOf(this.pageTitle, index + 1);
		}

		Matcher m = patternRoundBrackets.matcher(text);

		Set<Integer> removedPositions = new HashSet<Integer>();

		StringBuffer sb = new StringBuffer();
		groupsLoop: while (m.find()) {
			if (!forbiddenPositions.isEmpty()) {
				for (int i = m.start(); i < m.start() + m.group().length(); i++) {
					if (forbiddenPositions.contains(i)) {
						continue groupsLoop;
					}
					removedPositions.add(i);
				}
			}
			m.appendReplacement(sb, Matcher.quoteReplacement(""));
		}
		m.appendTail(sb);
		text = sb.toString();

		// after removing bracket, avoid the case of " , "
		Matcher m2 = Pattern.compile(" , ").matcher(text);
		sb = new StringBuffer();
		commaLoop: while (m2.find()) {
			if (!forbiddenPositions.isEmpty()) {
				for (int i = m2.start(); i < m2.start() + m2.group().length(); i++) {
					if (forbiddenPositions.contains(i)) {
						continue commaLoop;
					}
					removedPositions.add(i);
				}
			}
			m2.appendReplacement(sb, Matcher.quoteReplacement(", "));
		}
		m2.appendTail(sb);
		text = sb.toString();

		m = patternSquareBrackets.matcher(text);

		sb = new StringBuffer();
		groupsLoop: while (m.find()) {
			if (!forbiddenPositions.isEmpty()) {
				for (int i = m.start(); i < m.start() + m.group().length(); i++) {
					if (forbiddenPositions.contains(i)) {
						continue groupsLoop;
					}
				}
			}
			m.appendReplacement(sb, Matcher.quoteReplacement(""));
		}
		m.appendTail(sb);
		text = sb.toString();

		// correct links
		for (Iterator<Link> it = sentence.getLinks().iterator(); it.hasNext();) {
			Link link = it.next();

			// remove link if its text was removed
			Set<Integer> linkPositions = new HashSet<Integer>();

			for (int i = link.getStart(); i < link.getEnd(); i++)
				linkPositions.add(i);

			if (!Collections.disjoint(linkPositions, removedPositions)) {
				it.remove();
			} else {
				// shift the link to the left, if something was removed in front
				int offset = 0;
				for (int i = 0; i < link.getStart(); i++) {
					if (removedPositions.contains(i))
						offset += 1;
				}

				link.setStart(link.getStart() - offset);
				link.setEnd(link.getEnd() - offset);
			}

		}

		return text;
	}

	private Sentence mergeSentences(Sentence sentence, Sentence sentence2) {

		Sentence mergedSentence = new Sentence(sentences.get(0).getText() + " " + sentences.get(1).getText());
		for (Link link1 : sentences.get(0).getLinks())
			mergedSentence.addLink(link1);
		for (Link link2 : sentences.get(0).getLinks())
			mergedSentence.addLink(link2);

		return mergedSentence;
	}

//	private void createCSVString(Paragraph paragraph) {
//
//		if (this.splitTextIntoSections) {
//			Paragraph previousParagraphTopParagraph = this.topParagraph;
//			if (paragraph.getText() != null) {
//				this.topParagraph = paragraph.getTopParagraphAtLevel(2);
//				if (previousParagraphTopParagraph != null && !this.topParagraph.getTitle().equals("P")
//						&& this.topParagraph != previousParagraphTopParagraph) {
//				}
//			}
//		}
//
//		if (paragraph.getText() != null && !paragraph.getText().isEmpty() && !paragraph.getTitle().equals("T")
//				&& !paragraph.getText().trim().equals("*") && paragraph.getText().length() > 3) {
//			paragraph.setText(paragraph.getText().replaceAll("\t", " "));
//		}
//
//		for (Paragraph subParagraph : paragraph.getSubParagraphs()) {
//			this.createCSVString(subParagraph);
//		}
//
//	}

	public Paragraph getArticleParagraph() {
		return this.articleParagraph;
	}

	private void updateReferences(int position, int offset, Paragraph paragraph) {
		if (offset == 0) {
			return;
		}
		for (Reference reference : paragraph.getReferences()) {
			if (reference.getOriginalStartPosition() <= position)
				continue;
			reference.setStartPosition(reference.getStartPosition() + offset);
		}
	}

	private List<Span> splitIntoSentenceSpans(String line) {

		List<Span> spanList = new ArrayList<Span>();
		for (Span span : nlpUtils.sentenceSplitterPositions(line))
			spanList.add(span);

		return spanList;
	}

	public Output getOutput() {
		return output;
	}

	public void addSentencesToOutput() {
		this.output.setSentences(this.sentences);
	}

}
