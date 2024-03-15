package anon.subevents.source.wikipedia.mwdumper.model;

import java.util.ArrayList;
import java.util.List;

public class Sentence {

	private String text;

	private List<Link> links;
	private List<Annotation> annotations;

	public Sentence(String text) {
		super();
		this.text = text;
		this.links = new ArrayList<Link>();
		this.annotations = new ArrayList<Annotation>();
	}

	public void addLink(Link link) {
		this.links.add(link);
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public List<Link> getLinks() {
		return links;
	}

	public List<Annotation> getAnnotations() {
		return annotations;
	}

}
