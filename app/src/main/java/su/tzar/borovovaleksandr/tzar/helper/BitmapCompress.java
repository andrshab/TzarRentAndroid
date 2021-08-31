package su.tzar.borovovaleksandr.tzar.helper;

import android.graphics.Bitmap;

public class BitmapCompress {
    public Bitmap bitmap;

    public BitmapCompress(Bitmap b) {
        double width = b.getWidth();
        double height = b.getHeight();
        int max = 720;

        if (width > height && width > max) {
            height = (max / width) * height;
            width = max;
        } else if (height > max) {
            width = (max / height) * width;
            height = max;
        }

        bitmap = Bitmap.createScaledBitmap(b, (int) width, (int) height, false);
    }
}
