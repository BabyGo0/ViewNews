package com.example.viewnews;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.net.http.SslError;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;
import com.example.viewnews.tools.BaseActivity;
import org.litepal.LitePal;
import java.util.List;


@SuppressLint("SetJavaScriptEnabled")
public class WebActivity extends BaseActivity {

    private WebView webView;

    private Toolbar navToolbar, commentToolBar;

    private String urlData, pageUniquekey, pageTtile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web);
        webView = (WebView) findViewById(R.id.webView);
        navToolbar = (Toolbar) findViewById(R.id.toolbar_webView);
        commentToolBar = (Toolbar) findViewById(R.id.toolbar_webComment);

        findViewById(R.id.toolbar_webComment).bringToFront();
    }


    @Override
    protected void onStart() {
        super.onStart();

        urlData = getIntent().getStringExtra("pageUrl");
        pageUniquekey = getIntent().getStringExtra("uniquekey");
        pageTtile = getIntent().getStringExtra("news_title");

        System.out.println("ID For Current News:" + pageUniquekey);
        System.out.println("Title For Current News:" + pageTtile);

        WebSettings settings = webView.getSettings();

        settings.setJavaScriptEnabled(true);

        settings.setSupportZoom(true);

        settings.setBuiltInZoomControls(true);

        settings.setJavaScriptCanOpenWindowsAutomatically(true);

        settings.setUseWideViewPort(true);

        settings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NORMAL);

        settings.setLoadWithOverviewMode(true);

        settings.setDisplayZoomControls(false);

        webView.loadUrl(urlData);


        setSupportActionBar(commentToolBar);

        navToolbar.setTitle("ViewNews");
        setSupportActionBar(navToolbar);
        commentToolBar.inflateMenu(R.menu.tool_webbottom);
        commentToolBar.setTitle("Thanks for Watching");

        webView.setWebViewClient(new WebViewClient() {

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);

                List<NewsCollectBean> beanList = LitePal.where("userIdNumer = ? AND newsId = ?", MainActivity.currentUserId == null ? "" : MainActivity.currentUserId, pageUniquekey).find(NewsCollectBean.class);

                MenuItem u = commentToolBar.getMenu().getItem(0);
                if(beanList.size() > 0) {
                    u.setIcon(R.drawable.ic_star_border_favourite_yes);
                } else {
                    u.setIcon(R.drawable.ic_star_border_favourite_no);
                }
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);

                view.loadUrl("javascript:function setTop1(){document.querySelector('body > div.top-wrap.gg-item.J-gg-item').style.display=\"none\";}setTop1();");
                view.loadUrl("javascript:function setTop4(){document.querySelector('body > a.piclick-link').style.display=\"none\";}setTop4();");
                view.loadUrl("javascript:function setTop2(){document.querySelector('#news_check').style.display=\"none\";}setTop2();");
                view.loadUrl("javascript:function setTop3(){document.querySelector('body > div.articledown-wrap gg-item J-gg-item').style.display=\"none\";}setTop3();");
            }

            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                handler.proceed();
            }
        });

        webView.setWebChromeClient(new WebChromeClient(){
            // 每次网页加载进度改变时，就会执行一次js代码，保证广告一出来就被干掉
            // 缺点也很明显，会执行很多次无效的js代码。
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                super.onProgressChanged(view, newProgress);
                view.loadUrl("javascript:function setTop1(){document.querySelector('body > div.top-wrap.gg-item.J-gg-item').style.display=\"none\";}setTop1();");
                view.loadUrl("javascript:function setTop4(){document.querySelector('body > a.piclick-link').style.display=\"none\";}setTop4();");
                view.loadUrl("javascript:function setTop2(){document.querySelector('#news_check').style.display=\"none\";}setTop2();");
                view.loadUrl("javascript:function setTop3(){document.querySelector('body > div.articledown-wrap gg-item J-gg-item').style.display=\"none\";}setTop3();");
            }
        });


        commentToolBar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.news_share:
                        Intent intent = new Intent(Intent.ACTION_SEND);
                        intent.putExtra(Intent.EXTRA_SUBJECT, urlData);
                        intent.setType("text/plain");
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(Intent.createChooser(intent, getTitle()));
                        break;
                    case R.id.news_collect:
                        if(!TextUtils.isEmpty(MainActivity.currentUserId)) {
                            MenuItem u = commentToolBar.getMenu().getItem(0);
                            List<NewsCollectBean> bean = LitePal.where("userIdNumer = ? AND newsId = ?", MainActivity.currentUserId, pageUniquekey).find(NewsCollectBean.class);
                            NewsCollectBean currentNews = null;
                            System.out.println(bean);
                            String answer = "";
                            if(bean.size() > 0) {
                                int i = LitePal.deleteAll(NewsCollectBean.class, "userIdNumer = ? AND newsId = ?", MainActivity.currentUserId, pageUniquekey);
                                if(i > 0) {
                                    answer = "Canceled！";
                                    u.setIcon(R.drawable.ic_star_border_favourite_no);
                                } else answer = "Canceled Failed！";
                            } else {
                                currentNews = new NewsCollectBean();
                                currentNews.setUserIdNumer(MainActivity.currentUserId);
                                currentNews.setNewSTitle(pageTtile);
                                currentNews.setNewsId(pageUniquekey);
                                currentNews.setNewsUrl(urlData);
                                boolean isSave = currentNews.save();
                                System.out.println("Favourite News：" + currentNews);
                                if(isSave){
                                    answer = "Succeed！";
                                    u.setIcon(R.drawable.ic_star_border_favourite_yes);
                                }
                                else answer = "Failed！";
                            }
                            Toast.makeText(WebActivity.this , answer, Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(WebActivity.this, "Please login first！", Toast.LENGTH_SHORT).show();
                        }
                        break;
                }
                return true;
            }
        });
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_return_left);
        }
    }


    @Override
    protected void onStop()
    {
        super.onStop();
        webView.getSettings().setJavaScriptEnabled(false);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.toolbar_webview, menu);

        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case android.R.id.home:
                Intent returnIntent = new Intent();
                setResult(RESULT_OK, returnIntent);
                WebActivity.this.finish();
                break;

            case R.id.news_setting:


                int currentNightMode = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;//
                switch (currentNightMode) {
                    case Configuration.UI_MODE_NIGHT_NO:
                        // Night mode is not active, we're using the light theme
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                        Toast.makeText(this, "Dark Mode Actived", Toast.LENGTH_SHORT).show();
                        break;
                    case Configuration.UI_MODE_NIGHT_YES:
                        // Night mode is active, we're using dark theme
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                        Toast.makeText(this, "Light Mode Actived", Toast.LENGTH_SHORT).show();
                        break;
                }
                break;
            case R.id.news_feedback:
                Toast.makeText(this, "Report Received", Toast.LENGTH_SHORT).show();
                break;
            default:
                break;
        }
        return true;
    }
}