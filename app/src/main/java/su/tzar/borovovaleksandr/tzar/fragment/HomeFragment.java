package su.tzar.borovovaleksandr.tzar.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import su.tzar.borovovaleksandr.tzar.R;
import su.tzar.borovovaleksandr.tzar.activity.MainActivity;
import su.tzar.borovovaleksandr.tzar.network.Auth;
import su.tzar.borovovaleksandr.tzar.network.NetworkService;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.vk.api.sdk.VK;
import com.vk.api.sdk.auth.VKScope;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import static android.content.Context.MODE_PRIVATE;


public class HomeFragment extends Fragment {

    private List<VKScope> scope = new ArrayList<>(Arrays.asList(VKScope.OFFLINE, VKScope.FRIENDS, VKScope.EMAIL));
    private View.OnClickListener onAuthClickListener = v -> onAuthClick(getContext());
    private TextView authStateTv;
    private TextView balanceTv;
    private Button authBtn;
    private Button paymentBtn;
    private ProgressBar wait;
    private TextInputEditText sumEditText;
    private TextInputLayout sumTextLayout;
    private TextInputEditText phoneEditText;
    private TextInputLayout phoneTextLayout;

    private View.OnClickListener onPayClickListener = v -> onPayClick();

    private EditText.OnClickListener onSumClickListener = v -> onSumClicked();
    private TextView.OnEditorActionListener onEditorActionListener = (textView, i, keyEvent) -> {
        sumTextLayout.setError(null);
        if(i== EditorInfo.IME_ACTION_DONE){
//            onPayClick();
            phoneEditText.requestFocus();
            phoneEditText.setSelection(2);
        }
        return false;
    };
    private EditText.OnClickListener onPhoneClickListener = v -> onPhoneClicked();
    private TextView.OnEditorActionListener onPhoneEditorActionListener = (textView, i, keyEvent) -> {
        phoneTextLayout.setError(null);
        if(i== EditorInfo.IME_ACTION_DONE){
            onPayClick();
        }
        return false;
    };

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_home, container, false);
        authStateTv = root.findViewById(R.id.login_state);
        authBtn = root.findViewById(R.id.login_btn);
        paymentBtn = root.findViewById(R.id.payment_btn);
        balanceTv = root.findViewById(R.id.balance);
        sumEditText = root.findViewById(R.id.sum_edit_text);
        sumTextLayout = root.findViewById(R.id.sum_text_field);
        phoneEditText = root.findViewById(R.id.phone_edit_text);
        phoneTextLayout = root.findViewById(R.id.phone_text_field);
        wait = root.findViewById(R.id.home_wait);

        sumEditText.setOnEditorActionListener(onEditorActionListener);
        sumEditText.setOnClickListener(onSumClickListener);
        phoneEditText.setOnEditorActionListener(onPhoneEditorActionListener);
        phoneEditText.setOnClickListener(onPhoneClickListener);
        if(getContext() != null) {
            SharedPreferences sPrefs = getContext().getSharedPreferences(getString(R.string.tag_prefernces_name), MODE_PRIVATE);
            String lastSum = sPrefs.getString(getContext().getString(R.string.tag_last_sum), String.valueOf(requireContext().getResources().getInteger(R.integer.min_sum)));
            sumEditText.setText(lastSum);
            String lastPhone = sPrefs.getString(getContext().getString(R.string.tag_last_phone), "+7");
            phoneEditText.setText(lastPhone);
        }

        balanceTv.setVisibility(View.INVISIBLE);
        paymentBtn.setOnClickListener(onPayClickListener);
        authBtn.setOnClickListener(onAuthClickListener);
        initUI();
        return root;
    }

    private void initUI() {
        hideWait();
        if(getContext() != null) {
            SharedPreferences sPrefs = getContext().getSharedPreferences(getString(R.string.tag_prefernces_name), MODE_PRIVATE);
            if (!Auth.isAuthorized(getContext())) {
                setUIUserLoggedOut();
            } else {
                int vkUserID = sPrefs.getInt(getString(R.string.tag_vk_user_id), -1);
                if (vkUserID > 0) {
                    setUIUserLoggedIn(Integer.toString(vkUserID));
                } else {
                    authBtn.setText(R.string.logout);
                    authStateTv.setText(getString(R.string.warn_wrong_vk_user_id));
                    paymentBtn.setEnabled(false);
                    sumTextLayout.setEnabled(false);
                }
            }
        }
    }

    public void setUIUserLoggedIn(String id) {
        String signedInWithId = getString(R.string.title_signed_in) + id;
        authStateTv.setText(signedInWithId);
        authBtn.setText(R.string.logout);
        paymentBtn.setEnabled(true);
        sumTextLayout.setEnabled(true);
        balanceTv.setVisibility(View.INVISIBLE);
    }

    public void setUIUserLoggedOut() {
        authStateTv.setText(R.string.title_please_sign_in);
        authBtn.setText(R.string.login);
        paymentBtn.setEnabled(false);
        sumTextLayout.setEnabled(false);
        balanceTv.setVisibility(View.INVISIBLE);
    }

    public void onResume(){
        super.onResume();
        if(getContext()!=null) {
            if(balanceTv != null && Auth.isAuthorized(getContext())){
                NetworkService.getUserState(getContext(), response -> {
                    if(response.getBalance()!=null && getActivity() != null){
                        setBalanceTv(response.getBalance()+getString(R.string.tag_currency_name));
                    } else if(response.getError() != null && getActivity() != null) {
//                        new InfoMessage(getContext()).show(response.getError());
                    }
                });
            }
        }
    }

    public void onStop() {
        super.onStop();
        hideWait();
    }


    private void onAuthClick(Context context) {
        if(!Auth.isAuthorized(context)){
            VK.login((Activity) context, scope);
            showWait();
        } else {
            setUIUserLoggedOut();
            Auth.deleteJWT(context);
            VK.logout();
        }
    }



    private void onPayClick() {
        if(getSum() < requireContext().getResources().getInteger(R.integer.min_sum)) {
            if(getContext() != null) {
                sumTextLayout.setError(getContext().getString(R.string.warn_sum_input, requireContext().getResources().getInteger(R.integer.min_sum)));
            }
        } else if(getPhone() == 0) {
            if(getContext() != null) {
                phoneTextLayout.setError(getContext().getString(R.string.warn_phone_input));
            }
        } else {
            if (getActivity() != null) {
                sumTextLayout.setError(null);
                if(sumEditText.getText() != null && phoneEditText.getText() != null && getContext() != null) {
                    SharedPreferences sPrefs = getContext().getSharedPreferences(getString(R.string.tag_prefernces_name), MODE_PRIVATE);
                    sPrefs.edit().putString(getContext().getString(R.string.tag_last_sum), sumEditText.getText().toString()).apply();
                    sPrefs.edit().putString(getContext().getString(R.string.tag_last_phone), phoneEditText.getText().toString()).apply();
                }
                ((MainActivity) getActivity()).processPay();
            }
        }
    }

    private void onSumClicked() {
        sumTextLayout.setError(null);
    }

    private void onPhoneClicked() {
        phoneTextLayout.setError(null);
    }

    public void setBalanceTv(String sum){
        if(balanceTv != null) {
            String balance = getString(R.string.title_balance)+ sum;
            balanceTv.setText(balance);
            balanceTv.setVisibility(View.VISIBLE);
        }
    }

    public void showWait() {
        if(wait != null) {
            wait.setVisibility(View.VISIBLE);
        }
    }

    public void hideWait() {
        if(wait != null) {
            wait.setVisibility(View.INVISIBLE);
        }
    }

    public long getSum() {
        long sum = 0;
        if(sumEditText!=null && sumEditText.getText() != null) {
            try {
                sum = Long.parseLong(sumEditText.getText().toString(), 10) ;
            } catch (NumberFormatException e) {
                sum = 0;
            }
        }
        return  sum;
    }

    public long getPhone() {
        long phone = 0;
        if(phoneEditText!=null && phoneEditText.getText() != null) {
            String phoneStr = phoneEditText.getText().toString();
            if(phoneStr.length() == 12 && phoneStr.substring(0,2).equals("+7")) {
                try {
                    phone = Long.parseLong(phoneStr.substring(1,12));
                } catch (NumberFormatException e) {
                    phone = 0;
                }

            } else {
                return 0;
            }
        }
        return phone;
    }

    private void log(String message) {
        Log.i(getClass().getSimpleName(), message);
    }
}