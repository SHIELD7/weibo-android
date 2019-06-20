package site.imcu.weibo;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.lxj.xpopup.XPopup;
import com.lxj.xpopup.interfaces.OnConfirmListener;

import org.litepal.LitePal;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import site.imcu.weibo.adapter.CollectAdapter;
import site.imcu.weibo.entity.Collect;


public class CollectActivity extends AppCompatActivity {

    private static final String TAG = "CollectActivity";

    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;
    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_collect);
        ButterKnife.bind(this);
        init();
        initAdapter(queryCollect());
    }

    private void init(){
        setSupportActionBar(toolbar);
        if (getSupportActionBar()!=null){
            getSupportActionBar().setTitle("收藏");
        }
    }


    private List<Collect> queryCollect(){
        List<Collect> collectList = LitePal.where("userId = ?",MainActivity.CURRENT_USERVO.getUserId().toString()).find(Collect.class);
        Log.d(TAG, "queryCollect: "+collectList);
        return collectList;
    }



    private void initAdapter(List<Collect> collectList) {
        CollectAdapter collectAdapter = new CollectAdapter(R.layout.collect_item_view,collectList);
        collectAdapter.openLoadAnimation();
        collectAdapter.setOnItemLongClickListener(((adapter, view, position) -> {
                Intent intent = new Intent(CollectActivity.this,WeiboActivity.class);
                Collect collect = (Collect) adapter.getItem(position);
                if (collect!=null){
                    intent.putExtra("weiboId", collect.getWeiboId());
                    startActivity(intent);
                }
                return false;
        }));

        collectAdapter.setOnItemLongClickListener(((adapter, view, position) -> {
            new XPopup.Builder(CollectActivity.this).asConfirm("我是标题", "我是内容",
                    new OnConfirmListener() {
                        @Override
                        public void onConfirm() {
                            Collect collect = (Collect)adapter.getItem(position);
                            if (collect!=null){
                                int result = LitePal.delete(Collect.class,collect.getId());
                                if (result==1){
                                    Toast.makeText(CollectActivity.this,"取消收藏",Toast.LENGTH_SHORT).show();
                                }else {
                                    Toast.makeText(CollectActivity.this,"未知错误",Toast.LENGTH_SHORT).show();
                                }
                            }

                        }
                    })
                    .show();
            return false;
        }));


        recyclerView.setLayoutManager(new GridLayoutManager(this, 1));
        recyclerView.setAdapter(collectAdapter);

    }


}
