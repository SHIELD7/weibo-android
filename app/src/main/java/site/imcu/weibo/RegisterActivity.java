package site.imcu.weibo;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.alibaba.fastjson.JSONObject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import site.imcu.weibo.net.UserService;
import site.imcu.weibo.utils.DelayedProgressDialog;

public class RegisterActivity extends AppCompatActivity {
    private static final String TAG = "RegisterActivity";
    @BindView(R.id.input_name)
    EditText nameText;
    @BindView(R.id.input_password)
    EditText passwordText;
    @BindView(R.id.input_reEnterPassword)
    EditText reEnterPasswordText;
    @BindView(R.id.btn_register)
    Button registerButton;
    @BindView(R.id.link_login)
    TextView loginLink;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        ButterKnife.bind(this);
    }

    @OnClick(R.id.link_login)
    public void goLogin(){
        finish();
    }

    @OnClick(R.id.btn_register)
    public void register() {
        if (!checkDate()) {
            onRegisterFailed();
            return;
        }

        registerButton.setEnabled(false);

        DelayedProgressDialog progressDialog = new DelayedProgressDialog();
        progressDialog.show(getSupportFragmentManager(), "创建账号中");

        String username = nameText.getText().toString();
        String password = passwordText.getText().toString();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(MainActivity.HOST)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .build();
        UserService userService = retrofit.create(UserService.class);

        userService.register(username,password).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<JSONObject>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        onRegisterFailed();
                    }

                    @Override
                    public void onNext(JSONObject jsonObject) {
                        Log.d(TAG, "onNext: "+jsonObject);
                        if (jsonObject.getIntValue("code")==1){
                            onRegisterSuccess();
                        }else {
                            onRegisterFailed();
                        }
                        progressDialog.cancel();
                    }
                });

    }


    public void onRegisterSuccess() {
        registerButton.setEnabled(true);
        Intent intent = new Intent();
        intent.putExtra("username",nameText.getText().toString());
        intent.putExtra("password",passwordText.getText().toString());
        setResult(RESULT_OK, intent);
        finish();
    }

    public void onRegisterFailed() {
        Toast.makeText(getBaseContext(), "注册失败", Toast.LENGTH_LONG).show();
        registerButton.setEnabled(true);
    }

    public boolean checkDate() {
        boolean mark = true;

        String name = nameText.getText().toString();
        String password = passwordText.getText().toString();
        String reEnterPassword = reEnterPasswordText.getText().toString();

        if (name.isEmpty()) {
            nameText.setError("还没填写用户名");
            mark = false;
        } else {
            nameText.setError(null);
        }


        if (password.isEmpty() || password.length() < 4 || password.length() > 10) {
            passwordText.setError("密码长度不够");
            mark = false;
        } else {
            passwordText.setError(null);
        }

        if (reEnterPassword.isEmpty() || reEnterPassword.length() < 4 || reEnterPassword.length() > 10 || !(reEnterPassword.equals(password))) {
            reEnterPasswordText.setError("密码不匹配");
            mark = false;
        } else {
            reEnterPasswordText.setError(null);
        }

        return mark;
    }
}