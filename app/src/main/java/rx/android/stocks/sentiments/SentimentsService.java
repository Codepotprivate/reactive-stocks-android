package rx.android.stocks.sentiments;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import rx.android.stocks.sentiments.model.Sentiment;
import rx.android.stocks.sentiments.model.SentimentProbability;

import java.util.LinkedList;
import java.util.List;

import retrofit.RestAdapter;
import retrofit.converter.JacksonConverter;
import rx.Observable;

public class SentimentsService extends Service {

    private static final String TAG = SentimentsService.class.getSimpleName();

    private TwitterService twitterService = null;
    private TextSentimentService textSentimentService = null;

    public class Binder extends android.os.Binder {
        public SentimentsService getService() {
            return SentimentsService.this;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        setupServices();
        return new Binder();
    }

    private void setupServices() {
        Log.d(TAG, "Setup Retrofit services");
            RestAdapter twitterRestAdapter = new RestAdapter.Builder()
                    .setEndpoint(TwitterService.ENDPOINT)
                    .setLogLevel(RestAdapter.LogLevel.FULL)
                    .setConverter(new JacksonConverter())
                    .build();
            twitterService = twitterRestAdapter.create(TwitterService.class);

            RestAdapter sentimentRestAdapter = new RestAdapter.Builder()
                    .setEndpoint(TextSentimentService.ENDPOINT)
                    .setLogLevel(RestAdapter.LogLevel.FULL)
                    .build();
            this.textSentimentService = sentimentRestAdapter.create(TextSentimentService.class);
    }



    public Observable<Sentiment> get(String symbol) {
        return fetchTweets(symbol)
                .flatMap(this::fetchSentiment)
                .flatMap(this::averageSentiment);
    }

    private Observable<List<Sentiment>> fetchSentiment(List<String> tweets) {
        Log.d(TAG, "Fetching sentiment with list of tweets");
        Observable<Sentiment> sentimentObservable =
                Observable.from(tweets)
                        .flatMap(text -> textSentimentService.fetchSentiment(text));
        return sentimentObservable.<List<Sentiment>>collect(
                LinkedList<Sentiment>::new,
                (r, sentiment) -> r.add(sentiment)
        );
    }

    private Observable<List<String>> fetchTweets(String symbol) {
        Log.d(TAG, "Fetching tweets for symbol");
        return twitterService.getTweets(symbol).doOnNext(jsonNode -> Log.d(TAG, "Got Tweets")).map(response -> response.findPath("statuses"))
                .map(status -> status.findValue("text").asText())
                .collect(() -> new LinkedList<String>(), (strings, s) -> strings.add(s));
    }

    private Observable<Sentiment> averageSentiment(List<Sentiment> sentiments) {
        Log.d(TAG, "Averaging sentiments");
        Observable<Double> neg = collectNegativeAverage(sentiments);
        Observable<Double> neutral = collectNeutralAverage(sentiments);
        Observable<Double> pos = collectPositiveAverage(sentiments);

        return neutral.zipWith(
                neg, (neutralProb, negProb) -> {
                    SentimentProbability res = new SentimentProbability();
                    res.setNeutral(neutralProb);
                    res.setNeg(negProb);
                    return res;
                })
                .zipWith(pos, (sentimentProbability, posProb) -> {
                    sentimentProbability.setPos(posProb);
                    return sentimentProbability;
                })
                .map(SentimentsService::decideSentiment);
    }

    private Observable<Double> collectNegativeAverage(List<Sentiment> sentiments) {
        Observable<Double> negativeSentiments = Observable.from(sentiments)
                .filter(sentiment1 -> sentiment1.getLabel().equals("neg"))
                .map(sentiment1 -> sentiment1.getProbability().getNeg());
        return averageDouble(negativeSentiments);
    }

    private Observable<Double> collectNeutralAverage(List<Sentiment> sentiments) {
        Observable<Double> negativeSentiments = Observable.from(sentiments)
                .filter(sentiment1 -> sentiment1.getLabel().equals("neutral"))
                .map(sentiment1 -> sentiment1.getProbability().getNeutral());
        return averageDouble(negativeSentiments);
    }

    private Observable<Double> collectPositiveAverage(List<Sentiment> sentiments) {
        Observable<Double> negativeSentiments = Observable.from(sentiments)
                .filter(sentiment1 -> sentiment1.getLabel().equals("pos"))
                .map(sentiment1 -> sentiment1.getProbability().getPos());
        return averageDouble(negativeSentiments);
    }


    private Observable<Double> averageDouble(Observable<Double> doubles) {
        // TODO: Implement proper averaging on observable.
        // Use reduce or colllect and an accumulator type.
        return doubles.reduce(0.0, (acc, aDouble) -> (acc + aDouble)/2 );
    }

    private static Sentiment decideSentiment(SentimentProbability probability) {
        Log.d(TAG, "Deciding sentiment for sentiment probability " + probability);
        double neutral = probability.getNeutral();
        double pos = probability.getPos();
        double neg = probability.getNeg();
        String label = neutral > 0.5 ? "neutral" : (neg > pos) ? "neg" : "pos";
        Sentiment sentiment = new Sentiment();
        sentiment.setLabel(label);
        sentiment.setProbability(probability);
        return sentiment;
    }

}
