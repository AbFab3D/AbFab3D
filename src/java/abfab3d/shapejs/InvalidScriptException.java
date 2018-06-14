/*****************************************************************************
 *                        Shapeways, Inc Copyright (c) 2015
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/
package abfab3d.shapejs;

import java.util.List;
import java.util.Map;

/**
 * Exception thrown when a script is not valid
 *
 * @author Tony Wong
 */
public class InvalidScriptException extends Exception {
    private String script;
    private String message;
    private List<Map<String, String>> errors;
    
    public InvalidScriptException(String script, String message, List<Map<String, String>> errors) {
        this.script = script;
        this.message = message;
        this.errors = errors;
    }
    
    public String getScript() {
        return script;
    }
    
    public String getMessage() {
        return message;
    }
    
    public List<Map<String, String>> getErrors() {
        return errors;
    }
}
