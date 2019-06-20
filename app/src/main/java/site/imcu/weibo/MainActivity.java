package site.imcu.weibo;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.alibaba.fastjson.JSON;
import com.flyco.tablayout.CommonTabLayout;
import com.flyco.tablayout.listener.CustomTabEntity;
import com.flyco.tablayout.listener.OnTabSelectListener;
import com.joanzapata.iconify.Iconify;
import com.joanzapata.iconify.fonts.FontAwesomeModule;
import com.mikepenz.fontawesome_typeface_library.FontAwesome;
import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IProfile;
import com.mikepenz.materialdrawer.model.interfaces.Nameable;
import com.mikepenz.materialdrawer.util.AbstractDrawerImageLoader;
import com.mikepenz.materialdrawer.util.DrawerImageLoader;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import site.imcu.weibo.adapter.MyFragmentPagerAdapter;
import site.imcu.weibo.entity.TabEntity;
import site.imcu.weibo.fragment.BlankFragment;
import site.imcu.weibo.fragment.HotFragment;
import site.imcu.weibo.fragment.TimelineFragment;
import site.imcu.weibo.po.UserVo;

public class MainActivity extends AppCompatActivity {

   // public static final String HOST = "https://imcu.site/";
    public static final String HOST = "http://47.101.204.122/";
    public static final String PATH_IMG = "imgUpload/";
    private static final String TAG = "MainActivity";
    public static String USER_TOKEN;
    public static UserVo CURRENT_USERVO;
    private String[] mTitles = {"首页", "消息", "热门"};

    @BindView(R.id.fragment_vp)
    ViewPager viewPager;

    @BindView(R.id.tab)
    CommonTabLayout commonTabLayout;

    @BindView(R.id.toolbar)
    Toolbar toolbar;


    private ArrayList<CustomTabEntity> customTabEntities = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Iconify
                .with(new FontAwesomeModule());
        ButterKnife.bind(this);
        if (checkLogin()){
            initView();
        }
    }



    private void initView() {

        setSupportActionBar(toolbar);
        if (getSupportActionBar()!=null){
            getSupportActionBar().setTitle(R.string.app_name);
        }
        List<Fragment> fragmentList = new ArrayList<>();
        fragmentList.add(TimelineFragment.newInstance("微博"));
        fragmentList.add(BlankFragment.newInstance("消息"));
        fragmentList.add(HotFragment.newInstance("热门"));
        FragmentStatePagerAdapter fragmentStatePagerAdapter;
        fragmentStatePagerAdapter = new MyFragmentPagerAdapter(getSupportFragmentManager(), fragmentList);
        viewPager.setAdapter(fragmentStatePagerAdapter);
        viewPager.addOnPageChangeListener(onPageChangeListener);


        for (String title :mTitles) {
            customTabEntities.add(new TabEntity(title));
        }
        commonTabLayout.setTabData(customTabEntities);
        commonTabLayout.setOnTabSelectListener(onTabSelectListener);


        AccountHeader headerResult;
        IProfile profile = new ProfileDrawerItem().withName(CURRENT_USERVO.getUsername()).withIcon(MainActivity.HOST + PATH_IMG+CURRENT_USERVO.getFace());
        overrideDrawerImageLoaderPicasso();
        headerResult = new AccountHeaderBuilder()
                .withActivity(this)
                .withCompactStyle(true)
                .withSelectionListEnabledForSingleProfile(false)
                .addProfiles(
                        profile
                )
                .build();

        new DrawerBuilder()
                .withActivity(this)
                .withAccountHeader(headerResult)
                .addDrawerItems(
                        new PrimaryDrawerItem().withName(R.string.drawer_profile).withIcon(FontAwesome.Icon.faw_male),
                        new PrimaryDrawerItem().withName(R.string.drawer_edit_profile).withIcon(FontAwesome.Icon.faw_edit2),
                        new PrimaryDrawerItem().withName(R.string.drawer_comment_like).withIcon(FontAwesome.Icon.faw_heart2),
                        new PrimaryDrawerItem().withName(R.string.drawer_collect).withIcon(FontAwesome.Icon.faw_star2),
                        new PrimaryDrawerItem().withName(R.string.drawer_friends).withIcon(FontAwesome.Icon.faw_user2),
                        new PrimaryDrawerItem().withName(R.string.drawer_topic).withIcon(FontAwesome.Icon.faw_hashtag),
                        new PrimaryDrawerItem().withName(R.string.drawer_theme).withIcon(FontAwesome.Icon.faw_paint_brush),
                        new PrimaryDrawerItem().withName(R.string.drawer_setting).withIcon(FontAwesome.Icon.faw_cog),
                        new PrimaryDrawerItem().withName(R.string.drawer_github).withIcon(FontAwesome.Icon.faw_github),
                        new PrimaryDrawerItem().withName(R.string.drawer_logout).withIcon(FontAwesome.Icon.faw_sign_out_alt)
                )
                .withOnDrawerItemClickListener((View view, int position, IDrawerItem drawerItem)-> onDrawerItemClicked(drawerItem))
                .build();

    }

    private boolean onDrawerItemClicked(IDrawerItem drawerItem){
        String tag = ((Nameable) drawerItem).getName().getText(MainActivity.this);
        Log.d(TAG, "onDrawerItemClicked: "+((Nameable) drawerItem).getName().getText(MainActivity.this));

        if (tag.equals(getResources().getString(R.string.drawer_logout))){
            SharedPreferences sharedPreferences = getSharedPreferences("loginInfo", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.clear();
            editor.apply();
            Intent intent = new Intent(MainActivity.this,LoginActivity.class);
            startActivity(intent);
        }
        if (tag.equals(getResources().getString(R.string.drawer_edit_profile))){
            Intent intent = new Intent(MainActivity.this,UserInfoActivity.class);
            startActivity(intent);
        }
        if (tag.equals(getResources().getString(R.string.drawer_collect))){
            Intent intent = new Intent(MainActivity.this, CollectActivity.class);
            startActivity(intent);
        }
        if (tag.equals(getResources().getString(R.string.drawer_github))){
            Uri uri = Uri.parse("https://github.com/SHIELD7/weibo-android");
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(intent);
        }
        return false;
    }


    //https://github.com/mikepenz/MaterialDrawer/issues/2380
    private void overrideDrawerImageLoaderPicasso(){
        DrawerImageLoader.init(new AbstractDrawerImageLoader() {
            @Override
            public void set(ImageView imageView, Uri uri, Drawable placeholder) {
                Picasso.get().load(uri).placeholder(placeholder).into(imageView);
            }

            @Override
            public void cancel(ImageView imageView) {
                Picasso.get().cancelRequest(imageView);
            }
        });
    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
        viewPager.removeOnPageChangeListener(onPageChangeListener);
    }

    private  OnTabSelectListener onTabSelectListener = new OnTabSelectListener() {
        @Override
        public void onTabSelect(int position) {
            viewPager.setCurrentItem(position);
        }

        @Override
        public void onTabReselect(int position) {

        }
    };


    private ViewPager.OnPageChangeListener onPageChangeListener = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }

        @Override
        public void onPageSelected(int position) {
            commonTabLayout.setCurrentTab(position);
        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }
    };




    private boolean checkLogin(){
        SharedPreferences sharedPreferences = getSharedPreferences("loginInfo",0);
        boolean isLogin =  sharedPreferences.getBoolean("isLogin",false);
        if (isLogin){
            String string = sharedPreferences.getString("userInfo",null);
            Log.d(TAG, "checkLogin: "+string);
            CURRENT_USERVO = JSON.parseObject(string,UserVo.class);
            USER_TOKEN = sharedPreferences.getString("token",null);
            Log.d(TAG, "checkLogin: "+CURRENT_USERVO.toString());
            return true;
        }else {
            Intent intent = new Intent(MainActivity.this,LoginActivity.class);
            startActivity(intent);
            finish();
        }
        return false;

    }

}
