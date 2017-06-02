# SVG_Advance
SVG高级可交互地图（台湾为例）
在写之前我们需要引入Path路径的解析类PathParser
1. 下载含有中国地图的  SVG
2. 用http://inloop.github.io/svg2android/  网站 将svg资源转换成相应的Android代码
3. 利用Xml解析SVG的代码  封装成javaBean   最重要的得到Path
4. 重写OnDraw方法  利用Path绘制中国地图
5. 重写OnTouchEvent方法，记录手指触摸位置，判断这个位置是否坐落在某个省份上

##这里列举一些Path的指令

				M = moveto(M X,Y) ：将画笔移动到指定的坐标位置，相当于 android Path 里的moveTo()
				L = lineto(L X,Y) ：画直线到指定的坐标位置，相当于 android Path 里的lineTo()
				H = horizontal lineto(H X)：画水平线到指定的X坐标位置 
				V = vertical lineto(V Y)：画垂直线到指定的Y坐标位置 
				C = curveto(C X1,Y1,X2,Y2,ENDX,ENDY)：三次贝赛曲线 
				S = smooth curveto(S X2,Y2,ENDX,ENDY) 同样三次贝塞尔曲线，更平滑 
				Q = quadratic Belzier curve(Q X,Y,ENDX,ENDY)：二次贝赛曲线 
				T = smooth quadratic Belzier curveto(T ENDX,ENDY)：映射 同样二次贝塞尔曲线，更平滑 
				A = elliptical Arc(A RX,RY,XROTATION,FLAG1,FLAG2,X,Y)：弧线 ，相当于arcTo()
				Z = closepath()：关闭路径（会自动绘制链接起点和终点）

参考：http://www.w3school.com.cn/svg/svg_intro.asp 

地图资源下载：https://www.amcharts.com/dl/javascript-maps/

##进入正题

解析svg的代码，DOM解析

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
我们将所有的Path存到了一个集合中，如何让这些Path显示出来呢，需要重写onDraw方法

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
但是我们怎么判断你点击的位置所在的Path呢？这里我们使用Region类的方法进行判断

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
就这样如果点击的位置在Path里面则返回TRUE如果不在则返回FALSE，我们只需要增加一个for循环便能得到选中的对象了，这里我们使用手势识别器来获取点击的位置

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
这里需要注意的是：这里要将点击的坐标缩放到以前的状态再进行判断，至此SVG的高级应用就结束了
附上效果图
![image](https://github.com/AndLollipop/SVG_Advance/blob/master/img/svg2.gif)
