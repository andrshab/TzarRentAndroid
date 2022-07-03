package su.tzar.borovovaleksandr.tzar.activity;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import ru.yoomoney.sdk.kassa.payments.Checkout;
import ru.yoomoney.sdk.kassa.payments.TokenizationResult;
import ru.yoomoney.sdk.kassa.payments.checkoutParameters.Amount;
import ru.yoomoney.sdk.kassa.payments.checkoutParameters.PaymentMethodType;
import ru.yoomoney.sdk.kassa.payments.checkoutParameters.PaymentParameters;
import ru.yoomoney.sdk.kassa.payments.checkoutParameters.SavePaymentMethod;
import ru.yoomoney.sdk.kassa.payments.checkoutParameters.TestParameters;
import su.tzar.borovovaleksandr.tzar.App;
import su.tzar.borovovaleksandr.tzar.R;
import su.tzar.borovovaleksandr.tzar.ble.Ble;
import su.tzar.borovovaleksandr.tzar.fragment.HelpFragment;
import su.tzar.borovovaleksandr.tzar.helper.Codes;
import su.tzar.borovovaleksandr.tzar.helper.InfoMessage;
import su.tzar.borovovaleksandr.tzar.helper.Permissions;
import su.tzar.borovovaleksandr.tzar.fragment.HomeFragment;
import su.tzar.borovovaleksandr.tzar.fragment.MapFragment;
import su.tzar.borovovaleksandr.tzar.network.AuthRequest;
import su.tzar.borovovaleksandr.tzar.network.FirebaseToken;
import su.tzar.borovovaleksandr.tzar.network.NetworkService;
import su.tzar.borovovaleksandr.tzar.network.PaymentToken;

import com.google.firebase.iid.FirebaseInstanceId;
import com.vk.api.sdk.VK;
import com.vk.api.sdk.auth.VKAccessToken;
import com.vk.api.sdk.auth.VKAuthCallback;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Currency;
import java.util.HashSet;
import java.util.Set;
import javax.inject.Inject;
import static su.tzar.borovovaleksandr.tzar.helper.Codes.REQUEST_3DS;
import static su.tzar.borovovaleksandr.tzar.helper.Codes.REQUEST_CODE_TOKENIZE;


public class MainActivity extends AppCompatActivity {
    @Inject
    public Ble ble;
    private final HomeFragment mHomeFragment = new HomeFragment();
    private final MapFragment mMapFragment = new MapFragment();
    private final HelpFragment mHelpFragment = new HelpFragment();
//    private final FragmentManager fm = getSupportFragmentManager();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //https://stackoverflow.com/questions/55770376/onactivityresult-not-called-when-nested-fragment-is-using-replace-instead-of-add
        if(savedInstanceState == null) {
            FragmentManager fm = getSupportFragmentManager();
            fm.beginTransaction()
                    .add(R.id.host_fragment, mMapFragment, "Map")
                    .commit();
        }

        if (!Permissions.isPermissionsGranted(this)) {
            Permissions.requestPermissions(this);
        }

        App.getComponent().injectMainActivity(this);
        ble.registerBleStateReciever(getApplicationContext());

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        int locationPermissionIndex = Arrays.asList(permissions).indexOf(Manifest.permission.ACCESS_FINE_LOCATION);
        if (locationPermissionIndex >= 0 && grantResults[locationPermissionIndex] == PackageManager.PERMISSION_GRANTED) {
            mMapFragment.onLocationPermissionsGranted();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            ble.unregisterBleStateReciever(getApplicationContext());
        } catch (IllegalArgumentException e) {
            log(e.getMessage());
        }

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        log("onActivityResult CALLED");
        if (!VK.onActivityResult(requestCode, resultCode, data, new VKAuthCallback() {
            @Override
            public void onLogin(VKAccessToken res) {
                NetworkService.getJWT(getApplicationContext(), new AuthRequest(res.getAccessToken()),
                        response -> {
                            mHomeFragment.hideWait();
                            if(response.getToken()!=null && response.getRefreshToken()!=null) {
                                new InfoMessage(mHomeFragment.getContext()).show("Дождитесь push-уведомления об открытии доступа к шерингу, если вы здесь впервые");
                                SharedPreferences sPrefs = getSharedPreferences(getString(R.string.tag_prefernces_name), MODE_PRIVATE);
                                sPrefs.edit().putString(getString(R.string.tag_token), response.getToken()).apply();
                                sPrefs.edit().putString(getString(R.string.tag_refresh_token), response.getRefreshToken()).apply();
                                //TODO: on server add to JWT object data about user
                                if (res.getUserId() != null) {
                                    sPrefs.edit().putInt(getString(R.string.tag_vk_user_id), res.getUserId()).apply();
                                    mHomeFragment.setUIUserLoggedIn(Integer.toString(res.getUserId()));
                                    NetworkService.getUserState(getApplicationContext(), userState -> {
                                        if(userState.getBalance()!=null){
                                            mHomeFragment.setBalanceTv(userState.getBalance()+getString(R.string.tag_currency_name));
                                        }
                                    });
                                } else {
                                    sPrefs.edit().putInt(getString(R.string.tag_vk_user_id), -1).apply();
                                }

                                FirebaseInstanceId.getInstance().getInstanceId()
                                        .addOnCompleteListener(task -> {
                                            if (!task.isSuccessful()) {
                                                Log.w("MainActivity", "getInstanceId failed", task.getException());
                                                return;
                                            }
                                            if (task.getResult() != null) {
                                                String token = task.getResult().getToken();
                                                NetworkService.sendFirebaseToken(getApplicationContext(), new FirebaseToken(token), result -> {
                                                    if(result.getResult() != null) {
                                                        log("FirebaseToken was sent");
                                                    } else if (result.getError() != null) {
                                                        new InfoMessage(mHomeFragment.getContext()).show(result.getError());
                                                    }
                                                });
                                            }
                                        });

                            } else if(response.getError()!=null) {
                                new InfoMessage(mHomeFragment.getContext()).show(response.getError());
                            }

                        });
            }

            @Override
            public void onLoginFailed(int code) {
                log("VK LOGIN ERROR. CODE = " + code);
                mHomeFragment.hideWait();
                new InfoMessage(mHomeFragment.getContext()).show(getString(R.string.warn_vk_auth_failed));
                // Произошла ошибка авторизации (например, пользователь запретил авторизацию)
            }
        })) {
            super.onActivityResult(requestCode, resultCode, data);

            if (requestCode == REQUEST_CODE_TOKENIZE) {
                switch (resultCode) {
                    case RESULT_OK:
                        // successful tokenization
                        TokenizationResult result = Checkout.createTokenizationResult(data);
                        log(result.toString());
                        NetworkService
                                .getSecureURL(getApplicationContext(), new PaymentToken(result.getPaymentToken(), mHomeFragment.getSum(), mHomeFragment.getPhone()), response -> {
                                    if(response.getUrl() != null) {
                                        log(response.getUrl());
                                        Intent intent = Checkout.create3dsIntent(this, response.getUrl());
                                        startActivityForResult(intent, Codes.REQUEST_3DS);
//                                        mHomeFragment.hideWait();//REMOVE THIS
                                    } else if (response.getError() == null) {
                                        //GOOGLE PAY success
                                        mHomeFragment.hideWait();
                                        new InfoMessage(mHomeFragment.getContext()).show(getString(R.string.tip_google_pay));
                                    } else if (response.getError() != null) {
                                        mHomeFragment.hideWait();
                                        new InfoMessage(mHomeFragment.getContext()).show(response.getError());
                                    }

                                });
                        break;
                    case RESULT_CANCELED:
                        mHomeFragment.hideWait();
                        break;
                }
            }
            if (requestCode == REQUEST_3DS) {
                mHomeFragment.hideWait();
                switch (resultCode) {
                    case RESULT_OK:
                        // Аутентификация по 3-D Secure прошла успешно
                        log("3DS SUCCESS");
                        break;
                    case RESULT_CANCELED:
                        // Экран 3-D Secure был закрыт
                        log("3DS CANCELLED");
                        break;
                    case Checkout.RESULT_ERROR:
                        log("3DS ERROR");
                        String message = "Payment error. Code: " + data.getIntExtra(Checkout.EXTRA_ERROR_CODE, 0);
                        if(data.getStringExtra(Checkout.EXTRA_ERROR_DESCRIPTION) != null) {
                            message = message + "Description: " + data.getStringExtra(Checkout.EXTRA_ERROR_DESCRIPTION);
                        }
                        if(data.getStringExtra(Checkout.EXTRA_ERROR_FAILING_URL) != null) {
                            message = message + "URL: " + data.getStringExtra(Checkout.EXTRA_ERROR_FAILING_URL);
                        }
                        new InfoMessage(mHomeFragment.getContext()).show(message);
                        break;
                }
            }

        }
    }

    private Intent getPayIntent(){
        Set<PaymentMethodType> paymentMethodTypes =  new HashSet<>();
        paymentMethodTypes.add(PaymentMethodType.BANK_CARD);
        paymentMethodTypes.add(PaymentMethodType.GOOGLE_PAY);
        PaymentParameters paymentParameters = new PaymentParameters(
                new Amount(BigDecimal.valueOf(mHomeFragment.getSum()), Currency.getInstance("RUB")),
                getString(R.string.pay_title),
                getString(R.string.pay_description),
                getString(R.string.yandex_sdk_token),
                getString(R.string.yandex_shop_id),
                SavePaymentMethod.OFF,
                paymentMethodTypes
        );
        TestParameters testParameters = new TestParameters(true, false, null);
        final Intent intent = Checkout.createTokenizeIntent(this, paymentParameters, testParameters);
        return intent;
    }

    public void processPay() {
        Intent intent = getPayIntent();
        mHomeFragment.showWait();
        startActivityForResult(intent, REQUEST_CODE_TOKENIZE);
    }

    public HomeFragment getmHomeFragment() {
        return this.mHomeFragment;
    }
    public HelpFragment getmHelpFragment() {
        return this.mHelpFragment;
    }

    private void log(String message) {
        Log.i(getClass().getSimpleName(), message);
    }
}
