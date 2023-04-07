package com.vocalix.app.database.utils;

import androidx.room.TypeConverter;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Converters {

    @TypeConverter
    public static Date toDate(Long timestamp) {
        return timestamp == null ? null : new Date(timestamp);
    }

    @TypeConverter
    public static Long toTimestamp(Date date) {
        return date == null ? null : date.getTime();
    }

    @TypeConverter
    public static String fromStringList(List<String> strings) {
        if (strings == null) {
            return null;
        }
        JSONArray jsonArray = new JSONArray(strings);
        return jsonArray.toString();
    }

    @TypeConverter
    public static List<String> toStringList(String jsonString) {
        if (jsonString == null) {
            return null;
        }
        List<String> strings = new ArrayList<>();
        try {
            JSONArray jsonArray = new JSONArray(jsonString);
            for (int i = 0; i < jsonArray.length(); i++) {
                strings.add(jsonArray.getString(i));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return strings;
    }
}
