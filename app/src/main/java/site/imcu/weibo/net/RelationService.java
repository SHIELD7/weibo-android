package site.imcu.weibo.net;

import com.alibaba.fastjson.JSONObject;

import java.util.Map;

import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.HeaderMap;
import retrofit2.http.POST;
import rx.Observable;

public interface RelationService {

    @FormUrlEncoded
    @POST("/changeRelation.action")
    Observable<JSONObject> changeRelation(@HeaderMap Map<String,String> headers, @Field("profileId")int profileId);
}
