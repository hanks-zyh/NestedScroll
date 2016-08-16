package xyz.hanks.nestedwebview;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private WebView webView;
    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
//        setContentView(R.layout.activity_main_nest);
        setContentView(R.layout.activity_main_scroll);
        // Let's display the progress in the activity title bar, like the
        // browser app does.
        webView = (WebView) findViewById(R.id.webview);
        webView.getSettings().setJavaScriptEnabled(true);


        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                return true;
            }
        });

//        webView.loadUrl("http://blog.csdn.net/hpu_zyh/article/details/52116512");
        webView.loadUrl("http://www.jianshu.com/p/7cfb42b3749b");
//        webView.loadUrl("http://gold.xitu.io/post/57b074fda633bd0057035b6d");

        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
//        recyclerView.setNestedScrollingEnabled(false);

        recyclerView.setAdapter(new MyAdapter());

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
