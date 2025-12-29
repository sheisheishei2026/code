package x.shei.activity;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.TextView;

import java.util.Calendar;

import x.shei.R;

public class CustomCalendarView extends GridLayout {
    private Calendar currentCalendar;
    private OnDateSelectedListener dateSelectedListener;
    private OnMonthChangeListener monthChangeListener;
    private String selectedDate = null;
    private GestureDetector gestureDetector;
    
    // 星期标题（从周一开始）
    private final String[] weekDays = {"一", "二", "三", "四", "五", "六", "日"};
    
    public interface OnDateSelectedListener {
        void onDateSelected(int year, int month, int day);
    }
    
    public interface OnMonthChangeListener {
        void onMonthChanged(int year, int month);
    }
    
    public CustomCalendarView(Context context) {
        super(context);
        init();
    }
    
    public CustomCalendarView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    
    public CustomCalendarView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }
    
    private void init() {
        currentCalendar = Calendar.getInstance();
        setColumnCount(7);
        setRowCount(7); // 1行标题 + 最多6行日期
        
        // 初始化手势检测器
        gestureDetector = new GestureDetector(getContext(), new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                if (Math.abs(velocityX) > Math.abs(velocityY) && Math.abs(velocityX) > 500) {
                    if (velocityX > 0) {
                        // 向右滑动，显示上一个月
                        previousMonth();
                    } else {
                        // 向左滑动，显示下一个月
                        nextMonth();
                    }
                    return true;
                }
                return false;
            }
        });
        
        updateCalendar();
    }
    
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        gestureDetector.onTouchEvent(ev);
        return super.onInterceptTouchEvent(ev);
    }
    
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        gestureDetector.onTouchEvent(event);
        return super.onTouchEvent(event);
    }
    
    public void setOnDateSelectedListener(OnDateSelectedListener listener) {
        this.dateSelectedListener = listener;
    }
    
    public void setOnMonthChangeListener(OnMonthChangeListener listener) {
        this.monthChangeListener = listener;
    }
    
    public void updateCalendar() {
        removeAllViews();
        
        // 添加星期标题（高亮显示）
        for (int i = 0; i < 7; i++) {
            TextView dayHeader = createDayHeader(weekDays[i]);
            addView(dayHeader);
        }
        
        // 获取当前月份的第一天
        Calendar calendar = (Calendar) currentCalendar.clone();
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        int firstDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        int daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
        
        // 计算需要显示的空格数（从周一开始）
        // Calendar中：SUNDAY=1, MONDAY=2, ..., SATURDAY=7
        // 我们需要从周一开始，所以需要调整
        int startOffset;
        if (firstDayOfWeek == Calendar.SUNDAY) {
            startOffset = 6; // 周日放在最后，所以前面需要6个空格
        } else {
            startOffset = firstDayOfWeek - Calendar.MONDAY; // 周一到周六
        }
        
        // 添加前面的空格
        for (int i = 0; i < startOffset; i++) {
            TextView emptyView = createEmptyDay();
            addView(emptyView);
        }
        
        // 添加日期
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        
        for (int day = 1; day <= daysInMonth; day++) {
            String dateKey = String.format("%d-%02d-%02d", year, month + 1, day);
            TextView dayView = createDayView(day, dateKey, year, month);
            addView(dayView);
        }
        
        // 通知月份变化
        if (monthChangeListener != null) {
            monthChangeListener.onMonthChanged(year, month);
        }
    }
    
    private TextView createDayHeader(String text) {
        TextView textView = new TextView(getContext());
        textView.setText(text);
        textView.setTextSize(15);
        textView.setTextColor(Color.parseColor("#333333"));
        textView.setGravity(Gravity.CENTER);
        textView.setBackgroundColor(Color.parseColor("#F0F0F0")); // 高亮背景
        textView.setPadding(0, 12, 0, 12);
        
        GridLayout.LayoutParams params = new GridLayout.LayoutParams();
        params.width = 0;
        params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
        params.setMargins(6, 0, 6, 0);
        textView.setLayoutParams(params);
        
        return textView;
    }
    
    private TextView createEmptyDay() {
        TextView textView = new TextView(getContext());
        textView.setText("");
        textView.setMinHeight(70);
        
        GridLayout.LayoutParams params = new GridLayout.LayoutParams();
        params.width = 0;
        params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
        params.setMargins(6, 6, 6, 6);
        textView.setLayoutParams(params);
        
        return textView;
    }
    
    private TextView createDayView(int day, String dateKey, int year, int month) {
        TextView textView = new TextView(getContext());
        textView.setText(String.valueOf(day));
        textView.setTextSize(15);
        textView.setGravity(Gravity.CENTER);
        textView.setMinHeight(70);
        textView.setPadding(0, 12, 0, 12);
        
        // 判断是否是今天
        Calendar today = Calendar.getInstance();
        Calendar dateCalendar = Calendar.getInstance();
        dateCalendar.set(year, month, day);
        
        boolean isToday = isSameDay(today, dateCalendar);
        
        // 设置背景和文字颜色
        if (isToday) {
            // 今天：绿色背景，白色文字（高亮）
            textView.setBackgroundResource(R.drawable.calendar_today_bg);
            textView.setTextColor(Color.parseColor("#FFFFFF"));
        } else if (dateKey.equals(selectedDate)) {
            // 选中的日期：蓝色背景，白色文字
            textView.setBackgroundResource(R.drawable.calendar_selected_bg);
            textView.setTextColor(Color.parseColor("#FFFFFF"));
        } else {
            // 普通日期：白色背景，黑色文字
            textView.setTextColor(Color.parseColor("#333333"));
            textView.setBackgroundResource(R.drawable.calendar_normal_bg);
        }
        
        // 点击事件
        textView.setOnClickListener(v -> {
            selectedDate = dateKey;
            updateCalendar(); // 刷新日历以显示选中状态
            
            if (dateSelectedListener != null) {
                dateSelectedListener.onDateSelected(year, month, day);
            }
        });
        
        GridLayout.LayoutParams params = new GridLayout.LayoutParams();
        params.width = 0;
        params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
        params.setMargins(6, 6, 6, 6);
        textView.setLayoutParams(params);
        
        return textView;
    }
    
    private boolean isSameDay(Calendar cal1, Calendar cal2) {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
               cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH) &&
               cal1.get(Calendar.DAY_OF_MONTH) == cal2.get(Calendar.DAY_OF_MONTH);
    }
    
    public void setMonth(int year, int month) {
        currentCalendar.set(year, month, 1);
        updateCalendar();
    }
    
    public void previousMonth() {
        currentCalendar.add(Calendar.MONTH, -1);
        updateCalendar();
    }
    
    public void nextMonth() {
        currentCalendar.add(Calendar.MONTH, 1);
        updateCalendar();
    }
    
    public int getCurrentYear() {
        return currentCalendar.get(Calendar.YEAR);
    }
    
    public int getCurrentMonth() {
        return currentCalendar.get(Calendar.MONTH);
    }
}
