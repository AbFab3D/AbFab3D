/*****************************************************************************
 *                        Shapeways, Inc Copyright (c) 2011
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package app.common.upload.shapeways.oauth;

// External imports
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.scribe.builder.ServiceBuilder;
import org.scribe.model.*;
import org.scribe.oauth.*;
import org.apache.commons.codec.binary.*;
import org.apache.commons.io.FileUtils;
import com.google.gson.Gson;

// Internal imports
import app.common.upload.shapeways.oauth.api.ShapewaysApi;


/**
 * Uploads a model with OAuth authentication.
 *
 * @author Tony Wong
 */
public class ModelUploadOauthRunner {
	/** The upload url */
	private static final String UPLOAD_URL = "http://api.test102.ehv.shapeways.net/model/upload";
	
	/** Access token and secret */
	private String API_KEY = "my api key";
	private String API_SECRET = "my api secret";
	private String ACCESS_TOKEN = "my access token";
	private String ACCESS_SECRET = "my access secret";
	
	private OAuthService service;
	private Token accessToken;
	
	public ModelUploadOauthRunner() {
	    service = new ServiceBuilder().provider(ShapewaysApi.class).apiKey(API_KEY).apiSecret(API_SECRET).build();
		accessToken = new Token(ACCESS_TOKEN, ACCESS_SECRET);
	}
	
	public ModelUploadOauthRunner(String apiKey, String apiSecret, String accessToken, String accessSecret) {
		API_KEY = apiKey;
		API_SECRET = apiSecret;
		ACCESS_TOKEN = accessToken;
		ACCESS_SECRET = accessSecret;
		
	    service = new ServiceBuilder().provider(ShapewaysApi.class).apiKey(API_KEY).apiSecret(API_SECRET).build();
		this.accessToken = new Token(ACCESS_TOKEN, ACCESS_SECRET);
	}
/*
	public ModelUploadOauthRunner(String propertyFile) {
		Properties properties = new Properties();
		InputStream in = this.getClass().getResourceAsStream("/config/oauth.properties");
		
		try {
			properties.load(in);
			
			System.out.println(properties.toString());
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}
*/
	public boolean uploadModel(
			String modelFile, 
			Integer modelId, 
			Float scale,
			String title,
			String description,
			Integer isPublic,
			Integer viewState) {
		
		Gson gson = new Gson();
		File file = new File(modelFile);
		
		String encodedFile = null;
		try {
			encodedFile = encodeModel(file);
		} catch (IOException ioe) {
			System.out.println("Model not found: " + file.getAbsolutePath());
			return false;
		}
		
	    Map<String, Object> parameters = new HashMap<String, Object>();
	    parameters.put("file", encodedFile);
	    parameters.put("fileName", file.getName());
	    parameters.put("ownOrAuthorizedModel", 1);
	    parameters.put("acceptTermsAndConditions", 1);
	    
	    if (scale != null) {
	    	parameters.put("uploadScale", scale.doubleValue());
	    }
	    if (modelId != null) {
	    	parameters.put("modelId", modelId.intValue());
	    }
	    if (title != null) {
	    	parameters.put("title", title);
	    }
	    if (description != null) {
	    	parameters.put("description", description);
	    }
	    if (isPublic != null) {
	    	parameters.put("isPublic", isPublic.intValue());
	    }
	    if (viewState != null) {
	    	parameters.put("viewState", viewState.intValue());
	    }
	    
	    OAuthRequest request = new OAuthRequest(Verb.POST, UPLOAD_URL);
	    String encodedParams = gson.toJson(parameters);
//System.out.println(encodedParams);

		request.addPayload(encodedParams);
	    service.signRequest(accessToken, request);
	    Response response = request.send();
	    
	    if (!response.isSuccessful()) {
	    	System.out.println("Request unsuccessfull... response code " + response.getCode());
	    	return false;
	    }
	    
	    String responseBody = response.getBody();
	    System.out.println(responseBody);
	    
	    if (isSuccess(responseBody)) {
	    	return true;
	    } else {
			return false;
	    }
	}
	
	private boolean isSuccess(String response) {
		Gson gson = new Gson();
		Map responseMap = gson.fromJson(response, Map.class);
		
		String result = (String) responseMap.get("result");
		
		if (result.equals("failure")) {
			String reason = (String) responseMap.get("reason");
			System.out.println("Upload failed! Reason: " + reason);
			return false;
		} else if (result.equals("success")) {
			int modelId = ((Double) responseMap.get("model_id")).intValue();
			int movelVersion = ((Double) responseMap.get("model_version")).intValue();
			String fileName = (String) responseMap.get("fileName");
			System.out.println("Upload success... ");
			System.out.println("  model id: " + modelId);
			System.out.println("  model version: " + movelVersion);
			System.out.println("  file name: " + fileName);
			return true;
		} else {
			System.out.println("Missing result field in response!");
			return false;
		}
	}
	
	private String encodeModel(File modelFile) throws IOException{
		byte[] fileBytes = FileUtils.readFileToByteArray(modelFile);
		return Base64.encodeBase64String(fileBytes);
	}
}
