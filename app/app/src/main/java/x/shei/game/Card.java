package x.shei.game;

import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

public class Card extends FrameLayout {
    Context context;
    View background;
    TextView label;
    int num = 0;

    public Card(Context context) {
        super(context);
        this.context = context;
//        initDrawable();
//        sp = context.getSharedPreferences(Constant.PREFERENCE_FILE, Context.MODE_PRIVATE);

        background = new View(getContext());
        LayoutParams params = new LayoutParams(-1, -1);
        params.setMargins(15, 15, 0, 0);
        background.setBackgroundDrawable(Constant.getEmptyDrawable());
        addView(background, params);

        label = new TextView(getContext());
        label.setTextColor(GameActivity.textcolor);
        label.setTextSize(GameActivity.textsize);
        label.setGravity(Gravity.CENTER);
        LayoutParams layoutParams = new LayoutParams(-1, -1);//matchparent
        //只设置左和上就好了
        layoutParams.setMargins(15, 15, 0, 0);
        addView(label, layoutParams);


    }

//    public void initDrawable() {
//        if (drawable == null) {
//            drawable = new GradientDrawable();
//        }
//        drawable.setCornerRadius(Constant.DRAWABLE_RADIUS);
//    }
//
//    GradientDrawable drawable;

    public int getNum() {
        return num;
    }

    public void setNum(int num) {
        this.num = num;
        int x = num == 0 ? 0 : (int) ((Math.log(num) / Math.log(2)) + 0.05);
        //这里所有的数组长度都是12,num属性不变，变得只是样式
        if (x >= Constant.color.length) {
            x = Constant.color.length - 1;
        }
        label.setBackgroundDrawable(GameActivity.getGameActivity().drawable[x]);
        if (!GameActivity.isImage) {
//            Log.e("log",GameActivity.getGameActivity().array[x]);
            label.setText(GameActivity.getGameActivity().array[x]);
        }

//        drawable.setColor(x == 0 ? 0x000000 : Constant.color[x]);
//        if (sp.getBoolean(Constant.KEY_IMAGE, true)) {
//            if (x > 0) {
////                }
//                label.setBackgroundDrawable(GameActivity.drawable[x]);
//                //对于数字0，所有统一使用EmptyDrawable
//            } else {
//                label.setBackgroundDrawable(drawable);
//            }
//            //不是图像的使用文字，这里是读取的
//        } else {
//            drawable.setColor(x == 0 ? 0x0000000 : sp.getInt(Constant.KEY_COLORARRAY + x, Constant.color[x]));
//            label.setText(x == 0 ? "" : sp.getString(Constant.KEY_EDITARRAY + x, Constant.arrayBack[x]));
//            label.setBackgroundDrawable(drawable);
//        }
    }

    //做动画用
    public TextView getLabel() {
        return label;
    }

    //去掉override  compare number
    public boolean equals(Card card) {
        return getNum() == card.getNum();
    }
}
