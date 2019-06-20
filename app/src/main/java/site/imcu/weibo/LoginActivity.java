package site.imcu.weibo;

import android.content.Intent;
import android.content.SharedPreferences;
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

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";
    private static final int REQUEST_REGISTER = 10;

    @BindView(R.id.input_username)
    EditText usernameText;
    @BindView(R.id.input_password)
    EditText passwordText;
    @BindView(R.id.btn_login)
    Button loginButton;
    @BindView(R.id.link_register)
    TextView registerLink;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);
    }

    @OnClick(R.id.link_register)
    public void goRegister(){
        Intent intent = new Intent(getApplicationContext(), RegisterActivity.class);
        startActivityForResult(intent, REQUEST_REGISTER);
        overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
    }

    @OnClick(R.id.btn_login)
    public void login() {
        if (!checkData()) {
            onLoginFailed();
            return;
        }

        loginButton.setEnabled(false);

        DelayedProgressDialog progressDialog = new DelayedProgressDialog();
        progressDialog.show(getSupportFragmentManager(), "登录中");

        String username = usernameText.getText().toString();
        String password = passwordText.getText().toString();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(MainActivity.HOST)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .build();
        UserService userService = retrofit.create(UserService.class);

        userService.login(username,password).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<JSONObject>() {
                    @Override
                    public void onCompleted() {
                        progressDialog.cancel();

                    }

                    @Override
                    public void onError(Throwable e) {
                        onLoginFailed();
                        progressDialog.cancel();
                    }

                    @Override
                    public void onNext(JSONObject jsonObject) {
                        if (jsonObject.getIntValue("code")==1){
                            onLoginSuccess(jsonObject);
                        }else {
                            onLoginFailed();
                        }
                        progressDialog.cancel();

                    }
                });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode,resultCode,data);
        Log.d(TAG, "onActivityResult: ");
        if (requestCode == REQUEST_REGISTER) {
            if (resultCode == RESULT_OK) {
                String username = data.getStringExtra("username");
                String password = data.getStringExtra("password");
                usernameText.setText(username);
                passwordText.setText(password);
                loginButton.performClick();
                this.finish();
            }
        }
    }


    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }

    public void onLoginSuccess(JSONObject jsonObject) {
        loginButton.setEnabled(true);
        SharedPreferences sharedPreferences = getSharedPreferences("loginInfo", 0);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("token",jsonObject.getString("token"));
        editor.putBoolean("isLogin",true);
        Log.d(TAG, "onLoginSuccess: "+jsonObject.getJSONObject("data").toJSONString());
        editor.putString("userInfo",jsonObject.getJSONObject("data").toJSONString());
        editor.apply();
        Intent intent = new Intent(LoginActivity.this,MainActivity.class);
        startActivity(intent);
        finish();
    }

    public void onLoginFailed() {
        Toast.makeText(getBaseContext(), "登录失败", Toast.LENGTH_LONG).show();
        loginButton.setEnabled(true);
    }

    public boolean checkData() {
        boolean mark = true;
        String username = usernameText.getText().toString();
        String password = passwordText.getText().toString();

        if (username.isEmpty()) {
            usernameText.setError("用户名空");
            mark = false;
        } else {
            usernameText.setError(null);
        }

        if (password.isEmpty() || password.length() < 4 || password.length() > 10) {
            passwordText.setError("密码长度不对");
            mark = false;
        } else {
            passwordText.setError(null);
        }

        return mark;
    }
}
