package su.tzar.borovovaleksandr.tzar.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import su.tzar.borovovaleksandr.tzar.App;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.zxing.client.android.Intents;
import com.google.zxing.client.android.R;
import com.journeyapps.barcodescanner.CaptureManager;
import com.journeyapps.barcodescanner.CompoundBarcodeView;


/**
 *
 */
public class QRScannerActivity extends AppCompatActivity {
    private CaptureManager capture;
    private CompoundBarcodeView barcodeScannerView;
    private Button btn;
    private FloatingActionButton torchBtn;
    private View.OnClickListener onEnterIDlickListener = v -> onEnterIDClick();
    private View.OnClickListener onTorchClickListener = v -> onTorchClick();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(su.tzar.borovovaleksandr.tzar.R.layout.activity_qr_scanner);
        barcodeScannerView = (CompoundBarcodeView)findViewById(R.id.zxing_barcode_scanner);

        btn = findViewById(su.tzar.borovovaleksandr.tzar.R.id.inputIDButton);
        btn.setText(su.tzar.borovovaleksandr.tzar.R.string.input_ID);
        btn.setOnClickListener(onEnterIDlickListener);

        torchBtn = findViewById(su.tzar.borovovaleksandr.tzar.R.id.torch_btn);
        torchBtn.setOnClickListener(onTorchClickListener);

        barcodeScannerView.setStatusText(getApplicationContext().getString(su.tzar.borovovaleksandr.tzar.R.string.tip_scan_QR));
        capture = new CaptureManager(this, barcodeScannerView);
        capture.initializeFromIntent(getIntent(), savedInstanceState);
        capture.decode();
    }

    void onPositiveButtonClick(String string){
        Intent intent = new Intent(Intents.Scan.ACTION);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        intent.putExtra(Intents.Scan.RESULT, string);
        this.setResult(Activity.RESULT_OK, intent);
        finish();
    }
    void onEnterIDClick(){

        final EditText input = new EditText(getApplicationContext());
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setPositiveButton(getString(su.tzar.borovovaleksandr.tzar.R.string.OK), (dialog, id) -> {
            String lockID = input.getText().toString();
            String QRString = "{tzarID=";
            QRString = QRString+lockID+'}';
            onPositiveButtonClick(QRString);
        });
        builder.setNegativeButton(getString(su.tzar.borovovaleksandr.tzar.R.string.CANCEL), (dialog, id) -> {
            // User cancelled the dialog
        });
        // Set other dialog properties
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        builder.setView(input);
        builder.setTitle(getString(su.tzar.borovovaleksandr.tzar.R.string.title_enter_lock_id));

        // Create the AlertDialog
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    void onTorchClick() {
        barcodeScannerView.setTorchOn();
    }

    @Override
    protected void onResume() {
        super.onResume();
        capture.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        capture.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        capture.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        capture.onSaveInstanceState(outState);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return barcodeScannerView.onKeyDown(keyCode, event) || super.onKeyDown(keyCode, event);
    }
}

