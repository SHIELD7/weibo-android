package site.imcu.weibo.adapter;

import android.content.Context;
import android.content.Intent;
import android.widget.ImageView;

import androidx.annotation.Nullable;

import com.alibaba.fastjson.JSON;
import com.bumptech.glide.Glide;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import java.util.List;

import site.imcu.weibo.MainActivity;
import site.imcu.weibo.ProfileActivity;
import site.imcu.weibo.R;
import site.imcu.weibo.po.CommentVo;

public class CommentAdapter extends BaseQuickAdapter<CommentVo, BaseViewHolder>{

    private Context context;



    public CommentAdapter(int layoutResId, @Nullable List<CommentVo> data, Context context) {
        super(layoutResId,data);
        this.context =context;
    }

    @Override
    protected void convert(BaseViewHolder baseViewHolder, CommentVo item) {
        baseViewHolder.setText(R.id.commenter_content,item.getCommentContent());
        baseViewHolder.setText(R.id.commenter_username,item.getUser().getUsername());
        baseViewHolder.setText(R.id.commenter_time,item.getTime());

        ImageView headView = baseViewHolder.getView(R.id.commenter_user_avatar);
        Glide.with(context).load(MainActivity.HOST+"imgUpload/"+item.getUser().getFace()).into(headView);

        headView.setOnClickListener(view-> onClickHead(item));

    }



    private void onClickHead(CommentVo commentVo){
        Intent intent = new Intent(context, ProfileActivity.class);
        intent.putExtra("profile", JSON.toJSONString(commentVo.getUser()));
        context.startActivity(intent);
    }

}
