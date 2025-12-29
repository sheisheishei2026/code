package x.shei.game;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import android.app.FragmentManager;
import android.app.FragmentTransaction;

import x.shei.R;
import x.shei.util.ImmersedUtil;


public class GameActivity extends Activity implements View.OnClickListener {

    public Drawable[] drawable = new Drawable[13];
    //arrayback问题,对象类型不要直接等于
    public String[] array = new String[13];
    public static int textcolor = Color.DKGRAY;
    public static int textsize = 20;
    public static int numbers = 5;
    public static boolean isImage = true;
    //新纪录使用
    boolean newGame = true;
    TextView scoretext;
    TextView highscoretext;
    private static GameActivity gameActivity;
    GameFragment gameFragment;
    SharedPreferences sp;
    SharedPreferences.Editor ed;
    FragmentManager fragmentManager;

    //为了让外界获取到mainactivity的方法
    public GameActivity() {
        gameActivity = this;
    }

    public static GameActivity getGameActivity() {
        return gameActivity;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ImmersedUtil.setImmersedMode(this,false);
//        showUpdate("http://music.baidu.com/cms/mobile/static/apk/BaiduMusic-pcwebdownpagetest.apk");
        sp = getSharedPreferences(Constant.PREFERENCE_FILE, Context.MODE_PRIVATE);
        ed = sp.edit();
        initDrawable();
        updateConfig();
        setContentView(R.layout.ui_main2);
        gameFragment = new GameFragment();
        fragmentManager = getFragmentManager();

        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.content, gameFragment);
        fragmentTransaction.commit();
        scoretext = (TextView) findViewById(R.id.score);
        highscoretext = (TextView) findViewById(R.id.high);
        highscoretext.setText("最高分: " + getBestScore());
    }

    private void initDrawable() {
        GradientDrawable gradientDrawable = new GradientDrawable();
        gradientDrawable.setCornerRadius(Constant.DRAWABLE_RADIUS);
        gradientDrawable.setColor(0x30ffffff);
        drawable[0] = gradientDrawable;
//        drawable[0] = Constant.getEmptyDrawable();
        array[0] = "";
    }

    private void updateConfig() {
        numbers = sp.getInt(Constant.KEY_COLUMENUMBER, 5);
        isImage = sp.getBoolean(Constant.KEY_IMAGE, true);
        if (sp.getBoolean(Constant.KEY_IMAGE, true)) {
            for (int i = 1; i < drawable.length; i++) {
                String s2 = Constant.path + i + ".PNG";
                //如果有自定义图片就使用
                if (Tools.isFileExist(s2)) {
                    drawable[i] = Tools.bitmap2Drawable(Tools.file2Bitmap(s2));
                } else {
                    drawable[i] = getResources().getDrawable(Constant.dw[i - 1]);
                }
            }

        } else if (sp.getBoolean(Constant.KEY_DEFAULT, false)) {
            textsize = 20;
            textcolor = Color.DKGRAY;
            numbers = 5;
            try {
                getActionBar().setBackgroundDrawable(new ColorDrawable(textcolor));
            } catch (Exception e) {
                e.printStackTrace();
            }
            for (int i = 1; i < drawable.length; i++) {
                GradientDrawable gradientDrawable = new GradientDrawable();
                gradientDrawable.setCornerRadius(Constant.DRAWABLE_RADIUS);
                gradientDrawable.setColor(Constant.color[i]);
                drawable[i] = gradientDrawable;
                array[i] = Constant.arrayBack[i];
            }

        } else {
            textcolor = sp.getInt(Constant.KEY_TEXTCOLOR, Color.DKGRAY);
            try {
                getActionBar().setBackgroundDrawable(new ColorDrawable(textcolor));
            } catch (Exception e) {
                e.printStackTrace();
            }
            textsize = sp.getInt(Constant.KEY_TEXTSIZE, 20);
            for (int i = 1; i < drawable.length; i++) {
                GradientDrawable gradientDrawable = new GradientDrawable();
                gradientDrawable.setCornerRadius(Constant.DRAWABLE_RADIUS);
                gradientDrawable.setColor(sp.getInt(Constant.KEY_COLORARRAY + i, Constant.color[i]));
                drawable[i] = gradientDrawable;
                array[i] = sp.getString(Constant.KEY_EDITARRAY + i, Constant.arrayBack[i]);
            }
        }
    }


    int score = 0;

    public void addScore(int s) {
        score += s;
        scoretext.setText("" + score);
        if (score > getBestScore()) {
            if (newGame) {
                try {
                    new AlertDialog.Builder(this)
                            .setTitle("新纪录诞生")
                            .setMessage("恭喜你创造了新纪录！")
                            .show();
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    newGame = false;
                }
            }

        }
        int maxScore = Math.max(score, getBestScore());
        ed.putInt(Constant.KEY_HIGHSCORE, maxScore);
        ed.apply();
        showBest(maxScore);

    }

    public void showBest(int s) {
        highscoretext.setText("最高分: " + s);
    }

    public void clearScore() {
        score = 0;
        scoretext.setText("0");
    }

    public void restart(View v) {
        updateConfig();
        newGame = true;
        gameFragment.normal();
        // 初始化MediaPlay对象 ，准备播放音乐
        MediaPlayer mPlayer = MediaPlayer.create(this, R.raw.news);
        // 开始播放
        mPlayer.start();
    }

    private ImageView[] icon = new ImageView[12];//自定义头像序列
    private Integer index;//头像序列的tag

    public int getBestScore() {
        return sp.getInt(Constant.KEY_HIGHSCORE, 0);
    }

    //头像序列的监听器
    @Override
    public void onClick(View v) {
        if (v.getTag() != null) {
            index = (Integer) v.getTag();
//            dialog.dismiss();
        }
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);//ACTION_OPEN_DOCUMENT
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/jpeg");
//        if(android.os.Build.VERSION.SDK_INT>=android.os.Build.VERSION_CODES.KITKAT){
//            startActivityForResult(intent, SELECT_PIC_KITKAT);
        startActivityForResult(intent, 2);
//        this.v = (Button) v;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 2 && resultCode == Activity.RESULT_OK && data != null) {
            Uri imageFileUri = data.getData();//获取选择图片的URI
            Bitmap b = Tools.centerSquareScaleBitmap(BitmapFactory.decodeFile(Tools.getRealFilePath(this, imageFileUri)),
                    (int) (Tools.getWidth(this) / 4.5));//经测试发现4.5最好
            Bitmap round = Tools.getRoundedImage(b);
//          Tools.saveBitmap(b, Environment.getExternalStorageDirectory().getAbsolutePath() + "/", "test" + (index+1));
            Tools.saveBitmap(this, round, "test" + (index + 1));
            icon[index].setImageBitmap(round);
        }
    }

}
