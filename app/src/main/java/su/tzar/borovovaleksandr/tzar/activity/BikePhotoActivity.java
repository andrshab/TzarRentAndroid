package su.tzar.borovovaleksandr.tzar.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import su.tzar.borovovaleksandr.tzar.App;
import su.tzar.borovovaleksandr.tzar.R;
import su.tzar.borovovaleksandr.tzar.helper.BitmapCompress;
import su.tzar.borovovaleksandr.tzar.helper.Codes;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;


public class BikePhotoActivity extends AppCompatActivity {
    private String mCurrentPhotoPath;
    private Bitmap myBitmap;
    private Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bike_photo);

        intent = getIntent();
        new AlertDialog.Builder(this)
                .setMessage(getString(R.string.tip_make_bike_photo))
                .setPositiveButton(getString(R.string.OK), (paramDialogInterface, paramInt) ->
                        dispatchTakePictureIntent())
                .setNegativeButton(getString(R.string.CANCEL), (dialog, which) -> finish())
                .show();

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Boolean isCompressSuccessfully = false;
        if (resultCode == RESULT_OK) {
            if(mCurrentPhotoPath != null) {
                File imgFile = new File(mCurrentPhotoPath);
                if (imgFile.exists()) {
                    Bitmap b = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                    b = new BitmapCompress(b).bitmap;
                    try {
                        FileOutputStream out = new FileOutputStream(imgFile);
                        b.compress(Bitmap.CompressFormat.JPEG, 75, out);
                        out.flush();
                        out.close();
                        isCompressSuccessfully = true;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    Toast.makeText(this, getString(R.string.warn_imgfile_not_exist), Toast.LENGTH_SHORT).show();
                }
            }
        }
        if (intent.getByteExtra("lockType", (byte) 0xFF) == Codes.SCOOTER) {
            if (isCompressSuccessfully) {
                Intent resultData = new Intent();
                resultData.putExtra("lockID", intent.getStringExtra("lockID"));
                resultData.putExtra("lat", intent.getDoubleExtra("lat", 0));
                resultData.putExtra("lng", intent.getDoubleExtra("lng", 0));
                resultData.putExtra(MediaStore.EXTRA_OUTPUT, mCurrentPhotoPath);
                setResult(RESULT_OK, resultData);
            } else {
                Toast.makeText(this, getString(R.string.warn_unsuccess_make_photo), Toast.LENGTH_SHORT).show();
            }
        } else {
            if (isCompressSuccessfully) {
                Intent resultData = new Intent();
                resultData.putExtra("packetFromLock", intent.getByteArrayExtra("packetFromLock"));
                resultData.putExtra("realState", intent.getByteExtra("realState", (byte) 0xFF));
                resultData.putExtra("lat", intent.getDoubleExtra("lat", 0));
                resultData.putExtra("lng", intent.getDoubleExtra("lng", 0));
                resultData.putExtra(MediaStore.EXTRA_OUTPUT, mCurrentPhotoPath);
                setResult(RESULT_OK, resultData);
            } else {
                Toast.makeText(this, getString(R.string.warn_unsuccess_make_photo), Toast.LENGTH_SHORT).show();
            }
        }
        finish();
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
            }
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        getString(R.string.tag_fileprovider),
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, Codes.REQUEST_TAKE_PHOTO);
            }
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(imageFileName, ".jpg", storageDir);

        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

    public static boolean deletePhoto(String photoPath){
        File fdelete = new File(photoPath);
        if (fdelete.exists()) {
            if (fdelete.delete()) {
                return true;
            }
        }
        return false;
    }

}

