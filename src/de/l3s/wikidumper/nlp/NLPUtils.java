package de.l3s.wikidumper.nlp;

import opennlp.tools.util.Span;

public interface NLPUtils {

	public Span[] sentenceSplitterPositions(String text);

}
