package su.tzar.borovovaleksandr.tzar.fragment;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import su.tzar.borovovaleksandr.tzar.R;
import su.tzar.borovovaleksandr.tzar.helper.InfoMessage;
import su.tzar.borovovaleksandr.tzar.network.InternetCheck;

public class HelpFragment extends Fragment {
    private WebView infoWv;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_help, container, false);
        infoWv = root.findViewById(R.id.info_web_view);
        infoWv.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                String url = request.getUrl().toString();
                if (url.startsWith("tel:")) {
                    Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse(url));
                    startActivity(intent);
                    return true;
                }
                view.loadUrl(url);
                return false;
            }
        });

        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        loadInfo();
    }

    public void loadInfo() {
        new InternetCheck(isInternetAvailable -> {
            if (!isInternetAvailable) {
                new InfoMessage(getContext()).show(getString(R.string.warn_check_internet_connectio));
                return;
            } else {
                infoWv.loadUrl(getString(R.string.info_url));
            }
        });
    }

    private void log(String message) {
        Log.i(getClass().getSimpleName(), message);
    }


}
