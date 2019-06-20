package site.imcu.weibo.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.scwang.smartrefresh.header.DeliveryHeader;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.constant.SpinnerStyle;
import com.scwang.smartrefresh.layout.footer.BallPulseFooter;
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
import site.imcu.weibo.MainActivity;
import site.imcu.weibo.R;
import site.imcu.weibo.adapter.WeiboAdapter;
import site.imcu.weibo.net.WeiboService;
import site.imcu.weibo.po.WeiboVo;

public class HotFragment extends Fragment {
    private static final String ARG_SHOW_TEXT = "text";
    private static final String TAG = "HotFragment";



    @BindView(R.id.hot_refreshLayout)
    SmartRefreshLayout smartRefreshLayout;

    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;


    public HotFragment() {
    }

    public static HotFragment newInstance(String param1) {
        HotFragment fragment = new HotFragment();
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
        View rootView = inflater.inflate(R.layout.fragment_hot, container, false);
        ButterKnife.bind(this,rootView);
        initRefreshLayout();
        queryAll();
        return rootView;
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

        weiboSerVice.queryAll(headers).subscribeOn(Schedulers.io())
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
        WeiboAdapter weiboAdapter = new WeiboAdapter(R.layout.weibo_item_view, weiboVoList,getActivity());
        weiboAdapter.openLoadAnimation();
        recyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 1));
        recyclerView.setAdapter(weiboAdapter);
    }

    private void initRefreshLayout() {
        if (getActivity()==null){
            Log.d(TAG, "initRefreshLayout: ");
        }
        smartRefreshLayout.setRefreshHeader(new DeliveryHeader(getActivity()));
        smartRefreshLayout.setRefreshFooter(new BallPulseFooter(getActivity()).setSpinnerStyle(SpinnerStyle.Scale));
        smartRefreshLayout.setOnRefreshListener(refreshLayout -> {
            refreshLayout.finishRefresh(2000);
            queryAll();
        });
        smartRefreshLayout.setOnLoadMoreListener(refreshlayout-> refreshlayout.finishLoadMore(2000));
    }






}
