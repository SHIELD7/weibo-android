package site.imcu.weibo.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.github.clans.fab.FloatingActionButton;
import com.scwang.smartrefresh.header.DeliveryHeader;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.constant.SpinnerStyle;
import com.scwang.smartrefresh.layout.footer.BallPulseFooter;


import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import site.imcu.weibo.MainActivity;
import site.imcu.weibo.PostActivity;
import site.imcu.weibo.R;
import site.imcu.weibo.net.WeiboService;
import site.imcu.weibo.adapter.WeiboAdapter;
import site.imcu.weibo.po.WeiboVo;

public class TimelineFragment extends Fragment {
    private static final String ARG_SHOW_TEXT = "text";
    private static final String TAG = "TimelineFragment";



    @BindView(R.id.refreshLayout)
    SmartRefreshLayout smartRefreshLayout;

    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;

    @BindView(R.id.fab)
    FloatingActionButton floatingActionButton;

    private BaseQuickAdapter weiboAdapter;



    public TimelineFragment() {
    }

    public static TimelineFragment newInstance(String param1) {
        TimelineFragment fragment = new TimelineFragment();
        Bundle args = new Bundle();
        args.putString(ARG_SHOW_TEXT, param1);
        fragment.setArguments(args);
        return fragment;

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_timeline, container, false);
        ButterKnife.bind(this,rootView);
        initRefreshLayout();
        queryTimeline();
        return rootView;
    }


    private void queryTimeline() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(MainActivity.HOST)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .build();
        WeiboService weiboSerVice = retrofit.create(WeiboService.class);

        Map<String,String> headers = new HashMap<>();
        headers.put("Authorization", MainActivity.USER_TOKEN);
        headers.put("Accept","application/json; charset=utf-8");

        weiboSerVice.queryTimeline(headers,1,10).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<JSONObject>() {
                    @Override
                    public void onCompleted() {
                        Log.d(TAG, "onCompleted: ");
                    }

                    @Override
                    public void onError(Throwable e){
                        Log.d(TAG, "onError:出错了 "+e);
//                        if (e instanceof HttpException){
//                            try {
//                                ResponseBody responseBody = ((HttpException) e).response().errorBody();
//                                Log.d(TAG, "onError: "+responseBody.string());
//                            }catch (IOException e1){
//                                Log.d(TAG, "onError: "+e);
//                            }
//                        }

                    }

                    @Override
                    public void onNext(JSONObject jsonObject) {
                        if (jsonObject.getIntValue("code")==1){
                            Log.d(TAG, "onNext:完成 "+jsonObject.toString());
                            List<WeiboVo> weiboVoList = JSON.parseArray(jsonObject.getJSONArray("data").toString(),WeiboVo.class);
                            initAdapter(weiboVoList);
                        }
                    }
                });
    }

    private void loadMore(int mark,int position){
        Log.d(TAG, "loadMore: "+mark+"  "+position);
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(MainActivity.HOST)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .build();
        WeiboService weiboSerVice = retrofit.create(WeiboService.class);

        Map<String,String> headers = new HashMap<>();
        headers.put("Authorization", MainActivity.USER_TOKEN);
        headers.put("Accept","application/json; charset=utf-8");

        weiboSerVice.loadWeibo(headers,mark,position,1,10).subscribeOn(Schedulers.io())
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
                    @SuppressWarnings("unchecked")
                    public void onNext(JSONObject jsonObject) {
                        if (jsonObject.getIntValue("code")==1){
                            Log.d(TAG, "onNext:完成 "+jsonObject.toString());
                            List<WeiboVo> weiboVoList = JSON.parseArray(jsonObject.getJSONArray("data").toString(),WeiboVo.class);
                            for(int i=0;i<weiboVoList.size();i++){
                                Log.d(TAG, "onNext: "+ weiboVoList.get(i).getContent());
                            }
                            if (mark==1){
                                weiboAdapter.addData(0,weiboVoList);
                            }else {
                                weiboAdapter.addData(weiboVoList);
                            }
                            weiboAdapter.loadMoreComplete();
                        }
                    }
                });
    }


    private void initAdapter(List<WeiboVo> weiboVoList) {
        weiboAdapter = new WeiboAdapter(R.layout.weibo_item_view, weiboVoList,getActivity());
        weiboAdapter.openLoadAnimation();
        floatingActionButton.setShowAnimation(AnimationUtils.loadAnimation(getActivity(), R.anim.show_from_bottom));
        floatingActionButton.setHideAnimation(AnimationUtils.loadAnimation(getActivity(), R.anim.hide_to_bottom));
        recyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 1));
        recyclerView.setAdapter(weiboAdapter);

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                Log.d(TAG, "onScrolled: "+dx+"  "+dy);
                if (dy>0){
                    floatingActionButton.hide(true);
                }
                if (dy<0){
                    floatingActionButton.show(true);
                }
            }
        });

    }


    @OnClick(R.id.fab)
     void onClickFab(){
        Intent intent = new Intent(getActivity(), PostActivity.class);
        startActivity(intent);
    }


    private void initRefreshLayout() {
        if (getActivity()==null){
            Log.d(TAG, "initRefreshLayout: ");
        }

        smartRefreshLayout.setRefreshHeader(new DeliveryHeader(getActivity()));
        smartRefreshLayout.setRefreshFooter(new BallPulseFooter(getActivity()).setSpinnerStyle(SpinnerStyle.Scale));

        smartRefreshLayout.setOnRefreshListener(refreshLayout -> {
            refreshLayout.finishRefresh(2000);
            if (weiboAdapter!=null){
                WeiboVo weiboVo = (WeiboVo) weiboAdapter.getItem(0);
                if (weiboVo!=null){
                    loadMore(1,weiboVo.getWeiboId());
                }else {
                    queryTimeline();
                }
            }
        });
        smartRefreshLayout.setOnLoadMoreListener(refreshlayout->{
            refreshlayout.finishLoadMore(2000);
            if (weiboAdapter!=null){
                WeiboVo weiboVo = (WeiboVo) weiboAdapter.getItem(weiboAdapter.getItemCount()-1);
                if (weiboVo!=null){
                    loadMore(-1,weiboVo.getWeiboId());
                }else {
                    queryTimeline();
                }
            }
        });
    }






}
