package shapeways.api.robocreator;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import org.scribe.builder.*;
import org.scribe.builder.api.*;
import org.scribe.model.*;
import org.scribe.oauth.*;
import app.common.upload.shapeways.oauth.api.ShapewaysApi;
import com.google.gson.Gson;
import shapeways.api.ShapewaysAPI;


import java.io.File;
import java.io.IOException;
import java.util.*;

public class APIExample
{
	private static final String BASE_URL = "http://api.beekey.nyc.shapeways.net";
	//private static final String BASE_URL = "http://api.shapeways.com";

    private static String PROXY_HOST="127.0.0.1";
    private static String PROXY_PORT="8888";

  public static void main(String[] args)
  {

      if (PROXY_HOST != null) {
          System.out.println("Configuring proxy");

          Properties systemSettings =
                  System.getProperties();
          systemSettings.put("proxySet", "true");
//            systemSettings.put("http.proxyHost", PROXY_HOST);
//            systemSettings.put("http.proxyPort", PROXY_PORT);
          systemSettings.put("socksProxyHost", PROXY_HOST);
          systemSettings.put("socksProxyPort", PROXY_PORT);
      }

    // Replace these with your own api key and secret (you'll need an read/write api key)
    String apiKey = "c1100bbeeccc7f4f2fe9a0089b052b6a03e8af54";
    String apiSecret = "91fbff645d1a7ff539991f08604d5802590b1f71";
//    OAuthService service = new ServiceBuilder().provider(ShapewaysApi.class).apiKey(apiKey).apiSecret(apiSecret).build();
      OAuthService service = new ServiceBuilder().provider(new ShapewaysAPI(BASE_URL)).apiKey(apiKey).apiSecret(apiSecret).build();
    Scanner in = new Scanner(System.in);

    System.out.println("=== Shapeways OAuth Workflow ===");
    System.out.println();

    // Obtain the Request Token
    System.out.println("Fetching the Request Token...");
    Token requestToken = service.getRequestToken();
    System.out.println("Got the Request Token!");
    System.out.println("(if your curious it looks like this: " + requestToken + " )");
    System.out.println();

    System.out.println("Now go and authorize Scribe here:");
    String authorizationUrl = service.getAuthorizationUrl(requestToken);
    System.out.println(authorizationUrl);
    System.out.println("And paste the verifier here");
    System.out.print(">>");

    Verifier verifier = new Verifier(in.nextLine());
    System.out.println();

    // Trade the Request Token and Verfier for the Access Token
    System.out.println("Trading the Request Token for an Access Token...");
    Token accessToken = service.getAccessToken(requestToken, verifier);
    System.out.println("Got the Access Token!");
    System.out.println("(if your curious it looks like this: " + accessToken + " )");
    System.out.println();
/*
    // Now let's go and ask for a protected resource!
    System.out.println("Now we're going to access a protected resource...");
    OAuthRequest request = new OAuthRequest(Verb.GET, PROTECTED_RESOURCE_URL);
    service.signRequest(accessToken, request);
    Response response = request.send();
    System.out.println("Got it! Lets see what we found...");
    System.out.println();
    System.out.println(response.getBody());

    System.out.println();
    System.out.println("Thats it man! Go and build something awesome with Scribe! :)");
*/
//	ShapewaysExample ex = new ShapewaysExample();
//	ex.uploadModel();
  }
  
  public void uploadModel() {
	String UPLOAD_URL = "http://api.test102.ehv.shapeways.net/model/v1";
    String CONSUMER_KEY = "fcc8983c4e72e553e19583fcfbedf8d55d5dfc24";
    String CONSUMER_SECRET = "ae647dffab05dbabaf92f739c766cda3351664d1";

	String ACCESS_TOKEN = "b2beb1591e39e5405505598c9c48374cbf062862";
	String ACCESS_SECRET = "e4eede614557e265974a8d425ac1b564ce73cc8e";
	
	OAuthService service = new ServiceBuilder().provider(ShapewaysApi.class).apiKey(CONSUMER_KEY).apiSecret(CONSUMER_SECRET).build();
	Token accessToken = new Token(ACCESS_TOKEN, ACCESS_SECRET);
	
	File file = new File("/tmp/cube.stl");
	String encodedFile = null;
	Gson gson = new Gson();
	
	try {
		byte[] fileBytes = FileUtils.readFileToByteArray(file);
		encodedFile = Base64.encodeBase64String(fileBytes);
		
	    Map<String, Object> parameters = new HashMap<String, Object>();
	    parameters.put("file", encodedFile);
	    parameters.put("fileName", file.getName());
	    parameters.put("ownOrAuthorizedModel", 1);
	    parameters.put("acceptTermsAndConditions", 1);
	    String encodedParams = gson.toJson(parameters);
	    System.out.println(encodedParams);

//		System.out.println(encodedFile);
		OAuthRequest request = new OAuthRequest(Verb.POST, UPLOAD_URL);
//		request.addQuerystringParameter("file", gson.toJson(encodedFile));
//		request.addQuerystringParameter("fileName", gson.toJson(file.getName()));
//		request.addQuerystringParameter("ownOrAuthorizedModel", gson.toJson("1"));
//		request.addQuerystringParameter("acceptedTermsAndConditions", gson.toJson("1"));
//		request.addBodyParameter("file", gson.toJson(encodedFile));
//	    request.addBodyParameter("fileName", gson.toJson(file.getName()));
//	    request.addBodyParameter("ownOrAuthorizedModel", gson.toJson("1"));
//	    request.addBodyParameter("acceptedTermsAndConditions", gson.toJson("1"));

		System.out.println(request.getBodyContents());
		System.out.println(request.getQueryStringParams().asFormUrlEncodedString());
		System.out.println(request.getCompleteUrl());
	    request.addPayload(encodedParams);
	    service.signRequest(accessToken, request);
	    Response response = request.send();
	    String responseBody = response.getBody();
	    System.out.println(responseBody);
	} catch (IOException ioe) {
		System.out.println("Model not found: " + file.getAbsolutePath());
	}
	
//	Gson gson = new Gson();
	
//    OAuthRequest request = new OAuthRequest(Verb.POST, UPLOAD_URL);
/*
    request.addBodyParameter("file", gson.toJson(encodedFile));
    request.addBodyParameter("fileName", gson.toJson(file.getName()));
    request.addBodyParameter("ownOrAuthorizedModel", gson.toJson("1"));
    request.addBodyParameter("acceptedTermsAndConditions", gson.toJson("1"));
    
    service.signRequest(accessToken, request);
    Response response = request.send();

    String responseBody = response.getBody();
    System.out.println(responseBody);

    
    Map<String, String> parameters = new HashMap<String, String>();
    parameters.put("file", encodedFile);
    parameters.put("fileName", file.getName());
    parameters.put("ownOrAuthorizedModel", "1");
    parameters.put("acceptedTermsAndConditions", "1");

    for (Map.Entry<String, String> entry : parameters.entrySet()) {
      request.addQuerystringParameter(entry.getKey(), entry.getValue());
    }

    service.signRequest(accessToken, request);
    Response response = request.send();
    String responseBody = response.getBody();
    System.out.println(responseBody);
*/
  }
}
