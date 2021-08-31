package su.tzar.borovovaleksandr.tzar.helper;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.view.ContextThemeWrapper;

import su.tzar.borovovaleksandr.tzar.App;
import su.tzar.borovovaleksandr.tzar.R;

public class InfoMessage {
    private Context context;
    public interface Listener {
        void cancelOrOkAction();
    }
    Listener listener;

    public InfoMessage(Context context){
        this.context = context;
    }

    public InfoMessage(Context context, Listener listener) {
        this.context = context;
        this.listener = listener;
    }

    public void show(String message){
        if(context != null) {
            if(!((Activity)context).isFinishing()&&!((Activity)context).isDestroyed()){
                new AlertDialog.Builder(new ContextThemeWrapper(context, R.style.AppTheme))
                        .setMessage(message)
                        .setOnCancelListener((param) -> {
                            if(listener != null) {
                                listener.cancelOrOkAction();
                                Log.i("InfoMessage", "CANCEL ACTION");
                            }
                        })
                        .setPositiveButton(context.getString(R.string.OK), (dialog, which) -> {
                            if(listener != null) {
                                listener.cancelOrOkAction();
                                Log.i("InfoMessage", "OK ACTION");
                            }
                        })
                        .show();
            }
        }

    }
}
