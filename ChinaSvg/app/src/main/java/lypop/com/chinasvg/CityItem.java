package lypop.com.chinasvg;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Region;

/**
 * Created by yzl on 2017/6/2.
 */

public class CityItem {
    private Path path;
    private int drawColor;
    private final int unSelectFill = Color.WHITE;
    private final int unSelectStroke = Color.BLACK;
    private final int selectFill = Color.BLUE;
    private final int selectStroke = Color.YELLOW;
    private final int shadowColor = Color.GREEN;

    public Path getPath() {
        return path;
    }

    public void setPath(Path path) {
        this.path = path;
    }

    public int getDrawColor() {
        return drawColor;
    }

    public void setDrawColor(int drawColor) {
        this.drawColor = drawColor;
    }


    /**
     * @param canvas   当前的画布
     * @param paint    画笔
     * @param isSelect 该区域是否被选中
     */
    public void onDrawCityItem(Canvas canvas, Paint paint, boolean isSelect) {
        if (isSelect) {//选中Item
            //绘制内容
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(selectFill);
            /**
             设置阴影
             radius:阴影的倾斜度
             dx:水平位移
             dy:垂直位移
             */
            paint.setShadowLayer(20, 0, 0, shadowColor);
            canvas.drawPath(path, paint);

            //绘制边缘
            paint.clearShadowLayer();
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(9);
            paint.setColor(selectStroke);
            canvas.drawPath(path, paint);

        } else {//未选中Item
            paint.clearShadowLayer();//清除掉阴影
            //绘制内容
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(unSelectFill);
            canvas.drawPath(path, paint);

            //绘制边缘
            paint.setStrokeWidth(1);
            paint.setStyle(Paint.Style.STROKE);
            paint.setColor(unSelectStroke);
            canvas.drawPath(path, paint);
        }
    }


    /**
     * 判断触摸点是否在Path里
     *
     * @param x
     * @param y
     * @return
     */
    public boolean isTouch(int x, int y) {
        RectF rectF = new RectF();
        path.computeBounds(rectF, true);
        Region region = new Region();
        /*
            Path path：用来构造的区域的路径
            Region clip：与前面的path所构成的路径取交集，并将两交集设置为最终的区域
            如果你只想显示path的区域你只需要让clip取最大即可
         */
        region.setPath(path, new Region((int) rectF.left, (int) rectF.top, (int) rectF.right, (int) rectF.bottom));
        return region.contains(x, y);
    }
}
