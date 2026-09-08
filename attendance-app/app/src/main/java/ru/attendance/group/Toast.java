package ru.attendance.group;

public final class Toast {
    public static final int LENGTH_SHORT = android.widget.Toast.LENGTH_SHORT;
    public static final int LENGTH_LONG = android.widget.Toast.LENGTH_LONG;
    private Toast() {}
    public static android.widget.Toast makeText(android.content.Context context, CharSequence text, int duration) {
        return android.widget.Toast.makeText(context, text, duration);
    }
    public static android.widget.Toast makeText(android.content.Context context, int resId, int duration) {
        return android.widget.Toast.makeText(context, resId, duration);
    }
}
