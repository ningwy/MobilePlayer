package io.github.ningwy.mobileplayer.ui.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import io.github.ningwy.mobileplayer.domain.Lyric;
import io.github.ningwy.mobileplayer.utils.DensityUtil;
import io.github.ningwy.mobileplayer.utils.LogUtil;

/**
 * 歌词控件
 * Created by ningwy on 2016/11/14.
 */
public class LyricTextView extends TextView {

    private Paint greenPaint;//绿色画笔
    private Paint whitePaint;//白色画笔

    //得到控件的宽高
    private int width;
    private int height;

    //歌词文本的高度
    private int textHeight;

    /**
     * 高亮歌词在lyrics中的索引
     */
    private int index;

    private List<Lyric> lyrics;

    //当前歌曲播放的当前进度
    private int currentPosition;

    public LyricTextView(Context context) {
        this(context, null);
    }

    public void setCurrentPosition(int currentPosition) {
        this.currentPosition = currentPosition;
        //设置进度之后立马计算得出index并通知重绘
        index = getIndexByCurrentPosition();
        invalidate();
    }

    public void setLyrics(List<Lyric> lyrics) {
        this.lyrics = lyrics;
    }

    public LyricTextView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LyricTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    //初始化
    private void init(Context context) {
        lyrics = new ArrayList<>();

        //绿色画笔
        greenPaint = new Paint();
        greenPaint.setAntiAlias(true);
        greenPaint.setColor(Color.GREEN);//绿色
        //设置文字居中
        greenPaint.setTextAlign(Paint.Align.CENTER);
        greenPaint.setTextSize(DensityUtil.dp2px(context, 20));

        //白色画笔
        whitePaint = new Paint();
        whitePaint.setAntiAlias(true);
        whitePaint.setColor(Color.GRAY);//灰色
        //设置文字居中
        whitePaint.setTextAlign(Paint.Align.CENTER);
        whitePaint.setTextSize(DensityUtil.dp2px(context, 20));

        textHeight = DensityUtil.dp2px(context, 20);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        width = w;
        height = h;
        super.onSizeChanged(w, h, oldw, oldh);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (lyrics != null && lyrics.size() > 0) {
            //有歌词
            Lyric lyric = lyrics.get(index);
            //平移
            float plush;
            if (lyric.getShowTime() == 0) {
                plush = 0;
            } else {
                //平移
                //这一句所花的时间 ：休眠时间 = 移动的距离 ： 总距离（行高）
                //移动的距离 =  (这一句所花的时间 ：休眠时间)* 总距离（行高）
//                float delta = ((currentPositon-timePoint)/sleepTime )*textHeight;

                float timePoint = lyric.getTime();
                float sleepTime = lyric.getShowTime();
                plush = ((currentPosition - timePoint) / sleepTime) * textHeight;
            }
            canvas.translate(0, -plush);

            //1. 画高亮歌词
            String currentLyric = lyric.getLyricText();
            //高亮歌词画在中间
            canvas.drawText(currentLyric, width / 2, height / 2, greenPaint);
            //2. 画高亮歌词的前面部分
            int tempY = height / 2;
            for (int i = index - 1; i >= 0; i--) {
                String preLyric = lyrics.get(i).getLyricText();
                //减去歌词本身高度即得到上一句歌词的高度
                tempY -= textHeight;
                if (tempY < 0) {
                    break;
                }
                canvas.drawText(preLyric, width / 2, tempY, whitePaint);
            }
            //3. 画高亮歌词的后面部分
            tempY = height / 2;//将tempY高度重置为高亮歌词的高度
            for (int i = index + 1; i < lyrics.size(); i++) {
                String nextLyric = lyrics.get(i).getLyricText();
                tempY += textHeight;
                if (tempY > height) {
                    break;
                }
                canvas.drawText(nextLyric, width / 2, tempY, whitePaint);
            }
        } else {
            //没有歌词
            canvas.drawText("没有歌词", width / 2, width / 2, greenPaint);
        }
    }

    /**
     * 根据当前进度返回高亮歌词的index
     *
     * @return
     */
    private int getIndexByCurrentPosition() {
        if (lyrics != null && lyrics.size() > 0) {
            for (int i = 1; i < lyrics.size(); i++) {
                if (currentPosition < lyrics.get(i).getTime()) {
                    int tempIndex = i - 1;
                    if (currentPosition >= lyrics.get(tempIndex).getTime()) {
                        //当前正在播放的哪句歌词
                        return tempIndex;
                    }
                }
            }
        }
        return 0;
    }
}
