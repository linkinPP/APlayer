package remix.myplayer.request.network;

import io.reactivex.Observable;
import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import remix.myplayer.lyric.HttpHelper;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by Remix on 2017/11/20.
 */

public class HttpClient implements HttpHelper {
    private static final String NETEASE_BASE_URL = "http://music.163.com/api/";
    private static final String KUGOU_BASE_URL = "http://lyrics.kugou.com/";
    private static final String LASTFM_BASE_URL = "http://ws.audioscrobbler.com/2.0/";
    private static final String GITHUB_BASE_URL = "https://api.github.com/";
    private static final long TIMEOUT = 10000;

    private ApiService mNeteaseApi;
    private ApiService mKuGouApi;
    private ApiService mLastfmApi;
    private ApiService mGithubApi;

    public static HttpClient getInstance() {
        return SingletonHolder.mInstance;
    }

    public static ApiService getNeteaseApiservice() {
        return getInstance().mNeteaseApi;
    }

    public static ApiService getKuGouApiservice() {
        return getInstance().mKuGouApi;
    }

    public static ApiService getLastFMApiservice() {
        return getInstance().mLastfmApi;
    }

    public static ApiService getGithubApiservice() {
        return getInstance().mGithubApi;
    }

    private HttpClient() {
        Retrofit.Builder retrofitBuilder = new Retrofit.Builder();

        OkHttpClient okHttpClient = OkHttpHelper.getOkHttpClient();
        mNeteaseApi = retrofitBuilder
                .baseUrl(NETEASE_BASE_URL)
                .client(okHttpClient)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .build().create(ApiService.class);

        mKuGouApi = retrofitBuilder
                .baseUrl(KUGOU_BASE_URL)
                .client(okHttpClient)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .build().create(ApiService.class);

        mLastfmApi = retrofitBuilder
                .baseUrl(LASTFM_BASE_URL)
                .client(okHttpClient)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .build().create(ApiService.class);

        mGithubApi = retrofitBuilder
                .baseUrl(GITHUB_BASE_URL)
                .client(okHttpClient)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .build().create(ApiService.class);
    }

//    private static OkHttpClient sOkHttpClient;
//    private static SSLSocketFactory mSSLSocketFactory;
//    public static SSLSocketFactory getSSLSocketFactory(){
//        if(mSSLSocketFactory == null){
//            try {
//                // 自定义一个信任所有证书的TrustManager，添加SSLSocketFactory的时候要用到
//                final X509TrustManager trustAllCert =
//                        new X509TrustManager() {
//                            @Override
//                            public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType)  {
//                            }
//
//                            @Override
//                            public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType)  {
//                            }
//
//                            @Override
//                            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
//                                return new java.security.cert.X509Certificate[]{};
//                            }
//                        };
//                mSSLSocketFactory = new SSLSocketFactoryCompat(trustAllCert);
//            } catch (Exception e) {
//                throw new RuntimeException(e);
//            }
//        }
//        return mSSLSocketFactory;
//    }
//
//    public synchronized static OkHttpClient getOkHttpClient(){
//        if (sOkHttpClient == null) {
//            OkHttpClient.Builder builder = new OkHttpClient.Builder();
//            try {
//                // 自定义一个信任所有证书的TrustManager，添加SSLSocketFactory的时候要用到
//                final X509TrustManager trustAllCert =
//                        new X509TrustManager() {
//                            @Override
//                            public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType)  {
//                            }
//
//                            @Override
//                            public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType)  {
//                            }
//
//                            @Override
//                            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
//                                return new java.security.cert.X509Certificate[]{};
//                            }
//                        };
//                builder.sslSocketFactory(getSSLSocketFactory(), trustAllCert);
//            } catch (Exception e) {
//                throw new RuntimeException(e);
//            }
//            builder.connectTimeout(TIMEOUT, TimeUnit.MILLISECONDS)
//                    .readTimeout(TIMEOUT, TimeUnit.MILLISECONDS)
//                    .writeTimeout(TIMEOUT, TimeUnit.MILLISECONDS)
//                    .cache(createDefaultCache(App.getContext()));
//            sOkHttpClient = builder.build();
//        }
//        return sOkHttpClient;
//    }


    @Override
    public Observable<ResponseBody> getNeteaseSearch(String key, int offset, int limit, int type) {
        return mNeteaseApi.getNeteaseSearch(key, offset, limit, type);
    }

    @Override
    public Observable<ResponseBody> getNeteaseLyric(int song_id) {
        return mNeteaseApi.getNeteaseLyric("pc", song_id, -1, -1, -1);
    }

    @Override
    public Observable<ResponseBody> getKuGouSearch(String keyword, long duration, String hash) {
        return mKuGouApi.getKuGouSearch(1, "yes", "pc", keyword, duration, "");
    }

    @Override
    public Observable<ResponseBody> getKuGouLyric(int id, String accessKey) {
        return mKuGouApi.getKuGouLyric(1, "pc", "lrc", "utf8", id, accessKey);
    }

    private static class SingletonHolder {
        static HttpClient mInstance = new HttpClient();
    }
}
