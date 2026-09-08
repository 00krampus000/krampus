package ru.attendance.group;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;
import java.io.OutputStream;
import java.time.LocalDate;

public class ExportMainActivity extends ScheduleMainActivity {
    private static final int CREATE_DOCUMENT = 9001;
    private byte[] pendingExport;

    @Override void export(){
        try{
            StringBuilder s=new StringBuilder("Дата\tПара\tВремя\tПредмет");
            for(String n:students)s.append("\t").append(n);
            s.append("\r\n");
            LocalDate start=date.withDayOfMonth(1),end=date.withDayOfMonth(date.lengthOfMonth());
            for(LocalDate d=start;!d.isAfter(end);d=d.plusDays(1)){
                for(int l=1;l<=4;l++){
                    boolean any=false;
                    for(String n:students) if(!mark(d,l,n).equals("—")){any=true;break;}
                    if(!any)continue;
                    s.append(d).append("\t").append(l).append("\t")
                     .append(timeRange(d.getDayOfWeek().getValue(),l)).append("\t")
                     .append(subject(d.getDayOfWeek().getValue(),l));
                    for(String n:students)s.append("\t").append(mark(d,l,n));
                    s.append("\r\n");
                }
            }
            pendingExport=s.toString().getBytes("UTF-16LE");
            Intent i=new Intent(Intent.ACTION_CREATE_DOCUMENT);
            i.addCategory(Intent.CATEGORY_OPENABLE);
            i.setType("application/vnd.ms-excel");
            i.putExtra(Intent.EXTRA_TITLE,"Посещаемость_ЦТБИД-266.xls");
            startActivityForResult(i,CREATE_DOCUMENT);
        }catch(Exception e){Toast.makeText(this,"Ошибка подготовки экспорта",Toast.LENGTH_LONG).show();}
    }

    @Override protected void onActivityResult(int requestCode,int resultCode,Intent data){
        super.onActivityResult(requestCode,resultCode,data);
        if(requestCode!=CREATE_DOCUMENT)return;
        if(resultCode!=RESULT_OK||data==null||data.getData()==null){pendingExport=null;return;}
        Uri uri=data.getData();
        try(OutputStream out=getContentResolver().openOutputStream(uri)){
            if(out==null)throw new Exception("Не удалось открыть файл");
            out.write(pendingExport);out.flush();
            Toast.makeText(this,"Excel-файл сохранён",Toast.LENGTH_LONG).show();
        }catch(Exception e){Toast.makeText(this,"Не удалось сохранить файл",Toast.LENGTH_LONG).show();}
        pendingExport=null;
    }
}
