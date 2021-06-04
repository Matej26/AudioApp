package com.example.textandspeech;

import android.app.Activity;
import android.os.Build;
import android.util.Log;
import androidx.annotation.RequiresApi;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Objects;

class Countries {
    HashSet<String> set;
    HashSet<String> alreadyUsed = new HashSet<>();

    @RequiresApi(api = Build.VERSION_CODES.O)
    Countries(Activity main) {
        try {
            JSONObject obj = new JSONObject(Objects.requireNonNull(loadJSONFromAsset(main)));
            set = new HashSet<>();
            for (int i = 0; i < obj.length(); ++i) {
                try {
                    set.add(obj.getString(String.valueOf(i)).toLowerCase());
                } catch (JSONException e) {
                    Log.d("i", e.getMessage());
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private String loadJSONFromAsset(Activity main) {
        String json;
        try {
            InputStream is = main.getAssets().open("countries.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, StandardCharsets.UTF_8);
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return json;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    Object[] getCountriesStartWith(String prefix) {
        return this.set.stream().filter(data -> data.startsWith(prefix)).toArray();
    }
}
