package ru.attendance.group;

import android.os.Bundle;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.time.LocalDate;
import java.util.Locale;

public class ScheduleMainActivity extends MainActivity {
    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        seedSchedule();
        buildShell();
    }
    private int color(String key,int fallback){String v=prefs.getString(key,null);if(v==null||v.trim().isEmpty())return fallback;try{return Color.parseColor(v.trim());}catch(Exception e){return fallback;}}
    private int themeAccent(){return color("theme_accent",ACCENT);}
    private int themeBg(){return color("theme_bg",BG);}
    private int themeSurface(){return color("theme_surface",WHITE);}
    private int themeText(){return color("theme_text",INK);}
    private int themeSecondary(){return color("theme_secondary",MUTED);}
    private int themeSoft(){String saved=prefs.getString("theme_accent_soft",null);if(saved!=null)try{return Color.parseColor(saved);}catch(Exception ignored){}int a=themeAccent();return Color.rgb((Color.red(a)+2040)/9,(Color.green(a)+2040)/9,(Color.blue(a)+2040)/9);}
    @Override TextView tv(String s,float z,int c){int m=c==ACCENT?themeAccent():c==ACCENT_SOFT?themeSoft():c==INK?themeText():c==MUTED?themeSecondary():c==WHITE?themeSurface():c;return super.tv(s,z,m);}
    @Override GradientDrawable shape(int c,float r){int m=c==ACCENT?themeAccent():c==ACCENT_SOFT?themeSoft():c==INK?themeText():c==MUTED?themeSecondary():c==WHITE?themeSurface():c==BG?themeBg():c;return super.shape(m,r);}
    @Override void buildShell(){super.buildShell();root.setBackgroundColor(themeBg());if(root.getChildCount()>1)root.getChildAt(root.getChildCount()-1).setBackgroundColor(themeSurface());retheme(root);}
    private void retheme(View v){
        if(v instanceof TextView){TextView t=(TextView)v;int c=t.getCurrentTextColor();if(c==INK)t.setTextColor(themeText());else if(c==MUTED)t.setTextColor(themeSecondary());Drawable d=t.getBackground();if(d instanceof GradientDrawable){GradientDrawable g=(GradientDrawable)d;try{int bc=g.getColor()!=null?g.getColor().getDefaultColor():Integer.MIN_VALUE;if(bc==WHITE)g.setColor(themeSurface());else if(bc==BG)g.setColor(themeBg());else if(bc==ACCENT)g.setColor(themeAccent());else if(bc==ACCENT_SOFT)g.setColor(themeSoft());else if(bc==INK)g.setColor(themeText());}catch(Exception ignored){}}}
        if(v instanceof LinearLayout){Drawable d=v.getBackground();if(d instanceof ColorDrawable&&((ColorDrawable)d).getColor()==WHITE)v.setBackgroundColor(themeSurface());}
        if(v instanceof android.view.ViewGroup){android.view.ViewGroup g=(android.view.ViewGroup)v;for(int i=0;i<g.getChildCount();i++)retheme(g.getChildAt(i));}
    }
    @Override void more(LinearLayout b){
        super.more(b);
        TextView h=tv("Оформление",21,INK);h.setTypeface(android.graphics.Typeface.DEFAULT,android.graphics.Typeface.BOLD);h.setPadding(dp(20),dp(18),dp(20),dp(8));b.addView(h);
        TextView info=tv("Настрой цвета интерфейса. Вводи HEX, например #12A889",13,MUTED);info.setPadding(dp(20),0,dp(20),dp(10));b.addView(info);
        EditText accent=colorField(b,"Основной цвет / выделение","theme_accent",ACCENT);
        EditText bg=colorField(b,"Цвет фона","theme_bg",BG);
        EditText surface=colorField(b,"Цвет карточек и поверхностей","theme_surface",WHITE);
        EditText text=colorField(b,"Цвет основного текста","theme_text",INK);
        EditText secondary=colorField(b,"Цвет вторичного текста","theme_secondary",MUTED);
        TextView save=action("Применить цвета",themeAccent(),themeSurface(),14);
        save.setOnClickListener(v->{String[] keys={"theme_accent","theme_bg","theme_surface","theme_text","theme_secondary"};EditText[] es={accent,bg,surface,text,secondary};android.content.SharedPreferences.Editor e=prefs.edit();for(int i=0;i<keys.length;i++){String s=es[i].getText().toString().trim();try{Color.parseColor(s);e.putString(keys[i],s);}catch(Exception ex){Toast.makeText(this,"Проверь HEX: "+s,Toast.LENGTH_SHORT).show();return;}}e.remove("theme_accent_soft").apply();Toast.makeText(this,"Цвета сохранены",Toast.LENGTH_SHORT).show();buildShell();});
        b.addView(save,margins(20,4,20,20));
        TextView reset=action("Сбросить стандартные цвета",themeSurface(),themeText(),13);reset.setOnClickListener(v->{prefs.edit().remove("theme_accent").remove("theme_bg").remove("theme_surface").remove("theme_text").remove("theme_secondary").remove("theme_accent_soft").apply();buildShell();});b.addView(reset,margins(20,0,20,20));
    }
    private EditText colorField(LinearLayout b,String label,String key,int fallback){LinearLayout c=card();c.setPadding(dp(14),dp(10),dp(14),dp(10));TextView l=tv(label,13,themeText());c.addView(l);EditText e=new EditText(this);e.setSingleLine(true);e.setTextSize(14);e.setTextColor(themeText());e.setHintTextColor(themeSecondary());e.setText(prefs.getString(key,String.format(Locale.US,"#%06X",0xFFFFFF&fallback)));e.setBackground(shape(themeBg(),14));e.setPadding(dp(12),0,dp(12),0);c.addView(e,new LinearLayout.LayoutParams(-1,dp(46)));b.addView(c,margins(20,0,20,8));return e;}
    @Override void weekStrip(LinearLayout b){LinearLayout row=new LinearLayout(this);row.setPadding(dp(14),dp(2),dp(14),dp(12));for(int i=-3;i<=3;i++){LocalDate d=date.plusDays(i);TextView x=tv(d.getDayOfWeek().getDisplayName(java.time.format.TextStyle.SHORT,new Locale("ru")).substring(0,2)+"\n"+d.getDayOfMonth(),12,d.equals(date)?WHITE:INK);x.setGravity(Gravity.CENTER);x.setTypeface(android.graphics.Typeface.DEFAULT,android.graphics.Typeface.BOLD);x.setBackground(shape(d.equals(date)?ACCENT:WHITE,15));final LocalDate q=d;x.setOnClickListener(v->{date=q;buildShell();});row.addView(x,new LinearLayout.LayoutParams(0,dp(52),1));}b.addView(row);}
    private void put(android.content.SharedPreferences.Editor e,int day,int lesson,String subject,String from,String to){String key=day+"-"+lesson;e.putString("sub"+key,subject).putString("from"+key,from).putString("to"+key,to);}
    private void seedSchedule(){if(prefs.getBoolean("schedule_defaults_v2",false))return;android.content.SharedPreferences.Editor e=prefs.edit();
        put(e,1,1,"","08:00","09:30");put(e,1,2,"Введение в профессиональную деятельность","09:35","11:05");put(e,1,3,"Введение в профессиональную деятельность","11:25","12:55");put(e,1,4,"Библиотековедение: библиотековедение: общий курс","13:05","14:35");put(e,1,5,"Библиотековедение: библиотековедение: общий курс","14:55","16:25");
        put(e,2,1,"Русский язык и культура речи","08:00","09:30");put(e,2,2,"Основы научных исследований","09:35","11:05");put(e,2,3,"Информатика","11:25","12:55");put(e,2,4,"","13:05","14:35");put(e,2,5,"","14:55","16:25");put(e,2,6,"","16:35","18:05");put(e,2,7,"","18:10","19:40");
        put(e,3,1,"Основы российской государственности","08:10","09:40");put(e,3,2,"Основы российской государственности","09:45","11:15");put(e,3,3,"Иностранный язык (английский)","11:25","12:55");put(e,3,4,"Иностранный язык (английский)","13:05","14:35");put(e,3,5,"Основы российской государственности","14:55","16:25");put(e,3,6,"Элективные дисциплины по физической культуре и спорту (СМГ)","16:35","18:05");put(e,3,7,"Физическая культура и спорт (СМГ)","18:10","19:40");
        put(e,4,1,"","08:00","09:30");put(e,4,2,"Элективные дисциплины по физической культуре и спорту (СМГ)","09:35","11:05");put(e,4,3,"Элективные дисциплины по физической культуре и спорту (СМГ)","11:25","12:55");put(e,4,4,"Физическая культура и спорт","13:05","14:35");put(e,4,5,"Основы научных исследований","14:55","16:25");put(e,4,6,"Основы научных исследований","16:35","18:05");put(e,4,7,"Элективные дисциплины по физической культуре и спорту (СМГ)","19:45","21:15");
        put(e,5,1,"","08:00","09:30");put(e,5,2,"История России","09:35","11:05");put(e,5,3,"История России","11:25","12:55");put(e,5,4,"Безопасность жизнедеятельности","13:05","14:35");put(e,5,5,"Литература","14:55","16:25");
        put(e,6,1,"Иностранный язык (немецкий)","08:00","09:30");put(e,6,2,"","09:35","11:05");put(e,6,3,"","11:25","12:55");put(e,6,4,"","13:05","14:35");e.putBoolean("schedule_defaults_v2",true).apply();
    }
}
