package rx.android.stocks.sentiments;

import com.fasterxml.jackson.databind.JsonNode;

import retrofit.http.GET;
import retrofit.http.Query;
import rx.Observable;

/**
 * Created by p.ptaszynski on 2015-08-28.
 */
public interface TwitterService {
    static final String ENDPOINT = "http://twitter-search-proxy.herokuapp.com/";

    @GET("/search/tweets")
    Observable<JsonNode> getTweets(@Query("q") String query);
}
