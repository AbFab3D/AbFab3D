/****************************************************************************
 *                        Shapeways, Inc Copyright (c) 2014
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/
package abfab3d.param;

import java.util.LinkedHashMap;
import java.util.Map;

import static abfab3d.util.Output.fmt;

/**
   interface to create new nodes via name
 */
public class BaseSNodeFactory implements SNodeFactory {

    static final String NO_NAMES[] = new String[]{};
    static final String NO_CLASSES[] = new String[]{};

    protected String names[];
    protected String classNames[];

    protected Map<String, String> params = new LinkedHashMap<String,String>();
    
    
    public BaseSNodeFactory(){
        this(NO_NAMES, NO_CLASSES);
    }


    public BaseSNodeFactory(String names[], String classNames[]){
        this.names = names;
        this.classNames = classNames;
        initTable(names, classNames);        
    }

    private void initTable(String names[], String classNames[]){

        for(int i = 0; i < names.length; i++){
            params.put(names[i], classNames[i]);
        }
    }

    public String[] getNames(){
        return names;
    }
    public SNode createSNode(String nodeName){
        String className = params.get(nodeName);
        if(className == null)
            throw new RuntimeException(fmt("unknown new node name: %s", nodeName));

        Class c = null;
        try {
            c = Class.forName(className);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(fmt("failed to create Class: %s", className));
        }
        try {
            return (SNode)c.newInstance();
        } catch (Exception e) {
            throw new RuntimeException(fmt("failed to create new instance of %s", c));
        }
    }
    
}