package by.khrapovitsky.notesclient.adapter;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.LruCache;
import android.view.View;
import android.widget.ImageView;
import android.widget.ResourceCursorAdapter;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import by.khrapovitsky.notesclient.R;

public class NotesCursorAdapter extends ResourceCursorAdapter {

    private Context context;
    private LruCache<String, Bitmap> mLruCache;

    public NotesCursorAdapter(Context context, int layout, Cursor c, int flags) {
        super(context, layout, c, flags);
        this.context = context;
        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
        final int cacheSize = maxMemory / 4;
        mLruCache = new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                return bitmap.getByteCount() / 1024;
            }
        };
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        TextView id = (TextView) view.findViewById(R.id.archiveName);
        ImageView imageNote = (ImageView) view.findViewById(R.id.idImageNote);
        TextView note = (TextView) view.findViewById(R.id.note);
        TextView lastDateModify = (TextView) view.findViewById(R.id.lastDateModify);
        id.setText(cursor.getString(cursor.getColumnIndexOrThrow("_id")));
        String noteText = readTxtFile(cursor.getString(cursor.getColumnIndexOrThrow("txtPath")));
        String date = cursor.getString(cursor.getColumnIndexOrThrow("lastDateModify"));
        note.setText(noteText);
        lastDateModify.setText(date);
        String path = cursor.getString(cursor.getColumnIndexOrThrow("imagePath"));
        if (path == null)
            path = "-1";
        Bitmap bitmap = getBitmapFromMemCache(path);
        if (bitmap == null) {
            bitmap = getImage(path);
            addBitmapToMemoryCache(path, bitmap);
            imageNote.setImageBitmap(bitmap);
        } else {
            imageNote.setImageBitmap(bitmap);
        }
    }

    private Bitmap getImage(String path) {
        Bitmap bitmap = null;
        if (path != null) {
            if (!path.equals("-1")) {
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;
                BitmapFactory.decodeFile(Uri.parse(path).getPath(), options);
                final int REQUIRED_SIZE = 80;
                int scale = 1;
                while (options.outWidth / scale / 2 >= REQUIRED_SIZE
                        && options.outHeight / scale / 2 >= REQUIRED_SIZE)
                    scale *= 2;
                options.inSampleSize = scale;
                options.inJustDecodeBounds = false;
                bitmap = BitmapFactory.decodeFile(Uri.parse(path).getPath(), options);
            }
        }
        if (bitmap == null) {
            bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_no_image);
        }
        return bitmap;
    }

    private void addBitmapToMemoryCache(String key, Bitmap bitmap) {
        if (getBitmapFromMemCache(key) == null) {
            mLruCache.put(key, bitmap);
        }
    }

    private Bitmap getBitmapFromMemCache(String key) {
        return mLruCache.get(key);
    }

    private String readTxtFile(String txtPath) {
        if (txtPath != null) {
            File txtFile = new File(txtPath);
            StringBuilder text = new StringBuilder();
            String tmp = "String is Empty!";
            if (!txtFile.exists()) {
                return tmp;
            } else {
                try {
                    BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(txtPath), "UTF-8"));
                    String line;
                    while ((line = br.readLine()) != null) {
                        text.append(line);
                        text.append('\n');
                    }
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (text.toString().isEmpty()) {
                return tmp;
            } else {
                return text.toString();
            }
        }
        return "String is Empty!";
    }

}
