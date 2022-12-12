package com.lexidome.lexagame.games;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.lexidome.lexagame.helper.Misc;
import com.lexidome.lexagame.offers.GlobalAds;
import com.lexidome.lexagame.offers.Offers;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import com.lexidome.lexagame.Home;
import com.lexidome.lexagame.R;

import org.mintsoft.mintlib.jigsawPuzzle;
import org.mintsoft.mintlib.jpListener;

public class Jigsawpuzzle extends AppCompatActivity {
    private jigsawPuzzle puzzle;
    boolean isLocked;
    private int t, sc, pieceCost, roundCost, rows, cols;
    private TextView scoreView, btnText, btnAmt, diagAmtView;
    private ImageView btnIcon, imageView, imageFrame;
    private ProgressBar btnProgress;
    private CountDownTimer countDown;
    private String cat;
    private Dialog loadingDiag, congratsDiag, quitDiag, lowBalDiag, conDiag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.game_jigsawpuzzle);
        Bundle extras = getIntent().getExtras();
        if (extras == null) {
            finish();
        } else {
            cat = extras.getString("cat", null);
            rows = Integer.parseInt(extras.getString("row", "4"));
            cols = Integer.parseInt(extras.getString("col", "5"));
            roundCost = Integer.parseInt(extras.getString("cost_r", "--"));
            pieceCost = Integer.parseInt(extras.getString("cost_p", "--"));
            if (cat == null) {
                Toast.makeText(this, getString(R.string.invalid_category_selected), Toast.LENGTH_LONG).show();
                finish();
            } else {
                RelativeLayout layout = findViewById(R.id.game_jigsawpuzzle_layout);
                imageView = findViewById(R.id.game_jigsawpuzzle_imageView);
                imageFrame = findViewById(R.id.game_jigsawpuzzle_imageFrame);
                scoreView = findViewById(R.id.game_jigsawpuzzle_scoreView);
                btnIcon = findViewById(R.id.game_jigsawpuzzle_btnIcon);
                btnText = findViewById(R.id.game_jigsawpuzzle_btnText);
                btnAmt = findViewById(R.id.game_jigsawpuzzle_btnAmt);
                btnProgress = findViewById(R.id.game_jigsawpuzzle_btnProgress);
                changeBtn(0);
                puzzle = new jigsawPuzzle(this);
                RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                        RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
                puzzle.setLayoutParams(params);
                layout.addView(puzzle);
                initPuzzle();
                findViewById(R.id.game_jigsawpuzzle_close).setOnClickListener(view -> onBackPressed());
                findViewById(R.id.game_jigsawpuzzle_btnView).setOnClickListener(view -> puzzle.getHelp());
                showLoadingDiag();
                GlobalAds.fab(this, "fab_jp");
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (isLocked) {
            showQuitDiag();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        if (countDown != null) countDown.cancel();
        puzzle.onDestroy();
        if (loadingDiag != null && loadingDiag.isShowing()) loadingDiag.dismiss();
        super.onDestroy();
    }

    private void initPuzzle() {
        puzzle.init(cat, rows, cols, imageView, imageFrame, new jpListener() {
            @Override
            public void onImageRecieve(String imgUrl) {
                Picasso.get().load(imgUrl).into(new Target() {
                    @Override
                    public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                        puzzle.initGame(bitmap);
                        loadingDiag.dismiss();
                    }

                    @Override
                    public void onBitmapFailed(Exception e, Drawable errorDrawable) {
                        Toast.makeText(Jigsawpuzzle.this, "Image failed to load!", Toast.LENGTH_LONG).show();
                        finish();
                    }

                    @Override
                    public void onPrepareLoad(Drawable placeHolderDrawable) {
                    }
                });
            }

            @Override
            public void onBtnChange(int type) {
                puzzle.setBtnType(type);
                changeBtn(type);
            }

            @Override
            public void onScore(int score) {
                updateScore(score);
                showCongratsDiag(String.valueOf(score));
            }

            @Override
            public void isLocked(boolean locked) {
                isLocked = locked;
            }

            @Override
            public void onDiag(int type) {
                if (type == 0) {
                    showLowBalDiag();
                } else if (type == 1) {
                    showLoadingDiag();
                } else if (type == 2) {
                    loadingDiag.dismiss();
                }
            }

            @Override
            public void onReceiveTime(int time) {
                t = time;
            }

            @Override
            public void onConnectionFail(int type) {
                conDiag = Misc.noConnection(conDiag, Jigsawpuzzle.this, () -> {
                    puzzle.methodCall(type);
                    conDiag.dismiss();
                });
            }
        });
    }

    private void updateScore(int toAdd) {
        sc += toAdd;
        scoreView.setText(String.valueOf(sc));
        if (sc != 0) setResult(sc);
    }

    private void changeBtn(int type) {
        if (type == 0) {
            btnText.setText(getString(R.string.please_wait));
            btnProgress.setVisibility(View.GONE);
            btnIcon.setVisibility(View.GONE);
            btnAmt.setVisibility(View.GONE);
        } else if (type == 1) {
            btnText.setText(getString(R.string.jpz_btn_pre));
            btnIcon.setVisibility(View.VISIBLE);
            btnAmt.setText(String.valueOf(pieceCost));
            btnAmt.setVisibility(View.VISIBLE);
            setTimer();
            btnProgress.setVisibility(View.VISIBLE);
        } else if (type == 2) {
            isLocked = false;
            if (countDown != null) countDown.cancel();
            btnProgress.setVisibility(View.GONE);
            btnText.setText(getString(R.string.try_another_round_for));
            btnIcon.setVisibility(View.VISIBLE);
            btnAmt.setText(String.valueOf(roundCost));
            btnAmt.setVisibility(View.VISIBLE);
        }
    }

    public void setTimer() {
        btnProgress.setMax(t);
        btnProgress.setProgress(t);
        countDown = new CountDownTimer(t, 1000) {
            @Override
            public void onTick(long l) {
                btnProgress.setProgress((int) l);
            }

            @Override
            public void onFinish() {
                if (isLocked) {
                    countDown = null;
                    puzzle.setBtnType(2);
                    changeBtn(2);
                    Toast.makeText(Jigsawpuzzle.this, "Times up!", Toast.LENGTH_LONG).show();
                }
            }
        };
        countDown.start();
    }

    private void showLoadingDiag() {
        if (loadingDiag == null) loadingDiag = Misc.loadingDiag(this);
        loadingDiag.show();
    }

    private void showQuitDiag() {
        if (congratsDiag != null && congratsDiag.isShowing()) congratsDiag.dismiss();
        if (loadingDiag != null && loadingDiag.isShowing()) congratsDiag.dismiss();
        if (quitDiag == null) {
            quitDiag = Misc.decoratedDiag(this, R.layout.dialog_quit, 0.8f);
            quitDiag.findViewById(R.id.dialog_quit_no).setOnClickListener(view -> quitDiag.dismiss());
            quitDiag.findViewById(R.id.dialog_quit_yes).setOnClickListener(view -> {
                quitDiag.dismiss();
                finish();
            });
        }
        quitDiag.show();
    }

    private void showCongratsDiag(String wAmt) {
        if (quitDiag != null && quitDiag.isShowing()) quitDiag.dismiss();
        if (loadingDiag != null && loadingDiag.isShowing()) congratsDiag.dismiss();
        if (congratsDiag == null) {
            congratsDiag = Misc.decoratedDiag(this, R.layout.dialog_quiz_post, 0.8f);
            diagAmtView = congratsDiag.findViewById(R.id.dialog_quiz_post_title);
            TextView desc = congratsDiag.findViewById(R.id.dialog_quiz_post_desc);
            desc.setText(getString(R.string.ip_congrats));
            Button qB = congratsDiag.findViewById(R.id.dialog_quiz_post_quit);
            qB.setText(getString(R.string.back));
            qB.setOnClickListener(view -> congratsDiag.dismiss());
            Button nB = congratsDiag.findViewById(R.id.dialog_quiz_post_next);
            nB.setText(getString(R.string.ip_next));
            nB.setOnClickListener(view -> {
                puzzle.methodCall(1);
                congratsDiag.dismiss();
            });
        }
        diagAmtView.setText(("Received " + wAmt + " " + Home.currency.toLowerCase() + "s"));
        congratsDiag.show();
    }


    public void showLowBalDiag() {
        if (lowBalDiag == null) lowBalDiag = Misc.lowbalanceDiag(this, new Misc.yesNo() {
            @Override
            public void yes() {
                lowBalDiag.dismiss();
                startActivity(new Intent(Jigsawpuzzle.this, Offers.class));
                finish();
            }

            @Override
            public void no() {
                lowBalDiag.dismiss();
                finish();
            }
        });
        lowBalDiag.show();
    }
}