package x.shei.todo;

import android.annotation.TargetApi;
import android.app.Activity;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupMenu;

import x.shei.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


//这个程序也用不到数据库，也就不用sharepreference了，如果想重启或杀掉进程后数据仍在，需要重新设计
//灵感来源来当初做库的时候的style，透传推送，当时还做jar包，使用helper来找资源，现在有了aar就不需要了
public class NotifyActivityAdd extends Activity {
    EditText editText;
    Button button;
    View layout;
    SimpleDateFormat df;
    PopupMenu popupMenu;


    @TargetApi(Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate (Bundle savedInstanceState) {
        super.onCreate (savedInstanceState);
        setContentView (R.layout.activity_notify_add);
        notifyDbHelper = new NotifyDbHelper (this);

        editText = (EditText) findViewById (R.id.text);
        button = (Button) findViewById (R.id.time);
        layout = findViewById (R.id.layout);
        //最大是12月，int最大是2147483647，这样可以保证int的id唯一性在一年内
        df = new SimpleDateFormat("MMddHHmmss", Locale.getDefault ());

        editText.setOnKeyListener (new View.OnKeyListener () {
            @Override
            public boolean onKey (View v, int keyCode, KeyEvent event) {

                if (event.getAction () == KeyEvent.ACTION_DOWN) {
                    if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER
                            || keyCode == KeyEvent.KEYCODE_ENTER) {
                        sendNotice ();
                    }
                }
                return false;
            }
        });
        button.setOnClickListener (new View.OnClickListener () {
            @Override
            public void onClick (View v) {
                sendNotice ();
            }
        });

        //长按弹出菜单
        button.setOnLongClickListener (new View.OnLongClickListener () {
            @Override
            public boolean onLongClick (View v) {
                button.setText ("设置");
                button.setTextColor (Color.parseColor ("#33ff99"));
                popupMenu.show ();
                return false;
            }
        });

    }


    //回车和按钮都可以触发
    private void sendNotice () {
        //是要在这里取才对
        String s = editText.getText ().toString ();
        if (TextUtils.isEmpty (s) | s.equals ("")) {
        } else {
            String time = df.format (new Date());
            NotifyTODO notifyTODO = new NotifyTODO (s, time);
            editText.setText ("");
        }
    }

    NotifyDbHelper notifyDbHelper;

}
