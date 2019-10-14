/******************************************************************************
 *                        Shapeways, Inc Copyright (c) 2012-2019
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package shapejs.viewer;

import java.util.HashMap;
import java.util.UUID;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Vector;
import java.util.ArrayList;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;

import org.apache.commons.io.FileUtils;

import com.google.gson.JsonParser;
import com.google.gson.JsonObject;
import com.google.gson.JsonElement;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.Properties;
import java.util.Vector;

import abfab3d.core.Color;

import abfab3d.param.ColorParameter;
import abfab3d.param.BaseParameterizable;
import abfab3d.param.Parameter;
import abfab3d.param.StringListParameter;
import abfab3d.param.ParamJson;


import static abfab3d.core.Output.printf;

public class ViewerConfig extends BaseParameterizable{
    

    static final boolean DEBUG = true;

    static final String configName = "VolumeViewer.ini";
    static final String configJson = "VolumeViewer.json";
    public static final String LIB = "lib";
    public static final String CANVAS_BACKGROUND_COLOR = "canvasBackgroundColor";

    String m_path;    
    String m_pathJson;    
    Properties m_prop;
    History m_history = new History(50);

    StringListParameter mp_lib = new StringListParameter(LIB);
    StringListParameter mp_history = new StringListParameter("history");
    ColorParameter mp_canvasBackgroundColor = new ColorParameter(CANVAS_BACKGROUND_COLOR, new Color(1,1,1,1));

    Parameter m_params[] = new Parameter[]{
        mp_lib,
        mp_history,
        mp_canvasBackgroundColor,

    };

    
    // console to print messages to 
    Console m_console; 

    static private ViewerConfig sm_config;    

    private ViewerConfig(){
        super.addParams(m_params);
        
        String user_dir = System.getProperty("user.dir");

        m_path = user_dir + File.separator + configName;
        m_pathJson = user_dir + File.separator + configJson;

        //readProp(m_path);
        readJson(m_pathJson);


    }

    void readJson(String path){
        try {
            String str = FileUtils.readFileToString(new File(path));
            JsonParser parser = new JsonParser();        
            JsonObject obj = parser.parse(str).getAsJsonObject();
            ParamJson.getParamValuesFromJson(obj, m_params);

            for(int i = 0; i < m_params.length; i++){
                if(DEBUG)printf("param[%s]:%s\n", m_params[i].getName(),m_params[i].getValue());
            }
            
            m_history.read((ArrayList)mp_history.getValue());
            
        } catch(Exception e){
            // Don't print this exception trace
            printf("ViewerConfig. Cannot find config file: %s\n",path);
            //e.printStackTrace();
        }
    }
    /*
    void readProp(String path){

        m_prop = new Properties();

        try {
            
            FileInputStream in = new FileInputStream(path);
            m_prop.load(in);
            m_history.read(m_prop);
        } catch(Exception e){
            printf("error reading config file: %s\n", path);
        }
    }
    */
    public void save(){

        //saveProp(m_path);
        saveJson(new File(m_pathJson));
                    
    }

    public void saveJson(File file){
        
        ArrayList hist = (ArrayList)(mp_history.getValue());
        hist.clear();
        for(int i = 0; i < m_history.size(); i++){
            hist.add(m_history.getItem(i));
        }
        Map map = ParamJson.getJson(m_params);        
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String str = gson.toJson(map);
        try {
            FileUtils.writeStringToFile(file, str, "UTF-8");        
        } catch(Exception e){
            e.printStackTrace();
        }        
    }

    /*
    public void saveProp(String path){

        try {
            FileOutputStream out = new FileOutputStream(path);
            Properties prop = new Properties();
            m_history.write(prop);
            prop.store(out, "");

        } catch(Exception e){
            e.printStackTrace();
        }
    }
    */
    public History getOpenFileHistory(){
        return m_history;
    }

    public static ViewerConfig getInstance(){

        if(sm_config== null){
            sm_config = new ViewerConfig();
        }
        return sm_config;
    }


    public Console getConsole(){
        return m_console;
    }

    public void setConsole(Console console){
        m_console = console;
    }

    /**
       global way to print messages 
     */
    public void print(String str){
        if(m_console != null) 
            m_console.messageReport(str);
        else 
            printf(str);
    }
    
}
