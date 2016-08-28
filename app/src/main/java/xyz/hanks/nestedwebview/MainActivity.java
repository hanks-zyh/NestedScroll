package xyz.hanks.nestedwebview;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private WebView webView;
    private RecyclerView recyclerView;
    private String[] urls = new String[]{
//        "http://gold.xitu.io/post/57b074fda633bd0057035b6d",
            "http://blog.csdn.net/hpu_zyh/article/details/52116512",
//        "http://gold.xitu.io/entry/57b57e5f8ac2470064443834/view",
//        "http://wj.qq.com/s/721023/c03b",
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
//        setContentView(R.layout.activity_main_nest);
//        setContentView(R.layout.activity_main_scroll);
        setContentView(R.layout.activity_scroller);
//        webView = (WebView) findViewById(R.id.webview);
//        webView.getSettings().setJavaScriptEnabled(true);
//        webView.getSettings().setBuiltInZoomControls(true);
//        webView.getSettings().setLoadWithOverviewMode(true);
//        webView.getSettings().setDisplayZoomControls(false);
//        webView.getSettings().setUseWideViewPort(true);
//        webView.getSettings().setAppCacheEnabled(true);
//        webView.getSettings().setRenderPriority(WebSettings.RenderPriority.HIGH);
//        webView.getSettings().setDatabaseEnabled(true);
//        webView.getSettings().setDatabasePath("/data/data/com.daimajia.gold/databases/");
//        webView.getSettings().setDomStorageEnabled(true);
//        webView.setWebViewClient(new WebViewClient() {
//            @Override
//            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
//                return true;
//            }
//        });
//        //网址添加 https 后，显示 http 图片,KITKAT 及以下版本默认为 MIXED_CONTENT_ALWAYS_ALLOW
//        //see http://developer.android.com/intl/zh-cn/reference/android/webkit/WebSettings.html#setMixedContentMode(int)
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            webView.getSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
//        }
//
//        if (Build.VERSION.SDK_INT >= 19) {
//            // chromium, enable hardware acceleration
//            webView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
//        } else {
//            // older android version, disable hardware acceleration
//            webView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
//        }
//
//        webView.setScrollContainer(false);
//
////        webView.loadUrl("http://gold.xitu.io/post/57b074fda633bd0057035b6d");
////        webView.loadUrl("http://blog.csdn.net/hpu_zyh/article/details/52116512");
//        webView.loadUrl(urls[new Random().nextInt(urls.length)]);
//
//
//        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
//        recyclerView.setLayoutManager(new LinearLayoutManager(this));
////        recyclerView.setNestedScrollingEnabled(false);
//
//        recyclerView.setAdapter(new MyAdapter());


    }


    private class MyAdapter extends RecyclerView.Adapter {
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            TextView itemView = new TextView(parent.getContext());
            itemView.setTextSize(20);
            return new RecyclerView.ViewHolder(itemView) {
                @Override
                public String toString() {
                    return super.toString();
                }
            };
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            ((TextView) holder.itemView).setText(position + ". this is getDirection simple item");
        }

        @Override
        public int getItemCount() {
            return 100;
        }
    }
}
