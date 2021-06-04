package com.example.textandspeech;

import android.os.Build;
import androidx.annotation.RequiresApi;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

class Answerer {
    private HashMap<String, Integer> estimations = new HashMap<>();

    @RequiresApi(api = Build.VERSION_CODES.O)
    String getAnswer(Object[] candidates, Countries countries) {
        for (Object candidate : candidates) {
            String str = (String) candidate;
            char last = str.charAt(str.length() - 1);
            if (last == 'ы' || last == 'ь') {
                last = str.charAt(str.length() - 2);
            }
            int estimation = countries.getCountriesStartWith(String.valueOf(last)).length;
            estimations.put(str, estimation);
        }
        String key = estimations.entrySet().stream().max(Comparator.comparingInt(Map.Entry::getValue)).get().getKey();
        countries.set.remove(key);
        countries.alreadyUsed.add(key);
        return key;
    }
}
