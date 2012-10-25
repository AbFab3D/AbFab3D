package app.common.upload.shapeways.oauth.api;

import org.scribe.builder.api.DefaultApi10a;
import org.scribe.model.*;

/**
 * OAuth API for Shapeways
 *
 * @author Tony Wong
 */
public class ShapewaysApi extends DefaultApi10a
{
	private static final String BASE_URL = "http://api.test102.ehv.shapeways.net";

	@Override
	public String getRequestTokenEndpoint()
	{
	    return BASE_URL + "/oauth1/request_token";
	}

	@Override
	public String getAccessTokenEndpoint()
	{
	    return BASE_URL + "/oauth1/access_token";
	}

	@Override
	public String getAuthorizationUrl(Token requestToken)
	{
	    return String.format(BASE_URL + "/login?oauth_token=%s", requestToken.getToken());
	}
}

