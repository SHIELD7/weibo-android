package site.imcu.weibo.net;

import com.alibaba.fastjson.JSONObject;

import java.util.Map;

import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.HeaderMap;
import retrofit2.http.POST;
import rx.Observable;

public interface LikeService {
    @FormUrlEncoded
    @POST("/changeLike")
    Observable<JSONObject> changeLike(@HeaderMap Map<String,String> headers,@Field("weiboId")int weiboId);
}
