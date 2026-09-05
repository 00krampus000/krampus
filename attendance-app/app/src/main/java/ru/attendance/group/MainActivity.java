package ru.attendance.group;

import android.app.*;
import android.os.*;
import android.content.*;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.view.*;
import android.widget.*;
import java.io.*;
import java.time.*;
import java.time.format.*;
import java.util.*;

public class MainActivity extends Activity {
    static final String[] DEFAULT_NAMES = {
        "Басевская Яна", "Вдовин Данил", "Ведянина Анастасия", "Галичина Алена",
        "Корнеева Марина", "Косых Елизавета", "Крохина Полина", "Медведева Ангелина",
        "Филатов Кирилл", "Шабанова Екатерина", "Шаколова Виктория", "Джумазода Зикрулло",
        "Джумазода Кароматулло", "Исхоки Исмоил", "Рустамов Самандар", "Хаитов Сиёвуш"
    };
    static final int ACCENT = Color.rgb(18,168,137);
    static final int ACCENT_SOFT = Color.rgb(232,247,243);
    static final int INK = Color.rgb(23,33,43);
    static final int MUTED = Color.rgb(115,128,140);
    static final int BG = Color.rgb(247,249,250);
    static final int LINE = Color.rgb(231,236,239);
    static final int WHITE = Color.WHITE;

    SharedPreferences prefs;
    LocalDate date = LocalDate.now();
    int tab = 0;
    int lesson = 1;
    DayOfWeek scheduleDay = DayOfWeek.MONDAY;
    ArrayList<String> students = new ArrayList<>();
    HashMap<String,TextView> visibleBadges = new HashMap<>();
    TextView ratioView, stateView;
    ProgressBar progressView;

    int dp(float v) { return (int)(v * getResources().getDisplayMetrics().density + .5f); }

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        prefs = getSharedPreferences("attendance", MODE_PRIVATE);
        loadStudents();
        getWindow().setStatusBarColor(Color.rgb(128,128,128));
        getWindow().setNavigationBarColor(WHITE);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
        buildShell();
    }

    void loadStudents() {
        String saved = prefs.getString("students", "");
        if (saved.isEmpty()) students.addAll(Arrays.asList(DEFAULT_NAMES));
        else students.addAll(Arrays.asList(saved.split("\\n", -1)));
    }

    void saveStudents() { prefs.edit().putString("students", String.join("\n", students)).apply(); }

    TextView tv(String s, float size, int color) {
        TextView v = new TextView(this);
        v.setText(s); v.setTextSize(size); v.setTextColor(color); v.setFontFeatureSettings("kern");
        return v;
    }

    GradientDrawable shape(int color, float radius) {
        GradientDrawable g = new GradientDrawable(); g.setColor(color); g.setCornerRadius(dp(radius)); return g;
    }

    TextView action(String label, int bg, int fg, float radius) {
        TextView v = tv(label, 14, fg); v.setGravity(Gravity.CENTER); v.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        v.setBackground(shape(bg, radius)); return v;
    }

    LinearLayout card() {
        LinearLayout c = new LinearLayout(this); c.setOrientation(LinearLayout.VERTICAL); c.setBackground(shape(WHITE, 22)); return c;
    }

    LinearLayout.LayoutParams margins(float l,float t,float r,float b) {
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(-1,-2); p.setMargins(dp(l),dp(t),dp(r),dp(b)); return p;
    }

    void buildShell() {
        LinearLayout shell = new LinearLayout(this); shell.setOrientation(LinearLayout.VERTICAL); shell.setBackgroundColor(BG);
        FrameLayout page = new FrameLayout(this);
        shell.addView(page, new LinearLayout.LayoutParams(-1,0,1));
        shell.addView(bottomNav(), new LinearLayout.LayoutParams(-1,dp(82)));
        setContentView(shell); renderPage(page);
    }

    TextView navItem(String icon, String label, int index) {
        TextView v = tv(icon + "\n" + label, 11, index==tab ? ACCENT : MUTED); v.setGravity(Gravity.CENTER); v.setLineSpacing(0,1.0f);
        v.setBackground(shape(index==tab ? ACCENT_SOFT : WHITE,18));
        v.setOnClickListener(x -> { tab=index; buildShell(); }); return v;
    }

    LinearLayout bottomNav() {
        LinearLayout n = new LinearLayout(this); n.setPadding(dp(8),dp(8),dp(8),dp(8)); n.setGravity(Gravity.CENTER); n.setBackgroundColor(WHITE);
        String[] icons={"⌂","▦","◫","•••"}; String[] labels={"Главная","Расписание","Статистика","Ещё"};
        for(int i=0;i<4;i++) n.addView(navItem(icons[i],labels[i],i),new LinearLayout.LayoutParams(0,-1,1));
        return n;
    }

    void renderPage(FrameLayout host) {
        host.removeAllViews();
        ScrollView scroll = new ScrollView(this); scroll.setFillViewport(true); scroll.setVerticalScrollBarEnabled(false);
        LinearLayout body = new LinearLayout(this); body.setOrientation(LinearLayout.VERTICAL); body.setPadding(0,0,0,dp(18));
        if(tab==0) home(body); else if(tab==1) schedule(body); else if(tab==2) stats(body); else more(body);
        scroll.addView(body); host.addView(scroll,new FrameLayout.LayoutParams(-1,-1));
    }

    void header(LinearLayout body, String title, String subtitle, boolean calendar) {
        LinearLayout h = new LinearLayout(this); h.setGravity(Gravity.CENTER_VERTICAL); h.setPadding(dp(20),dp(24),dp(20),dp(14));
        LinearLayout texts = new LinearLayout(this); texts.setOrientation(LinearLayout.VERTICAL);
        TextView a=tv(title,29,INK); a.setTypeface(Typeface.DEFAULT,Typeface.BOLD); texts.addView(a);
        TextView b=tv(subtitle,15,MUTED); b.setPadding(0,dp(3),0,0); texts.addView(b);
        h.addView(texts,new LinearLayout.LayoutParams(0,-2,1));
        if(calendar){ TextView cal=action("▣",WHITE,INK,17); cal.setTextSize(18); cal.setOnClickListener(v->datePicker()); h.addView(cal,new LinearLayout.LayoutParams(dp(48),dp(48))); }
        body.addView(h);
    }

    void home(LinearLayout body) {
        visibleBadges.clear(); ratioView=null; stateView=null; progressView=null;
        header(body,"ЦТБИД-266","Посещаемость группы",true);
        LinearLayout week = new LinearLayout(this); week.setPadding(dp(20),dp(4),dp(20),dp(12));
        LocalDate monday=date.minusDays(date.getDayOfWeek().getValue()-1);
        for(int i=0;i<7;i++){
            LocalDate d=monday.plusDays(i); String day=d.getDayOfWeek().getDisplayName(TextStyle.SHORT,new Locale("ru")).replace(".","");
            if(day.length()>2) day=day.substring(0,2);
            TextView v=tv(day.toUpperCase(Locale.ROOT)+"\n"+d.getDayOfMonth(),12,d.equals(date)?WHITE:MUTED); v.setGravity(Gravity.CENTER); v.setLineSpacing(0,1.05f);
            v.setBackground(shape(d.equals(date)?ACCENT:WHITE,17)); final LocalDate pick=d; v.setOnClickListener(x->{date=pick;buildShell();});
            LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,dp(58),1); lp.setMargins(dp(3),0,dp(3),0); week.addView(v,lp);
        }
        body.addView(week);

        LinearLayout summary=card(); summary.setPadding(dp(19),dp(17),dp(19),dp(16));
        LinearLayout top=new LinearLayout(this); top.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout left=new LinearLayout(this); left.setOrientation(LinearLayout.VERTICAL);
        TextView today=tv(date.equals(LocalDate.now())?"Сегодня":formatDateShort(date),17,INK); today.setTypeface(Typeface.DEFAULT,Typeface.BOLD); left.addView(today);
        left.addView(tv(formatDateLong(date),14,MUTED)); top.addView(left,new LinearLayout.LayoutParams(0,-2,1));
        ratioView=tv("",26,ACCENT); ratioView.setTypeface(Typeface.DEFAULT,Typeface.BOLD); top.addView(ratioView); summary.addView(top);
        progressView=new ProgressBar(this,null,android.R.attr.progressBarStyleHorizontal); progressView.setMax(Math.max(1,students.size())); progressView.setProgressTintList(ColorStateList.valueOf(ACCENT)); progressView.setProgressBackgroundTintList(ColorStateList.valueOf(LINE)); summary.addView(progressView,new LinearLayout.LayoutParams(-1,dp(7)));
        stateView=tv("",13,MUTED); stateView.setPadding(0,dp(10),0,0); summary.addView(stateView); refreshSummary();
        body.addView(summary,margins(20,0,20,16));

        TextView pairs=tv("Пары",22,INK); pairs.setTypeface(Typeface.DEFAULT,Typeface.BOLD); pairs.setPadding(dp(20),0,dp(20),dp(9)); body.addView(pairs);
        HorizontalScrollView pairScroll=new HorizontalScrollView(this); pairScroll.setHorizontalScrollBarEnabled(false); LinearLayout chips=new LinearLayout(this); chips.setPadding(dp(20),0,dp(20),dp(10));
        for(int i=1;i<=4;i++){final int x=i; String sub=subject(date.getDayOfWeek().getValue(),i); String label=i+(sub.isEmpty()?"":"  "+sub); TextView c=tv(label,13,i==lesson?WHITE:MUTED); c.setGravity(Gravity.CENTER); c.setTypeface(Typeface.DEFAULT,Typeface.BOLD); c.setBackground(shape(i==lesson?ACCENT:WHITE,18)); c.setOnClickListener(v->{lesson=x;buildShell();}); LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(dp(118),dp(45)); lp.setMargins(0,0,dp(8),0); chips.addView(c,lp);}
        pairScroll.addView(chips); body.addView(pairScroll,new LinearLayout.LayoutParams(-1,dp(55)));

        LinearLayout subjectCard=card(); subjectCard.setPadding(dp(18),dp(14),dp(18),dp(14)); subjectCard.setBackground(shape(ACCENT_SOFT,21));
        subjectCard.addView(tv(lesson+" пара",13,Color.rgb(8,122,101)));
        String currentSubject=subject(date.getDayOfWeek().getValue(),lesson); String currentTime=timeRange(date.getDayOfWeek().getValue(),lesson);
        TextView sn=tv(currentSubject.isEmpty()?"Предмет не задан":currentSubject,20,INK); sn.setTypeface(Typeface.DEFAULT,Typeface.BOLD); sn.setPadding(0,dp(3),0,0); subjectCard.addView(sn);
        TextView stime=tv(currentTime.isEmpty()?"Время не задано":currentTime,13,MUTED); stime.setPadding(0,dp(4),0,0); subjectCard.addView(stime); body.addView(subjectCard,margins(20,0,20,17));

        LinearLayout studentHead=new LinearLayout(this); studentHead.setGravity(Gravity.CENTER_VERTICAL); studentHead.setPadding(dp(20),0,dp(20),dp(9));
        TextView st=tv("Студенты",22,INK); st.setTypeface(Typeface.DEFAULT,Typeface.BOLD); studentHead.addView(st,new LinearLayout.LayoutParams(0,-2,1));
        boolean grid=isGrid(); TextView toggle=action(grid?"☷  Список":"▦  Сетка",WHITE,INK,15); toggle.setTextSize(12); toggle.setOnClickListener(v->{prefs.edit().putBoolean("grid",!isGrid()).apply();buildShell();}); studentHead.addView(toggle,new LinearLayout.LayoutParams(dp(96),dp(40))); body.addView(studentHead);

        if(grid) buildStudentGrid(body); else buildStudentList(body);

        LinearLayout actions=new LinearLayout(this); actions.setPadding(dp(20),dp(15),dp(20),0);
        TextView all=action("✓  Все были",INK,WHITE,18); all.setOnClickListener(v->{for(String n:students){setMark(date,lesson,n,"P");TextView b=visibleBadges.get(n);if(b!=null)applyStatus(b,"✓");}refreshSummary();}); actions.addView(all,new LinearLayout.LayoutParams(0,dp(54),1));
        TextView ex=action("Экспорт",WHITE,INK,18); ex.setOnClickListener(v->export()); LinearLayout.LayoutParams ep=new LinearLayout.LayoutParams(0,dp(54),1); ep.setMargins(dp(9),0,0,0); actions.addView(ex,ep); body.addView(actions);
    }

    boolean isGrid(){return prefs.getBoolean("grid",false);}

    void buildStudentList(LinearLayout body){
        LinearLayout list=new LinearLayout(this); list.setOrientation(LinearLayout.VERTICAL); list.setPadding(dp(20),0,dp(20),0);
        for(int i=0;i<students.size();i++) addStudentRow(list,i,date,lesson);
        body.addView(list);
    }

    void buildStudentGrid(LinearLayout body){
        LinearLayout grid=new LinearLayout(this); grid.setOrientation(LinearLayout.VERTICAL); grid.setPadding(dp(20),0,dp(20),0);
        for(int i=0;i<students.size();i+=2){
            LinearLayout row=new LinearLayout(this); row.setOrientation(LinearLayout.HORIZONTAL); row.setGravity(Gravity.TOP);
            addStudentTile(row,i,date,lesson);
            if(i+1<students.size()) addStudentTile(row,i+1,date,lesson); else row.addView(new Space(this),new LinearLayout.LayoutParams(0,dp(1),1));
            grid.addView(row,new LinearLayout.LayoutParams(-1,dp(126)));
        }
        body.addView(grid);
    }

    void addStudentRow(LinearLayout list,int index,LocalDate d,int l){
        String name=students.get(index), status=mark(d,l,name);
        LinearLayout row=card(); row.setOrientation(LinearLayout.HORIZONTAL); row.setGravity(Gravity.CENTER_VERTICAL); row.setPadding(dp(14),dp(9),dp(10),dp(9));
        TextView num=tv(String.format(Locale.US,"%02d",index+1),12,MUTED); num.setGravity(Gravity.CENTER); row.addView(num,new LinearLayout.LayoutParams(dp(32),dp(44)));
        TextView avatar=avatarView(name); row.addView(avatar,new LinearLayout.LayoutParams(dp(44),dp(44)));
        TextView nm=tv(name,15,INK); nm.setTypeface(Typeface.DEFAULT,Typeface.BOLD); nm.setGravity(Gravity.CENTER_VERTICAL); nm.setMaxLines(2); nm.setPadding(dp(12),0,dp(8),0); row.addView(nm,new LinearLayout.LayoutParams(0,dp(48),1));
        TextView badge=statusBadge(status); row.addView(badge,new LinearLayout.LayoutParams(dp(44),dp(44))); visibleBadges.put(name,badge);
        row.setOnClickListener(v->{next(d,l,name);applyStatus(badge,mark(d,l,name));refreshSummary();});
        LinearLayout.LayoutParams rp=new LinearLayout.LayoutParams(-1,dp(70)); rp.setMargins(0,0,0,dp(7)); list.addView(row,rp);
    }

    void addStudentTile(LinearLayout parent,int index,LocalDate d,int l){
        String name=students.get(index), status=mark(d,l,name);
        LinearLayout tile=card(); tile.setGravity(Gravity.CENTER); tile.setPadding(dp(8),dp(9),dp(8),dp(8));
        TextView badge=statusBadge(status); tile.addView(badge,new LinearLayout.LayoutParams(dp(42),dp(42))); visibleBadges.put(name,badge);
        TextView nm=tv(name,13,INK); nm.setTypeface(Typeface.DEFAULT,Typeface.BOLD); nm.setGravity(Gravity.CENTER); nm.setMaxLines(2); tile.addView(nm,new LinearLayout.LayoutParams(-1,dp(38)));
        TextView no=tv(String.format(Locale.US,"№%02d",index+1),11,MUTED); no.setGravity(Gravity.CENTER); tile.addView(no,new LinearLayout.LayoutParams(-1,dp(20)));
        tile.setOnClickListener(v->{next(d,l,name);applyStatus(badge,mark(d,l,name));refreshSummary();});
        LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(0,dp(116),1); p.setMargins(0,0,dp(4),dp(10)); parent.addView(tile,p);
    }

    TextView avatarView(String name){TextView a=tv(initials(name),12,ACCENT);a.setGravity(Gravity.CENTER);a.setTypeface(Typeface.DEFAULT,Typeface.BOLD);a.setBackground(shape(ACCENT_SOFT,22));return a;}
    String initials(String n){String[] p=n.trim().split("\\s+");if(p.length>=2)return (""+p[0].charAt(0)+p[1].charAt(0)).toUpperCase(new Locale("ru"));return n.substring(0,Math.min(2,n.length())).toUpperCase(new Locale("ru"));}
    TextView statusBadge(String s){TextView v=tv("",17,MUTED);v.setGravity(Gravity.CENTER);v.setTypeface(Typeface.DEFAULT,Typeface.BOLD);applyStatus(v,s);return v;}
    void applyStatus(TextView v,String s){int c;String label=s;if(s.equals("✓"))c=ACCENT;else if(s.equals("×"))c=Color.rgb(214,69,69);else if(s.equals("О"))c=Color.rgb(190,125,0);else if(s.equals("У"))c=Color.rgb(71,116,184);else{c=Color.rgb(241,243,245);label="—";}v.setText(label);v.setTextColor(s.equals("—")?MUTED:WHITE);v.setBackground(shape(c,22));}
    void refreshSummary(){if(ratioView==null)return;int present=countGood(date,lesson),marked=countMarked(date,lesson);ratioView.setText(present+" / "+students.size());progressView.setProgress(present);stateView.setText(marked==0?"Никто ещё не отмечен":"Отмечено "+marked+" из "+students.size());}

    void schedule(LinearLayout body){
        header(body,"Расписание","Предметы и время на каждый день",false);
        HorizontalScrollView daysScroll=new HorizontalScrollView(this);daysScroll.setHorizontalScrollBarEnabled(false);LinearLayout days=new LinearLayout(this);days.setPadding(dp(20),0,dp(20),dp(14));
        for(DayOfWeek d:DayOfWeek.values()){final DayOfWeek q=d;String s=d.getDisplayName(TextStyle.SHORT,new Locale("ru")).replace(".","");if(s.length()>2)s=s.substring(0,2);TextView v=tv(s.toUpperCase(Locale.ROOT),13,q==scheduleDay?WHITE:MUTED);v.setGravity(Gravity.CENTER);v.setTypeface(Typeface.DEFAULT,Typeface.BOLD);v.setBackground(shape(q==scheduleDay?ACCENT:WHITE,17));v.setOnClickListener(x->{scheduleDay=q;buildShell();});LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(dp(52),dp(45));lp.setMargins(0,0,dp(7),0);days.addView(v,lp);}
        daysScroll.addView(days);body.addView(daysScroll,new LinearLayout.LayoutParams(-1,dp(60)));
        for(int i=1;i<=4;i++)scheduleRow(body,i);
    }

    void scheduleRow(LinearLayout body,int i){
        LinearLayout c=card();c.setPadding(dp(17),dp(14),dp(17),dp(14));
        LinearLayout head=new LinearLayout(this);head.setGravity(Gravity.CENTER_VERTICAL);TextView n=tv(i+" пара",17,INK);n.setTypeface(Typeface.DEFAULT,Typeface.BOLD);head.addView(n,new LinearLayout.LayoutParams(0,-2,1));TextView save=action("Сохранить",ACCENT_SOFT,ACCENT,14);head.addView(save,new LinearLayout.LayoutParams(dp(102),dp(38)));c.addView(head);
        EditText e=new EditText(this);e.setSingleLine(true);e.setTextSize(15);e.setTextColor(INK);e.setHintTextColor(MUTED);e.setHint("Название предмета");e.setPadding(dp(13),0,dp(13),0);e.setBackground(shape(BG,14));e.setText(subject(scheduleDay.getValue(),i));c.addView(e,new LinearLayout.LayoutParams(-1,dp(50)));
        LinearLayout times=new LinearLayout(this);times.setGravity(Gravity.CENTER_VERTICAL);times.setPadding(0,dp(9),0,0);
        TextView start=action("Начало  "+timeValue(scheduleDay.getValue(),i,"start"),WHITE,INK,14);TextView end=action("Конец  "+timeValue(scheduleDay.getValue(),i,"end"),WHITE,INK,14);
        times.addView(start,new LinearLayout.LayoutParams(0,dp(44),1));LinearLayout.LayoutParams ep=new LinearLayout.LayoutParams(0,dp(44),1);ep.setMargins(dp(8),0,0,0);times.addView(end,ep);c.addView(times);
        final int day=scheduleDay.getValue();start.setOnClickListener(v->pickTime(day,i,"start",start));end.setOnClickListener(v->pickTime(day,i,"end",end));
        save.setOnClickListener(v->{prefs.edit().putString("sub"+day+"-"+i,e.getText().toString().trim()).apply();Toast.makeText(this,"Расписание сохранено",Toast.LENGTH_SHORT).show();});
        body.addView(c,margins(20,0,20,10));
    }

    String defaultStart(int l){String[] a={"","08:30","10:10","11:50","13:40"};return a[l];}
    String defaultEnd(int l){String[] a={"","10:00","11:40","13:20","15:10"};return a[l];}
    String timeValue(int day,int l,String which){return prefs.getString("time"+day+"-"+l+"-"+which,which.equals("start")?defaultStart(l):defaultEnd(l));}
    String timeRange(int day,int l){String s=timeValue(day,l,"start"),e=timeValue(day,l,"end");return s.isEmpty()||e.isEmpty()?"":s+"–"+e;}
    void pickTime(int day,int l,String which,TextView button){String[] parts=timeValue(day,l,which).split(":");int h=9,m=0;try{h=Integer.parseInt(parts[0]);m=Integer.parseInt(parts[1]);}catch(Exception ignored){}new TimePickerDialog(this,(v,hh,mm)->{String val=String.format(Locale.US,"%02d:%02d",hh,mm);prefs.edit().putString("time"+day+"-"+l+"-"+which,val).apply();button.setText((which.equals("start")?"Начало  ":"Конец  ")+val);},h,m,true).show();}

    void stats(LinearLayout body){
        header(body,"Статистика","Общая картина посещаемости",true);int marked=0,good=0;LocalDate start=date.withDayOfMonth(1),end=date.withDayOfMonth(date.lengthOfMonth());
        for(LocalDate d=start;!d.isAfter(end);d=d.plusDays(1))for(int l=1;l<=4;l++)for(String n:students){String s=mark(d,l,n);if(!s.equals("—")){marked++;if(s.equals("✓")||s.equals("О"))good++;}}
        int pct=marked==0?0:good*100/marked;LinearLayout hero=card();hero.setBackground(shape(INK,24));hero.setPadding(dp(20),dp(19),dp(20),dp(19));hero.addView(tv("Посещаемость за "+formatMonth(date),14,Color.LTGRAY));TextView big=tv(pct+"%",42,WHITE);big.setTypeface(Typeface.DEFAULT,Typeface.BOLD);big.setPadding(0,dp(3),0,0);hero.addView(big);hero.addView(tv(marked+" отметок · "+good+" посещений",13,Color.LTGRAY));body.addView(hero,margins(20,0,20,18));
        TextView h=tv("По студентам",22,INK);h.setTypeface(Typeface.DEFAULT,Typeface.BOLD);h.setPadding(dp(20),0,dp(20),dp(9));body.addView(h);
        for(String n:students){int m=0,g=0;for(LocalDate d=start;!d.isAfter(end);d=d.plusDays(1))for(int l=1;l<=4;l++){String s=mark(d,l,n);if(!s.equals("—")){m++;if(s.equals("✓")||s.equals("О"))g++;}}LinearLayout c=card();c.setPadding(dp(15),dp(12),dp(15),dp(12));LinearLayout line=new LinearLayout(this);line.setGravity(Gravity.CENTER_VERTICAL);TextView name=tv(n,14,INK);name.setTypeface(Typeface.DEFAULT,Typeface.BOLD);line.addView(name,new LinearLayout.LayoutParams(0,-2,1));TextView percent=tv((m==0?0:g*100/m)+"%",15,ACCENT);percent.setTypeface(Typeface.DEFAULT,Typeface.BOLD);line.addView(percent);c.addView(line);c.addView(tv(m==0?"Нет отметок":"Отмечено "+m+" · присутствовал "+g,12,MUTED));body.addView(c,margins(20,0,20,7));}
    }

    void more(LinearLayout body){
        header(body,"Ещё","Группа и данные",false);TextView add=action("＋  Добавить студента",INK,WHITE,18);add.setGravity(Gravity.CENTER_VERTICAL);add.setPadding(dp(17),0,0,0);add.setOnClickListener(v->addStudent());body.addView(add,margins(20,0,20,9));
        TextView ex=action("↗  Экспорт в Excel",ACCENT,WHITE,18);ex.setGravity(Gravity.CENTER_VERTICAL);ex.setPadding(dp(17),0,0,0);ex.setOnClickListener(v->export());body.addView(ex,margins(20,0,20,18));
        LinearLayout info=card();info.setPadding(dp(17),dp(15),dp(17),dp(15));info.addView(tv("Состав группы",14,MUTED));TextView count=tv(students.size()+" студентов",20,INK);count.setTypeface(Typeface.DEFAULT,Typeface.BOLD);count.setPadding(0,dp(3),0,0);info.addView(count);body.addView(info,margins(20,0,20,12));
        TextView hint=tv("Все отметки и расписание хранятся только на этом телефоне.",13,MUTED);hint.setPadding(dp(20),dp(4),dp(20),0);body.addView(hint);
    }

    void addStudent(){final EditText input=new EditText(this);input.setHint("Фамилия Имя");input.setSingleLine(true);input.setPadding(dp(12),0,dp(12),0);input.setBackground(shape(BG,14));LinearLayout wrap=new LinearLayout(this);wrap.setPadding(dp(20),0,dp(20),0);wrap.addView(input,new LinearLayout.LayoutParams(-1,dp(52)));new AlertDialog.Builder(this).setTitle("Новый студент").setView(wrap).setNegativeButton("Отмена",null).setPositiveButton("Добавить",(d,w)->{String s=input.getText().toString().trim();if(!s.isEmpty()){students.add(s);saveStudents();buildShell();}}).show();}
    void datePicker(){new DatePickerDialog(this,(v,y,m,day)->{date=LocalDate.of(y,m+1,day);buildShell();},date.getYear(),date.getMonthValue()-1,date.getDayOfMonth()).show();}

    String formatDateLong(LocalDate d){return d.format(DateTimeFormatter.ofPattern("d MMMM yyyy",new Locale("ru")));}
    String formatDateShort(LocalDate d){return d.format(DateTimeFormatter.ofPattern("d MMMM",new Locale("ru")));}
    String formatMonth(LocalDate d){return d.format(DateTimeFormatter.ofPattern("LLLL yyyy",new Locale("ru")));}
    String subject(int day,int l){return prefs.getString("sub"+day+"-"+l,"");}
    String key(LocalDate d,int l,String n){return "mark/"+d+"/"+l+"/"+n;}
    String mark(LocalDate d,int l,String n){String s=prefs.getString(key(d,l,n),"");return s.equals("P")?"✓":s.equals("A")?"×":s.equals("L")?"О":s.equals("E")?"У":"—";}
    void setMark(LocalDate d,int l,String n,String s){prefs.edit().putString(key(d,l,n),s).apply();}
    void next(LocalDate d,int l,String n){String s=prefs.getString(key(d,l,n),"");String q=s.equals("")?"P":s.equals("P")?"A":s.equals("A")?"L":s.equals("L")?"E":"";setMark(d,l,n,q);}
    int countMarked(LocalDate d,int l){int c=0;for(String n:students)if(!mark(d,l,n).equals("—"))c++;return c;}
    int countGood(LocalDate d,int l){int c=0;for(String n:students){String s=mark(d,l,n);if(s.equals("✓")||s.equals("О"))c++;}return c;}

    void export(){String name="Посещаемость_ЦТБИД-266_"+date.getYear()+".xls";Intent i=new Intent(Intent.ACTION_CREATE_DOCUMENT);i.setType("application/vnd.ms-excel");i.putExtra(Intent.EXTRA_TITLE,name);startActivityForResult(i,42);}
    @Override protected void onActivityResult(int requestCode,int resultCode,Intent data){super.onActivityResult(requestCode,resultCode,data);if(requestCode!=42||resultCode!=RESULT_OK||data==null)return;Uri uri=data.getData();StringBuilder h=new StringBuilder("<html><head><meta charset='UTF-8'></head><body><table border='1'><tr><th>Дата</th><th>Пара</th><th>Предмет</th><th>Время</th><th>Студент</th><th>Статус</th></tr>");LocalDate a=LocalDate.of(date.getYear(),1,1),b=LocalDate.of(date.getYear(),12,31);for(LocalDate d=a;!d.isAfter(b);d=d.plusDays(1))for(int l=1;l<=4;l++)for(String n:students){String s=mark(d,l,n);if(!s.equals("—")){String st=s.equals("✓")?"Был":s.equals("×")?"Не был":s.equals("О")?"Опоздал":"Уважительная причина";h.append("<tr><td>").append(d).append("</td><td>").append(l).append("</td><td>").append(escape(subject(d.getDayOfWeek().getValue(),l))).append("</td><td>").append(timeRange(d.getDayOfWeek().getValue(),l)).append("</td><td>").append(escape(n)).append("</td><td>").append(st).append("</td></tr>");}}h.append("</table></body></html>");try(OutputStream out=getContentResolver().openOutputStream(uri)){out.write(h.toString().getBytes("UTF-8"));Toast.makeText(this,"Excel-файл сохранён",Toast.LENGTH_SHORT).show();}catch(Exception e){Toast.makeText(this,"Не удалось сохранить файл",Toast.LENGTH_LONG).show();}}
    String escape(String s){return s.replace("&","&amp;").replace("<","&lt;").replace(">","&gt;");}
}
