package com.lexidome.lexagame.games;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.BounceInterpolator;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.lexidome.lexagame.helper.Misc;
import com.lexidome.lexagame.offers.GlobalAds;

import org.mintsoft.mintlib.GetGame;
import org.mintsoft.mintlib.onResponse;
import com.lexidome.lexagame.Home;
import com.lexidome.lexagame.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Random;

public class Lotto extends AppCompatActivity {
    private int selection, cost, chances, pts;
    private TextView ptsView, chanceView, costView, winnerView, timerView;
    private final int[] icons = new int[]{R.drawable.lotto_1, R.drawable.lotto_2,
            R.drawable.lotto_3, R.drawable.lotto_4, R.drawable.lotto_5};
    private TextView[] textViews;
    private Dialog dialog, progressDialog;
    private CountDownTimer countDown;
    private ScaleAnimation smallBounce;
    private ActivityResultLauncher<Intent> activityForResult;

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.game_lotto);
        progressDialog = Misc.loadingDiag(this);
        progressDialog.show();
        ptsView = findViewById(R.id.game_lotto_ptsView);
        chanceView = findViewById(R.id.game_lotto_chance);
        costView = findViewById(R.id.game_lotto_cost);
        winnerView = findViewById(R.id.game_lotto_winner);
        timerView = findViewById(R.id.game_lotto_next_draw);
        textViews = new TextView[]{
                findViewById(R.id.lotto_num1),
                findViewById(R.id.lotto_num2),
                findViewById(R.id.lotto_num3),
                findViewById(R.id.lotto_num4),
                findViewById(R.id.lotto_num5)
        };
        smallBounce = new ScaleAnimation(0.85f, 1f, 0.85f, 1f,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        smallBounce.setInterpolator(new BounceInterpolator());
        smallBounce.setDuration(500);
        smallBounce.setFillAfter(true);
        LinearLayout gridView = findViewById(R.id.game_lotto_gridView);
        new Handler().postDelayed(() -> {
            final LayoutInflater inflater = getLayoutInflater();
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            LinearLayout linearLayout;
            for (int i = 0; i < 6; i++) {
                linearLayout = new LinearLayout(Lotto.this);
                linearLayout.setOrientation(LinearLayout.HORIZONTAL);
                linearLayout.setLayoutParams(params);
                for (int j = 0; j < 6; j++) {
                    View cellView = inflater.inflate(R.layout.game_lotto_item, linearLayout, false);
                    ImageView img = cellView.findViewById(R.id.game_lotto_item_image);
                    img.setBackgroundResource(icons[new Random().nextInt(5)]);
                    TextView num = cellView.findViewById(R.id.game_lotto_item_text);
                    final String txt = String.valueOf(11 + (i * 6) + j);
                    num.setText(txt);
                    num.setOnClickListener(v -> {
                        if (selection < textViews.length) {
                            textViews[selection].setText(txt);
                            selection += 1;
                            v.startAnimation(smallBounce);
                            img.startAnimation(smallBounce);
                        }
                    });
                    linearLayout.addView(cellView);
                }
                gridView.addView(linearLayout);
            }
            netCall();
        }, 1000);
        activityForResult = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == 8) {
                        if (result.getData() != null) {
                            String pt = result.getData().getStringExtra("balance");
                            pts = Integer.parseInt(pt);
                            ptsView.setText(pt);
                        }
                    }
                });
        findViewById(R.id.game_lotto_back).setOnClickListener(view -> onBackPressed());
        findViewById(R.id.game_lotto_reset).setOnClickListener(view -> {
            selection = 0;
            for (TextView textView : textViews) {
                textView.setText("");
            }
        });
        findViewById(R.id.game_lotto_confirm).setOnClickListener(view -> {
            StringBuilder numbers = new StringBuilder();
            for (TextView textView : textViews) {
                numbers.append(textView.getText());
            }
            if (chances < 1) {
                Toast.makeText(this, getString(R.string.no_chances), Toast.LENGTH_LONG).show();
            } else if (numbers.length() == 10) {
                postData(numbers.toString());
                Home.showInterstitial = true;
            } else {
                Toast.makeText(this, getString(R.string.lotto_enter_all), Toast.LENGTH_LONG).show();
            }
        });
        findViewById(R.id.game_lotto_history).setOnClickListener(view -> {
            Intent intent = new Intent(Lotto.this, LottoHistory.class);
            activityForResult.launch(intent);
        });
        GlobalAds.fab(this, "fab_lg");
    }

    private void netCall() {
        if (!progressDialog.isShowing()) progressDialog.show();
        GetGame.getLotto(this, new onResponse() {
            @Override
            public void onSuccessHashMap(HashMap<String, String> data) {
                progressDialog.dismiss();
                ptsView.setText(data.get("balance"));
                costView.setText(data.get("cost"));
                chanceView.setText(data.get("chances"));
                pts = Integer.parseInt(data.get("balance"));
                cost = Integer.parseInt(data.get("cost"));
                chances = Integer.parseInt(data.get("chances"));
                StringBuilder winner = new StringBuilder(data.get("winner"));
                for (int i = 2; i < winner.length(); i += 4) winner.insert(i, "  ");
                winnerView.setText(winner.toString());
                timeRemain(Long.parseLong(data.get("s_time")));
                String date = new SimpleDateFormat("dd", Locale.getDefault())
                        .format(new Date(Home.spf.getLong("stime", 0L)));
                String date2 = Home.spf.getString("l_time", null);
                if (!date.equals(date2)) {
                    startActivity(new Intent(Lotto.this, LottoHistory.class));
                }
            }

            @Override
            public void onError(int errorCode, String error) {
                progressDialog.dismiss();
                if (errorCode == -9) {
                    dialog = Misc.noConnection(dialog, Lotto.this, () -> {
                        netCall();
                        dialog.dismiss();
                    });
                } else {
                    Toast.makeText(Lotto.this, error, Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void postData(String numbers) {
        if (!progressDialog.isShowing()) progressDialog.show();
        GetGame.postLotto(this, numbers, new onResponse() {
            @Override
            public void onSuccess(String response) {
                progressDialog.dismiss();
                chances -= 1;
                pts -= cost;
                chanceView.setText(String.valueOf(chances));
                ptsView.setText(String.valueOf(pts));
                selection = 0;
                for (TextView textView : textViews) {
                    textView.setText("");
                }
                Misc.showMessage(Lotto.this, getString(R.string.lotto_added), false);
            }

            @Override
            public void onError(int errorCode, String error) {
                progressDialog.dismiss();
                if (errorCode == -9) {
                    dialog = Misc.noConnection(dialog, Lotto.this, () -> {
                        postData(numbers);
                        dialog.dismiss();
                    });
                } else {
                    Toast.makeText(Lotto.this, error, Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void timeRemain(long time) {
        long timeInMillis = time - System.currentTimeMillis();
        if (timeInMillis < 0) return;
        countDown = new CountDownTimer(timeInMillis, 1000) {
            public void onTick(long millisUntilFinished) {
                long t = millisUntilFinished / 1000;
                long s = t % 60;
                long m = (t / 60) % 60;
                long h = (t / (60 * 60)) % 24;
                timerView.setText(String.format(Locale.getDefault(), "%d:%02d:%02d", h, m, s));
            }

            public void onFinish() {
                finish();
                startActivity(getIntent());
            }
        };
        countDown.start();
    }

    @Override
    protected void onDestroy() {
        if (countDown != null) countDown.cancel();
        if (progressDialog.isShowing()) progressDialog.dismiss();
        super.onDestroy();
    }
}