package rx.android.stocks.sentiments.model;

/**
 * Created by p.ptaszynski on 2015-08-28.
 */
public class Sentiment {
    private SentimentProbability probability;
    private String label;

    public SentimentProbability getProbability() {
        return probability;
    }

    public void setProbability(SentimentProbability probability) {
        this.probability = probability;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }
}
