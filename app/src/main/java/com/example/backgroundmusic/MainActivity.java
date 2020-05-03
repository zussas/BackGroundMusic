package com.example.backgroundmusic;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.media.Image;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.ArrayMap;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {


    /**
     * フィールド
     */
    private ImageSwitcher mImageSwitcher;
    private ImageButton mBtnBack;            //  戻るボタン
    private ImageButton mBtnNext;            //  進むボタン
    private ImageButton mBtnPlay;            //  再生ボタン
    private MediaPlayer mMediaPlayer;
    private SeekBar seekBar;
    private TextView mTvTitle;               //  タイトルテキスト
    private TextView mTvRemainTime;          //  残り時間テキスト
    private int mNowPlayNum = 0;             //  現在表示されている曲の格納番号
    private int mNowPlayBgmNum = 0;          //  現在表示されている曲のBGMリソース番号
    private int mTimeTotal = 0;              //  再生曲の総時間


    /**
     * データ格納配列
     */
    private int[] mImageList = {                //  画像　ID　データ格納
            R.drawable.bgm1,
            R.drawable.bgm2,
            R.drawable.bgm3
    };
    private String[] mTitleList = {             //  BGM　タイトル　格納
            "お天道様の下",
            "春風",
            "halfway through one's journey"
    };
    private int [] mBgmList = {                 //  BGM　ID　格納
            R.raw.bgm1,
            R.raw.bgm2,
            R.raw.bgm3
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        /**
         * 処理　：　ImageSwitcher
         */
        mImageSwitcher = findViewById(R.id.imageSwitcher);
        mImageSwitcher.setInAnimation(MainActivity.this,
                android.R.anim.slide_in_left);
        mImageSwitcher.setOutAnimation(MainActivity.this,
                android.R.anim.slide_out_right);
        mImageSwitcher.setFactory(new ViewSwitcher.ViewFactory() {                //  ImageSwitcher　ファクトリーの設定
            @Override
            public View makeView() {
                ImageView imageView = new ImageView(MainActivity.this);    //  イメージの生成
                return imageView;
            }
        });
        mImageSwitcher.setImageResource(R.drawable.bgm1);                         //  ImageSwitcher　に初期の画像を設置


        /**
         * 処理　：　Title　テキスト
         */
        mTvTitle = findViewById(R.id.tv_title);         //  タイトル用テキスト
        mTvTitle.setText(mTitleList[0]);                //  初期値設定
        mTvRemainTime = findViewById(R.id.tv_remainTime);    //  残り時間表示用テキスト


        /**
         * 処理　：　Button
         */
        mBtnBack = findViewById(R.id.imgBtnBack);
        mBtnNext = findViewById(R.id.imgBtnNext);
        mBtnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {                           //  TODO　：　クリックリスナを変更　onClickSkipButton()
                skip(-1);                                       //  一つ前の画像に戻る
            }
        });
        mBtnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {                           //  TODO　：　クリックリスナを変更　onClickBackButton()
                skip(1);                                        //  次の画像に進む
            }
        });
        mBtnPlay = findViewById(R.id.imgBtnPlay);
        mBtnPlay.setOnClickListener(new btnClickPlay());              //  再生ボタンクリック


        /**
         * 処理　：　MediaPlayer
         */
        mMediaPlayer = MediaPlayer.create(MainActivity.this, R.raw.bgm1);
        mMediaPlayer.seekTo(0);
        mMediaPlayer.setOnCompletionListener(new PlayerCompletionListener());       //  メディア再生の終了を検知するリスナ
        mTimeTotal = mMediaPlayer.getDuration();                                    //  音楽再生総時間の取得


        /**
         * 処理　：　seekBar
         */
        seekBar = findViewById(R.id.seekBar);
        seekBar.setMax(mTimeTotal);                     //  SeekBar　の最大値に現在の曲の再生総時間をセット
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            boolean isMediaPlayStatement = false;   //  MediaPlayer　が再生中だったかどうか

            //  参考サイト　：　https://codeforfun.jp/android-studio-music-player-4/
            //  進行レベルが変更されたときに呼ばれる
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    if (mMediaPlayer != null) {
                        if (!mMediaPlayer.isPlaying()) {
                            mMediaPlayer.seekTo(progress);
                            seekBar.setProgress(progress);
                        }
                    }
                }
            }

            //  ユーザーがタッチしたときに呼ばれる
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                if (mMediaPlayer.isPlaying()) {         //  もし再生中に　Bar　に触ったら
                    isMediaPlayStatement = true;        //  再生中だったことを記録する
                    mMediaPlayer.pause();               //  消すと　Bar　を動かしても途中から再生されない
                }
            }

            //  ユーザーがタッチを離したときに呼ばれる
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (isMediaPlayStatement) {             //  もし再生中にタッチしたときだったら
                    mMediaPlayer.start();               //  プレーヤーを再生させて
                    isMediaPlayStatement = false;       //  false に戻す
                }
            }
        });


        /**
         * 処理　：　Thread　の作成
         */
        //  参考サイト　：　https://codeforfun.jp/android-studio-music-player-5/

        new Thread(new Runnable() {
            @Override
            public void run() {
                while (mMediaPlayer != null) {
                    try {
                        Message msg = new Message();                     //  ここから
                        msg.what = mMediaPlayer.getCurrentPosition();    //
                        handler.sendMessage(msg);                        //
                        Thread.sleep(1000);                         //  ここまで　1000ミリ秒（ms）ごとに実行する
                    } catch (InterruptedException e) {
                    }
                }
            }
        }).start();

    }


    @Override
    protected void onPause() {
        super.onPause();
        mMediaPlayer.stop();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        mMediaPlayer.stop();
        mMediaPlayer.release();
        mMediaPlayer = null;
    }


    /**
     * Handler　の作成
     */
    //  参考サイト　：　https://codeforfun.jp/android-studio-music-player-5/
    private Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message msg) {

            int currentPosition = msg.what;

            //  再生位置を更新
            if (seekBar.getProgress() == mTimeTotal) {
                seekBar.setProgress(0);                     //  再生終了後　Bar を左端に戻す
            }
            else {
                seekBar.setProgress(currentPosition);       //  Bar　の位置を更新する
            }

            //  残り時間のラベルを更新
            String remainTime = "- " + createTimeLabel(mTimeTotal - currentPosition);
            mTvRemainTime.setText(remainTime);

            return true;
        }
    });


    /**
     * 自作メソッド　：　指定した数だけ　NowPlayNum　の値を変更し、その後画像を　mImageList にセットする
     */
    private void skip(int move) {

        mMediaPlayer.stop();
        mNowPlayNum = mNowPlayNum + move;

        if (mNowPlayNum < 0) {                              //  nowPlayNub が　０　以下になったとき
            mNowPlayNum = mImageList.length -1;             //  配列の最後の番号に設定
        } else if (mNowPlayNum >= mImageList.length) {      //  nowPlayNub が　配列の長さを超えたとき
            mNowPlayNum = 0;                                //  配列の最初の番号に設定
        }
        mImageSwitcher.setImageResource(mImageList[mNowPlayNum]);
        mTvTitle.setText(mTitleList[mNowPlayNum]);

        seekBar.setProgress(0);                             //  SeekBar を最初に戻す

    }


    /**
     * 自作メソッド　：　再生している曲の残り時間を計算する処理
     * time : 現在の再生位置の時間
     * return timeLabel : 残り時間を　0:00 の形式で出力
     */
    private String createTimeLabel(int time) {

        String timeLabel = "";
        int min = time / 1000 / 60;
        int sec = time / 1000 % 60;

        timeLabel = min + ":";
        if (sec < 10) timeLabel += "0";
        timeLabel += sec;

        return timeLabel;

    }


    /**
     * Listener : PlayerCompletion メディア再生が終了した際のリスナ
     */
    private class PlayerCompletionListener implements MediaPlayer.OnCompletionListener {
        @Override
        public void onCompletion(MediaPlayer mp) {
            mMediaPlayer.seekTo(0);                                       //  メディアの再生位置を最初に戻す
            seekBar.setProgress(0);                                            //   SeekBar も最初に戻す
            Toast.makeText(
                    MainActivity.this,
                    "再生が終了しました！",
                    Toast.LENGTH_LONG).show();
            mBtnPlay.setImageResource(android.R.drawable.ic_media_play);        //  終了したら再生ボタンの　”一時停止アイコン”　を　”再生アイコン”　に切り替える
        }
    }


    /**
     * ClickListener : Play　ボタンクリック時の処理
     */
    private class btnClickPlay implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            if (mMediaPlayer.isPlaying()) {
                mMediaPlayer.pause();                                              // 一時停止実行
                mBtnPlay.setImageResource(android.R.drawable.ic_media_play);       // imgButtonPlay のアイコンを　一時停止　に設定
            }
            else {
                mMediaPlayer.start();;                                             // 再生実行
                mBtnPlay.setImageResource(android.R.drawable.ic_media_pause);      // imgButtonPlay のアイコンを　再生アイコン　に設定
            }
        }
    }
}
