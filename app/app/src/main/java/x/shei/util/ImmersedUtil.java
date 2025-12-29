package x.shei.util;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.Size;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * 沉浸式状态栏工具
 *
 * Created by trzhang on 2017/10/16.
 */

public final class ImmersedUtil {

    private static final String TAG = ImmersedUtil.class.getSimpleName();
    private static final String ISIMMERSED = "isImmersed_";
    private static final String MEIZU = "meizu";
    private static final String MIUI = "xiaomi";
    private static Method mSetStatusBarColorIcon;
    private static Method mSetStatusBarDarkIcon;
    private static Field mStatusBarColorFiled;
    private static int SYSTEM_UI_FLAG_LIGHT_STATUS_BAR = 0;
    private static int mColorLevel = 50;

    private ImmersedUtil() {
    }

    /**
     * 设置View的padding
     *
     * @param viewGroup 需要调整的View的父View
     */
    public static void setPadding(@NonNull View viewGroup, int left, int top, int right,
                                  int bottom) {
        viewGroup.setPadding(left, top, right, bottom);
    }

    /**
     * 设置View的padding
     *
     * @param viewGroup          需要调整的View的父View
     * @param marginLayoutParams 调整的布局参数
     */
    public static void setLayoutParams(@NonNull View viewGroup,
                                       @NonNull ViewGroup.MarginLayoutParams marginLayoutParams) {
        viewGroup.setLayoutParams(marginLayoutParams);
    }

    /**
     * 设置沉浸式View的位置
     *
     * @param context            上下文环境
     * @param viewGroup          需要调整的View的父View
     * @param location           调整位置的 left,top,right,bottom
     * @param marginLayoutParams 调整的布局参数
     */
    public static void setImmersedView(@NonNull Context context, @NonNull View viewGroup,
                                       @NonNull @Size(4) int[] location,
                                       @NonNull ViewGroup.MarginLayoutParams marginLayoutParams) {
        setPadding(viewGroup,
                DensityUtil.dip2px(context, location[0]),
                DensityUtil.dip2px(context, location[1]),
                DensityUtil.dip2px(context, location[2]),
                DensityUtil.dip2px(context, location[3]));
        setLayoutParams(viewGroup, marginLayoutParams);
    }

    /**
     * 是否设置沉浸式
     *
     * @param context    上下文环境
     * @param isImmersed 是否设置沉浸式
     */
    public static void setImmersed(@NonNull Activity context, boolean isImmersed) {
        putCache(context, isImmersed);
    }

    /**
     * 查看当前沉浸式状态
     *
     * @param context 上下文环境
     */
    public static boolean getImmersed(@NonNull Activity context) {
        try {
            boolean isImmersed = getCache(context);
            return ((Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) && isImmersed);
        } catch (Exception ex) {
            return false;
        }
    }

    /**
     * 查看当前沉浸式状态,不考虑系统版本，目前只有PoiNotificationWindow用到
     *
     * @param context 上下文环境
     */
    public static boolean getActivityImmersed(@NonNull Activity context) {
        try {
            return getCache(context);
        } catch (Exception ex) {
            return false;
        }
    }

    /**
     * 开启沉浸式模式
     *
     * @param isDark 状态栏文字是否为黑色
     */
    public static void setImmersedMode(Activity activity, boolean isDark) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Window window = activity.getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
                    | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);

            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);

            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);

            InitStatusBarColor(activity, isDark);
        }
    }

    /**
     * 初始化状态栏上的字体颜色状态
     *
     * @param isDark 状态栏文字是否为黑色
     */
    private static void InitStatusBarColor(Activity activity, boolean isDark) {
        if (sIsMiui()) {
            setMIUIStatusBarDarkMode(activity, isDark);
        } else if (sIsFlyme()) {
            setFlymeStatusBarDarkIcon(activity, isDark);
        } else {
            setStatusBarDarkIcon(activity, isDark);
        }
    }

    /**
     * 获取当前机型是否是魅族
     */
    public static boolean sIsFlyme() {
        return MEIZU.equalsIgnoreCase(Build.BRAND);
    }

    /**
     * 获取当前机型是否是小米
     */
    public static boolean sIsMiui() {
        return MIUI.equalsIgnoreCase(Build.BRAND);
    }

    /**
     * 小米官方API  设置状态栏字体颜色
     * https://dev.mi.com/doc/p=4769/index.html
     *
     * @param dark 状态栏文字是否为黑色
     */
    private static void setMIUIStatusBarDarkMode(Activity activity, boolean dark) {
        Class<? extends Window> clazz = activity.getWindow().getClass();
        try {
            int darkModeFlag;
            Class<?> layoutParams = Class.forName("android.view.MiuiWindowManager$LayoutParams");
            Field field = layoutParams.getField("EXTRA_FLAG_STATUS_BAR_DARK_MODE");
            darkModeFlag = field.getInt(layoutParams);
            Method extraFlagField = clazz.getMethod("setExtraFlags", int.class, int.class);
            extraFlagField.invoke(activity.getWindow(), dark ? darkModeFlag : 0, darkModeFlag);
            //这是MIUI的坑 上面是旧版本的MIUI V6.0生效，该方法是MIUI 7以上有效 https://dev.mi
            // .com/console/doc/detail?pId=1159
            setStatusBarDarkIcon(activity, dark);
        } catch (Exception e) {

        }
    }

    /**
     * 魅族官方API 设置状态栏字体颜色
     * http://open-wiki.flyme.cn/index.php?title=Flyme%E7%B3%BB%E7%BB%9FAPI
     * <p>
     * 设置状态栏字体图标颜色
     *
     * @param activity 当前activity
     * @param dark     字体是否深色 true为深色 false 为白色
     */
    private static void setFlymeStatusBarDarkIcon(Activity activity, boolean dark) {
        try {
            mSetStatusBarColorIcon = Activity.class.getMethod("setStatusBarDarkIcon", int.class);
        } catch (NoSuchMethodException e) {

        }
        try {
            mSetStatusBarDarkIcon = Activity.class.getMethod("setStatusBarDarkIcon", boolean.class);
        } catch (NoSuchMethodException e) {

        }
        try {
            mStatusBarColorFiled = WindowManager.LayoutParams.class.getField("statusBarColor");
        } catch (NoSuchFieldException e) {

        }
        try {
            Field field = View.class.getField("SYSTEM_UI_FLAG_LIGHT_STATUS_BAR");
            SYSTEM_UI_FLAG_LIGHT_STATUS_BAR = field.getInt(null);
        } catch (NoSuchFieldException e) {

        } catch (IllegalAccessException e) {

        }
        setFlymeStatusBarDarkIcon(activity, dark, true);
    }

    /**
     * 魅族手机的状态栏颜色修改
     */
    private static void setFlymeStatusBarDarkIcon(Activity activity, boolean dark, boolean flag) {

        if (mSetStatusBarDarkIcon != null) {
            try {
                mSetStatusBarDarkIcon.invoke(activity, dark);
            } catch (IllegalAccessException e) {

            } catch (InvocationTargetException e) {

            }
        } else {
            if (flag) {
                setFlymeStatusBarDarkIcon(activity, activity.getWindow(), dark);
            }
        }
    }

    /**
     * 设置状态栏颜色
     */

    private static void setStatusBarDarkIcon(Activity activity, boolean dark) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            View view = activity.getWindow().getDecorView();
            int oldVis = view.getSystemUiVisibility();
            int newVis = oldVis;
            if (dark) {
                newVis |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            } else {
                newVis &= ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            }
            if (newVis != oldVis) {
                view.setSystemUiVisibility(newVis);
            }
        }
    }

    public static void setLightStatusBar(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            View view = activity.getWindow().getDecorView();
            int flags = view.getSystemUiVisibility();
            flags &= ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            view.setSystemUiVisibility(flags);
        }
    }


    public static void setDarkStatusBar(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            View view = activity.getWindow().getDecorView();
            int flags = view.getSystemUiVisibility();
            flags |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            view.setSystemUiVisibility(flags);
        }
    }


    /**
     * 魅族手机设置状态栏字体图标颜色
     *
     * @param window 当前窗口
     * @param dark   字体是否深色 true为深色 false 为白色
     */
    private static void setFlymeStatusBarDarkIcon(Activity activity, Window window, boolean dark) {
        View decorView = window.getDecorView();
        if (decorView != null) {
            setStatusBarDarkIcon(activity, dark);
            setFlymeStatusBarColor(window, 0);
        }
    }

    /**
     * 魅族手机设置状态栏颜色
     */
    private static void setFlymeStatusBarColor(Window window, int color) {
        WindowManager.LayoutParams winParams = window.getAttributes();
        if (mStatusBarColorFiled != null) {
            try {
                int oldColor = mStatusBarColorFiled.getInt(winParams);
                if (oldColor != color) {
                    mStatusBarColorFiled.set(winParams, color);
                    window.setAttributes(winParams);
                }
            } catch (IllegalAccessException e) {

            }
        }
    }

    /**
     * 根据图片的颜色动态的改变状态栏上文字的颜色
     */
    public static void setStatusBarColorByDynamic(@NonNull Activity activity, int resourceID) {
        boolean isDark = isBlackColor(activity, resourceID, null, null, mColorLevel);
        setStatusBarColorActivi(activity, isDark);
    }

    /**
     * 根据图片的颜色动态的改变状态栏上文字的颜色
     */
    public static void setStatusBarColorByDynamic(@NonNull Activity activity, Bitmap bitmap) {
        boolean isDark = isBlackColor(activity, 0, bitmap, null, mColorLevel);
        setStatusBarColorActivi(activity, isDark);
    }

    /**
     * 根据图片的颜色动态的改变状态栏上文字的颜色
     */
    public static void setStatusBarColorByDynamic(@NonNull Activity activity, Drawable drawable) {
        boolean isDark = isBlackColor(activity, 0, null, drawable, mColorLevel);
        setStatusBarColorActivi(activity, isDark);
    }


    /**
     * 根据相应的色值进行对状态栏进行动态的设置
     *
     * @param isDark 当前状态栏的背景颜色 true 黑色 false 白色
     */
    public static void setStatusBarColorActivi(@NonNull Activity activity, boolean isDark) {
        if (getImmersed(activity)) {
            if (sIsFlyme()) {
                setFlymeStatusBarDarkIcon(activity, !isDark);
            } else if (sIsMiui()) {
                setMIUIStatusBarDarkMode(activity, !isDark);
            } else {
                setStatusBarDarkIcon(activity, !isDark);
            }
        }
    }

    /**
     * 改变状态栏（6.0以上系统 文字&图标）颜色
     */
    public static void changeStatusBarColor(@NonNull Activity activity, boolean isDark) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (sIsFlyme()) {
                setFlymeStatusBarDarkIcon(activity, !isDark);
            } else if (sIsMiui()) {
                setMIUIStatusBarDarkMode(activity, !isDark);
            } else {
                setStatusBarDarkIcon(activity, !isDark);
            }
        }
    }


    /**
     * 判断当前背景图色值是否为深色色值
     *
     * @param level 是划分黑和非黑的临界数值 白色(255) 黑色(0)
     */
    public static boolean isBlackColor(Activity activity, int resourceID, Bitmap bitmap,
                                       Drawable drawable, int level) {
        int color = getColor(activity, resourceID, bitmap, drawable);
        int grey = toGrey(color);
        return grey < level;
    }

    /**
     * 获取对应资源的主要颜色值
     */
    private static int getColor(Activity activity, int resourceID, Bitmap bitmap,
                                Drawable drawable) {
//        Palette palette = null;
//        if (bitmap != null) {
//            palette = Palette.from(cropStatusBarArea(activity, bitmap)).generate();
//        } else if (drawable != null) {
//            Bitmap drawableBitmap = drawableToBitmap(activity, drawable);
//            if (drawableBitmap != null) {
//                palette = Palette.from(drawableBitmap).generate();
//            }
//        } else if (resourceID != 0) {
//            Bitmap resourceBitmap = BitmapFactory.decodeResource(activity.getResources(),
//                    resourceID);
//            palette = Palette.from(resourceBitmap).generate();
//        }
//        return palette != null ? palette.getDominantColor(Color.WHITE) : -1;
        return Color.WHITE;
    }


    /**
     * 将color转换到相应的0～255范围
     */
    private static int toGrey(int rgb) {
        int blue = rgb & 0x000000FF;
        int green = (rgb & 0x0000FF00) >> 8;
        int red = (rgb & 0x00FF0000) >> 16;
        return (red * 38 + green * 75 + blue * 15) >> 7;
    }

    /**
     * 将Drawable转换成Bitmap,同时如果为非BitmapDrawable的时候，这里也是为了减少内存的使用
     */
    private static Bitmap drawableToBitmap(Context context, Drawable drawable) {
        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        } else {
            Bitmap bitmap = Bitmap.createBitmap(
                    DensityUtil.dip2px(context, 100),
                    DensityUtil.dip2px(context, 25), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            drawable.setBounds(0, 0,
                    DensityUtil.dip2px(context, 100), DensityUtil.dip2px(context, 25));
            drawable.draw(canvas);
            return bitmap;
        }
    }

    private static Bitmap cropStatusBarArea(Context context, Bitmap bitmap) {
        if (bitmap.getHeight() <= ViewUtil.getStatusBarHeight()) {
            return bitmap;
        }
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), ViewUtil.getStatusBarHeight());
    }

    private static void putCache(@NonNull Activity context, boolean isImmersed) {
//        SGCIPStorage storage = new SGCIPStorage("quickbuy_ImmersedUtil");
//        storage.putBoolean(context, ISIMMERSED + context.getClass().getSimpleName(), isImmersed);
    }

    private static boolean getCache(@NonNull Activity context) {
        return true;
//        SGCIPStorage storage = new SGCIPStorage("quickbuy_ImmersedUtil");
//        return storage.getBoolean(context,
//                ISIMMERSED + context.getClass().getSimpleName(), false);
    }
}
