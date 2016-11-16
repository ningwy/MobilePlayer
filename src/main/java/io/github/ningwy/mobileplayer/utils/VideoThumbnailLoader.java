package io.github.ningwy.mobileplayer.utils;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.widget.ImageView;

/**
 *
 * Created by ningwy on 2016/10/23.
 */
public class VideoThumbnailLoader {

    private static VideoThumbnailLoader vtl;

    public static VideoThumbnailLoader getInstance() {
        synchronized (VideoThumbnailLoader.class) {
            if (vtl == null) {
                vtl = new VideoThumbnailLoader();
            }
        }
        return vtl;
    }

    public void display(ImageView iv, Context context, ContentResolver cr, String Videopath, ThumbnailListener listener) {
        new VideoThumbnailTask(iv, context, cr, Videopath, listener).execute();
    }

    /**
     * @param context context
     * @param cr ContentResolver
     * @param Videopath 视频的绝对路径
     * @return
     */
    private Bitmap getVideoThumbnail(Context context, ContentResolver cr, String Videopath) {
        ContentResolver testcr = context.getContentResolver();
        String[] projection = { MediaStore.Video.Media.DATA, MediaStore.Video.Media._ID, };
        String whereClause = MediaStore.Video.Media.DATA + " = '" + Videopath + "'";
        Cursor cursor = testcr.query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, projection, whereClause,
                null, null);
        int _id = 0;
        String videoPath = "";
        if (cursor == null || cursor.getCount() == 0) {
            return null;
        }
        if (cursor.moveToFirst()) {

            int _idColumn = cursor.getColumnIndex(MediaStore.Video.Media._ID);
            int _dataColumn = cursor.getColumnIndex(MediaStore.Video.Media.DATA);
            do {
                _id = cursor.getInt(_idColumn);
                videoPath = cursor.getString(_dataColumn);
            } while (cursor.moveToNext());
        }
        cursor.close();
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inDither = false;
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        Bitmap bitmap = MediaStore.Video.Thumbnails.getThumbnail(cr, _id, MediaStore.Images.Thumbnails.MICRO_KIND,
                options);
        return bitmap;
    }

    class VideoThumbnailTask extends AsyncTask<Void, Void, Bitmap> {

        private ImageView iv;
        private Context context;
        private ContentResolver cr;
        private String Videopath;
        private ThumbnailListener thumbnailListener;

        private VideoThumbnailTask(ImageView iv, Context context, ContentResolver cr, String Videopath, ThumbnailListener listener) {
            this.iv = iv;
            this.context = context;
            this.cr = cr;
            this.Videopath = Videopath;
            this.thumbnailListener = listener;
        }


        @Override
        protected Bitmap doInBackground(Void... params) {
            return getVideoThumbnail(context, cr, Videopath);
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
//            iv.setImageBitmap(bitmap != null ? bitmap : BitmapFactory.decodeResource(context.getResources(), R.drawable.video_default_icon));
            thumbnailListener.onThumbnailLoadCompleted(Videopath, iv, bitmap);
        }
    }

    //自己定义一个回调,通知外部图片加载完毕
    public interface ThumbnailListener {
        void onThumbnailLoadCompleted(String url,ImageView iv,Bitmap bitmap);
    }

}
