package site.imcu.weibo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import java.io.File;
import java.net.FileNameMap;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.bingoogolapple.baseadapter.BGABaseAdapterUtil;
import cn.bingoogolapple.photopicker.imageloader.BGAImage;
import cn.bingoogolapple.photopicker.util.BGAPhotoHelper;
import cn.bingoogolapple.photopicker.util.BGAPhotoPickerUtil;
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

public class UserInfoActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks{

    private static final String TAG = "UserInfoActivity";

    @BindView(R.id.img_avatar)
    ImageView imageAvatar;
    @BindView(R.id.toolbar)
    Toolbar toolbar;


    private static final int REQUEST_CODE_PERMISSION_CHOOSE_PHOTO = 1;
    private static final int REQUEST_CODE_CHOOSE_PHOTO = 1;
    private static final int REQUEST_CODE_CROP = 3;


    private BGAPhotoHelper bgaPhotoHelper;

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
        File takePhotoDir = new File(Environment.getExternalStorageDirectory(), "BGAPhotoPickerTakePhoto");
        bgaPhotoHelper = new BGAPhotoHelper(takePhotoDir);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        BGAPhotoHelper.onSaveInstanceState(bgaPhotoHelper, outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        BGAPhotoHelper.onRestoreInstanceState(bgaPhotoHelper, savedInstanceState);
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

        if (bgaPhotoHelper.getCropFilePath()==null){
            Toast.makeText(UserInfoActivity.this,"文件不存在",Toast.LENGTH_SHORT).show();
            return;
        }


        File file = new File(bgaPhotoHelper.getCropFilePath());

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


    @OnClick(R.id.choose_system)
    @AfterPermissionGranted(REQUEST_CODE_PERMISSION_CHOOSE_PHOTO)
    public void choosePhoto() {
        String[] perms = {Manifest.permission.WRITE_EXTERNAL_STORAGE};
        if (EasyPermissions.hasPermissions(this, perms)) {
            startActivityForResult(bgaPhotoHelper.getChooseSystemGalleryIntent(), REQUEST_CODE_CHOOSE_PHOTO);
        } else {
            EasyPermissions.requestPermissions(this, "请开起存储空间权限，以正常使用 Demo", REQUEST_CODE_PERMISSION_CHOOSE_PHOTO, perms);
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResult: "+bgaPhotoHelper.getCropFilePath());
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK&&data.getData()!=null) {
            if (requestCode == REQUEST_CODE_CHOOSE_PHOTO) {
                try {
                    startActivityForResult(bgaPhotoHelper.getCropIntent(getFilePathFromUri(data.getData()), 200, 200), REQUEST_CODE_CROP);
                } catch (Exception e) {
                    bgaPhotoHelper.deleteCropFile();
                    BGAPhotoPickerUtil.show(R.string.bga_pp_not_support_crop);
                    e.printStackTrace();
                }
            } else if (requestCode == REQUEST_CODE_CROP) {
              BGAImage.display(imageAvatar, R.mipmap.bga_pp_ic_holder_light, bgaPhotoHelper.getCropFilePath(), BGABaseAdapterUtil.dp2px(200));
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {

    }

    @Override
    public void onPermissionsDenied(int requestCode,@NonNull List<String> perms) {
    }

    private  String getFilePathFromUri(Uri uri) {
        if (uri == null) {
            return null;
        }

        String scheme = uri.getScheme();
        String filePath = null;
        if (TextUtils.isEmpty(scheme) || TextUtils.equals(ContentResolver.SCHEME_FILE, scheme)) {
            filePath = uri.getPath();
        } else if (TextUtils.equals(ContentResolver.SCHEME_CONTENT, scheme)) {
            String[] filePathColumn = {MediaStore.MediaColumns.DATA};
            Cursor cursor = BGABaseAdapterUtil.getApp().getContentResolver().query(uri, filePathColumn, null, null, null);
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                    if (columnIndex > -1) {
                        filePath = cursor.getString(columnIndex);
                    }
                }
                cursor.close();
            }
        }
        return filePath;
    }

}
