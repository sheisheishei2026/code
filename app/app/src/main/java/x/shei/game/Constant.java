package x.shei.game;

import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;

import x.shei.R;

public class Constant {

    //默认的色调，第一个不会被用到
    public static final int[] color = {0x000000, 0xffeee4da, 0xffede0c8, 0xfff2b179, 0xfff59563, 0xfff67c5f, 0xfff65e3b,
            0xffedcf72, 0xffedcc61, 0xffedc850, 0xffedc53f, 0xffedc22e, 0xFF95B716};
    //默认的图片数组
    public static final int[] dw = {R.drawable.liubei, R.drawable.lvbu, R.drawable.caocao, R.drawable.caoren,
            R.drawable.zhouyu, R.drawable.sunquan, R.drawable.zhangliao, R.drawable.guanyu, R.drawable.lvmeng,
            R.drawable.luxun, R.drawable.simayi, R.drawable.zhugeliang};
    //默认的三国人物名字
    public static final String[] arrayBack = {"", "刘备", "吕布", "曹操", "曹仁", "周瑜", "孙权", "张辽",
            "关羽", "吕蒙", "陆逊", "司马懿", "诸葛亮"};
    //游戏规则
    public static final String role = "刘备  最可怜了，被吕布追着打;\n" + "吕布 被曹操活捉;\n" + "曹操  在赤壁被曹仁救走了了;\n"
            + "曹仁  多次被周瑜打败;\n" + "周瑜  忠心于孙权;\n" + "孙权  在濡须口差点被张辽活捉;\n" + "张辽  应该打不过关羽吧;\n"
            + "关羽  被吕蒙偷袭了;\n" + "吕蒙  的接替者是陆逊;\n" + "陆逊  在运筹上略逊于司马懿;\n" + "司马懿  常年被诸葛亮打败;\n";

    //    final static String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/test";
    final static String path = "/data/data/feifei.com.example.myapplication.game/files/test";
    public static final String KEY_EDITARRAY = "edit";
    public static final String KEY_COLORARRAY = "color";
    public static final String KEY_COLUMENUMBER = "number";
    public static final String KEY_TEXTSIZE = "textsize";
    public static final String KEY_TEXTCOLOR = "textcolor";
    public static final String KEY_IMAGE = "image";
    public static final String KEY_DEFAULT = "defaulttext";
    public static final String KEY_HIGHSCORE = "highscore";
    public static final int DRAWABLE_RADIUS = 15;
    public static final String PREFERENCE_FILE = "sanguo2048";

    public static GradientDrawable tempDrawable = new GradientDrawable();

    public static Drawable getEmptyDrawable() {
        tempDrawable.setCornerRadius(Constant.DRAWABLE_RADIUS);
        tempDrawable.setColor(0x33ffffff);
        return tempDrawable;
    }
}
