package com.example.playem.appsettings;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public interface ProfileSerializable {
    String ProfilesDir = "Profiles";
    String Settings = "AppSettings";
    static boolean SaveProfile(Context context, @NonNull List<ControlsData> controlsData, @NonNull GridData gridData, @NonNull String name){
        Gson gb = new Gson();
        File directory = new File(context.getFilesDir(),ProfilesDir);
        if(directory.mkdirs()) {
            File file = new File(directory, name);
            ProfileData pd = new ProfileData(name,controlsData,gridData);
            pd.controlsList = controlsData;
            pd.gridData = gridData;

            try (FileOutputStream fos = new FileOutputStream(file,false)) {
                fos.write(gb.toJson(pd).getBytes(StandardCharsets.UTF_8));
            }catch(IOException e){
                Log.e("FILE","READ ERROR\n"+ e);
            }
            return true;
        }
        return false;
    }
    static List<String> GetProfiles(Context context){
        String dir = context.getFilesDir().getPath()+"/"+ProfilesDir;
        File pro_fir = new File(dir);
        List<String> fl = new ArrayList<>();
        File[] files =pro_fir.listFiles();
        if((files != null ? files.length : 0) >0){
            for(File f : files){
                fl.add(f.getName());
            }
        }
        return fl;
    }
    static Object GetObjectFromFile(Context context,Type objectType,String name){
        Gson g = new Gson();
        if(objectType==ProfileData.class){
            File directory = new File(context.getFilesDir(),ProfilesDir);
            File file = new File(directory, name);
            try{
                return g.fromJson(new JsonReader(new FileReader(file)),objectType);
            }
            catch (Exception e){
                Log.e("FILEREAD",e.toString());
            }
        }
        return null;
    }

}
