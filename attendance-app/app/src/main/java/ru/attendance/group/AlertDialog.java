package ru.attendance.group;

/** Small compatibility wrapper used by ScheduleMainActivity. */
final class AlertDialog {
    static final int BUTTON_POSITIVE = android.app.AlertDialog.BUTTON_POSITIVE;
    private final android.app.AlertDialog delegate;
    private AlertDialog(android.app.AlertDialog delegate) { this.delegate = delegate; }
    void setOnShowListener(android.content.DialogInterface.OnShowListener l) { delegate.setOnShowListener(l); }
    android.widget.Button getButton(int which) { return delegate.getButton(which); }
    void show() { delegate.show(); }
    void dismiss() { delegate.dismiss(); }

    static final class Builder {
        private final android.app.AlertDialog.Builder delegate;
        Builder(android.content.Context context) { delegate = new android.app.AlertDialog.Builder(context); }
        Builder setTitle(CharSequence title) { delegate.setTitle(title); return this; }
        Builder setMessage(CharSequence message) { delegate.setMessage(message); return this; }
        Builder setView(android.view.View view) { delegate.setView(view); return this; }
        Builder setItems(CharSequence[] items, android.content.DialogInterface.OnClickListener l) { delegate.setItems(items,l); return this; }
        Builder setNegativeButton(CharSequence text, android.content.DialogInterface.OnClickListener l) { delegate.setNegativeButton(text,l); return this; }
        Builder setPositiveButton(CharSequence text, android.content.DialogInterface.OnClickListener l) { delegate.setPositiveButton(text,l); return this; }
        AlertDialog create() { return new AlertDialog(delegate.create()); }
        AlertDialog show() { return new AlertDialog(delegate.show()); }
    }
}
