package com.example.masterai.utils;

import android.os.Build;
import android.text.format.DateUtils;

import java.time.Instant;

public class PostUtils {
    public static String getTimeAgo(String createdAt) {
        try {
            long timeMillis = 0;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                timeMillis = Instant.parse(createdAt).toEpochMilli();
            }

            CharSequence result = DateUtils.getRelativeTimeSpanString(
                    timeMillis,
                    System.currentTimeMillis(),
                    DateUtils.MINUTE_IN_MILLIS
            );

            return result.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }
}
