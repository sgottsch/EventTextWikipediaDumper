package anon.subevents.source.wikipedia.mwdumper.model;

public class Annotation {

	private int start;
	private int end;

	private String text;
	private String normalisedValue;

	private String type;

	public Annotation(int start, int end, String text, String normalisedValue, String type) {
		super();
		this.start = start;
		this.end = end;
		this.text = text;
		this.normalisedValue = normalisedValue;
		this.type = type;
	}

	public int getStart() {
		return this.start;
	}

	public void setStart(int start) {
		this.start = start;
	}

	public int getEnd() {
		return this.end;
	}

	public void setEnd(int end) {
		this.end = end;
	}

	public String getText() {
		return text;
	}

	public String getNormalisedValue() {
		return normalisedValue;
	}

	public String getType() {
		return type;
	}

}
