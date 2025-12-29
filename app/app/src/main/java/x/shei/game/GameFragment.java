package x.shei.game;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.app.Fragment;

import x.shei.R;


//把原来的activity中的东西分到了fragment里面
public class GameFragment extends Fragment {
    GameView gameView;

    public GameFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.ui_gameview, container, false);
        gameView = (GameView) view.findViewById(R.id.gameview);
        return view;
    }

    public void normal() {
        gameView.startGame();
    }

}
