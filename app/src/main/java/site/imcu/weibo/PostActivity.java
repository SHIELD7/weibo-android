package site.imcu.weibo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.alibaba.fastjson.JSONObject;
import java.io.File;
import java.net.FileNameMap;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.bingoogolapple.photopicker.activity.BGAPhotoPickerActivity;
import cn.bingoogolapple.photopicker.activity.BGAPhotoPickerPreviewActivity;
import cn.bingoogolapple.photopicker.widget.BGASortableNinePhotoLayout;
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
import site.imcu.weibo.net.WeiboService;
import site.imcu.weibo.po.WeiboVo;
import site.imcu.weibo.utils.DelayedProgressDialog;

public class PostActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks,BGASortableNinePhotoLayout.Delegate{

    private static final int PRC_PHOTO_PICKER = 1;
    private static final int RC_CHOOSE_PHOTO = 1;
    private static final int RC_PHOTO_PREVIEW = 2;
    private static final String TAG = "PostActivity";

    private DelayedProgressDialog progressDialog;

    @BindView(R.id.post_content)
    EditText editContent;


    @BindView(R.id.post_add_photos)
    BGASortableNinePhotoLayout bgaSortableNinePhotoLayout;

    @BindView(R.id.toolbar)
    Toolbar toolbar;


    private List<String> backFileNameList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);
        ButterKnife.bind(this);
        init();
    }

    private void init(){
        setSupportActionBar(toolbar);
        if (getSupportActionBar()!=null){
            getSupportActionBar().setTitle("发微博");
        }
        bgaSortableNinePhotoLayout.setDelegate(this);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.post_toolbar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.toolbar_send) {
            progressDialog = new DelayedProgressDialog();
            progressDialog.show(getSupportFragmentManager(), "发布中");
            if (bgaSortableNinePhotoLayout.getItemCount()==0){
                onUploadFinish();
            }else {
                uploadImage();
            }
        }
        return true;
    }

    private void uploadImage(){

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(MainActivity.HOST)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .build();
        WeiboService weiboService = retrofit.create(WeiboService.class);


        Map<String,String> headers = new HashMap<>();
        headers.put("Authorization", MainActivity.USER_TOKEN);
        headers.put("Accept","application/json; charset=utf-8");

        List<File> fileList = new ArrayList<>();

        for(int i = 0; i< bgaSortableNinePhotoLayout.getData().size(); i++){
            fileList.add(new File(bgaSortableNinePhotoLayout.getData().get(i)));
        }

        for(int i=0;i<fileList.size();i++){

            RequestBody requestFile = RequestBody.create(guessMimeType(fileList.get(i).getPath()),fileList.get(i));

            MultipartBody.Part body = MultipartBody.Part.createFormData("file", fileList.get(i).getName(), requestFile);

            weiboService.uploadImg(headers,body).subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Subscriber<JSONObject>() {
                        @Override
                        public void onCompleted() {
                            progressDialog.cancel();
                        }

                        @Override
                        public void onError(Throwable e) {
                            progressDialog.cancel();
                            Toast.makeText(PostActivity.this,e.getMessage(),Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onNext(JSONObject jsonObject) {
                            if (jsonObject.getIntValue("code")==1){
                                backFileNameList.add(jsonObject.getString("data"));
                                if (backFileNameList.size() == bgaSortableNinePhotoLayout.getItemCount()){
                                    onUploadFinish();
                                }
                            }else {
                                Toast.makeText(PostActivity.this,"上传图片失败",Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

        }


    }

    private void onUploadFinish(){

        WeiboVo weiboVo = new WeiboVo();
        weiboVo.setContent(editContent.getText().toString());
        Log.d(TAG, "onClick: "+ bgaSortableNinePhotoLayout.getData());

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(MainActivity.HOST)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .build();
        WeiboService weiboService = retrofit.create(WeiboService.class);


        Map<String,String> headers = new HashMap<>();
        headers.put("Authorization", MainActivity.USER_TOKEN);
        headers.put("Accept","application/json; charset=utf-8");

        Map<String,RequestBody> requestBodyMap = new HashMap<>();
        RequestBody requestBody = RequestBody.create(MediaType.parse("multipart/form-data"),editContent.getText().toString());
        requestBodyMap.put("content",requestBody);

        for(int i=0;i<backFileNameList.size();i++){
            requestBody = RequestBody.create(MediaType.parse("multipart/form-data"),backFileNameList.get(i));
            requestBodyMap.put("pic"+(i+1), requestBody);
        }

        weiboService.postWeibo(headers,requestBodyMap).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<JSONObject>() {
                    @Override
                    public void onCompleted() {
                        progressDialog.dismiss();
                    }

                    @Override
                    public void onError(Throwable e) {
                        Toast.makeText(PostActivity.this,"发送失败,请求出错"+e.getMessage(),Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onNext(JSONObject jsonObject) {
                        if (jsonObject.getIntValue("code")==1){
                            Toast.makeText(PostActivity.this,"发送成功",Toast.LENGTH_SHORT).show();
                            finish();
                        }else {
                            Toast.makeText(PostActivity.this,"发送失败,请求得到响应",Toast.LENGTH_SHORT).show();
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
    public void onClickAddNinePhotoItem(BGASortableNinePhotoLayout sortableNinePhotoLayout, View view, int position, ArrayList<String> models) {
        choicePhotoWrapper();
    }

    @Override
    public void onClickDeleteNinePhotoItem(BGASortableNinePhotoLayout sortableNinePhotoLayout, View view, int position, String model, ArrayList<String> models) {
        bgaSortableNinePhotoLayout.removeItem(position);
    }

    @Override
    public void onClickNinePhotoItem(BGASortableNinePhotoLayout sortableNinePhotoLayout, View view, int position, String model, ArrayList<String> models) {
        Intent photoPickerPreviewIntent = new BGAPhotoPickerPreviewActivity.IntentBuilder(this)
                .previewPhotos(models)
                .selectedPhotos(models)
                .maxChooseCount(bgaSortableNinePhotoLayout.getMaxItemCount())
                .currentPosition(position)
                .isFromTakePhoto(true)
                .build();
        startActivityForResult(photoPickerPreviewIntent, RC_PHOTO_PREVIEW);
    }

    @Override
    public void onNinePhotoItemExchanged(BGASortableNinePhotoLayout sortableNinePhotoLayout, int fromPosition, int toPosition, ArrayList<String> models) {
        Toast.makeText(this, "排序发生变化", Toast.LENGTH_SHORT).show();
    }

    @AfterPermissionGranted(PRC_PHOTO_PICKER)
    private void choicePhotoWrapper() {
        String[] perms = {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA};
        if (EasyPermissions.hasPermissions(this, perms)) {
            File takePhotoDir = new File(Environment.getExternalStorageDirectory(), "BGAPhotoPickerTakePhoto");

            Intent photoPickerIntent = new BGAPhotoPickerActivity.IntentBuilder(this)
                    .cameraFileDir(takePhotoDir)
                    .maxChooseCount(bgaSortableNinePhotoLayout.getMaxItemCount() - bgaSortableNinePhotoLayout.getItemCount())
                    .selectedPhotos(null)
                    .pauseOnScroll(false)
                    .build();
            startActivityForResult(photoPickerIntent, RC_CHOOSE_PHOTO);
        } else {
            EasyPermissions.requestPermissions(this, "图片选择需要以下权限:\n\n1.访问设备上的照片\n\n2.拍照", PRC_PHOTO_PICKER, perms);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == RC_CHOOSE_PHOTO) {
            bgaSortableNinePhotoLayout.addMoreData(BGAPhotoPickerActivity.getSelectedPhotos(data));
        } else if (requestCode == RC_PHOTO_PREVIEW) {
            bgaSortableNinePhotoLayout.setData(BGAPhotoPickerPreviewActivity.getSelectedPhotos(data));
        }
    }

    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {
        choicePhotoWrapper();
    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {
        Toast.makeText(PostActivity.this,"您拒绝了权限",Toast.LENGTH_SHORT).show();
    }


}
