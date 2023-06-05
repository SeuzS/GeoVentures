package com.example.gpsgame;


import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.mapbox.maps.plugin.annotation.generated.PointAnnotation;

public class ToBattleDialog extends DialogFragment {
    
    PointAnnotation mob;
    
    ToBattleDialog(PointAnnotation pt){
        mob = pt;
    }
    
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        String name = "Name: Unknown";
        String msg = "";
        int dr = 0, mobHealth = 0, mobDamage = 0, mobxp = 0;
        switch (mob.getTextField()) {
            case "red_slime":
                name = "Имя: Красный слизень";
                mobHealth = 20;
                mobDamage = 10;
                mobxp = 5;
                dr = R.drawable.red_slimeb;
                break;
            case "blue_slime":
                name = "Имя: Синий слизень";
                mobHealth = 20;
                mobDamage = 15;
                mobxp = 10;
                dr = R.drawable.blue_slimeb;
                break;
            case "green_slime":
                name = "Имя: Зелёный слизень";
                mobHealth = 35;
                mobDamage = 15;
                mobxp = 20;
                dr = R.drawable.green_slimeb;
                break;
            case "wolf1":
                name = "Имя: Волк";
                mobHealth = 60;
                mobDamage = 25;
                mobxp = 45;
                dr = R.drawable.wolfb;
                break;
            case "wolf2":
                name = "Имя: Волк";
                mobHealth = 60;
                mobDamage = 25;
                mobxp = 45;
                dr = R.drawable.wolfab;
                break;
        }
        msg += name + "\nЗдоровье: " + mobHealth + "\nУрон: " + mobDamage + "\nНаграда: " + mobxp + " опыта";
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Битва")
                .setMessage(msg)
                .setIcon(getResources().getDrawable(dr))
                .setPositiveButton("Атаковать", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        ((MainActivity) getActivity()).startBattle(mob);
                    }
                })
                .setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                })
                .setCancelable(true);
        return builder.create();
    }
}
