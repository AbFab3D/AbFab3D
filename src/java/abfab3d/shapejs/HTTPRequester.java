/*****************************************************************************
 *                        Shapeways, Inc Copyright (c) 2016
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
import java.util.Map;

/**
 * Handles HTTP requests.
 *
 * TODO: This code is experimental and likely will change
 *
 * @author Alan Hudson
 */
public interface HTTPRequester {
    public enum Method {GET, POST};

    public String sendRequest(String url, Method method, Map<String, String> params, Map<String, String> multipartParams, Map<String, String> headers, String accept, String user, String password, int connectionTimeout, int socketTimeout, boolean suppressErrorMsgs) throws Exception;

}
