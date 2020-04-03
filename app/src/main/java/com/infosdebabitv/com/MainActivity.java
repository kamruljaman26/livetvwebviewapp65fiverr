package com.infosdebabitv.com;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    @Override // On Create Method
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //toolbar & Navigation
        createHeaderNavigation();

        //Webview & TV
        loadWebViewTV(savedInstanceState);
    }


    /**
     * Load Live TV In Webview
     */
    private WebView webView;
    private void loadWebViewTV(Bundle savedInstanceState) {
        //WebView
        final ProgressBar progressBar = findViewById(R.id.progress_bar_id);
        webView = findViewById(R.id.main_activity_webview_id);
        //Play & Stop Button
        final Button palyButton = findViewById(R.id.play_bn_id);
        final Button stopButton = findViewById(R.id.pose_bn_id);
        final TextView tvTextView = findViewById(R.id.video_txt_view_id);

        //Webview
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebChromeClient(new ChromeClient());

        //WebView Client
        webView.setWebViewClient(new WebViewClient(){
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);

            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                progressBar.setVisibility(View.INVISIBLE);
            }
        });

        stopButton.setEnabled(false);

        //Play Button
        palyButton.setOnClickListener(view -> {
            webView.setVisibility(View.VISIBLE);
            webView.loadUrl("file:///android_asset/livetv.html");
            palyButton.setEnabled(false);
            stopButton.setEnabled(true);
            progressBar.setVisibility(View.VISIBLE);
            tvTextView.setVisibility(View.INVISIBLE);
        });

        //Stop Button
        stopButton.setOnClickListener(view -> {
            webView.loadUrl("file:///android_asset/black.html");
            palyButton.setEnabled(true);
            stopButton.setEnabled(false);
            webView.setVisibility(View.INVISIBLE);
            progressBar.setVisibility(View.INVISIBLE);
            tvTextView.setVisibility(View.VISIBLE);
        });
    }

    // For Full Screen
    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState, @NonNull PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
        webView.saveState(outState);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        webView.restoreState(savedInstanceState);
    }

    /**
     * WebChromeClient for active full screen in WebView
     */
    private class ChromeClient extends WebChromeClient {
        private View mCustomView;
        private WebChromeClient.CustomViewCallback mCustomViewCallback;
        protected FrameLayout mFullscreenContainer;
        private int mOriginalOrientation;
        private int mOriginalSystemUiVisibility;

        ChromeClient() {}

        public Bitmap getDefaultVideoPoster()
        {
            if (mCustomView == null) {
                return null;
            }
            return BitmapFactory.decodeResource(getApplicationContext().getResources(), 2130837573);
        }

        public void onHideCustomView()
        {
            ((FrameLayout)getWindow().getDecorView()).removeView(this.mCustomView);
            this.mCustomView = null;
            getWindow().getDecorView().setSystemUiVisibility(this.mOriginalSystemUiVisibility);
            setRequestedOrientation(this.mOriginalOrientation);
            this.mCustomViewCallback.onCustomViewHidden();
            this.mCustomViewCallback = null;
        }

        public void onShowCustomView(View paramView, WebChromeClient.CustomViewCallback paramCustomViewCallback)
        {
            if (this.mCustomView != null)
            {
                onHideCustomView();
                return;
            }
            this.mCustomView = paramView;
            this.mOriginalSystemUiVisibility = getWindow().getDecorView().getSystemUiVisibility();
            this.mOriginalOrientation = getRequestedOrientation();
            this.mCustomViewCallback = paramCustomViewCallback;
            ((FrameLayout)getWindow().getDecorView()).addView(this.mCustomView, new FrameLayout.LayoutParams(-1, -1));
            getWindow().getDecorView().setSystemUiVisibility(3846 | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        }
    }


    /**
     * Create and handle Navigation Menu Fully Programmatically
     */
    private void createHeaderNavigation(){
        //set toolbar
        Toolbar toolbar = findViewById(R.id.toolbar_id);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle(getResources().getString(R.string.app_name));

        //Navigation Drawer Items
        PrimaryDrawerItem home = new PrimaryDrawerItem().withIdentifier(1).withName("Home").withIcon(R.drawable.ic_home_black_24dp);
        PrimaryDrawerItem website = new PrimaryDrawerItem().withIdentifier(2).withName("Website").withIcon(R.drawable.ic_web_black_24dp);
        PrimaryDrawerItem facebook = new PrimaryDrawerItem().withIdentifier(3).withName("Facebook").withIcon(R.drawable.ic_facebook_black_24dp);
        PrimaryDrawerItem instagram = new PrimaryDrawerItem().withIdentifier(4).withName("Instagram").withIcon(R.drawable.ic_instragrum_icon);
        PrimaryDrawerItem twitter = new PrimaryDrawerItem().withIdentifier(5).withName("Twitter").withIcon(R.drawable.ic_twiter_24dp);
        PrimaryDrawerItem youtube = new PrimaryDrawerItem().withIdentifier(6).withName("YouTube").withIcon(R.drawable.ic_youtube_24dp);
        //Drawer footer items
        PrimaryDrawerItem share_app = new PrimaryDrawerItem().withIdentifier(7).withName("Share App").withIcon(R.drawable.ic_share_black_24dp);
        PrimaryDrawerItem rate_app = new PrimaryDrawerItem().withIdentifier(8).withName("Rate App").withIcon(R.drawable.ic_rateapp_icon);

        // todo: add header

        //Active Drawer
        final Drawer result = new DrawerBuilder()
                .withActivity(this)
                .withToolbar(toolbar)
                .withHeader(R.layout.nav_header_layout)
                .withActionBarDrawerToggleAnimated(true)
                .addDrawerItems(
                        home,
                        website,
                        facebook,
                        instagram,
                        twitter,
                        youtube,
                        share_app,
                        rate_app
                )
                .build();

        // set drawer handler
        result.setOnDrawerItemClickListener((view, position, drawerItem) -> {

            Intent intent = new Intent(MainActivity.this,WebActivity.class);
            int id = (int) drawerItem.getIdentifier();
            final String[] urlArray = {
                    "https://www.infosdebabi.com/",
                    "https://www.facebook.com/infosdebabilewisparker/",
                    "https://www.instagram.com/infosdebabi.com2/",
                    "https://twitter.com/infosdebabi",
                    "https://www.youtube.com/channel/UC87uGGev9VKRR9YjoOl_TuA"
            };

            switch (id) {
                case 1:
                    //Home
                    break;
                case 2:
                    //Website
                    intent.putExtra("Url",urlArray[0]);
                    intent.putExtra("title","Website");
                    startActivity(intent);
                    break;
                case 3:
                    //Facebook
                    intent.putExtra("Url",urlArray[1]);
                    intent.putExtra("title","Facebook");
                    startActivity(intent);
                    break;
                case 4:
                    //Instragum
                    intent.putExtra("Url",urlArray[2]);
                    intent.putExtra("title","Instagram");
                    startActivity(intent);
                    break;
                case 5:
                    //Twiter
                    intent.putExtra("Url",urlArray[3]);
                    intent.putExtra("title","Twitter");
                    startActivity(intent);
                    break;
                case 6:
                    //Youtube
                    intent.putExtra("Url",urlArray[4]);
                    intent.putExtra("title","YouTube");
                    startActivity(intent);
                    break;
                case 7:
                    // share app
                    shareApp();
                    break;
                case 8:
                    // share app
                    rateApp();
                    break;
            }


            return false;
        });

        //Drawer footer items
        PrimaryDrawerItem footerTitle = new PrimaryDrawerItem().withName("développé par Cmedialinks");

        //lets set drawer footer
        result.addStickyFooterItemAtPosition(footerTitle,0);

    }

    /**
     * Will Show share app popup
     */
    private void shareApp(){
        Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");
        String shareBody = "Application Link : https://play.google.com/store/apps/details?id=${App.context.getPackageName()}";
        sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "App Link");
        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
        startActivity(Intent.createChooser(sharingIntent, "Share App Link Via :"));
    }

    /**
     * Will goto playstore for rate the app.
     */
    private void rateApp(){
        Context context = getApplicationContext();
        Uri uri = Uri.parse("market://details?id=" + context.getPackageName());
        Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
        // To count with Play market backstack, After pressing back button,
        // to taken back to our application, we need to add following flags to intent.
        goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY |
                Intent.FLAG_ACTIVITY_NEW_DOCUMENT |
                Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
        try {
            startActivity(goToMarket);
        } catch (ActivityNotFoundException e) {
            startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("http://play.google.com/store/apps/details?id=" + context.getPackageName())));
        }
    }

    /**
     * On Back Pressed
     */
    @Override
    public void onBackPressed() {
        exitConfirmation();
    }

    /**
     * Exit Confirmation Dialog
     */
    private void exitConfirmation(){
         AlertDialog.Builder alertDialog= new AlertDialog.Builder(this)
                 .setMessage("Are you sure you want to exit?")
                .setCancelable(true)
                .setNeutralButton("Rate App", ((dialog,which) -> {
                    shareApp();
                }))
                .setPositiveButton("Yes",new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        finish();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                    }
                });
         alertDialog.show();
    }

}
