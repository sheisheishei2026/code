package x.shei.game;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Point;
import android.media.AudioManager;
import android.media.SoundPool;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.widget.GridLayout;

import x.shei.R;

import java.util.ArrayList;
import java.util.List;

public class GameView extends GridLayout {
    Context context;
    Card[][] cards;
    //空位置的列表集合
    List<Point> empty = new ArrayList<>();

    public GameView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initGameView(context);
    }

    public GameView(Context context) {
        super(context);
        initGameView(context);
    }

    public GameView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initGameView(context);
    }

    AnimationSet set1 = new AnimationSet(true);
    AnimationSet set2 = new AnimationSet(true);

    AlphaAnimation a1 = new AlphaAnimation(0, 1);
    AlphaAnimation a2 = new AlphaAnimation(1, 0);
    ScaleAnimation s1 = new ScaleAnimation(0.1f, 1, 0.1f, 1, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
    ScaleAnimation s2 = new ScaleAnimation(1.0f, 1.15f, 1.0f, 1.15f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);

    SoundPool soundPool;
    int track1;
    int track2;

    private void initSound() {
        soundPool = new SoundPool(1, AudioManager.STREAM_MUSIC, 0);
        track1 = soundPool.load(context, R.raw.move, 0);
        track2 = soundPool.load(context, R.raw.merge, 0);
    }

    private void initGameView(Context context) {
        this.context = context;
        initSound();
        initAnim();
//        setBackgroundColor(0Xffbbada0);
        startGame();
    }

    private void initAnim() {
        s1.setDuration(100);
        s2.setDuration(100);
        a1.setDuration(100);
        a2.setDuration(100);
        set1.addAnimation(a1);
        set1.addAnimation(s1);
        set2.addAnimation(a2);
        set2.addAnimation(s2);
    }

    int cardWidth;


    public int getsWidth() {
        DisplayMetrics mDisplayMetrics = new DisplayMetrics();
        ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(mDisplayMetrics);
        return mDisplayMetrics.widthPixels;
    }

    public int getsHeight() {
        DisplayMetrics mDisplayMetrics = new DisplayMetrics();
        ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(mDisplayMetrics);
        return mDisplayMetrics.heightPixels;
    }

    //终于不耦合了，可以作为参数改变了
    public void startGame() {
        removeAllViews();
        cardWidth = Math.min(getsWidth()-15, getsHeight() - 5) / GameActivity.numbers;
        cards = new Card[GameActivity.numbers][GameActivity.numbers];
        addCard(cardWidth, cardWidth);
        setColumnCount(GameActivity.numbers);

        setOnTouchListener(new OnTouchListener() {
            float startX, StartY, offsetX, offsetY;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        startX = event.getX();
                        StartY = event.getY();
                        break;
                    case MotionEvent.ACTION_UP:
                        offsetX = event.getX() - startX;
                        offsetY = event.getY() - StartY;
                        if (Math.abs(offsetX) > Math.abs(offsetY)) {
                            if (offsetX < -5) {
                                swipeLeft();
                            } else if (offsetX > 5) {
                                swipeRight();
                            }
                        } else {
                            if (offsetY < -5) {
                                swipeUp();
                            } else if (offsetY > 5) {
                                swipeDown();
                            }
                        }
                        break;
                    case MotionEvent.ACTION_MOVE:
                        break;
                }
                return true;

            }
        });
        for (int y = 0; y < GameActivity.numbers; y++) {
            for (int x = 0; x < GameActivity.numbers; x++) {
                cards[x][y].setNum(0);
            }
        }
        GameActivity.getGameActivity().clearScore();

        //根据设置来产生几个随机数
        switch (GameActivity.numbers) {
            case 3:
            case 4:
                forAdd(2);
                break;
            case 5:
                forAdd(4);
                break;
            case 6:
                forAdd(8);
                break;
            default:
                forAdd(12);
                break;
        }
    }

    private void forAdd(int x) {
        for (int i = 0; i < x; i++) {
            addRNum();
        }
    }

    private void addCard(int w, int h) {
        for (int y = 0; y < GameActivity.numbers; y++) {
            for (int x = 0; x < GameActivity.numbers; x++) {
                Card card = new Card(context);
                addView(card, w, h);
                cards[x][y] = card;
            }
        }

    }

    private void addRNum() {
        empty.clear();
        for (int y = 0; y < GameActivity.numbers; y++) {
            //line
            for (int x = 0; x < GameActivity.numbers; x++) {
                if (cards[x][y].getNum() <= 0) {
                    empty.add(new Point(x, y));
                }
            }
        }
        //核心代码，从列表的长度中随机取一个
        if (empty.size() > 0) {
            Point p = empty.remove((int) (Math.random() * empty.size()));
            if (Math.random() > 0.7) {
                cards[p.x][p.y].setNum(16);
//            } else if (Math.random() > 0.5) {
//                cards[p.x][p.y].setNum(8);
//            } else if (Math.random() > 0.3) {
//                cards[p.x][p.y].setNum(4);
            } else {
                cards[p.x][p.y].setNum(8);
            }
            cards[p.x][p.y].startAnimation(set1);
        }
    }

    //只要把一个逻辑弄懂了，其他的就都会了
    private void swipeLeft() {
        boolean merge = false;
        for (int y = 0; y < GameActivity.numbers; y++) {
            for (int x = 0; x < GameActivity.numbers; x++) {
                for (int x1 = x + 1; x1 < GameActivity.numbers; x1++) {
                    if (cards[x1][y].getNum() > 0) {
                        if (cards[x][y].getNum() <= 0) {

                            cards[x][y].setNum(cards[x1][y].getNum());
                            cards[x1][y].setNum(0);
                            move = true;
                            //这里相当于又走了一遍循环
                            x--;
                            merge = true;
                        } else if (cards[x][y].equals(cards[x1][y])) {
                            cards[x][y].setNum(cards[x][y].getNum() * 2);
                            cards[x1][y].setNum(0);
                            hebing = true;
                            addscore(cards[x][y]);
                            merge = true;
                        }
                        break;
                    }
                }
            }
        }
        if (hebing) {
            soundPool.play(track2, 1, 1, 0, 0, 1);
            hebing = false;
        } else if (move) {
            soundPool.play(track1, 1, 1, 0, 0, 1);
            move = false;
        }
        if (merge) {
            addAndCheck();
        }
    }


    //合并的地方有加分
    private void addscore(Card card) {
        GameActivity.getGameActivity().addScore(card.getNum());
        card.startAnimation(s2);
    }

    boolean move = false, hebing = false;

    private void swipeRight() {
        boolean merge = false;
        for (int y = 0; y < GameActivity.numbers; y++) {
            for (int x = GameActivity.numbers - 1; x >= 0; x--) {
                for (int x1 = x - 1; x1 >= 0; x1--) {
                    if (cards[x1][y].getNum() > 0) {
                        if (cards[x][y].getNum() <= 0) {
                            cards[x][y].setNum(cards[x1][y].getNum());
                            cards[x1][y].setNum(0);
                            move = true;
                            //相当于又继续重新遍历，去掉则移动后的相同元素不会合并，也就是只有空的时候才遍历
                            x++;
                            merge = true;
                        } else if (cards[x][y].equals(cards[x1][y])) {
                            cards[x][y].setNum(cards[x][y].getNum() * 2);
                            cards[x1][y].setNum(0);
                            addscore(cards[x][y]);
                            hebing = true;
                            merge = true;
                        }
                        break;
                    }
                }
            }
        }
        if (hebing) {
            soundPool.play(track2, 1, 1, 0, 0, 1);
            hebing = false;
        } else if (move) {
            soundPool.play(track1, 1, 1, 0, 0, 1);
            move = false;
        }
        if (merge) {
            addAndCheck();
        }
    }

    private void swipeUp() {
        boolean merge = false;
        for (int x = 0; x < GameActivity.numbers; x++) {
            for (int y = 0; y < GameActivity.numbers; y++) {
                for (int y1 = y + 1; y1 < GameActivity.numbers; y1++) {
                    if (cards[x][y1].getNum() > 0) {
                        if (cards[x][y].getNum() <= 0) {
                            cards[x][y].setNum(cards[x][y1].getNum());
                            cards[x][y1].setNum(0);
                            move = true;
                            y--;
                            merge = true;
                        } else if (cards[x][y].equals(cards[x][y1])) {
                            cards[x][y].setNum(cards[x][y].getNum() * 2);
                            cards[x][y1].setNum(0);
                            hebing = true;
                            addscore(cards[x][y]);
                            merge = true;
                        }
                        break;
                    }
                }
            }
        }
        if (hebing) {
            soundPool.play(track2, 1, 1, 0, 0, 1);
            hebing = false;
        } else if (move) {
            soundPool.play(track1, 1, 1, 0, 0, 1);
            move = false;
        }
        if (merge) {
            addAndCheck();
        }
    }

    private void swipeDown() {
        boolean merge = false;
        for (int x = 0; x < GameActivity.numbers; x++) {
            for (int y = GameActivity.numbers - 1; y >= 0; y--) {
                for (int y1 = y - 1; y1 >= 0; y1--) {
                    if (cards[x][y1].getNum() > 0) {
                        if (cards[x][y].getNum() <= 0) {
                            cards[x][y].setNum(cards[x][y1].getNum());
                            cards[x][y1].setNum(0);
                            move = true;
                            y++;
                            merge = true;
                        } else if (cards[x][y].equals(cards[x][y1])) {
                            cards[x][y].setNum(cards[x][y].getNum() * 2);
                            cards[x][y1].setNum(0);
                            hebing = true;
                            addscore(cards[x][y]);
                            merge = true;
                        }
                        break;
                    }
                }
            }
        }
        if (hebing) {
            soundPool.play(track2, 1, 1, 0, 0, 1);
            hebing = false;
        } else if (move) {
            soundPool.play(track1, 1, 1, 0, 0, 1);
            move = false;
        }
        if (merge) {
            addAndCheck();
        }
    }

    private void addAndCheck() {
        //根据设置来产生几个随机数
        switch (GameActivity.numbers) {
            case 3:
            case 4:
            case 5:
                addRNum();
                checkComplete();
                break;
            case 6:
                addRNum();
                checkComplete();
                addRNum();
                checkComplete();
                break;
            default:
                addRNum();
                checkComplete();
                addRNum();
                checkComplete();
                addRNum();
                checkComplete();
                addRNum();
                checkComplete();
                break;
        }
    }

    private void checkComplete() {
        boolean complete = true;
//        if (empty.size()>0)
        all:
        for (int y = 0; y < GameActivity.numbers; y++) {
            for (int x = 0; x < GameActivity.numbers; x++) {
                //这个也可以写成判断list的长度是否为0，放到外面，节约性能
                if (cards[x][y].getNum() == 0 ||
                        x > 0 && cards[x][y].equals(cards[x - 1][y]) ||
                        x < GameActivity.numbers - 1 && cards[x][y].equals(cards[x + 1][y]) ||
                        y > 0 && cards[x][y].equals(cards[x][y - 1]) ||
                        y < GameActivity.numbers - 1 && cards[x][y].equals(cards[x][y + 1])) {
                    complete = false;
                    break all;
                }
            }
        }
        //是走完循环之后判断条件在决定是否生成对话框
        if (complete) {
            new AlertDialog.Builder(context)
                    .setTitle("游戏结束")
                    .setMessage("你已无路可走")
                    .setPositiveButton("再来一局", (dialog, which) -> {
                        startGame();
                    })
                    .setNegativeButton("取消", (dialog, which) -> {

                    })
                    .setCancelable(false)
                    .show();
        }
    }

}
