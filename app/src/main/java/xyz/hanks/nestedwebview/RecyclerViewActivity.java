package xyz.hanks.nestedwebview;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class RecyclerViewActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private List<String> data = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recycler_view);

        for (int i = 0; i < 50; i++) {
            data.add(i + " : this is a simple item ");
        }
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(new MyAdapter());
    }

    private class MyAdapter extends RecyclerView.Adapter {
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            if (viewType == 0) {// webview
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.holder_webview, parent, false);
                WebViewHolder webViewHolder = new WebViewHolder(view);
                WebView webView = webViewHolder.webView;
                webView.setWebViewClient(new WebViewClient() {
                    @Override
                    public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                        return true;
                    }
                });
                webView.loadUrl("http://blog.csdn.net/hpu_zyh/article/details/52116512");
                return webViewHolder;
            } else if (viewType == 1) { // middle
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.holder_middleview, parent, false);
                return new MiddleViewHolder(view);
            }

            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.holder_normalview, parent, false);
            return new NormalViewHolder(view);
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            if (position > 1 && holder instanceof NormalViewHolder) {
                ((NormalViewHolder) holder).textView.setText(data.get(position - 2));
            }
        }

        @Override
        public int getItemCount() {
            return data.size() + 2;
        }

        @Override
        public int getItemViewType(int position) {
            return position;
        }

    }

    class WebViewHolder extends RecyclerView.ViewHolder {

        public WebView webView;

        public WebViewHolder(View itemView) {
            super(itemView);
            this.webView = (WebView) itemView;
        }
    }

    class MiddleViewHolder extends RecyclerView.ViewHolder {

        public MiddleViewHolder(View itemView) {
            super(itemView);
        }
    }

    class NormalViewHolder extends RecyclerView.ViewHolder {

        public TextView textView;

        public NormalViewHolder(View itemView) {
            super(itemView);
            textView = (TextView) itemView.findViewById(R.id.tv);
        }
    }
}
