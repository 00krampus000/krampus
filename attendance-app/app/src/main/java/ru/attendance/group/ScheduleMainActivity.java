package ru.attendance.group;

import android.os.Bundle;
import android.graphics.Color;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.MotionEvent;
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
        TextView info=tv("Выбирай цвет круговой палитрой или укажи HEX-код",13,MUTED);info.setPadding(dp(20),0,dp(20),dp(10));b.addView(info);
        EditText accent=colorField(b,"Основной цвет / выделение","theme_accent",ACCENT);
        EditText bg=colorField(b,"Цвет фона","theme_bg",BG);
        EditText surface=colorField(b,"Цвет карточек и поверхностей","theme_surface",WHITE);
        EditText text=colorField(b,"Цвет основного текста","theme_text",INK);
        EditText secondary=colorField(b,"Цвет вторичного текста","theme_secondary",MUTED);
        TextView save=action("Применить цвета",themeAccent(),themeSurface(),14);
        save.setOnClickListener(v->{String[] keys={"theme_accent","theme_bg","theme_surface","theme_text","theme_secondary"};EditText[] es={accent,bg,surface,text,secondary};android.content.SharedPreferences.Editor e=prefs.edit();for(int i=0;i<keys.length;i++){String s=es[i].getText().toString().trim();try{Color.parseColor(s);e.putString(keys[i],s);}catch(Exception ex){Toast.makeText(this,"Проверь HEX: "+s,Toast.LENGTH_SHORT).show();return;}}e.remove("theme_accent_soft").apply();Toast.makeText(this,"Цвета сохранены",Toast.LENGTH_SHORT).show();buildShell();});
        b.addView(save,margins(20,4,20,10));
        TextView reset=action("Сбросить стандартные цвета",themeSurface(),themeText(),13);reset.setOnClickListener(v->{prefs.edit().remove("theme_accent").remove("theme_bg").remove("theme_surface").remove("theme_text").remove("theme_secondary").remove("theme_accent_soft").apply();buildShell();});b.addView(reset,margins(20,0,20,20));
    }
    private EditText colorField(LinearLayout b,String label,String key,int fallback){
        LinearLayout c=card();c.setPadding(dp(14),dp(10),dp(14),dp(10));
        TextView l=tv(label,13,themeText());c.addView(l);
        LinearLayout line=new LinearLayout(this);line.setGravity(Gravity.CENTER_VERTICAL);
        EditText e=new EditText(this);e.setSingleLine(true);e.setTextSize(14);e.setTextColor(themeText());e.setHintTextColor(themeSecondary());e.setText(prefs.getString(key,String.format(Locale.US,"#%06X",0xFFFFFF&fallback)));e.setBackground(shape(themeBg(),14));e.setPadding(dp(12),0,dp(12),0);
        TextView pick=action("🎨",themeSurface(),themeText(),20);pick.setGravity(Gravity.CENTER);pick.setOnClickListener(v->showColorPicker(e));
        line.addView(e,new LinearLayout.LayoutParams(0,dp(46),1));LinearLayout.LayoutParams pp=new LinearLayout.LayoutParams(dp(54),dp(46));pp.setMargins(dp(7),0,0,0);line.addView(pick,pp);c.addView(line);
        b.addView(c,margins(20,0,20,8));return e;
    }
    private void showColorPicker(EditText target){
        int initial;
        try{initial=Color.parseColor(target.getText().toString().trim());}catch(Exception e){initial=themeAccent();}
        final ColorPickerView picker=new ColorPickerView(this,initial);
        LinearLayout box=new LinearLayout(this);box.setOrientation(LinearLayout.VERTICAL);box.setPadding(dp(16),dp(8),dp(16),0);
        box.addView(picker,new LinearLayout.LayoutParams(-1,dp(270)));
        EditText hex=new EditText(this);hex.setSingleLine(true);hex.setTextSize(15);hex.setText(String.format(Locale.US,"#%06X",0xFFFFFF&initial));hex.setTextColor(themeText());hex.setBackground(shape(themeBg(),14));hex.setPadding(dp(12),0,dp(12),0);box.addView(hex,new LinearLayout.LayoutParams(-1,dp(48)));
        picker.listener=c->{hex.setText(String.format(Locale.US,"#%06X",0xFFFFFF&c));hex.setSelection(hex.length());};
        AlertDialog dlg=new AlertDialog.Builder(this).setTitle("Выбор цвета").setView(box).setNegativeButton("Отмена",null).setPositiveButton("Выбрать",null).create();
        dlg.setOnShowListener(x->{dlg.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v->{try{Color.parseColor(hex.getText().toString().trim());target.setText(hex.getText().toString().trim());dlg.dismiss();}catch(Exception ex){hex.setError("Неверный HEX");}});});
        dlg.show();
    }
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
    static class ColorPickerView extends View {
        Paint p=new Paint(3);Paint line=new Paint(3);float hue,sat,val;float cx,cy,radius;interface Listener{void onColor(int c);}Listener listener;
        ColorPickerView(android.content.Context c,int initial){super(c);float[] hsv=new float[3];Color.colorToHSV(initial,hsv);hue=hsv[0];sat=hsv[1];val=hsv[2];line.setStyle(Paint.Style.STROKE);setLayerType(View.LAYER_TYPE_SOFTWARE,null);}
        protected void onDraw(Canvas c){super.onDraw(c);float w=getWidth(),h=getHeight();cx=w/2f;cy=Math.min(dpStatic(getContext(),220),h*0.47f);radius=Math.min(w*0.37f,cy-18);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(dpStatic(getContext(),26));int n=60;for(int i=0;i<n;i++){p.setColor(Color.HSVToColor(new float[]{i*360f/n,1,1}));float a1=i*360f/n-90,a2=(i+1)*360f/n-90;c.drawArc(new RectF(cx-radius,cy-radius,cx+radius,cy+radius),a1,a2-a1+1.2f,false,p);}p.setStyle(Paint.Style.FILL);float ang=(float)Math.toRadians(hue-90);float hx=cx+(float)Math.cos(ang)*radius;float hy=cy+(float)Math.sin(ang)*radius;line.setColor(Color.WHITE);line.setStrokeWidth(dpStatic(getContext(),3));c.drawCircle(hx,hy,dpStatic(getContext(),15),line);line.setColor(Color.BLACK);line.setStrokeWidth(dpStatic(getContext(),1));c.drawCircle(hx,hy,dpStatic(getContext(),15),line);float sr=radius-dpStatic(getContext(),35);float sx=cx-sr,sy=cy-sr;for(int yy=0;yy<12;yy++){float vv=1f-yy/11f;for(int xx=0;xx<24;xx++){float ss=xx/23f;p.setColor(Color.HSVToColor(new float[]{hue,ss,vv}));c.drawRect(sx+xx*(2*sr/24f),sy+yy*(2*sr/12f),sx+(xx+1)*(2*sr/24f)+1,sy+(yy+1)*(2*sr/12f)+1,p);}}float px=sx+sat*2*sr,py=sy+(1-val)*2*sr;line.setColor(Color.WHITE);line.setStyle(Paint.Style.STROKE);line.setStrokeWidth(dpStatic(getContext(),3));c.drawCircle(px,py,dpStatic(getContext(),8),line);line.setStyle(Paint.Style.FILL);}
        public boolean onTouchEvent(MotionEvent e){float x=e.getX(),y=e.getY();if(e.getAction()!=MotionEvent.ACTION_DOWN&&e.getAction()!=MotionEvent.ACTION_MOVE)return true;float dx=x-cx,dy=y-cy,dist=(float)Math.hypot(dx,dy);if(dist>radius-dpStatic(getContext(),22)&&dist<radius+dpStatic(getContext(),22)){hue=(float)Math.toDegrees(Math.atan2(dy,dx))+90;if(hue<0)hue+=360;invalidate();emit();return true;}float sr=radius-dpStatic(getContext(),35),sx=cx-sr,sy=cy-sr;if(x>=sx&&x<=sx+2*sr&&y>=sy&&y<=sy+2*sr){sat=Math.max(0,Math.min(1,(x-sx)/(2*sr)));val=Math.max(0,Math.min(1,1-(y-sy)/(2*sr)));invalidate();emit();}return true;}
        void emit(){if(listener!=null)listener.onColor(Color.HSVToColor(new float[]{hue,sat,val}));}
        static int dpStatic(android.content.Context c,float v){return (int)(v*c.getResources().getDisplayMetrics().density+.5f);}
    }
}
