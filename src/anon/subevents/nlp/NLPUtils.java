package anon.subevents.nlp;

import opennlp.tools.util.Span;

public interface NLPUtils {

	public Span[] sentenceSplitterPositions(String text);

}
