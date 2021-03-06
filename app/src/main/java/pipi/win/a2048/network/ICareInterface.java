package pipi.win.a2048.network;

import io.reactivex.Observable;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.http.*;

public interface ICareInterface {
    String BASE_URL="http://127.0.0.1:5000/";
    String BASE_URL_SERVER="http://usslabinternal.5pipi.win:5000/";


    @GET("api/detect/{id}")
    Call<ResponseBody> queryID(@Path("id") String id);

    @GET("api/detect/{id}")
    Observable<String> queryIDRx(@Path("id") String id);


    @POST("api/detect")
    @FormUrlEncoded
    Call<String> uploadData(@Field("sensor") String sensor, @Field("touch") String touch);


    @POST("api/detect")
    @FormUrlEncoded
    Observable<String> uploadDataRx(@Field("sensor") String sensor, @Field("touch") String touch);


}
