package site.imcu.weibo.adapter;
import android.util.Log;
import android.widget.ImageView;

import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;

import java.util.List;

import site.imcu.weibo.MainActivity;
import site.imcu.weibo.R;
import site.imcu.weibo.entity.Collect;

public class CollectAdapter extends BaseQuickAdapter<Collect, BaseViewHolder> {

    public CollectAdapter(int layoutResId, @Nullable List<Collect> data) {
        super(layoutResId,data);
    }

    @Override
    protected void convert(BaseViewHolder baseViewHolder, Collect item) {
        Log.d(TAG, "convert: "+item.toString());
        baseViewHolder.setText(R.id.collect_weibo_username,item.getWeiboUserName());
        baseViewHolder.setText(R.id.collect_content,item.getWeiboContent());
        baseViewHolder.setText(R.id.collect_time,item.getCollectTime().toString());
        if (item.getWeiboContent().equals("")||item.getWeiboContent()==null){
            baseViewHolder.setText(R.id.collect_content,"大概是个图？");
        }

        ImageView headView = baseViewHolder.getView(R.id.collect_weibo_user_avatar);
        Glide.with(baseViewHolder.itemView.getContext()).load(MainActivity.HOST+MainActivity.PATH_IMG+item.getWeiboUserFace()).into(headView);

    }

}
