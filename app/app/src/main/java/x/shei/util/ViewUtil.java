package x.shei.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.style.ClickableSpan;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.ViewCompat;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * View操作相关工具类
 * Created by jzj on 2017/5/2.
 */
public final class ViewUtil {

    public static final int SIZE_IGNORE = Integer.MIN_VALUE;

    private ViewUtil() {
    }

    private static volatile ViewUtil sViewUtil;

    public static ViewUtil getInstance() {
        if (sViewUtil == null) {
            synchronized (ViewUtil.class) {
                if (sViewUtil == null) {
                    sViewUtil = new ViewUtil();
                }
            }
        }
        return sViewUtil;
    }

    TextView textView;

    public void set(Context context) {
        textView = new TextView(context);
        textView.setText("asdfasdfasdf");
    }

    public View get() {
        return textView;
    }

    private static final int[] tempLoc = new int[2];

    /**
     * 设置textView的text。text为空时，textView不显示。
     *
     * @param textView 请勿传入null
     * @return textView是否显示
     */
    public static boolean setText(TextView textView, CharSequence text) {
        if (null == textView) {
            return false;
        }
        if (TextUtils.isEmpty(text)) {
            textView.setVisibility(View.GONE);
            return false;
        } else {
            textView.setText(text);
            textView.setVisibility(View.VISIBLE);
            return true;
        }
    }

    /**
     * 设置textView的text。text为0时，textView不显示。
     *
     * @param textView 请勿传入null
     * @return textView是否显示
     */
    public static boolean setText(TextView textView, int text) {
        if (text == 0) {
            textView.setVisibility(View.GONE);
            return false;
        } else {
            textView.setText(text);
            textView.setVisibility(View.VISIBLE);
            return true;
        }
    }

    /**
     * 设置textView的text。text为空时，layout不显示。
     *
     * @param layout   请勿传入null
     * @param textView 请勿传入null
     * @return textView是否显示
     */
    public static boolean setText(ViewGroup layout, TextView textView, CharSequence text) {
        if (TextUtils.isEmpty(text)) {
            layout.setVisibility(View.GONE);
            return false;
        } else {
            textView.setText(text);
            layout.setVisibility(View.VISIBLE);
            return true;
        }
    }

    /**
     * 设置TextView的text,当text为空时,展示默认的text
     */
    public static void setText(TextView textView, CharSequence text, CharSequence defaultText) {
        if (textView == null) {
            return;
        }
        if (!TextUtils.isEmpty(text)) {
            textView.setText(text);
        } else {
            textView.setText(defaultText);
        }
    }

    /**
     * 设置ImageView的icon。icon为0时，imageView不显示。
     *
     * @param imageView 请勿传入null
     */
    public static boolean setImageResource(ImageView imageView, int resId) {
        if (resId == 0) {
            imageView.setVisibility(View.GONE);
            return false;
        } else {
            imageView.setImageResource(resId);
            imageView.setVisibility(View.VISIBLE);
            return true;
        }
    }
    /**
     * 设置ImageView的icon。icon为0时，imageView不显示。
     *
     * @param imageView 请勿传入null
     */
    public static boolean setImageBitmap(ImageView imageView, Bitmap bitmap) {
        if (bitmap == null) {
            imageView.setVisibility(View.GONE);
            return false;
        } else {
            imageView.setImageBitmap(bitmap);
            imageView.setVisibility(View.VISIBLE);
            return true;
        }
    }

    /**
     * 设置top上下移动View
     */
    public static void offsetViewTop(View view, int top) {
        if (view != null && top != view.getTop()) {
            ViewCompat.offsetTopAndBottom(view, top - view.getTop());
        }
    }

    /**
     * 通过LayoutParams设置View的尺寸
     *
     * @param view   View
     * @param width  宽度。可以是ViewGroup.LayoutParams.MATCH_PARENT，ViewGroup.LayoutParams
     *               .WRAP_CONTENT。为0则不设置。
     * @param height 高度。可以是ViewGroup.LayoutParams.MATCH_PARENT，ViewGroup.LayoutParams
     *               .WRAP_CONTENT。为0则不设置。
     * @return LayoutParams为null，则返回false
     */
    @Deprecated
    public static boolean setViewLayout(View view, int width, int height) {
        ViewGroup.LayoutParams lp = view.getLayoutParams();
        if (lp == null) {
            return false;
        }
        if (width != 0) {
            lp.width = width;
        }
        if (height != 0) {
            lp.height = height;
        }
        view.setLayoutParams(lp);
        return true;
    }

    /**
     * 通过LayoutParams设置View的尺寸
     *
     * @param view   View
     * @param width  宽度。可以是ViewGroup.LayoutParams.MATCH_PARENT，ViewGroup.LayoutParams.WRAP_CONTENT。为
     *               {@link #SIZE_IGNORE} 则不设置。
     * @param height 高度。可以是ViewGroup.LayoutParams.MATCH_PARENT，ViewGroup.LayoutParams.WRAP_CONTENT。为
     *               {@link #SIZE_IGNORE} 则不设置。
     * @return LayoutParams为null，则返回false
     */
    public static boolean setLayoutParamSize(View view, int width, int height) {
        ViewGroup.LayoutParams lp = view.getLayoutParams();
        if (lp == null) {
            return false;
        }
        if (width != SIZE_IGNORE) {
            lp.width = width;
        }
        if (height != SIZE_IGNORE) {
            lp.height = height;
        }
        view.setLayoutParams(lp);
        return true;
    }

    /**
     * 设置View的Alpha。Alpha=0时，View不展示
     */
    public static void setAlpha(View view, float alpha) {
        if (null != view) {
            view.setVisibility(alpha > 0f ? View.VISIBLE : View.INVISIBLE);
            view.setAlpha(alpha);
        }
    }

    /**
     * 根据Class倒序搜索View，返回第一个搜索结果。没搜索到则返回null。
     *
     * @param clazz 要搜索的View的类型是clazz或其子类
     */
    @SuppressWarnings("unchecked")
    public static <T> T inverseFindViewBySuperClass(View view, @NonNull Class<? extends T> clazz) {
        if (view == null) {
            return null;
        }

        if (clazz.isInstance(view)) {
            return (T) view;
        }

        if (view instanceof ViewGroup) {
            final ViewGroup viewGroup = (ViewGroup) view;
            final int count = viewGroup.getChildCount();
            for (int i = count - 1; i >= 0; i--) {
                T result = inverseFindViewBySuperClass(viewGroup.getChildAt(i), clazz);
                if (result != null) {
                    return result;
                }
            }
        }
        return null;
    }

    /**
     * 根据Class倒序搜索View，返回第一个搜索结果。没搜索到则返回null。
     *
     * @param clazz 要搜索的View的类型是clazz
     */
    @SuppressWarnings("unchecked")
    public static <T> T inverseFindViewByClass(View view, @NonNull Class<? extends T> clazz) {
        if (view == null) {
            return null;
        }

        if (clazz == view.getClass()) {
            return (T) view;
        }

        if (view instanceof ViewGroup) {
            final ViewGroup viewGroup = (ViewGroup) view;
            final int count = viewGroup.getChildCount();
            for (int i = count - 1; i >= 0; i--) {
                T result = inverseFindViewByClass(viewGroup.getChildAt(i), clazz);
                if (result != null) {
                    return result;
                }
            }
        }
        return null;
    }

    /**
     * 判断一个View是否是另一个View的嵌套ChildView
     */
    public static boolean isChildViewOf(View view, final View parent) {
        if (view == null || parent == null) {
            return false;
        }
        while (true) {
            if (view == parent) {
                return true;
            }

            final ViewParent viewParent = view.getParent();
            if (viewParent != null && viewParent instanceof View) {
                view = (View) viewParent;
            } else {
                return false;
            }
        }
    }

    /**
     * 获取状态栏高度
     */
    public static int getStatusBarHeight() {
        int result = 0;
        Resources resources = Resources.getSystem();
        int resourceId = resources.getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = resources.getDimensionPixelSize(resourceId);
        }
        return result;
    }

    /**
     * 获取当前应用的StatusBarHeight
     */
    public static int getStatusBarHeightForApp(Context context) {
        if(null == context){
            return 0;
        }
        Resources resources = context.getResources();
        if(null == resources){
            return 0;
        }
        int resourceId = resources.getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            return resources.getDimensionPixelSize(resourceId);
        }
        return 0;
    }


    /**
     * 获取 ActionBar 高度，用于 android:layout_height="?android:attr/actionBarSize" 这类的 actionBar 高度获取
     */
    public static int getActionBarHeight(Context context) {
        if (context == null) {
            return 0;
        }
        Resources.Theme theme = context.getTheme();
        if (theme == null) {
            return 0;
        }
        TypedArray styledAttributes = theme.obtainStyledAttributes(
                new int[]{android.R.attr.actionBarSize});
        if (styledAttributes == null) {
            return 0;
        }
        int actionBarSize = (int) styledAttributes.getDimension(0, 0);
        styledAttributes.recycle();
        return actionBarSize;
    }

    /**
     * 加载Drawable并启动动画（如果支持）
     */
    public static void loadAnimatableDrawable(ImageView imageView, @DrawableRes int res) {
        Drawable dr = imageView.getResources().getDrawable(res);
        if (dr != null) {
            imageView.setImageDrawable(dr);
            if (dr instanceof Animatable) {
                ((Animatable) dr).start();
            }
        }
    }

    /**
     * 释放Drawable
     */
    public static void releaseDrawable(ImageView imageView) {
        if (imageView.getDrawable() != null) {
            imageView.getDrawable().setCallback(null);
        }
        imageView.setImageDrawable(null);
    }

    /**
     * 判断点击屏幕的位置是否在指定的view上
     *
     * @param view 点击的view
     * @param x    屏幕所在X位置
     * @param y    屏幕所在Y位置
     */
    public static boolean isInViewArea(View view, float x, float y) {

        if (view == null) {
            return false;
        }

        int[] location = new int[2];
        // 获取控件在屏幕中的位置，返回的数组分别为控件左顶点的 x、y 的值
        view.getLocationOnScreen(location);

        RectF rect = new RectF(location[0], location[1], location[0] + view.getWidth(),
                location[1] + view.getHeight());
        return rect.contains(x, y);
    }

    /**
     * view相对refView的顶部位置差（topLocationOf(view) - topLocationOf(ref)）
     *
     * @return 大于0：view顶部在refView下方；小于0：在上方
     */
    public static int getDiffY(View view, View refView) {
        if (view == null || refView == null) {
            return 0;
        }
        view.getLocationInWindow(tempLoc);
        int y = tempLoc[1];
        refView.getLocationInWindow(tempLoc);
        y = y - tempLoc[1];
        return y;
    }

    /**
     * 获取View相对Window的Rect
     */
    public static Rect getViewRectInWindow(@Nullable View view) {
        if (view == null) {
            return null;
        }
        view.getLocationInWindow(tempLoc);
        final int l = tempLoc[0];
        final int t = tempLoc[1];
        return new Rect(l, t, l + view.getWidth(), t + view.getHeight());
    }

    /**
     * View在Window中的位置，和指定的Rect是否有重叠
     */
    public static boolean isViewIntersectRect(@Nullable View view, @Nullable Rect rect) {
        return view != null && rect != null && Rect.intersects(getViewRectInWindow(view), rect);
    }

    public static boolean isInScreen(View view) {
        return view != null && isInScreen(view, view.getContext());
    }

    /**
     * 判断View是否在屏幕内
     */
    public static boolean isInScreen(View view, Context context) {
        if (view == null || view.getVisibility() != View.VISIBLE || context == null) {
            return false;
        }
        // 判断view先是区域和屏幕区域是否有交集 有交集则说明需要曝光
        int screenWidth = context.getResources().getDisplayMetrics().widthPixels;
        int screenHeight = context.getResources().getDisplayMetrics().heightPixels;
        int viewTop = view.getTop();
        int viewBottom = view.getBottom();
        int viewLeft = view.getLeft();
        int viewRight = view.getRight();

        // view在屏幕上的左上角坐标
        int[] locationOnScreen = new int[2];
        view.getLocationOnScreen(locationOnScreen);
        // view在屏幕上的rect
        Rect viewVisibleRect = new Rect(locationOnScreen[0], locationOnScreen[1],
                viewRight - viewLeft + locationOnScreen[0],
                viewBottom - viewTop + locationOnScreen[1]);
        // 屏幕rect
        Rect screenRect = new Rect(0, 0, screenWidth, screenHeight);
        // 屏幕rect是否包含view rect
        boolean isIntersect = screenRect.intersect(viewVisibleRect);
        return isIntersect;

    }

    public static boolean isInScreen(View child, View parent) {
        if (child == null || child.getVisibility() != View.VISIBLE || parent == null
                || parent.getVisibility() != View.VISIBLE) {
            return false;
        }
        //子view
        Rect viewVisibleRect = getRect(child);
        //父view
        Rect screenRect = getRect(parent);
        //是否有交集
        boolean isIntersect = screenRect.intersect(viewVisibleRect);
        return isIntersect;

    }

    private static Rect getRect(View view) {
        int viewTop = view.getTop();
        int viewBottom = view.getBottom();
        int viewLeft = view.getLeft();
        int viewRight = view.getRight();

        // view在屏幕上的左上角坐标
        int[] locationOnScreen = new int[2];
        view.getLocationOnScreen(locationOnScreen);
        // view在屏幕上的rect
        Rect viewVisibleRect = new Rect(locationOnScreen[0], locationOnScreen[1],
                viewRight - viewLeft + locationOnScreen[0],
                viewBottom - viewTop + locationOnScreen[1]);
        return viewVisibleRect;
    }


    public static boolean isInViewTree(View view) {
        boolean ret = false;
        if (view == null) {
            return false;
        }
        ViewParent parent = view.getParent();
        while (parent != null) {
            if (!(parent instanceof View)) {
                ret = true;
                break;
            }
            parent = parent.getParent();
        }
        return ret;
    }

    /**
     * 修改View的Padding。传入负数表示不改变，0和正数表示新的px值。
     */
    public static void setPadding(View view, int left, int top, int right, int bottom) {
        if (view == null) return;
        left = left < 0 ? view.getPaddingLeft() : left;
        top = top < 0 ? view.getPaddingTop() : top;
        right = right < 0 ? view.getPaddingRight() : right;
        bottom = bottom < 0 ? view.getPaddingBottom() : bottom;
        view.setPadding(left, top, right, bottom);
    }

    /**
     * 修改View的Padding。传入负数表示不改变，0和正数表示新的dp值。
     */
    public static void setPaddingInDp(View view, int left, int top, int right, int bottom) {
        if (view == null) return;
        left = left < 0 ? view.getPaddingLeft() : DensityUtil.dip2px(view.getContext(), left);
        top = top < 0 ? view.getPaddingTop() : DensityUtil.dip2px(view.getContext(), top);
        right = right < 0 ? view.getPaddingRight() : DensityUtil.dip2px(view.getContext(), right);
        bottom = bottom < 0 ? view.getPaddingBottom() : DensityUtil.dip2px(view.getContext(),
                bottom);
        view.setPadding(left, top, right, bottom);
    }

    /**
     * 设置TextView的RightCompoundDrawable
     */
    public static void setRightCompoundDrawable(@NonNull TextView textView,
                                                @Nullable Drawable drawable) {
        //noinspection ConstantConditions
        if (textView != null) {
            if (drawable != null) {
                drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
            }
            textView.setCompoundDrawables(null, null, drawable, null);
        }
    }

    /**
     * 获取View相对Parent中的(x, y)坐标。View应该是Parent的直接或嵌套子View。
     *
     * @return 如果View在Parent中，则返回数组；否则返回null
     */
    @Nullable
    public static int[] getRelativeLocation(View view, View parent) {
        if (view != null && parent != null) {
            int[] location = new int[]{0, 0};
            location[0] += view.getX();
            location[1] += view.getY();
            while (true) {
                final ViewParent viewParent = view.getParent();
                if (viewParent == parent) {
                    return location;
                }
                if (viewParent instanceof View) {
                    view = (View) viewParent;
                    location[0] += view.getX();
                    location[1] += view.getY();
                } else {
                    return null;
                }
            }
        }
        return null;
    }

    public static Drawable getDrawable(Context context, @DrawableRes int id) {
        if (context == null || id == 0) {
            return null;
        }
        try {
            Drawable drawable = context.getResources().getDrawable(id);
            if (drawable != null) {
                drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
            }
            return drawable;
        } catch (Resources.NotFoundException e) {
//            L.e(e);
            return null;
        }
    }

    public static void getHighlightSpannableString(final TextView textView, String content,
                                                   String highlightDesc, final int highlightColor, final boolean isUnderLine,
                                                   final boolean isBold) {
        SpannableString spStr = new SpannableString(content);

        spStr.setSpan(new ClickableSpan() {
                          @Override
                          public void updateDrawState(TextPaint ds) {
                              super.updateDrawState(ds);
                              ds.setColor(highlightColor);
                              ds.setUnderlineText(isUnderLine);
                              ds.setFakeBoldText(isBold);
                          }

                          @Override
                          public void onClick(View widget) {
                          }
                      }, content.indexOf(highlightDesc), content.indexOf(highlightDesc) +
                        highlightDesc.length(),
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        textView.setText(spStr);
    }

    public static void setTextBold(TextView textView, boolean bold) {
        if (textView != null) {
            textView.setTypeface(bold ? Typeface.DEFAULT_BOLD : Typeface.DEFAULT);
        }
    }

    /**
     * 为textview设置text,若text为空,则textview置为gone
     */
    public static void setTextAndFitVisibility(TextView tv, CharSequence text) {
        if (tv == null) {
            return;
        }
        if (TextUtils.isEmpty(text)) {
            tv.setVisibility(View.GONE);
        } else {
            tv.setVisibility(View.VISIBLE);
            tv.setText(text);
        }
    }

    /**
     * 设置字体加粗
     */
    public static void setTextViewBold(TextView tv, boolean isBold) {
        if (tv == null) {
            return;
        }
        TextPaint tp = tv.getPaint();
        tp.setFakeBoldText(isBold);
    }

    /**
     * 通过targetChild找到directChild
     * View嵌套关系：root --> direct --> xxx --> target
     */
    public static View findDirectView(View root, View target) {
        return findDirectView(root, target, 10);
    }

    /**
     * 通过targetChild找到directChild
     * View嵌套关系：root --> direct --> xxx --> target
     *
     * @param maxIteration 最大循环次数
     */
    public static View findDirectView(View root, View target, int maxIteration) {
        if (root == null || target == null || maxIteration <= 0) {
            return null;
        }
        View view = target;
        for (int i = 0; i < maxIteration; i++) {
            ViewParent viewParent = view.getParent();
            if (viewParent == root) {
                return view;
            }
            if (!(viewParent instanceof View)) {
                return null;
            }
            view = (View) viewParent;
        }
        return null;
    }

    /**
     * 设置View的Margin。l t r b大于等于0时才设置。
     */
    public static void setLayoutMargin(View view, int l, int t, int r, int b) {
        if (view != null) {
            ViewGroup.LayoutParams lp = view.getLayoutParams();
            if (lp instanceof ViewGroup.MarginLayoutParams) {
                ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) lp;
                if (l >= 0) {
                    params.leftMargin = l;
                }
                if (t >= 0) {
                    params.topMargin = t;
                }
                if (r >= 0) {
                    params.rightMargin = r;
                }
                if (b >= 0) {
                    params.bottomMargin = b;
                }
            }
        }
    }

    /**
     * 设置View的Margin。l t r b为 {@link #SIZE_IGNORE} 时不设置。
     */
    public static void setLayoutParamMargin(View view, int l, int t, int r, int b) {
        if (view != null) {
            ViewGroup.LayoutParams lp = view.getLayoutParams();
            if (lp instanceof ViewGroup.MarginLayoutParams) {
                ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) lp;
                if (l != SIZE_IGNORE) {
                    params.leftMargin = l;
                }
                if (t != SIZE_IGNORE) {
                    params.topMargin = t;
                }
                if (r != SIZE_IGNORE) {
                    params.rightMargin = r;
                }
                if (b != SIZE_IGNORE) {
                    params.bottomMargin = b;
                }
            }
        }
    }

    /**
     * 解决setbackground时padding不生效问题
     */
    public static void setBackgroundAndKeepPadding(View view, @DrawableRes int resid) {
        int top = view.getPaddingTop();
        int left = view.getPaddingLeft();
        int right = view.getPaddingRight();
        int bottom = view.getPaddingBottom();
        view.setBackgroundResource(resid);
        view.setPadding(left, top, right, bottom);
    }

    /**
     * @Description: 判断ListView是否可以滑动，即ListView的Item是否显示在同一个屏幕内，不需要滑动就能全部显示。
     * @author zlf
     * @date 2016年01月09日 下午 15:06:08
     */
    @SuppressLint("NewApi")
    public static boolean listViewCanScroll(ListView mListView) {
        boolean canScroll = false;
        if (mListView == null) {
            return canScroll;
        }
        int count = mListView.getCount();//返回的是 ListView 的所有的 Item 的总数。
        int firstVisiblePosition = mListView.getFirstVisiblePosition();
        int lastVisibkePosition = mListView.getLastVisiblePosition();

        /*
         * 第一个可见的Item等于0，证明ListView是在最顶部，
         *并且最后一个可见I的tem+1（Item是从0开始的）等于Item的总数的话，证明ListView在最底部
         *如果一个ListView同时可以看见顶部和底部的话，证明该ListView是不可滑动的，即ListView的Item
         *全在一个屏幕内，不需要滑动。
         */
        if (firstVisiblePosition == 0 && count == lastVisibkePosition + 1) {
            canScroll = false;
        } else {
            canScroll = true;
        }
        return canScroll;
    }

    public static int getMarginTop(View view) {
        if (view == null) {
            return 0;
        }
        ViewGroup.LayoutParams params = view.getLayoutParams();
        if (params instanceof ViewGroup.MarginLayoutParams) {
            return ((ViewGroup.MarginLayoutParams) params).topMargin;
        }
        return 0;
    }

    public static void setLayoutWeight(View view, float weight) {
        if (view == null) {
            return;
        }
        ViewGroup.LayoutParams params = view.getLayoutParams();
        if (params instanceof LinearLayout.LayoutParams) {
            ((LinearLayout.LayoutParams) params).weight = weight;
        }
    }

    public static void setVisibility(View view, int visibility) {
        if (null != view) {
            if (view.getVisibility() != visibility) {
                view.setVisibility(visibility);
            }
        }
    }

    /**
     * visible
     */
    public static void visible(View... views) {
        if (null != views && 0 < views.length) {
            for (View view : views) {
                setVisibility(view, View.VISIBLE);
            }
        }
    }

    /**
     * invisible
     */
    public static void invisible(View... views) {
        if (null != views && 0 < views.length) {
            for (View view : views) {
                setVisibility(view, View.INVISIBLE);
            }
        }
    }

    /**
     * gone
     */
    public static void gone(View... views) {
        if (null != views && 0 < views.length) {
            for (View view : views) {
                setVisibility(view, View.GONE);
            }
        }
    }

    /**
     * 设置是否可用
     */
    public static void enable(boolean flag, View... views) {
        if (null != views && 0 < views.length) {
            for (View view : views) {
                if (null != view) {
                    view.setEnabled(flag);
                }
            }
        }
    }

    public static void setOnClickListener(View.OnClickListener listener, View... views) {
        if (null != listener && null != views) {
            for (View view : views) {
                if (null != view) {
                    view.setOnClickListener(listener);
                }
            }
        }
    }

    /**
     * todo 滚动ScrollingView，并返回消费掉的像素。注意：在RecyclerView中不好用……
     */
//    public static int verticalScrollBy(ScrollingView view, int dy) {
//        if (dy == 0 || !(view instanceof View)) {
//            return 0;
//        }
//        // 当前offset
//        final int from = view.computeVerticalScrollOffset();
//        // 滚动范围
//        final int range = view.computeVerticalScrollRange() - view.computeVerticalScrollExtent();
//        if (range == 0) {
//            return 0;
//        }
//        // 滚动后位置
//        final int to = MathUtil.constrain(from + dy, 0, range);
//        if (to == from) {
//            return 0;
//        }
//        ((View) view).scrollTo(0, to);
//        return to - from;
//    }

    /**
     * 加粗字体
     * by duanzhenchao
     *
     * @param textView 目标textView
     * @param isBold   是否加粗
     */
    public static void makeTextBold(@NonNull TextView textView, boolean isBold) {
        TextPaint tp = textView.getPaint();
        tp.setFakeBoldText(isBold);
    }

    @NonNull
    public static List<View> getChildViews(ViewGroup viewGroup) {
        if (viewGroup == null) {
            return Collections.emptyList();
        }
        int count = viewGroup.getChildCount();
        if (count == 0) {
            return Collections.emptyList();
        }
        List<View> list = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            list.add(viewGroup.getChildAt(i));
        }
        return list;
    }

    // 动态获取listView的高度
    public static int measureListViewHeight(ListView listView, int listViewWidth) {
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null) {
            return 0;
        }
        int totalHeight = 0;
        int widthSpec = View.MeasureSpec.makeMeasureSpec(listViewWidth, View.MeasureSpec.AT_MOST);
        for (int i = 0; i < listAdapter.getCount(); i++) {
            View listItem = listAdapter.getView(i, null, listView);
            if (null == listItem) {
                continue;
            }
            listItem.measure(widthSpec, 0);

            int itemHeight = listItem.getMeasuredHeight();
            totalHeight += itemHeight;
        }
        // 减掉底部分割线的高度
        int historyHeight = totalHeight
                + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        return historyHeight;
    }

}
