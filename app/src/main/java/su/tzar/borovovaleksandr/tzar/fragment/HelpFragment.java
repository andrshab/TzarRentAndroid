package su.tzar.borovovaleksandr.tzar.fragment;


import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

import com.google.zxing.integration.android.IntentIntegrator;

import java.io.IOException;

import su.tzar.borovovaleksandr.tzar.R;
import su.tzar.borovovaleksandr.tzar.activity.QRScannerActivity;
import su.tzar.borovovaleksandr.tzar.helper.InfoMessage;
import su.tzar.borovovaleksandr.tzar.network.InternetCheck;
import su.tzar.borovovaleksandr.tzar.network.NetworkService;

import static android.content.Context.MODE_PRIVATE;

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
//        if(getContext() != null) {
//            SharedPreferences sPrefs = getContext().getSharedPreferences(getString(R.string.tag_prefernces_name), MODE_PRIVATE);
//            String infoPage = sPrefs.getString(getContext().getString(R.string.tag_info_page), "null");
//            if(!infoPage.equals("null")) {
//                infoWv.loadData(infoPage, "text/html", "UTF-8");
//            }
//        }

        return root;
    }
    @Override
    public void onResume() {
        super.onResume();
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
