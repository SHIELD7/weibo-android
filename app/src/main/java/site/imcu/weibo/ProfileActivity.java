package site.imcu.weibo;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.scwang.smartrefresh.header.DeliveryHeader;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.constant.SpinnerStyle;
import com.scwang.smartrefresh.layout.footer.BallPulseFooter;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import site.imcu.weibo.adapter.WeiboAdapter;
import site.imcu.weibo.net.RelationService;
import site.imcu.weibo.net.UserService;
import site.imcu.weibo.net.WeiboService;
import site.imcu.weibo.po.UserVo;
import site.imcu.weibo.po.WeiboVo;

public class ProfileActivity extends AppCompatActivity {
    private static final String TAG = "ProfileActivity";

    @BindView(R.id.profile_refreshLayout)
    SmartRefreshLayout smartRefreshLayout;
    @BindView(R.id.profile_recyclerView)
    RecyclerView recyclerView;
    @BindView(R.id.toolbar)
    Toolbar toolbar;

    private UserVo userVo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        ButterKnife.bind(this);
        init();
    }

    private void init(){
        Intent intent = getIntent();
        String string = intent.getStringExtra("profile");
        userVo = JSON.parseObject(string,UserVo.class);
        initRefreshLayout();
        queryProfile();
        queryAll();

        setSupportActionBar(toolbar);
        if (getSupportActionBar()!=null){
            getSupportActionBar().setTitle(userVo.getUsername());
        }

    }

    private void queryProfile(){
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(MainActivity.HOST)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .build();
        UserService userService = retrofit.create(UserService.class);

        Map<String,String> headers = new HashMap<>();
        headers.put("Authorization", MainActivity.USER_TOKEN);
        headers.put("Accept","application/json; charset=utf-8");

        userService.profile(headers,userVo.getUserId()).subscribeOn(Schedulers.io())
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
                            userVo = JSON.parseObject(jsonObject.getJSONObject("data").toJSONString(),UserVo.class);
                            Log.d(TAG, "onNext: "+userVo);
                            queryAll();
                        }
                    }
                });


    }


    private void queryAll() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(MainActivity.HOST)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .build();
        WeiboService weiboSerVice = retrofit.create(WeiboService.class);

        Map<String,String> headers = new HashMap<>();
        headers.put("Authorization", MainActivity.USER_TOKEN);
        headers.put("Accept","application/json; charset=utf-8");

        weiboSerVice.querySomeone(headers,userVo.getUserId(),1,0).subscribeOn(Schedulers.io())
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
                            List<WeiboVo> weiboVoList = JSON.parseArray(jsonObject.getJSONArray("data").toString(),WeiboVo.class);
                            for(int i=0;i<weiboVoList.size();i++){
                                Log.d(TAG, "onNext: "+ weiboVoList.get(i).getContent());
                            }
                            initAdapter(weiboVoList);
                        }
                    }
                });
    }



    private void initAdapter(List<WeiboVo> weiboVoList) {
        WeiboAdapter weiboAdapter = new WeiboAdapter(R.layout.weibo_item_view, weiboVoList,this);
        weiboAdapter.openLoadAnimation();
        recyclerView.setLayoutManager(new GridLayoutManager(this,1));
        recyclerView.setAdapter(weiboAdapter);

        weiboAdapter.addHeaderView(getHeaderView(v -> {}));
    }
    private View getHeaderView(View.OnClickListener listener) {
        View view = getLayoutInflater().inflate(R.layout.head_view, (ViewGroup) recyclerView.getParent(), false);
        view.setOnClickListener(listener);
        String fanCount = String.valueOf(userVo.getFansCount());
        String followCount = String.valueOf(userVo.getFollowCount());
        String weiboCount = String.valueOf(userVo.getWeiboCount());
        TextView nameText =  view.findViewById(R.id.user_username);
        nameText.setText(userVo.getUsername());
        TextView fanText = view.findViewById(R.id.user_fan_num);
        fanText.setText(fanCount);
        TextView followText = view.findViewById(R.id.user_follow_num);
        followText.setText(followCount);
        TextView weiboText = view.findViewById(R.id.user_weibo_num);
        weiboText.setText(weiboCount);

        Button relationText = view.findViewById(R.id.user_relation);
        if (userVo.getRelation().getRelationId()!=null){
            relationText.setText("正在关注");

        } else if (userVo.getUserId().equals(MainActivity.CURRENT_USERVO.getUserId())) {
            relationText.setText("我");
        }else {
            relationText.setText("关注");
        }

        relationText.setOnClickListener(v -> {
            changeRelation();
            if (relationText.getText().toString().equals("正在关注")){
                relationText.setText("关注");
            }
            if (relationText.getText().toString().equals("关注")){
                relationText.setText("正在关注");
            }
        });

        ImageView avatar = view.findViewById(R.id.user_avatar);
        Picasso.get().load(MainActivity.HOST+MainActivity.PATH_IMG+userVo.getFace()).into(avatar);
        Log.d(TAG, "getHeaderView: "+userVo.getFansCount());
        Log.d(TAG, "getHeaderView: "+view);
        return view;
    }


    private void initRefreshLayout() {
        smartRefreshLayout.setRefreshHeader(new DeliveryHeader(this));
        smartRefreshLayout.setRefreshFooter(new BallPulseFooter(this).setSpinnerStyle(SpinnerStyle.Scale));
        smartRefreshLayout.setOnRefreshListener(v->{queryAll();v.finishRefresh(2000);});
        smartRefreshLayout.setOnLoadMoreListener(v->v.finishLoadMore(2000));
    }


    private void changeRelation(){
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(MainActivity.HOST)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .build();
        RelationService relationService = retrofit.create(RelationService.class);

        Map<String,String> headers = new HashMap<>();
        headers.put("Authorization", MainActivity.USER_TOKEN);
        headers.put("Accept","application/json; charset=utf-8");

        relationService.changeRelation(headers,userVo.getUserId()).subscribeOn(Schedulers.io())
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
                            Toast.makeText(ProfileActivity.this,"关系改变",Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

}
