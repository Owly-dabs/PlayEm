package com.example.playem.appsettings;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public interface ProfileSerializable {
    static final String ProfilesDir = "Profiles";
    static final String Settings = "AppSettings";
    static boolean SaveProfile(Context context, @NonNull List<ControlsData> controlsData, @NonNull GridData gridData, @NonNull String name){
        Gson gb = new Gson();
        File directory = new File(context.getFilesDir(),ProfilesDir);
        directory.mkdirs();
        File file = new File(directory, name);
        ProfileData pd = new ProfileData(name,controlsData,gridData);
        pd.controlsList = controlsData;
        pd.gridData = gridData;

        try (FileOutputStream fos = new FileOutputStream(file,false)) {
            fos.write(gb.toJson(pd).getBytes(StandardCharsets.UTF_8));
        }catch(IOException e){
            Log.e("FILE","READ ERROR\n"+e.toString());
        }
        return true;
    }
    static List<String> GetProfiles(Context context){
        String dir = context.getFilesDir().getPath()+"/"+ProfilesDir;
        File pro_fir = new File(dir);
        List<String> fl = new ArrayList<>();
        for(File f : pro_fir.listFiles())
        {
            fl.add(f.getName());
            //Log.e("FILE",f.getName());
        }

        return fl;
    }
    static Object GetObjectFromFile(Context context,Type objectType,String name){
        Gson g = new Gson();
        if(objectType==ProfileData.class){
            File directory = new File(context.getFilesDir(),ProfilesDir);
            File file = new File(directory, name);
            try(FileInputStream fis = new FileInputStream(file)){
                Scanner s = new Scanner(fis).useDelimiter("\\A");
                String result = s.hasNext() ? s.next() : "";
                //Log.i("JSON",result);
                return g.fromJson(new JsonReader(new FileReader(file)),objectType);
            }
            catch (Exception e){
                Log.e("FILEREAD",e.toString());
            }
        }
        return null;
    }

}
