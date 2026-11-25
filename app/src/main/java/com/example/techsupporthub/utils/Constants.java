package com.example.techsupporthub.utils;

import android.os.Build;

import com.example.techsupporthub.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class Constants {
    public static final String USERS_DB = "users";
    public static final String WORKSHOP_DB = "workshops";

    // Static device categories array
    public static final String[] DEVICE_CATEGORIES = {
            "Laptops",
            "Smartphones",
            "Printers",
            "CCTV Cameras",
            "Networking Devices",
            "Home Appliances",
            "Gaming Consoles",
            "TVs & Monitors",
            "Wearables"
    };

    // Static array of drawable resource IDs corresponding to each device category
    public static final int[] DEVICE_CATEGORY_ICONS = {
            R.drawable.ic_laptop,           // Laptops
            R.drawable.ic_smartphone,       // Smartphones
            R.drawable.ic_printer,          // Printers
            R.drawable.ic_cctv,           // CCTV Cameras
            R.drawable.ic_network,          // Networking Devices
            R.drawable.ic_home,   // Home Appliances
            R.drawable.ic_gaming_console,          // Gaming Consoles
            R.drawable.ic_smart_tv,               // TVs & Monitors
            R.drawable.ic_wearables          // Wearables
    };
    public static final String CHATS_DB = "chats";
    public static final String PAYMENTS_DB = "payments";


    public static String getDate() {
        String date = null;

        DateTimeFormatter dtf = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
        }
        LocalDateTime now = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            now = LocalDateTime.now();
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            date = dtf.format(now);
        }
        return date;
    }

    public static String convertDateFormat(String inputDate) {
        SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.getDefault());
        SimpleDateFormat outputFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());

        try {
            Date date = inputFormat.parse(inputDate);
            return outputFormat.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String getTimeAgo(String dateTime) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        try {
            Date pastDate = dateFormat.parse(dateTime);
            Date now = new Date();

            long duration = now.getTime() - pastDate.getTime();
            long seconds = TimeUnit.MILLISECONDS.toSeconds(duration);
            long minutes = TimeUnit.MILLISECONDS.toMinutes(duration);
            long hours = TimeUnit.MILLISECONDS.toHours(duration);
            long days = TimeUnit.MILLISECONDS.toDays(duration);

            if (days > 0) {
                return days + (days == 1 ? " day ago" : " days ago");
            } else if (hours > 0) {
                return hours + (hours == 1 ? " hour ago" : " hours ago");
            } else if (minutes > 0) {
                return minutes + (minutes == 1 ? " min ago" : " mins ago");
            } else {
                return seconds + (seconds == 1 ? " sec ago" : " secs ago");
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return "";
    }
}
