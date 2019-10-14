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


import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;

import java.util.ArrayList;
import java.util.Properties;
import java.util.Vector;


import static abfab3d.core.Output.printf;
import static abfab3d.core.Output.fmt;

/**
   manages a history of items 
   items can be removed from history and added 
 */
public class History {

    protected String m_prefix = "";
    protected int m_maxCount;

    protected Vector<String> m_items = new Vector<String>();

    
    public History(String prefix, int maxCount){
        m_maxCount = maxCount;
    }

    public History(int maxCount){
        m_maxCount = maxCount;
    }

    public int size(){
        return m_items.size();
    }

    public String getItem(int index){
        return m_items.get(index);
    }

    /**
       loads history from properties
       items in properties are expected to be stored as pair (key, value) 
       and that have has form prefix_index
       
     */
    public void read(Properties prop){       
        for(int i = 0; i < m_maxCount; i++){
            String item = prop.getProperty(keyName(i));
            if(item != null)
                add(item);
        }    
    }

    /**
       loads history from a list
       
     */
    public void read(ArrayList list){       
        for(int i = 0; i < list.size(); i++){
            add((String)list.get(i));
        }                
    }

    /**
      saves history into properties 
     */
    public void write(Properties prop){  
        for(int i = 0; i < m_maxCount; i++){
            prop.remove(keyName(i));
        }
        for(int i = 0; i < m_items.size(); i++){
            String item = m_items.get(i);
            prop.setProperty(keyName(i), item);
        }
    }


    protected String keyName(int i){
        return fmt("%s%02d", m_prefix,i);
    }

    public void remove(String item){
        int index = m_items.indexOf(item);        
        if(index >= 0) {
            m_items.removeElementAt(index);
        }
    }

    public void clear(){
        m_items.setSize(0);
    }

    public void add(String item){

        int index = m_items.indexOf(item);
        if(index < 0) {
            // new element 
            while(m_items.size() >= m_maxCount){
                m_items.removeElementAt(0);
            }
            m_items.add(item);
        } else {
            // existing item
            m_items.removeElementAt(index);
            m_items.add(item);            
        }
    }      

    public void print(){
        for(int i = 0; i < m_items.size(); i++){
            printf("item:%d = %s\n", i, m_items.get(i));
        }
    }

}