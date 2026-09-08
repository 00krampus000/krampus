package ru.attendance.group;

public final class Toast {
    private Toast() {}
    public static android.widget.Toast makeText(android.content.Context context, CharSequence text, int duration) {
        return android.widget.Toast.makeText(context, text, duration);
    }
    public static android.widget.Toast makeText(android.content.Context context, int resId, int duration) {
        return android.widget.Toast.makeText(context, resId, duration);
    }
}
