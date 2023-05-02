package com.example.viewnews;

import static org.litepal.LitePalApplication.getContext;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.example.viewnews.tools.ActivityCollector;
import com.example.viewnews.tools.BaseActivity;
import com.example.viewnews.tools.DataCleanManager;
import com.example.viewnews.usermodel.ArticleActivity;
import com.example.viewnews.usermodel.LoginActivity;
import com.example.viewnews.usermodel.UserDetailActivity;
import com.example.viewnews.usermodel.UserFavoriteActivity;
import com.example.viewnews.usermodel.UserInfo;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.tabs.TabLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.litepal.LitePal;
import org.litepal.tablemanager.Connector;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

import cn.pedant.SweetAlert.SweetAlertDialog;
import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends BaseActivity {

    private static final String TAG = "MainActivity";
    private static final int LOCATION_CODE = 9;

    private Toolbar toolbar;
    private DrawerLayout mDrawerLayout;
    private NavigationView navigationView;
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private List<String> list;
    private String latitude;
    private String longitude;
    private FusedLocationProviderClient fusedLocationClient;
    private TextView weather;
    private TextView userNickName, userSignature;
    private ImageView userAvatar;
    public static String currentUserId;
    private String currentUserNickName, currentSignature, currentImagePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar = findViewById(R.id.toolbar);

        Connector.getDatabase();

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        navigationView = (NavigationView) findViewById(R.id.nav_design);

        View v = navigationView.getHeaderView(0);

        CircleImageView circleImageView = (CircleImageView) v.findViewById(R.id.icon_image);

        tabLayout = (TabLayout) findViewById(R.id.tabLayout);

        viewPager = (ViewPager) findViewById(R.id.viewPager);

        list = new ArrayList<>();
    }


    @Override
    protected void onStart() {
        super.onStart();

        toolbar.setTitle("ViewNews");

        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {

            actionBar.setDisplayHomeAsUpEnabled(true);

            actionBar.setHomeAsUpIndicator(R.drawable.ic_menu);
        }

        navigationView.setCheckedItem(R.id.nav_edit);

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {

                mDrawerLayout.closeDrawers();
                switch (menuItem.getItemId()) {
                    case R.id.nav_edit:

                        if (!TextUtils.isEmpty(currentUserId)) {
                            Intent editIntent = new Intent(MainActivity.this, UserDetailActivity.class);
                            editIntent.putExtra("user_edit_id", currentUserId);
                            startActivityForResult(editIntent, 3);
                        } else {
                            Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
                            loginIntent.putExtra("loginStatus", "Please login first！");
                            startActivityForResult(loginIntent, 1);
                        }
                        break;
                    case R.id.nav_articles:

                        if (!TextUtils.isEmpty(currentUserId)) {
                            Intent editIntent = new Intent(MainActivity.this, ArticleActivity.class);
                            editIntent.putExtra("user_article_id", currentUserId);
                            startActivityForResult(editIntent, 6);
                        } else {
                            Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
                            loginIntent.putExtra("loginStatus", "Please login first！");
                            startActivityForResult(loginIntent, 1);
                        }
                        break;
                    case R.id.nav_favorite:
                        if (!TextUtils.isEmpty(currentUserId)) {
                            Intent loveIntent = new Intent(MainActivity.this, UserFavoriteActivity.class);
                            loveIntent.putExtra("user_love_id", currentUserId);
                            startActivity(loveIntent);
                        } else {
                            Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
                            loginIntent.putExtra("loginStatus", "Please login first！");
                            startActivityForResult(loginIntent, 1);
                        }
                        break;
                    case R.id.nav_clear_cache:

                        clearCacheData();
                        break;
                    case R.id.nav_switch:

                        Intent intent = new Intent(MainActivity.this, LoginActivity.class);

                        startActivityForResult(intent, 1);
                        break;
                    default:
                }
                return true;
            }
        });

        list.add("Headlines");
        list.add("Society");
        list.add("China");
        list.add("International");
        list.add("Entertainment");
        list.add("Sports");
        list.add("Military");
        list.add("Technology");
        list.add("Finance");


        viewPager.setAdapter(new FragmentStatePagerAdapter(getSupportFragmentManager(), 1) {

            @Nullable
            @Override
            public CharSequence getPageTitle(int position) {
                return list.get(position);
            }


            @Override
            public Fragment getItem(int position) {
                NewsFragment newsFragment = new NewsFragment();
                Bundle bundle = new Bundle();
                if (list.get(position).equals("Headlines")) {
                    bundle.putString("name", "top");
                } else if (list.get(position).equals("Society")) {
                    bundle.putString("name", "shehui");
                } else if (list.get(position).equals("China")) {
                    bundle.putString("name", "guonei");
                } else if (list.get(position).equals("International")) {
                    bundle.putString("name", "guoji");
                } else if (list.get(position).equals("Entertainment")) {
                    bundle.putString("name", "yule");
                } else if (list.get(position).equals("Sports")) {
                    bundle.putString("name", "tiyu");
                } else if (list.get(position).equals("Military")) {
                    bundle.putString("name", "junshi");
                } else if (list.get(position).equals("Technology")) {
                    bundle.putString("name", "keji");
                } else if (list.get(position).equals("Finance")) {
                    bundle.putString("name", "caijing");
                } else if (list.get(position).equals("Fashion")) {
                    bundle.putString("name", "shishang");
                }

                newsFragment.setArguments(bundle);
                return newsFragment;
            }

            @NonNull
            @Override
            public Object instantiateItem(@NonNull ViewGroup container, int position) {
                NewsFragment newsFragment = (NewsFragment) super.instantiateItem(container, position);
                return newsFragment;
            }

            @Override
            public int getItemPosition(@NonNull Object object) {
                return FragmentStatePagerAdapter.POSITION_NONE;
            }

            @Override
            public int getCount() {
                return list.size();
            }
        });

        tabLayout.setupWithViewPager(viewPager);

        String inputText = load();
        if (!TextUtils.isEmpty(inputText) && TextUtils.isEmpty(currentUserId)) {
            currentUserId = inputText;
        }
        View v = navigationView.getHeaderView(0);
        userNickName = v.findViewById(R.id.text_nickname);
        userSignature = v.findViewById(R.id.text_signature);
        userAvatar = v.findViewById(R.id.icon_image);
        weather = v.findViewById(R.id.weather_textview);
        if (!TextUtils.isEmpty(currentUserId)) {
            List<UserInfo> userInfos = LitePal.where("userAccount = ?", currentUserId).find(UserInfo.class);
            userNickName.setText(userInfos.get(0).getNickName());
            userSignature.setText(userInfos.get(0).getUserSignature());
            currentImagePath = userInfos.get(0).getImagePath();
            System.out.println("Main page data" + userInfos);
            diplayImage(currentImagePath);
        } else {
            userNickName.setText("Please login first");
            userSignature.setText("");
            userAvatar.setImageResource(R.drawable.no_login_avatar);
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION}, LOCATION_CODE);
        } else {

        }
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(getContext());
        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                .addOnSuccessListener(currentlocation -> {
                    if (currentlocation != null) {
                        latitude = String.valueOf(currentlocation.getLatitude());
                        longitude = String.valueOf(currentlocation.getLongitude());
                        sendRequest();
                    }
                    else {
                        Log.d("Main", "get location failed");
                    }
                });

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case LOCATION_CODE: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED
                        && grantResults[1] == PackageManager.PERMISSION_GRANTED) {

                } else {

                }
            }
        }
    }

    private void diplayImage(String imagePath) {
        if (!TextUtils.isEmpty(imagePath)) {
            Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
            userAvatar.setImageBitmap(bitmap);
        } else {
            userAvatar.setImageResource(R.drawable.no_login_avatar);
        }
    }

    private void sendRequest() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpsURLConnection connection = null;
                BufferedReader reader = null;
                try {
                    URL url = new URL("https://api.weatherapi.com/v1/current.json?key=84adf5905ea643d095a224510232904&q="+latitude+","+longitude+"&aqi=no");
                    connection = (HttpsURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");
                    connection.setConnectTimeout(8000);
                    connection.setReadTimeout(8000);
                    InputStream in = connection.getInputStream();
                    reader = new BufferedReader(new InputStreamReader(in));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    Log.v("DiscoverFragment", response.toString());
                    ProcessJSON(response.toString());
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (reader != null) {
                        try {
                            reader.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    if (connection != null) {
                        connection.disconnect();

                    }
                }
            }
        }).start();
    }
    private void ProcessJSON(String jsonData){
        try {
            JSONObject jsonObject = new JSONObject(jsonData);
            JSONObject data = jsonObject.getJSONObject("current");
            String city = jsonObject.getJSONObject("location").getString("name");
            JSONObject condition_data = data.getJSONObject("condition");
            String condition_text = condition_data.getString("text");
            String temp = data.getString("temp_c");
            weather.setText(city+"  "+condition_text+"  "+temp+"°C");

        } catch (JSONException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        View v = navigationView.getHeaderView(0);
        userNickName = v.findViewById(R.id.text_nickname);
        userSignature = v.findViewById(R.id.text_signature);

        switch (requestCode) {
            case 1:
                if (resultCode == RESULT_OK) {
                    Toast.makeText(MainActivity.this, "Login successfully", Toast.LENGTH_SHORT).show();
                    currentUserId = data.getStringExtra("userID");
                    currentUserNickName = data.getStringExtra("userNick");
                    currentSignature = data.getStringExtra("userSign");
                    currentImagePath = data.getStringExtra("imagePath");
                    userNickName.setText(currentUserNickName);
                    userSignature.setText(currentSignature);
                    diplayImage(currentImagePath);
                }
                break;
            case 3:
                if (resultCode == RESULT_OK) {
                    currentUserNickName = data.getStringExtra("nickName");
                    currentSignature = data.getStringExtra("signature");
                    currentImagePath = data.getStringExtra("imagePath");
                    userNickName.setText(currentUserNickName);
                    userSignature.setText(currentSignature);
                    diplayImage(currentImagePath);
                }
                break;
            default:
                break;
        }
    }


    public void clearCacheData() {
        File file = new File(MainActivity.this.getCacheDir().getPath());
        System.out.println("cache directory:" + MainActivity.this.getCacheDir().getPath());
        String cacheSize = null;
        try {
            cacheSize = DataCleanManager.getCacheSize(file);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("Cache size is:" + cacheSize);
        new MaterialDialog.Builder(MainActivity.this)
                .title("Mind")
                .content("Cache size is" + cacheSize + ". Are you sure to clean?")
                .positiveText("Confirm")
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(MaterialDialog dialog, DialogAction which) {
                        DataCleanManager.cleanInternalCache(MainActivity.this);
                        Toast.makeText(MainActivity.this, "Cache Cleaned", Toast.LENGTH_SHORT).show();
                    }
                })
                .negativeText("Cancel")
                .show();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case android.R.id.home:

                mDrawerLayout.openDrawer(GravityCompat.START);
                break;
            case R.id.userFeedback:
                new MaterialDialog.Builder(MainActivity.this)
                        .title("Feedback")
                        .inputRangeRes(1, 50, R.color.colorBlack)
                        .inputType(InputType.TYPE_CLASS_TEXT)
                        .input(null, null, new MaterialDialog.InputCallback() {
                            @Override
                            public void onInput(MaterialDialog dialog, CharSequence input) {
                                System.out.println("Content：" + input);
                                Toast.makeText(MainActivity.this, "Feedback success" + input, Toast.LENGTH_SHORT).show();
                            }
                        })
                        .positiveText("Confirm")
                        .negativeText("Cancel")
                        .show();
                break;
            case R.id.userExit:
                SweetAlertDialog mDialog = new SweetAlertDialog(MainActivity.this, SweetAlertDialog.NORMAL_TYPE)
                        .setTitleText("Warning")
                        .setContentText("Are you sure to exit？")
                        .setCustomImage(null)
                        .setCancelText("Cancel")
                        .setConfirmText("Confirm")
                        .setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
                            @Override
                            public void onClick(SweetAlertDialog sweetAlertDialog) {
                                sweetAlertDialog.dismiss();
                            }
                        }).setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                            @Override
                            public void onClick(SweetAlertDialog sweetAlertDialog) {
                                sweetAlertDialog.dismiss();
                                ActivityCollector.finishAll();
                            }
                        });
                mDialog.show();
                break;
            default:
        }
        return true;
    }

    public String load() {
        FileInputStream in = null;
        BufferedReader reader = null;
        StringBuilder content = new StringBuilder();
        try {
            in = openFileInput("data");
            reader = new BufferedReader(new InputStreamReader(in));
            String line = "";
            while ((line = reader.readLine()) != null) {
                content.append(line);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return content.toString();
    }
}