package com.example.gpsgame;

import android.app.Dialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

public class PlayerInfoDialog  extends DialogFragment {
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        int lvl = sharedPreferences.getInt("Level", 0);
        int xp = sharedPreferences.getInt("Xp", 0);
        builder.setTitle("О персонаже")
                .setMessage("HP: " + (25 + 10 * lvl) + "\n" +
                        "Урон: " + (10 + 3 * lvl) + "\n" +
                        "Уровень: " + lvl + "(" + xp + "/" + (40 + 20 * lvl) + ")");
        return builder.create();
    }
}
