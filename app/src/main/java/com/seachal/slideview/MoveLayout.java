package com.seachal.slideview;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MoveLayout extends RelativeLayout implements OnClickListener {
    private static final String TAG = "Measure";

    private Context context;
    private int downX;
    private TextView tv_top;
    private TextView delete;
    private TextView more;
    private TextView tvCenter;

    /**
     * 最后都会执行这个构造方法
     *
     * @param context
     * @param attrs
     * @param defStyleAttr
     */
    public MoveLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public MoveLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MoveLayout(Context context) {
        this(context, null);
    }


    private void init(Context context) {
        this.context = context;
        LayoutInflater.from(context).inflate(R.layout.move_layout, this, true);
        delete = (TextView) findViewById(R.id.delete);
        more = (TextView) findViewById(R.id.more);
        tv_top = (TextView) findViewById(R.id.tv_top);
        tvCenter = (TextView) findViewById(R.id.tv_center);
        tv_top.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                //根据 handlerTouch 的返回值确定怎样移动。  返回false就不可以点击。
                return handlerTouch(v, event);
            }
        });
        tv_top.setOnClickListener(this);
        more.setOnClickListener(this);
        delete.setOnClickListener(this);
    }

    /* ---------------------处理 Touch-------------------------- */
    boolean result = false;  //
    boolean isOpen = false;  // 打开闭合状态，  是否已经向左划开

    protected boolean handlerTouch(View v, MotionEvent event) {

        int bottomWidth = delete.getWidth() + delete.getWidth();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                // Log.i("", "ACTION_DOWN");
                //触摸点相对于屏幕默认坐标系的坐标,屏幕边缘
                downX = (int) event.getRawX();
                break;
            // 移动事件是实时的。
            case MotionEvent.ACTION_MOVE:
                // Log.i("", "ACTION_MOVE");
                //按下后的移动量  seachal
                int dx = (int) (event.getRawX() - downX);
                if (isOpen) {
                    // 打开状态
                    // 向右滑动    （可以还原为闭合状态）
                    if (dx > 0 && dx < bottomWidth) {
                        //dx > 0说明手指是向右移动的， seachal
                        // dx < bottomWidth,应该是移动量超过文本块的宽度就不再移动。seachal
                        Log.i(TAG, "v.getLeft:" + v.getLeft());
                        /**
                         * setTranslationX 的移动方式是以getLeft（）的位置为参考，-》
                         * setTranslationX(0)，setTranslationX(1)...setTranslationX(15)渐进式的移动动画->
                         * *
                         * dx - bottomWidth 的数据值变化方向 -50->0,数值-50只是用于举例，并不是真实值。
                         */
                        v.setTranslationX(dx - bottomWidth);// 参数是负数，相对于getleft移动
                        Log.i(TAG, "打开状态向右移动dx - bottomWidth: " + (dx - bottomWidth));
                        // 允许移动，阻止点击
                        result = true;
                    }
                } else {
                    // 闭合状态
                    // 向左移动
                    if (dx < 0 && Math.abs(dx) < bottomWidth) {
                        //Math.abs(dx) < bottomWidth ，超过这个移动量就不再滑动
                        /**
                         *  dx - bottomWidth 的数据值变化方向 0 -> -50,
                         */
                        v.setTranslationX(dx);   //  参数是负数
                        Log.i(TAG, "闭合状态向左移动 dx: " + dx);
                        // 允许移动，阻止点击
                        result = true;
                    }
                }
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:

                // Log.i("", "ACTION_UP" + v.getTranslationX());
                //  view.getTranslationX计算的是该view的偏移量。初始值为0，向左偏移值为负，向右偏移值为正。
                // 获取偏移量， 都是相对于原位置向左偏移
                float ddx = v.getTranslationX();
                Log.i(TAG, "获取偏移量ddx:" + v.getTranslationX());


                if (ddx <= 0 && ddx > -(bottomWidth / 2)) {
                    /**
                     * 偏移量不足bottomWidth的二分之一，置为关闭状态 ，
                     */
                    //第一个参数为 view对象，第二个参数为 动画改变的类型，第三，第四个参数依次是开始位置和结束位置。
                    ObjectAnimator oa1 = ObjectAnimator.ofFloat(v, "translationX", ddx, 0).setDuration(100);
                    oa1.start();
                    oa1.addListener(new AnimatorListener() {
                        @Override
                        public void onAnimationStart(Animator animation) {
                        }

                        @Override
                        public void onAnimationRepeat(Animator animation) {
                        }

                        @Override
                        public void onAnimationEnd(Animator animation) {
                            isOpen = false;
                            result = false;
                        }

                        @Override
                        public void onAnimationCancel(Animator animation) {
                            isOpen = false;
                            result = false;
                        }
                    });

                }
                /**
                 * 偏移量超过bottomWidth 的二分之， 置为打开状态。
                 */
                if (ddx <= -(bottomWidth / 2) && ddx > -bottomWidth) {
                    ObjectAnimator oa1 = ObjectAnimator.ofFloat(v, "translationX", ddx, -bottomWidth)
                            .setDuration(100);
                    oa1.start();
                    result = true;
                    isOpen = true;
                }
                break;
            default:
                break;
        }
        return result;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_top:
                Toast.makeText(context, "item", Toast.LENGTH_SHORT).show();
                break;
            case R.id.more:
                Toast.makeText(context, "more", Toast.LENGTH_SHORT).show();
                break;
            case R.id.delete:
                Toast.makeText(context, "delete", Toast.LENGTH_SHORT).show();
                break;
            default:
                break;
        }
    }
}