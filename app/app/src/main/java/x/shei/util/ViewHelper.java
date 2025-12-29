package x.shei.util;

import android.widget.TextView;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ViewHelper {
    public static void appendWithTime(TextView textView, String input) {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        String time = sdf.format(new Date());
        textView.append(time);
        textView.append(" : ");
        textView.append(input);
        textView.append("\n\n");
    }
}
