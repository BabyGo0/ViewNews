package com.example.viewnews.usermodel;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.viewnews.MainActivity;
import com.example.viewnews.R;
import com.example.viewnews.tools.BaseActivity;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.litepal.LitePal;

import java.util.List;

public class ArticleDetailActivity extends BaseActivity {

    public static final String  ARTICLE_NAME = "artile_name";

    public static final String ARTICLE_IMAGE_ID = "artile_image_id";

    public static final String ARTICLE_TIME = "artile_time";

   private String articleName, articleImageId, articleTime;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_article_detail);

        Intent intent = getIntent();

        articleName = intent.getStringExtra(ARTICLE_NAME);
        articleImageId = intent.getStringExtra(ARTICLE_IMAGE_ID);
        articleTime = intent.getStringExtra(ARTICLE_TIME);
        Toolbar toolbar = (Toolbar) findViewById(R.id.article_detail_toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_return_left);
        }

        CollapsingToolbarLayout collapsingToolbar = (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
        ImageView articleImageView = (ImageView) findViewById(R.id.article_image_view);
        TextView articleContentText = (TextView) findViewById(R.id.article_content_text);

        TextView articleAuthor = findViewById(R.id.article_author11);

        TextView articleTime12 = findViewById(R.id.article_time11);

        List<Article> articles = LitePal.where("userId = ? AND articleTitle = ? AND articleTime = ?", MainActivity.currentUserId, articleName, articleTime).find(Article.class);

        collapsingToolbar.setTitle(articleName);
        articleTime12.setText(articleTime);
        articleAuthor.setText(articles.get(0).getArticleAuthor());
        articleContentText.setText(articles.get(0).getArticleContent());

        Glide.with(this).load(articleImageId)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .placeholder(R.mipmap.ic_launcher)
                .error(R.mipmap.ic_launcher)
                .into(articleImageView);


        FloatingActionButton delFab = (FloatingActionButton) findViewById(R.id.delete_article);
        delFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new MaterialDialog.Builder(ArticleDetailActivity.this)
                        .title("Mind")
                        .content("Are you sure to delete this article?")
                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(MaterialDialog dialog, DialogAction which) {
                                int isOk = LitePal.deleteAll(Article.class, "userId = ? AND articleTitle = ? AND articleTime = ?", MainActivity.currentUserId, articleName, articleTime);
                                if(isOk > 0) {
                                    Toast.makeText(ArticleDetailActivity.this, "Delete Successfully!", Toast.LENGTH_SHORT).show();
                                    ArticleDetailActivity.this.finish();
                                } else {
                                    Toast.makeText(ArticleDetailActivity.this, "Delete Failed!", Toast.LENGTH_SHORT).show();
                                }
                            }
                        })
                        .positiveText("Confirm")
                        .negativeText("Cancel")
                        .show();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                ArticleDetailActivity.this.finish();
                return true;
        }
        return true;
    }
}
