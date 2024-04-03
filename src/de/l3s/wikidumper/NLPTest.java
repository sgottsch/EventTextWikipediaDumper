package de.l3s.wikidumper;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;

import de.l3s.wikidumper.source.wikipedia.mwdumper.model.Annotation;
import edu.stanford.nlp.ling.CoreAnnotations.NormalizedNamedEntityTagAnnotation;
import edu.stanford.nlp.pipeline.CoreDocument;
import edu.stanford.nlp.pipeline.CoreEntityMention;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;

public class NLPTest {

	public static void main(String[] args) {
		Properties props = new Properties();
		props.setProperty("annotators", "tokenize,pos,lemma,ner");
		StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
			Date previousDate = null;

			long avg=0;
		for(int i=0;i<200;i++) {
			
			Date currentDate = new Date();

			if (previousDate == null) {
				previousDate = currentDate;
				System.out.println("Time: " + currentDate);
			} else {

				long ms = (currentDate.getTime() - previousDate.getTime());
				avg+=ms;
				System.out.println("Time diff: " + ms + "ms");

				previousDate = currentDate;
			}

			
			System.out.println(i);
			String t = "In the morning, Barack Obama went to the hotel in January 2020 to get 3 fish.";
			testString(pipeline,t);
		}
		System.out.println(avg/200);
		
		// 3 => 124
		// 1 => 58
	}
	
	private static void testString(StanfordCoreNLP nlpPipeline , String text) {
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

	}

}
