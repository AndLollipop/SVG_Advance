package lypop.com.chinasvg;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.support.v4.view.GestureDetectorCompat;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

/**
 * Created by yzl on 2017/6/2.
 */

public class MapView extends View {
    private List<CityItem> items;
    private GestureDetectorCompat gestureDetectorCompat;//手势识别
    private float scale = 1.3f;
    private Paint paint;
    private CityItem selectItem;

    public MapView(Context context) {
        this(context, null);
    }

    public MapView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        items = new ArrayList<>();
        paint = new Paint();
        paint.setAntiAlias(true);

        gestureDetectorCompat = new GestureDetectorCompat(getContext(), new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onDown(MotionEvent e) {
                //实现手势识别的点击事件
                for (int i = 0; i < items.size(); i++) {
                    if (items.get(i).isTouch((int) (e.getX() / scale), (int) (e.getY() / scale))) {//在判断点击位置所在的Path的时候，要将x,y坐标还原到初始状态
                        selectItem = items.get(i);
                        postInvalidate();
                    }
                }
                return super.onDown(e);
            }
        });
        new MyThread().start();
    }

    class MyThread extends Thread {
        @Override
        public void run() {
            parseListBean();
        }
    }

    /**
     * 解析svg生成Path对象并存入List集合中
     */
    private void parseListBean() {
        InputStream inputStream = getContext().getResources().openRawResource(R.raw.taiwan);
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = null;
        try {
            builder = factory.newDocumentBuilder();
            Document document = builder.parse(inputStream);//使用DOM对svg进行解析
            Element rootElement = document.getDocumentElement();//得到根元素
            NodeList pathNodeList = rootElement.getElementsByTagName("path");
            for (int i = 0; i < pathNodeList.getLength(); i++) {
                Element node = (Element) pathNodeList.item(i);
                String pathData = node.getAttribute("android:pathData");//得到path的路径信息
                Path path = PathParser.createPathFromPathData(pathData);
                CityItem item = new CityItem();
                item.setPath(path);
                items.add(item);
            }
            postInvalidate();//异步刷新
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.save();
        canvas.scale(scale, scale);
        //绘制未选中的
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i) != selectItem) {
                items.get(i).onDrawCityItem(canvas, paint, false);
            }
        }
        //绘制选中的
        if (selectItem != null) {
            selectItem.onDrawCityItem(canvas, paint, true);
        }
        canvas.restore();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return gestureDetectorCompat.onTouchEvent(event);
    }
}
