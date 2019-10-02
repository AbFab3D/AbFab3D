/*****************************************************************************
 * Shapeways, Inc Copyright (c) 2019
 * Java Source
 * <p/>
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 * <p/>
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 ****************************************************************************/
package abfab3d.shapejs;

/**
 * Result from a update scene or make image request.
 *
 * @author Tony Wong
 */
public class RequestResult {
	public boolean success;
	public byte[] responseData;
	public long responseTime;
	public int errorCode;
	public String mimeType;
	public String endpoint;
	
	public RequestResult(String endpoint, boolean success, byte[] responseData, long responseTime) {
		this.endpoint = endpoint;
		this.success = success;
		this.responseData = responseData;
		this.responseTime = responseTime;
		this.errorCode = 0;
	}
	
	public RequestResult(String endpoint, int errorCode, byte[] responseData) {
		this(endpoint, false, responseData, -1);
		this.errorCode = errorCode;
	}
	
	public RequestResult(String endpoint, boolean success, byte[] responseData, long responseTime, String mimeType) {
		this(endpoint, success, responseData, responseTime);
		this.mimeType = mimeType;
	}
}
