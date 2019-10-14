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

import java.util.Vector;

import abfab3d.shapejs.ScriptManager;
import abfab3d.shapejs.ScriptResources;
import org.apache.commons.io.FileUtils;

import abfab3d.param.ParamChangedListener;
import abfab3d.param.Parameter;


import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.UUID;

import static abfab3d.core.Output.printf;

/**
 * Watch the disk for changes in modified times and then reload the script
 *
 * @author Alan Hudson
 */
public class DiskWatcher implements Runnable {

    static final boolean DEBUG = false;

    private volatile boolean terminate = false;
    // listeners to inform on param changes 
    Vector<ParamChangedListener> m_listeners = new Vector<ParamChangedListener>(1);
    // params to watch 
    Vector<Parameter> m_params = new Vector<Parameter>(1);
    // initial param values 
    Vector<String> m_paramStrings = new Vector<String>(1);

    private File file;
    private long last;
    private long m_sleepTime = 100;

    public DiskWatcher(){

        if(DEBUG)printf("DiskWatcher()\n");

    }

    /**
       updates watched value for parameter (in case if parameteras changed via UI)
     */
    public void updateParam(Parameter param){

        if(DEBUG)printf("DiskWatcher.updateParam(%s)\n", param.getName());
        for(int i = 0; i < m_params.size(); i++){
            if(m_params.get(i) ==  param){
                m_paramStrings.setElementAt(param.getParamString(), i);
                break;
            }
        }        
    }

    /**
       adds listener for given parameter value changed 
     */
    public void addParamChangedListener(Parameter param, ParamChangedListener listener){

        if(DEBUG)printf("DiskWatcher.addListener(%s)%s\n", param.getName(), param.getParamString());

        m_listeners.add(listener);
        m_params.add(param);
        m_paramStrings.add(param.getParamString());

    }

    public void run() {
        

        while(true) {
            try {
                Thread.sleep(m_sleepTime);
            } catch (InterruptedException ie) {
            }
            if(terminate) return;
            
            for(int i = 0; i < m_params.size(); i++){
                Parameter param = m_params.get(i);
                String pstr = m_paramStrings.get(i);
                if(!param.getParamString().equals(pstr)){
                    // value changed 
                    m_listeners.get(i).paramChanged(param);
                    if(DEBUG) printf("DiskWatcher param(%s) changed:%s\n", param.getName(), param.getParamString());
                    break;
                }                
            }                
        }
    }

    /*
    private boolean isValidScript(File f) {

        if(DEBUG) printf("Testing file: %s\n", f);
        String script = null;
        try {
            script = FileUtils.readFileToString(f);
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }

        ScriptManager sm = ScriptManager.getInstance();
        String jobID = UUID.randomUUID().toString();

        HashMap<String, Object> params = new HashMap<String, Object>();

        ScriptResources sr = sm.prepareScript(jobID, script, params);
        sm.executeScript(sr, params);

        if (sr.evaluatedScript.isSuccess()) {
            console.messageReport("Scene loaded");
            return true;
        }

        console.printScriptResult(sr.evaluatedScript);

        return false;
    }
    */
    public void shutdown() {
        terminate = true;
    }
}
