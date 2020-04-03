package com.infosdebabitv.com;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.webkit.DownloadListener;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class WebActivity extends AppCompatActivity {

    private WebView browser;
    public String homepage;
    private SwipeRefreshLayout myswiperfreshlayout;
    private ViewTreeObserver.OnScrollChangedListener mOnScrollchangedListener;
    private RelativeLayout errorlayout;
    private TextView errtext;
    private int loadnum = 0;
    Bitmap favbit;

    //for file loaders
    private static final String TAG = WebActivity.class.getSimpleName();

    private String mCM;
    private ValueCallback<Uri> mUM;
    private ValueCallback<Uri[]> mUMA;
    private final static int FCR=1;
    private boolean multiple_files = false;

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent){

        if(Build.VERSION.SDK_INT >= 21){
            Uri[] results = null;
            //checking if response is positive
            if(resultCode== Activity.RESULT_OK){
                if(requestCode == FCR){
                    if(null == mUMA){
                        return;
                    }
                    if(intent == null || intent.getData() == null){
                        if(mCM != null){
                            results = new Uri[]{Uri.parse(mCM)};
                        }
                    }else{
                        String dataString = intent.getDataString();
                        if(dataString != null){
                            results = new Uri[]{Uri.parse(dataString)};
                        } else {
                            if(multiple_files) {
                                if (intent.getClipData() != null) {
                                    final int numSelectedFiles = intent.getClipData().getItemCount();
                                    results = new Uri[numSelectedFiles];
                                    for (int i = 0; i < numSelectedFiles; i++) {
                                        results[i] = intent.getClipData().getItemAt(i).getUri();
                                    }
                                }
                            }
                        }
                    }
                }
            }
            mUMA.onReceiveValue(results);
            mUMA = null;
        }else {
            if (requestCode == FCR) {
                if (null == mUM) return;
                Uri result2 = intent == null || resultCode != RESULT_OK ? null : intent.getData();
                mUM.onReceiveValue(result2);
                mUM = null;
            }
        }

        super.onActivityResult(requestCode, resultCode, intent);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web);

        errorlayout = findViewById(R.id.errorlayout);
        errtext = findViewById(R.id.errortext);

        //Hare Browser Load Website Data
        Bundle bundle = getIntent().getExtras();

        try{
            if (bundle!=null){
                homepage = bundle.getString("Url");;

                //Set Title
                String title = bundle.getString("title");
                setTitle(title);

                if(homepage.equals(null)){
                    Toast.makeText(getApplicationContext(),"Null Error to get URL", Toast.LENGTH_SHORT).show();
                }
            }
        }catch (Exception ex){

        }

        //swiperefresh start
        myswiperfreshlayout = (SwipeRefreshLayout)findViewById(R.id.swiperefreshlayout);
        myswiperfreshlayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                browser.reload();
            }
        });

        /**
         * Set Refreshing True
         */
        myswiperfreshlayout.setRefreshing(true);



        //browser functionality
        browser = findViewById(R.id.browserview);
        WebSettings ezwebset = browser.getSettings();
        ezwebset.setUseWideViewPort(true);

        ezwebset.setJavaScriptEnabled(true);
        ezwebset.setDatabaseEnabled(true);
        ezwebset.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NARROW_COLUMNS);
        ezwebset.setJavaScriptCanOpenWindowsAutomatically(true);
        ezwebset.setLoadsImagesAutomatically(true);
        browser.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);

        ezwebset.setDomStorageEnabled(true);
        ezwebset.setAllowContentAccess(true);
        ezwebset.setAllowFileAccess(true);
        ezwebset.setLoadsImagesAutomatically(true);
        favbit = BitmapFactory.decodeResource(getBaseContext().getResources(), R.mipmap.ic_launcher);

        browser.setWebChromeClient(new WebChromeClient(){

            @Override
            public void onReceivedIcon(WebView view, Bitmap icon) {
                favbit = icon;
                super.onReceivedIcon(view, icon);
                // tst("icon tcvd",1);
            }

            @Override
            public void onReceivedTitle(WebView view, String title) {
                super.onReceivedTitle(view, title);
            }

            /*
             * openFileChooser is not a public Android API and has never been part of the SDK.
             */
            //handling input[type="file"] requests for android API 16+
            @SuppressLint("ObsoleteSdkInt")
            @SuppressWarnings("unused")
            public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType, String capture) {
                mUM = uploadMsg;
                Intent i = new Intent(Intent.ACTION_GET_CONTENT);
                i.addCategory(Intent.CATEGORY_OPENABLE);
                i.setType("*/*");
                if (multiple_files && Build.VERSION.SDK_INT >= 18) {
                    i.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                }
                startActivityForResult(Intent.createChooser(i, "File Chooser"), FCR);
            }

            //handling input[type="file"] requests for android API 21+
            @SuppressLint("InlinedApi")
            public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {
                if (file_permission()) {
                    String[] perms = {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA};

                    //checking for storage permission to write images for upload
                    if (ContextCompat.checkSelfPermission(WebActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(WebActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(WebActivity.this, perms, FCR);

                        //checking for WRITE_EXTERNAL_STORAGE permission
                    } else if (ContextCompat.checkSelfPermission(WebActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(WebActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, FCR);

                        //checking for CAMERA permissions
                    } else if (ContextCompat.checkSelfPermission(WebActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(WebActivity.this, new String[]{Manifest.permission.CAMERA}, FCR);
                    }
                    if (mUMA != null) {
                        mUMA.onReceiveValue(null);
                    }
                    mUMA = filePathCallback;
                    Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    if (takePictureIntent.resolveActivity(WebActivity.this.getPackageManager()) != null) {
                        File photoFile = null;
                        try {
                            photoFile = createImageFile();
                            takePictureIntent.putExtra("PhotoPath", mCM);
                        } catch (IOException ex) {
                            Log.e(TAG, "Image file creation failed", ex);
                        }
                        if (photoFile != null) {
                            mCM = "file:" + photoFile.getAbsolutePath();
                            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
                        } else {
                            takePictureIntent = null;
                        }
                    }
                    Intent contentSelectionIntent = new Intent(Intent.ACTION_GET_CONTENT);
                    contentSelectionIntent.addCategory(Intent.CATEGORY_OPENABLE);
                    contentSelectionIntent.setType("*/*");
                    if (multiple_files) {
                        contentSelectionIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                    }
                    Intent[] intentArray;
                    if (takePictureIntent != null) {
                        intentArray = new Intent[]{takePictureIntent};
                    } else {
                        intentArray = new Intent[0];
                    }

                    Intent chooserIntent = new Intent(Intent.ACTION_CHOOSER);
                    chooserIntent.putExtra(Intent.EXTRA_INTENT, contentSelectionIntent);
                    chooserIntent.putExtra(Intent.EXTRA_TITLE, "File Chooser");
                    chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, intentArray);
                    startActivityForResult(chooserIntent, FCR);
                    return true;
                }else{
                    return false;
                }
            }//file uploader end

            // for fullscreen


            private View mCustomView;
            private CustomViewCallback mCustomViewCallback;
            private int mOriginalOrientation;
            private int mOriginalSystemUiVisibility;


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

            public void onShowCustomView(View paramView, CustomViewCallback paramCustomViewCallback)
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
                mCustomView.setBackgroundColor(Color.parseColor("#000000"));
                ((FrameLayout)getWindow().getDecorView()).addView(this.mCustomView, new FrameLayout.LayoutParams(-1, -1));
                getWindow().getDecorView().setSystemUiVisibility(3846);
            }

            //--


        });


        browser.setWebViewClient(new ezwebviewclient(){
            public void onReceivedError(WebView view, int errcode, String desc, String failurl){

                errorlayout.setVisibility(View.VISIBLE);
                errtext.setText("error "+ errcode +". " +desc);
                loadnum++;

            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                loadnum = 0;
                myswiperfreshlayout.setRefreshing(true);

                super.onPageStarted(view, url, favicon);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                if(loadnum==0) {
                    errorlayout.setVisibility(View.GONE);
                }
                myswiperfreshlayout.setRefreshing(false);

                super.onPageFinished(view, url);
            }



        });

        browser.setDownloadListener(new DownloadListener() {
            public void onDownloadStart(String durl, String userAgent,
                                        String contentDisposition, String mimetype,
                                        long contentLength) {

                Uri uri = Uri.parse(durl);
                Intent intent = new Intent(Intent.ACTION_VIEW,uri);
                startActivity(intent);
            }
        });

        //Load URL
        if(getIntent().getExtras() != null ){
            //Toast.makeText(getApplicationContext(),homepage, Toast.LENGTH_LONG).show();
            browser.loadUrl(homepage);
        }
    }

    @Override
    protected void onStart() {
        myswiperfreshlayout.getViewTreeObserver().addOnScrollChangedListener(mOnScrollchangedListener =
                new ViewTreeObserver.OnScrollChangedListener(){
                    @Override
                    public void onScrollChanged(){
                        if ( !myswiperfreshlayout.isRefreshing()){
                            if (browser.getScrollY() == 0) {
                                myswiperfreshlayout.setEnabled(true);
                            } else if (browser.getScrollY() > 10) {
                                myswiperfreshlayout.setEnabled(false);
                            }
                        }
                    }
                });


        super.onStart();
    }

    @Override
    protected void onStop() {
        myswiperfreshlayout.getViewTreeObserver().removeOnScrollChangedListener( mOnScrollchangedListener);
        super.onStop();
    }

    @Override
    public void onBackPressed() {
        if (browser.canGoBack()) {
            browser.goBack();
        } else {
            super.onBackPressed();
        }
    }

    public void tryagain(View v){
        browser.reload();
    }

    private class ezwebviewclient extends WebViewClient {

        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request){

            //Page redirect Done Hare Perfectly
            String runningURL = request.getUrl().toString();
            if(!request.hasGesture()){return false;}
            view.loadUrl(runningURL);
            return true;
        }



    }

    @Override
    protected void onDestroy() {

        // appDataBaseAdapter.close();
        super.onDestroy();
    }

    //For uploader
    public boolean file_permission(){
        if(Build.VERSION.SDK_INT >=23 && (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)) {
            ActivityCompat.requestPermissions(WebActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA}, 1);
            return false;
        }else{
            return true;
        }
    }

    //creating new image file here
    private File createImageFile() throws IOException {
        @SuppressLint("SimpleDateFormat") String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "img_"+timeStamp+"_";
        File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        return File.createTempFile(imageFileName,".jpg",storageDir);
    }

}
