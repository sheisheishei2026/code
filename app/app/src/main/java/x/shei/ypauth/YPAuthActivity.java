package x.shei.ypauth;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import x.shei.App;


public class YPAuthActivity extends AppCompatActivity {
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        App.aliyunpanClient.fetchToken(this);
        Log.e("asd","asdfasdfasdf");
    }
}
