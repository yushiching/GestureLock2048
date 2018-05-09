package pipi.win.a2048.network;

import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class ClientFactory {
    public static ICareInterface newInterface(String url){
        Retrofit retrofit=new Retrofit.Builder()
                .baseUrl(url)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build();
        ICareInterface iCareInterface=retrofit.create(ICareInterface.class);

        return iCareInterface;
    }

    public static ICareInterface newInterface(){
        return newInterface(ICareInterface.BASE_URL_SERVER);
    }
}
