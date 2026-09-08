package ru.attendance.group;

import android.app.AlertDialog;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

public class FinalMainActivity extends ScheduleMainActivity {
    @Override void actions(LinearLayout b) {
        LinearLayout r = new LinearLayout(this);
        r.setPadding(dp(20), dp(5), dp(20), dp(20));

        TextView all = action("Отметить всех", INK, WHITE, 14);
        all.setOnClickListener(v -> showMarkAllDialog());

        TextView ex = action("Экспорт", themeAccentForButton(), WHITE, 14);
        ex.setOnClickListener(v -> export());

        r.addView(all, new LinearLayout.LayoutParams(0, dp(46), 1));
        ((LinearLayout.LayoutParams) all.getLayoutParams()).setMargins(0, 0, dp(7), 0);
        r.addView(ex, new LinearLayout.LayoutParams(0, dp(46), 1));
        b.addView(r);
    }

    private int themeAccentForButton() {
        try {
            String s = prefs.getString("theme_accent", "");
            return s.isEmpty() ? ACCENT : Color.parseColor(s);
        } catch (Exception e) { return ACCENT; }
    }

    private void showMarkAllDialog() {
        String[] labels = {
                "✓  Присутствует",
                "×  Отсутствует",
                "О  Опоздал",
                "У  Уважительная причина",
                "—  Сбросить отметку"
        };
        String[] values = {"P", "A", "L", "E", ""};

        new AlertDialog.Builder(this)
                .setTitle("Отметить всех студентов")
                .setItems(labels, (dialog, which) -> {
                    String value = values[which];
                    String badge = which == 0 ? "✓" : which == 1 ? "×" : which == 2 ? "О" : which == 3 ? "У" : "—";
                    for (String n : students) {
                        setMark(date, lesson, n, value);
                        updateBadge(n, badge);
                    }
                })
                .setNegativeButton("Отмена", null)
                .show();
    }
}
