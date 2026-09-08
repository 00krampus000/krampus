package ru.attendance.group;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import java.io.OutputStream;
import java.time.LocalDate;
import java.util.Locale;

public class ScheduleMainActivity extends MainActivity {
    private static final int CREATE_EXPORT = 4217;
    private String pendingExport;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        seedSchedule();
        buildShell();
    }

    @Override void weekStrip(LinearLayout b) {
        LinearLayout row = new LinearLayout(this);
        row.setPadding(dp(14), dp(2), dp(14), dp(12));
        for (int i = -3; i <= 3; i++) {
            LocalDate d = date.plusDays(i);
            TextView x = tv(d.getDayOfWeek().getDisplayName(java.time.format.TextStyle.SHORT, new Locale("ru")).substring(0,2)
                    + "\n" + d.getDayOfMonth(), 12, d.equals(date) ? WHITE : INK);
            x.setGravity(Gravity.CENTER);
            x.setTypeface(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD);
            x.setBackground(shape(d.equals(date) ? ACCENT : WHITE, 15));
            final LocalDate q = d;
            x.setOnClickListener(v -> { date = q; buildShell(); });
            row.addView(x, new LinearLayout.LayoutParams(0, dp(52), 1));
        }
        b.addView(row);
    }

    @Override void export() {
        try {
            StringBuilder s = new StringBuilder("Дата\tПара\tВремя\tПредмет");
            for (String n : students) s.append("\t").append(n);
            s.append("\n");
            LocalDate start = date.withDayOfMonth(1), end = date.withDayOfMonth(date.lengthOfMonth());
            for (LocalDate d = start; !d.isAfter(end); d = d.plusDays(1)) {
                for (int l = 1; l <= 4; l++) {
                    boolean any = false;
                    for (String n : students) if (!mark(d, l, n).equals("—")) { any = true; break; }
                    if (!any) continue;
                    s.append(d).append("\t").append(l).append("\t")
                            .append(timeRange(d.getDayOfWeek().getValue(), l)).append("\t")
                            .append(subject(d.getDayOfWeek().getValue(), l));
                    for (String n : students) s.append("\t").append(mark(d, l, n));
                    s.append("\n");
                }
            }
            pendingExport = s.toString();
            Intent i = new Intent(Intent.ACTION_CREATE_DOCUMENT);
            i.addCategory(Intent.CATEGORY_OPENABLE);
            i.setType("application/vnd.ms-excel");
            i.putExtra(Intent.EXTRA_TITLE, "Посещаемость_ЦТБИД-266.xls");
            startActivityForResult(i, CREATE_EXPORT);
        } catch (Exception e) {
            Toast.makeText(this, "Ошибка экспорта", Toast.LENGTH_LONG).show();
        }
    }

    @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode != CREATE_EXPORT || resultCode != RESULT_OK || data == null || data.getData() == null) return;
        try {
            Uri uri = data.getData();
            try (OutputStream out = getContentResolver().openOutputStream(uri)) {
                if (out == null) throw new java.io.IOException("Не удалось открыть файл");
                out.write(pendingExport.getBytes("UTF-16LE"));
                out.flush();
            }
            pendingExport = null;
            Toast.makeText(this, "Excel-файл сохранён", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(this, "Ошибка сохранения Excel", Toast.LENGTH_LONG).show();
        }
    }

    private void put(android.content.SharedPreferences.Editor e, int day, int lesson, String subject, String from, String to) {
        String key = day + "-" + lesson;
        e.putString("sub" + key, subject).putString("from" + key, from).putString("to" + key, to);
    }

    private void seedSchedule() {
        if (prefs.getBoolean("schedule_defaults_v2", false)) return;
        android.content.SharedPreferences.Editor e = prefs.edit();

        put(e,1,1,"","08:00","09:30");
        put(e,1,2,"Введение в профессиональную деятельность","09:35","11:05");
        put(e,1,3,"Введение в профессиональную деятельность","11:25","12:55");
        put(e,1,4,"Библиотековедение: библиотековедение: общий курс","13:05","14:35");
        put(e,1,5,"Библиотековедение: библиотековедение: общий курс","14:55","16:25");

        put(e,2,1,"Русский язык и культура речи","08:00","09:30");
        put(e,2,2,"Основы научных исследований","09:35","11:05");
        put(e,2,3,"Информатика","11:25","12:55");
        put(e,2,4,"","13:05","14:35");
        put(e,2,5,"","14:55","16:25");
        put(e,2,6,"","16:35","18:05");
        put(e,2,7,"","18:10","19:40");

        put(e,3,1,"Основы российской государственности","08:10","09:40");
        put(e,3,2,"Основы российской государственности","09:45","11:15");
        put(e,3,3,"Иностранный язык (английский)","11:25","12:55");
        put(e,3,4,"Иностранный язык (английский)","13:05","14:35");
        put(e,3,5,"Основы российской государственности","14:55","16:25");
        put(e,3,6,"Элективные дисциплины по физической культуре и спорту (СМГ)","16:35","18:05");
        put(e,3,7,"Физическая культура и спорт (СМГ)","18:10","19:40");

        put(e,4,1,"","08:00","09:30");
        put(e,4,2,"Элективные дисциплины по физической культуре и спорту (СМГ)","09:35","11:05");
        put(e,4,3,"Элективные дисциплины по физической культуре и спорту (СМГ)","11:25","12:55");
        put(e,4,4,"Физическая культура и спорт","13:05","14:35");
        put(e,4,5,"Основы научных исследований","14:55","16:25");
        put(e,4,6,"Основы научных исследований","16:35","18:05");
        put(e,4,7,"Элективные дисциплины по физической культуре и спорту (СМГ)","19:45","21:15");

        put(e,5,1,"","08:00","09:30");
        put(e,5,2,"История России","09:35","11:05");
        put(e,5,3,"История России","11:25","12:55");
        put(e,5,4,"Безопасность жизнедеятельности","13:05","14:35");
        put(e,5,5,"Литература","14:55","16:25");

        put(e,6,1,"Иностранный язык (немецкий)","08:00","09:30");
        put(e,6,2,"","09:35","11:05");
        put(e,6,3,"","11:25","12:55");
        put(e,6,4,"","13:05","14:35");

        e.putBoolean("schedule_defaults_v2", true).apply();
    }
}
