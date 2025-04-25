package com.example.schoolproject.Utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.format.DateUtils;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.FirebaseDatabase;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.List;

public class Utils extends AppCompatActivity{
    private static final char[] SUFFIXES = {'k', 'm', 'g', 't', 'p', 'e'};

    public static Utils utils = new Utils();
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    SharedPreferences read_prefs;
    Context context;

    public Utils() {

    }
    public List<String> grades = Arrays.asList("Freshman Year", "Sophomore Year", "Year 3", "Year 4", "Year 5", "Year 6", "Year 7", "Year 8");
    public List<String> semesters = Arrays.asList("Sem 1", "Sem 2", "Sem 3");
    public List<String> examTypes = Arrays.asList("CAT 1", "CAT 2", "Finals", "Take Away CAT", "Assingment");

    public boolean isEmailValid(String email) {

        String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";

        if (email.matches(emailPattern) && email.length() > 0) {
            return true;
        }
        return false;
    }

    public String calculateTimeAgo(String datePost) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd-M-yyyy HH:mm:ss");
        try {
            long time = sdf.parse(datePost).getTime();
            long now = System.currentTimeMillis();
            CharSequence ago = DateUtils.getRelativeTimeSpanString(time, now, DateUtils.MINUTE_IN_MILLIS);

            return ago + "";
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return "";
    }


    public static String formatValue(long number) {
        if (number < 1000) {
            // No need to format this
            return String.valueOf(number);
        }
        // Convert to a string
        final String string = String.valueOf(number);
        // The suffix we're using, 1-based
        final int magnitude = (string.length() - 1) / 3;
        // The number of digits we must show before the prefix
        final int digits = (string.length() - 1) % 3 + 1;

        // Build the string
        char[] value = new char[4];
        for (int i = 0; i < digits; i++) {
            value[i] = string.charAt(i);
        }
        int valueLength = digits;
        // Can and should we add a decimal point and an additional number?
        if (digits == 1 && string.charAt(1) != '0') {
            value[valueLength++] = '.';
            value[valueLength++] = string.charAt(1);
        }
        value[valueLength++] = SUFFIXES[magnitude - 1];
        return new String(value, 0, valueLength);
    }
}
