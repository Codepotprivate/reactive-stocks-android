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
        // Flatmap Tweets to fetch sentiment and average it
        return Observable.empty();
    }

    private Observable<List<Sentiment>> fetchSentiment(List<String> tweets) {
        Log.d(TAG, "Fetching sentiment with list of tweets");
        // TODO: Flatmap list of tweets to sentiment observable.
        Observable<Sentiment> sentimentObservable = Observable.empty();

        // Collect List of sentiments. Use collect
        return sentimentObservable.<List<Sentiment>>collect(
                LinkedList<Sentiment>::new,
                (r, sentiment) -> r.add(sentiment)
        );
    }

    private Observable<List<String>> fetchTweets(String symbol) {
        Log.d(TAG, "Fetching tweets for symbol");
        // TODO: Fetch tweets. map response to statuses and to text
        return Observable.empty();
    }

    private Observable<Sentiment> averageSentiment(List<Sentiment> sentiments) {
        Log.d(TAG, "Averaging sentiments");
        Observable<Double> neg = collectNegativeAverage(sentiments);
        Observable<Double> neutral = collectNeutralAverage(sentiments);
        Observable<Double> pos = collectPositiveAverage(sentiments);


        // TODO: Zip observables to construct SentimentProbability object.
        return Observable.empty();
    }

    private Observable<Double> collectNegativeAverage(List<Sentiment> sentiments) {
        // TODO: Filter negative sentiments and map to negative.
        Observable<Double> negativeSentiments = Observable.empty();
        return averageDouble(negativeSentiments);
    }

    private Observable<Double> collectNeutralAverage(List<Sentiment> sentiments) {
        // TODO: By analogy
        Observable<Double> negativeSentiments = Observable.empty();
        return averageDouble(negativeSentiments);
    }

    private Observable<Double> collectPositiveAverage(List<Sentiment> sentiments) {
        // Todo: By analogy
        Observable<Double> negativeSentiments = Observable.empty();
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
