package site.imcu.weibo.adapter;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.bumptech.glide.Glide;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.sackcentury.shinebuttonlib.ShineButton;

import org.litepal.LitePal;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.bingoogolapple.photopicker.activity.BGAPhotoPreviewActivity;
import cn.bingoogolapple.photopicker.widget.BGANinePhotoLayout;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import site.imcu.weibo.MainActivity;
import site.imcu.weibo.ProfileActivity;
import site.imcu.weibo.R;
import site.imcu.weibo.WeiboActivity;
import site.imcu.weibo.entity.Collect;
import site.imcu.weibo.net.LikeService;
import site.imcu.weibo.po.WeiboVo;

public class WeiboAdapter extends BaseQuickAdapter<WeiboVo, BaseViewHolder> implements BGANinePhotoLayout.Delegate {

    private Context context;

    private BGANinePhotoLayout bgaNinePhotoLayout;


    public WeiboAdapter(int layoutResId,@Nullable List<WeiboVo> data,Context context) {
        super(layoutResId,data);
        this.context =context;
    }


    @Override
    protected void convert(BaseViewHolder baseViewHolder, WeiboVo item) {
        baseViewHolder.setText(R.id.weibo_content,item.getContent());
        baseViewHolder.setText(R.id.weibo_username,item.getUser().getUsername());
        baseViewHolder.setText(R.id.weibo_post_time,item.getDate());
        baseViewHolder.setText(R.id.repost_count,String.valueOf(item.getRepostCount()));
        baseViewHolder.setText(R.id.comment_count,String.valueOf(item.getCommentCount()));
        baseViewHolder.setText(R.id.like_count,String.valueOf(item.getLikeCount()));


        ShineButton shineButton = baseViewHolder.getView(R.id.weibo_like);
        Log.d(TAG, "convert: "+item.getLikes());
        if (item.getLikes()!=0){
            shineButton.setChecked(true);
        }

        ImageView headView = baseViewHolder.getView(R.id.weibo_user_avatar);
        Glide.with(context).load(MainActivity.HOST+"imgUpload/"+item.getUser().getFace()).into(headView);

        bgaNinePhotoLayout= baseViewHolder.getView(R.id.weibo_photos);
        ArrayList<String> photos = new ArrayList<>();
        if (item.getPic1()!=null){
            photos.add(MainActivity.HOST+MainActivity.PATH_IMG+item.getPic1());
        }
        if (item.getPic2()!=null){
            photos.add(MainActivity.HOST+MainActivity.PATH_IMG+item.getPic2());
        }
        if (item.getPic3()!=null){
            photos.add(MainActivity.HOST+MainActivity.PATH_IMG+item.getPic3());
        }
        if (item.getPic4()!=null){
            photos.add(MainActivity.HOST+MainActivity.PATH_IMG+item.getPic4());
        }
        if (item.getPic5()!=null){
            photos.add(MainActivity.HOST+MainActivity.PATH_IMG+item.getPic5());
        }
        if (item.getPic6()!=null){
            photos.add(MainActivity.HOST+MainActivity.PATH_IMG+item.getPic6());
        }
        if (item.getPic7()!=null){
            photos.add(MainActivity.HOST+MainActivity.PATH_IMG+item.getPic7());
        }
        if (item.getPic8()!=null){
            photos.add(MainActivity.HOST+MainActivity.PATH_IMG+item.getPic8());
        }
        if (item.getPic9()!=null){
            photos.add(MainActivity.HOST+MainActivity.PATH_IMG+item.getPic9());
        }
        bgaNinePhotoLayout.setDelegate(WeiboAdapter.this);
        bgaNinePhotoLayout.setData(photos);

        List<Collect> collectList = LitePal.where("userId = ? and weiboId = ?",MainActivity.CURRENT_USERVO.getUserId().toString(),item.getWeiboId().toString()).find(Collect.class);



        headView.setOnClickListener(view-> onClickHead(item));

        ShineButton collectView = baseViewHolder.getView(R.id.weibo_repost);
        collectView.setOnClickListener(view-> onClickCollect(item));

        if (collectList.size()==1){
            collectView.setChecked(true);
        }

        ShineButton commentView = baseViewHolder.getView(R.id.weibo_comment);
        commentView.setOnClickListener(view-> onClickComment(item));

        ShineButton likeView = baseViewHolder.getView(R.id.weibo_like);
        likeView.setOnClickListener(view-> onClickLike(item,baseViewHolder));






    }



    private void onClickHead(WeiboVo weiboVo){
        Intent intent = new Intent(context, ProfileActivity.class);
        intent.putExtra("profile", JSON.toJSONString(weiboVo.getUser()));
        context.startActivity(intent);
    }

    private void onClickCollect(WeiboVo weiboVo){
        List<Collect> collectList =  LitePal.where("weiboId = ? and userId = ?",weiboVo.getWeiboId().toString(),MainActivity.CURRENT_USERVO.getUserId().toString()).find(Collect.class);
        if (collectList.size()!=0){
            LitePal.deleteAll(Collect.class,"weiboId = ? and userId =?",weiboVo.getWeiboId().toString(),MainActivity.CURRENT_USERVO.getUserId().toString());
            Toast.makeText(context,"取消收藏",Toast.LENGTH_LONG).show();
        }
        Collect collect = new Collect();
        collect.setUserId(MainActivity.CURRENT_USERVO.getUserId());
        collect.setCollectTime(new Date());
        collect.setWeiboId(weiboVo.getWeiboId());
        collect.setWeiboContent(weiboVo.getContent());
        collect.setWeiboUserFace(weiboVo.getUser().getFace());
        collect.setWeiboUserName(weiboVo.getUser().getUsername());
        collect.save();
        Toast.makeText(context,"收藏成功",Toast.LENGTH_LONG).show();

    }

    private void onClickComment(WeiboVo weiboVo){
        Intent intent = new Intent(context, WeiboActivity.class);
        intent.putExtra("weiboVo",JSON.toJSONString(weiboVo));
        context.startActivity(intent);
    }

    private void onClickLike(WeiboVo weiboVo,BaseViewHolder baseViewHolder){
        ShineButton shineButton = baseViewHolder.getView(R.id.weibo_like);
        TextView textView = baseViewHolder.getView(R.id.like_count);
        int count = Integer.parseInt(textView.getText().toString());
        if (shineButton.isChecked()){
            textView.setText(String.valueOf(count+1));
        }else {
            textView.setText(String.valueOf(count-1));
        }

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(MainActivity.HOST)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .build();
        LikeService likeService = retrofit.create(LikeService.class);


        Map<String,String> headers = new HashMap<>();
        headers.put("Authorization", MainActivity.USER_TOKEN);
        headers.put("Accept","application/json; charset=utf-8");

        likeService.changeLike(headers,weiboVo.getWeiboId()).subscribeOn(Schedulers.io())
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
                            Toast.makeText(context,"点赞(取赞)成功",Toast.LENGTH_SHORT).show();
                        }
                    }
                });


    }






    @Override
    public void onClickNinePhotoItem(BGANinePhotoLayout ninePhotoLayout, View view, int position, String model, List<String> models) {
        Log.d(TAG, "onClickNinePhotoItem: ");
        this.bgaNinePhotoLayout = ninePhotoLayout;
        photoPreviewWrapper();
    }

    private void photoPreviewWrapper() {
        if (bgaNinePhotoLayout == null) {
            return;
        }

        BGAPhotoPreviewActivity.IntentBuilder photoPreviewIntentBuilder = new BGAPhotoPreviewActivity.IntentBuilder(context)
                    .saveImgDir(null);

        if (bgaNinePhotoLayout.getItemCount() == 1) {
            photoPreviewIntentBuilder.previewPhoto(bgaNinePhotoLayout.getCurrentClickItem());
        } else if (bgaNinePhotoLayout.getItemCount() > 1) {
            photoPreviewIntentBuilder.previewPhotos(bgaNinePhotoLayout.getData())
                    .currentPosition(bgaNinePhotoLayout.getCurrentClickItemPosition());
            }
            context.startActivity(photoPreviewIntentBuilder.build());
        }


}
