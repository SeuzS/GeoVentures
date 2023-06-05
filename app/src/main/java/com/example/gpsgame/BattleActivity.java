package com.example.gpsgame;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentActivity;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentManager;

import java.io.FileInputStream;

public class BattleActivity extends AppCompatActivity {

    double playerDamage, playerHealth, mobDamage, mobHealth, maxMobHealth, maxPlayerHealth;
    int cnt, mobxp;
    EnemyIntent eIntent;
    ProgressBar pbe, pbp;
    TextView ei, hpe, hpp;
    EnemyIntent[] eIntents;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_battle);
        String mob_type = getIntent().getStringExtra("mob");
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        int lvl = sharedPreferences.getInt("Level", 0);
        int dr;
        playerDamage = 10 + 3 * lvl;
        playerHealth = 25 + 10 * lvl;
        maxPlayerHealth = 25 + 10 * lvl;
        ei = (TextView) findViewById(R.id.enemyIntent);
        hpe = (TextView) findViewById(R.id.hpEnemy);
        hpp = (TextView) findViewById(R.id.hpPlayer);
        pbe = (ProgressBar) findViewById(R.id.healthMob);
        pbp = (ProgressBar) findViewById(R.id.healthPlayer);
        ImageButton info = (ImageButton) findViewById(R.id.infoButton);
        Button atk = (Button) findViewById(R.id.attack);
        Button blk = (Button) findViewById(R.id.block);
        ImageView iv = (ImageView) findViewById(R.id.enemy);
        info.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AboutDialog ad = new AboutDialog(eIntent.description);
                ad.show(getSupportFragmentManager(), "About");
                Log.d("IB", "Click!");
            }
        });
        switch(mob_type){
            case "red_slime":
                mobHealth = 20;
                mobDamage = 10;
                mobxp = 5;
                dr = R.drawable.red_slimeb;
                eIntents = new EnemyIntent[]{new EnemyIntent("Плевок слизью", "Наносит " + Double.toString(mobDamage) + " урона", "Attack")};
                break;
            case "blue_slime":
                mobHealth = 20;
                mobDamage = 15;
                mobxp = 10;
                dr = R.drawable.blue_slimeb;
                eIntents = new EnemyIntent[]{new EnemyIntent("Плевок слизью", "Наносит " + Double.toString(mobDamage) + " урона", "Attack"), new EnemyIntent("Щит из слизи", "Блокирует 50% наносимого урона", "Block")};
                break;
            case "green_slime":
                mobHealth = 35;
                mobDamage = 15;
                mobxp = 20;
                dr = R.drawable.green_slimeb;
                eIntents = new EnemyIntent[]{new EnemyIntent("Атака липкой слизью", "Наносит " + Double.toString(mobDamage) + " урона, при попытке атаковать на этом ходу вы нанесёте только 50% урона", "Special1"), new EnemyIntent("Щит из слизи", "Блокирует 50% наносимого урона", "Block"), new EnemyIntent("Плевок слизью", "Наносит " + Double.toString(mobDamage) + " урона", "Attack")};
                break;
            case "wolf1":
                mobHealth = 60;
                mobDamage = 25;
                mobxp = 45;
                dr = R.drawable.wolfb;
                eIntents = new EnemyIntent[]{new EnemyIntent("Прыжок", "Получает 0% урона", "Avoid"), new EnemyIntent("Укус", "Наносит " + Double.toString(mobDamage) + " урона", "Attack"), new EnemyIntent("Укус", "Наносит " + Double.toString(mobDamage) + " урона", "Attack"), new EnemyIntent("Атака в прыжке", "Наносит " + Double.toString(mobDamage) + " урона, при попытке атаковать на этом ходу вы нанесёте только 50% урона", "Special1"), new EnemyIntent("Укус", "Наносит " + Double.toString(mobDamage) + " урона", "Attack"), new EnemyIntent("Передышка", "Пропускает свой ход", "")};
                break;
            case "wolf2":
                mobHealth = 60;
                mobDamage = 25;
                mobxp = 45;
                dr = R.drawable.wolfab;
                eIntents = new EnemyIntent[]{new EnemyIntent("Прыжок", "Получает 0% урона", "Avoid"), new EnemyIntent("Укус", "Наносит " + Double.toString(mobDamage) + " урона", "Attack"), new EnemyIntent("Укус", "Наносит " + Double.toString(mobDamage) + " урона", "Attack"), new EnemyIntent("Атака в прыжке", "Наносит " + Double.toString(mobDamage) + " урона, при попытке атаковать на этом ходу вы нанесёте только 50% урона", "Special1"), new EnemyIntent("Укус", "Наносит " + Double.toString(mobDamage) + " урона", "Attack"), new EnemyIntent("Передышка", "Пропускает свой ход", "")};
                break;
            default:
                dr = R.drawable.red_slimeb;
        }
        iv.setImageResource(dr);
        maxMobHealth = mobHealth;
        eIntent = eIntents[0];
        ei.setText(eIntent.name);
        pbe.setMax((int)mobHealth);
        pbp.setMax((int)playerHealth);
        updateHealthBar(0, 0);

        blk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                makeTurn("Block");
            }
        });

        atk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                makeTurn("Attack");
            }
        });
    }
    
    void makeTurn(String pIntent){
        switch (pIntent){
            case "Attack":
                if(eIntent.type != "Block" && eIntent.type != "Special1" && eIntent.type != "Avoid")
                    updateHealthBar(0, playerDamage * -1);
                else
                    if(eIntent.type == "Avoid"){
                        updateHealthBar(0, 0);
                    }
                    else{updateHealthBar(0, playerDamage / -2);}
                break;
        }
        if(eIntent.type == "Attack" || eIntent.type == "Special1"){
            if(pIntent != "Block"){
                updateHealthBar(mobDamage * - 1, 0);
            }
            else{
                updateHealthBar(mobDamage / -2, 0);
            }
        }
        if(playerHealth <= 0){
            Intent in = new Intent();
            in.putExtra("Victory", false);
            setResult(RESULT_OK, in);
            finish();
        }
        if(mobHealth <= 0){
            Intent in = new Intent();
            in.putExtra("Victory", true);
            in.putExtra("xp", mobxp);
            setResult(RESULT_OK, in);
            finish();
        }
        cnt++;
        if(cnt == eIntents.length)
            cnt = 0;
        eIntent = eIntents[cnt];
        ei.setText(eIntent.name);
    }

    void updateHealthBar(double playerChange, double mobChange){
        mobHealth += mobChange;
        playerHealth += playerChange;
        pbe.setProgress((int)mobHealth);
        pbp.setProgress((int)playerHealth);
        hpp.setText(Double.toString(playerHealth) + "/" + Double.toString(maxPlayerHealth));
        hpe.setText(Double.toString(mobHealth) + "/" + Double.toString(maxMobHealth));
    }

    @Override
    public void onBackPressed() { }

}
