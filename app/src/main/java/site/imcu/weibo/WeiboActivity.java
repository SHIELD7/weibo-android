package site.imcu.weibo;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.lxj.xpopup.XPopup;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.bingoogolapple.photopicker.widget.BGANinePhotoLayout;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import site.imcu.weibo.adapter.CommentAdapter;
import site.imcu.weibo.net.CommentService;
import site.imcu.weibo.net.WeiboService;
import site.imcu.weibo.po.CommentVo;
import site.imcu.weibo.po.WeiboVo;

public class WeiboActivity extends AppCompatActivity {

    private static final String TAG = "WeiboActivity";

    private WeiboVo weiboVo;

    @BindView(R.id.weibo_user_avatar)
    ImageView avatar;
    @BindView(R.id.weibo_username)
    TextView usernameText;
    @BindView(R.id.weibo_post_time)
    TextView postTimeText;
    @BindView(R.id.weibo_content)
    TextView contentText;
    @BindView(R.id.weibo_photos)
    BGANinePhotoLayout bgaNinePhotoLayout;
    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;
    @BindView(R.id.btn_add_comment)
    TextView btn_add_comment;
    @BindView(R.id.my_avatar)
    ImageView myAvatar;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weibo);
        ButterKnife.bind(this);
        init();
    }

    private void init(){
        setSupportActionBar(toolbar);
        if (getSupportActionBar()!=null){
            getSupportActionBar().setTitle("微博正文");
        }
        Picasso.get().load(MainActivity.HOST+MainActivity.PATH_IMG+MainActivity.CURRENT_USERVO.getFace()).into(myAvatar);
        Intent intent = getIntent();
        int weiboId= intent.getIntExtra("weiboId",0);
        if (weiboId==0){
            String string = intent.getStringExtra("weiboVo");
            weiboVo = JSON.parseObject(string,WeiboVo.class);
            initWeibo();
            queryComment();
        }else {
            queryWeibo(weiboId);
        }
    }

    @OnClick(R.id.btn_add_comment)
    public void addComment(){
        new XPopup.Builder(WeiboActivity.this).asInputConfirm("发表评论", "", this::postComment).show();
    }




    private void postComment(String comment){
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(MainActivity.HOST)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .build();
        CommentService commentService = retrofit.create(CommentService.class);

        Map<String,String> headers = new HashMap<>();
        headers.put("Authorization", MainActivity.USER_TOKEN);
        headers.put("Accept","application/json; charset=utf-8");

        commentService.postComment(headers,comment,weiboVo.getWeiboId()).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<JSONObject>() {
                    @Override
                    public void onCompleted() {
                        Log.d(TAG, "onCompleted: ");
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d(TAG, "onError:出错了 "+e);
//                        if (e instanceof HttpException){
//                            ResponseBody responseBody = ((HttpException) e).response().errorBody();
//                            try {
//                                Log.d(TAG, "onError: "+responseBody.string());
//                            }catch (IOException e1){
//                            }
//                        }

                    }

                    @Override
                    public void onNext(JSONObject jsonObject) {
                        if (jsonObject.getIntValue("code")==1){
                            Toast.makeText(WeiboActivity.this,"评论成功",Toast.LENGTH_SHORT).show();
                            queryComment();
                        }
                    }
                });
    }

    private void initWeibo(){
        Picasso.get().load(MainActivity.HOST+MainActivity.PATH_IMG+weiboVo.getUser().getFace()).into(avatar);
        usernameText.setText(weiboVo.getUser().getUsername());
        postTimeText.setText(weiboVo.getDate());
        contentText.setText(weiboVo.getContent());

        ArrayList<String> photos = new ArrayList<>();
        if (weiboVo.getPic1()!=null){
            photos.add(MainActivity.HOST+MainActivity.PATH_IMG+weiboVo.getPic1());
        }
        if (weiboVo.getPic2()!=null){
            photos.add(MainActivity.HOST+MainActivity.PATH_IMG+weiboVo.getPic2());
        }
        if (weiboVo.getPic3()!=null){
            photos.add(MainActivity.HOST+MainActivity.PATH_IMG+weiboVo.getPic3());
        }
        if (weiboVo.getPic4()!=null){
            photos.add(MainActivity.HOST+MainActivity.PATH_IMG+weiboVo.getPic4());
        }
        if (weiboVo.getPic5()!=null){
            photos.add(MainActivity.HOST+MainActivity.PATH_IMG+weiboVo.getPic5());
        }
        if (weiboVo.getPic6()!=null){
            photos.add(MainActivity.HOST+MainActivity.PATH_IMG+weiboVo.getPic6());
        }
        if (weiboVo.getPic7()!=null){
            photos.add(MainActivity.HOST+MainActivity.PATH_IMG+weiboVo.getPic7());
        }
        if (weiboVo.getPic8()!=null){
            photos.add(MainActivity.HOST+MainActivity.PATH_IMG+weiboVo.getPic8());
        }
        if (weiboVo.getPic9()!=null){
            photos.add(MainActivity.HOST+MainActivity.PATH_IMG+weiboVo.getPic9());
        }

        bgaNinePhotoLayout.setData(photos);


    }

    private void queryComment(){
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(MainActivity.HOST)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .build();
        CommentService commentService = retrofit.create(CommentService.class);

        Map<String,String> headers = new HashMap<>();
        headers.put("Authorization", MainActivity.USER_TOKEN);
        headers.put("Accept","application/json; charset=utf-8");

        commentService.queryComment(headers,weiboVo.getWeiboId()).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<JSONObject>() {
                    @Override
                    public void onCompleted() {
                        Log.d(TAG, "onCompleted: ");
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d(TAG, "onError:出错了 "+e);
//                        if (e instanceof HttpException){
//                            ResponseBody responseBody = ((HttpException) e).response().errorBody();
//                            try {
//                                Log.d(TAG, "onError: "+responseBody.string());
//                            }catch (IOException e1){
//                            }
//                        }

                    }

                    @Override
                    public void onNext(JSONObject jsonObject) {
                        if (jsonObject.getIntValue("code")==1){
                            Log.d(TAG, "onNext:完成 "+jsonObject.toString());
                            List<CommentVo> commentVoList = JSON.parseArray(jsonObject.getJSONArray("data").toString(),CommentVo.class);
                            initAdapter(commentVoList);
                        }
                    }
                });
    }


    private void initAdapter(List<CommentVo> commentVoList) {
        CommentAdapter commentAdapter = new CommentAdapter(R.layout.comment_item_view, commentVoList,this);
        commentAdapter.openLoadAnimation();
        recyclerView.setLayoutManager(new GridLayoutManager(this, 1));
        recyclerView.setAdapter(commentAdapter);
    }

    private void queryWeibo(int weiboId){
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(MainActivity.HOST)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .build();
        WeiboService weiboService = retrofit.create(WeiboService.class);

        Map<String,String> headers = new HashMap<>();
        headers.put("Authorization", MainActivity.USER_TOKEN);
        headers.put("Accept","application/json; charset=utf-8");

        weiboService.queryOne(headers,weiboId).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<JSONObject>() {
                    @Override
                    public void onCompleted() {
                        Log.d(TAG, "onCompleted: ");
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d(TAG, "onError:出错了 "+e);
//                        if (e instanceof HttpException){
//                            ResponseBody responseBody = ((HttpException) e).response().errorBody();
//                            try {
//                                Log.d(TAG, "onError: "+responseBody.string());
//                            }catch (IOException e1){
//                            }
//                        }

                    }

                    @Override
                    public void onNext(JSONObject jsonObject) {
                        if (jsonObject.getIntValue("code")==1){
                            Log.d(TAG, "onNext:完成 "+jsonObject.toString());
                            weiboVo = JSON.parseObject(jsonObject.getJSONObject("data").toJSONString(),WeiboVo.class);
                            initWeibo();
                            queryComment();
                        }
                    }
                });

        }
}
