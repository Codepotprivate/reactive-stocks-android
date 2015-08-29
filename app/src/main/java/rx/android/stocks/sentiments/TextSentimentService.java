package rx.android.stocks.sentiments;

import rx.android.stocks.sentiments.model.Sentiment;

import retrofit.http.Field;
import retrofit.http.FormUrlEncoded;
import retrofit.http.POST;
import rx.Observable;

public interface TextSentimentService {
    static final String ENDPOINT = "http://text-processing.com/api/";

    @FormUrlEncoded
    @POST("/sentiment/")
    Observable<Sentiment> fetchSentiment(@Field("text") String text);
}
