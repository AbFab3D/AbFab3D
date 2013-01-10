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
	private static final String UPLOAD_URL = "http://api.shapeways.com/model/v1";
	
	/** Access token and secret */
	private String API_KEY = "7830248cd2e086aa97358e3469d94e3b965a7df8";
	private String API_SECRET = "6f9e2f3b12d3cb6c1d7b50037dfbad2f56107879";
	private String ACCESS_TOKEN = "933ce0b75d0a3c335b969bf2dc34e14e273ffac6";
	private String ACCESS_SECRET = "262ca70394a04534a3c9e3d849c8b51c2e39f4df";
	
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
	public String uploadModel(
			byte[] bytes, 
			String fileName,
			Integer modelId, 
			Float scale,
			String title,
			String description,
			Integer isPublic,
			Integer viewState) throws Exception {
		
		String reason = null;
		Gson gson = new Gson();
		
		String encodedFile = null;
		encodedFile = encodeModel(bytes);
		
	    Map<String, Object> parameters = new HashMap<String, Object>();
	    parameters.put("file", encodedFile);
	    parameters.put("fileName", fileName);
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
//System.out.println("json params: " + encodedParams);

		request.addPayload(encodedParams);
	    service.signRequest(accessToken, request);
	    Response response = request.send();
	    
	    if (!response.isSuccessful()) {
	    	reason = "Upload failed: " + UPLOAD_URL + ", response code: " + response.getCode();
	    	System.out.println(reason);
	    	throw new Exception(reason);
	    }
	    
	    String responseBody = response.getBody();
	    System.out.println(responseBody);
	    
	    return responseBody;
	}
	
	public String uploadModel(
			String modelFile,
			Integer modelId, 
			Float scale,
			String title,
			String description,
			Integer isPublic,
			Integer viewState) throws Exception {
		
//		System.out.println("in ModelUploadOauthRunner, uploading: " + modelFile);
		String reason = null;
		File file = new File(modelFile);

		try {
			byte[] fileBytes = FileUtils.readFileToByteArray(file);
			return uploadModel(fileBytes, file.getName(), modelId, scale, title, description, isPublic, viewState);
		} catch (IOException ioe) {
			reason = "Model not found: " + file.getAbsolutePath();
			System.out.println(reason);
			throw new Exception(reason);
		}
	}

	public boolean isSuccess(String response) {
		Gson gson = new Gson();
		Map responseMap = gson.fromJson(response, Map.class);
		
		String result = (String) responseMap.get("result");
		
		if (result.equals("failure")) {
			String reason = (String) responseMap.get("reason");
			System.out.println("Upload failed! Reason: " + reason);
			return false;
		} else if (result.equals("success")) {
			int modelId = ((Double) responseMap.get("modelId")).intValue();
			int movelVersion = ((Double) responseMap.get("modelVersion")).intValue();
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

	private String encodeModel(byte[] bytes) {
		return Base64.encodeBase64String(bytes);
	}
	
	private String encodeModel(File modelFile) throws IOException {
		byte[] fileBytes = FileUtils.readFileToByteArray(modelFile);
		return Base64.encodeBase64String(fileBytes);
	}
	
}
