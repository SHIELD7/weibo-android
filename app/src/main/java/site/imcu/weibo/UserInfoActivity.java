package site.imcu.weibo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestOptions;
import com.vondear.rxtool.RxPhotoTool;
import com.vondear.rxtool.RxSPTool;
import com.vondear.rxtool.RxTool;
import com.vondear.rxui.view.dialog.RxDialogChooseImage;
import com.yalantis.ucrop.UCrop;
import com.yalantis.ucrop.UCropActivity;

import java.io.File;
import java.net.FileNameMap;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import site.imcu.weibo.net.UserService;
import site.imcu.weibo.po.UserVo;

import static com.vondear.rxui.view.dialog.RxDialogChooseImage.LayoutType.TITLE;

public class UserInfoActivity extends AppCompatActivity{

    private static final String TAG = "UserInfoActivity";


    @BindView(R.id.img_avatar)
    ImageView imageAvatar;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    private Uri resultUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_info);
        ButterKnife.bind(this);
        init();
    }

    private void init(){
        setSupportActionBar(toolbar);
        if (getSupportActionBar()!=null){
            getSupportActionBar().setTitle("用户资料");
        }
        RxTool.init(this);
        imageAvatar.setOnClickListener(v -> initDialogChooseImage());
    }

    @AfterPermissionGranted(2)
    private void initDialogChooseImage() {
        String[] perms = {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA};
        if (EasyPermissions.hasPermissions(this, perms)) {
            RxDialogChooseImage dialogChooseImage = new RxDialogChooseImage(UserInfoActivity.this, TITLE);
            dialogChooseImage.show();
        } else {
            EasyPermissions.requestPermissions(this, "请开起存储空间和相机权限，以正常使用", 2, perms);
        }

    }



    @OnClick(R.id.btn_submit)
    public void onSubmit(){
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(MainActivity.HOST)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .build();
        UserService userService = retrofit.create(UserService.class);
        Log.d(TAG, "onSubmit: ");

        File file = new File(resultUri.getPath());

        Map<String,String> headers = new HashMap<>();
        headers.put("Authorization", MainActivity.USER_TOKEN);
        headers.put("Accept","application/json; charset=utf-8");

        RequestBody requestFile = RequestBody.create(guessMimeType(file.getPath()),file);

        MultipartBody.Part part = MultipartBody.Part.createFormData("file", file.getName(), requestFile);

        userService.changeAvatar(headers,part).subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Subscriber<JSONObject>() {
                        @Override
                        public void onCompleted() {
                            Log.d(TAG, "onCompleted: ");
                        }

                        @Override
                        public void onError(Throwable e) {
                            Log.d(TAG, "onError: "+e.getMessage());
//                            if (e instanceof HttpException){
//                                ResponseBody responseBody = ((HttpException) e).response().errorBody();
//                                try {
//                                    Log.d(TAG, "onError: "+responseBody.string());
//                                }catch (IOException e1){
//                                }
//                            }

                        }

                        @Override
                        public void onNext(JSONObject jsonObject) {
                            Log.d(TAG, "onNext: "+jsonObject);
                            if (jsonObject.getIntValue("code")==1){
                                Toast.makeText(UserInfoActivity.this,"修改成功",Toast.LENGTH_SHORT).show();
                                SharedPreferences sharedPreferences = getSharedPreferences("loginInfo",0);
                                String string = sharedPreferences.getString("userInfo",null);
                                UserVo userVo = JSON.parseObject(string, UserVo.class);
                                if (userVo!=null){
                                    userVo.setFace(jsonObject.getString("data"));
                                }
                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                editor.putString("userInfo",JSON.toJSONString(userVo));
                                editor.apply();
                            }else {
                                Toast.makeText(UserInfoActivity.this,"修改失败",Toast.LENGTH_SHORT).show();
                            }
                        }
                    });



    }

    private static MediaType guessMimeType(String path) {
        FileNameMap fileNameMap = URLConnection.getFileNameMap();
        path = path.replace("#", "");
        String contentType = fileNameMap.getContentTypeFor(path);
        if (contentType == null) {
            contentType = "application/octet-stream";
        }
        return MediaType.parse(contentType);
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case RxPhotoTool.GET_IMAGE_FROM_PHONE:
                if (resultCode == RESULT_OK) {
                    initUCrop(data.getData());
                }

                break;
            case RxPhotoTool.GET_IMAGE_BY_CAMERA://选择照相机之后的处理
                if (resultCode == RESULT_OK) {
                    initUCrop(RxPhotoTool.imageUriFromCamera);
                }

                break;
            case RxPhotoTool.CROP_IMAGE://普通裁剪后的处理
                RequestOptions options = new RequestOptions()
                        .placeholder(R.drawable.circle_elves_ball)
                        .error(R.drawable.circle_elves_ball)
                        //禁止Glide硬盘缓存缓存
                        .diskCacheStrategy(DiskCacheStrategy.RESOURCE);

                Glide.with(UserInfoActivity.this).
                        load(RxPhotoTool.cropImageUri).
                        apply(options).
                        thumbnail(0.5f).
                        into(imageAvatar);
                break;

            case UCrop.REQUEST_CROP://UCrop裁剪之后的处理
                if (resultCode == RESULT_OK) {
                    resultUri = UCrop.getOutput(data);
                    roadImageView(resultUri, imageAvatar);
                    RxSPTool.putContent(UserInfoActivity.this, "AVATAR", resultUri.toString());
                }
                break;
            default:
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void roadImageView(Uri uri, ImageView imageView) {
        RequestOptions options = new RequestOptions()
                .placeholder(R.drawable.circle_elves_ball)
                .error(R.drawable.circle_elves_ball)
                .transform(new CircleCrop())
                .diskCacheStrategy(DiskCacheStrategy.RESOURCE);

        Glide.with(UserInfoActivity.this).
                load(uri).
                apply(options).
                thumbnail(0.5f).
                into(imageView);
    }

    private void initUCrop(Uri uri) {
        SimpleDateFormat timeFormatter = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.CHINA);
        long time = System.currentTimeMillis();
        String imageName = timeFormatter.format(new Date(time));
        Uri destinationUri = Uri.fromFile(new File(getCacheDir(), imageName + ".jpeg"));
        UCrop.Options options = new UCrop.Options();
        options.setAllowedGestures(UCropActivity.SCALE, UCropActivity.ROTATE, UCropActivity.ALL);
        options.setToolbarColor(ActivityCompat.getColor(this, R.color.colorPrimary));
        options.setStatusBarColor(ActivityCompat.getColor(this, R.color.colorPrimaryDark));
        options.setMaxScaleMultiplier(5);
        options.setImageToCropBoundsAnimDuration(666);

        UCrop.of(uri, destinationUri)
                .withAspectRatio(1, 1)
                .withMaxResultSize(1000, 1000)
                .withOptions(options)
                .start(this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }


}
