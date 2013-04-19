package shapeways.api.robocreator;

import shapeways.api.Result;
import shapeways.api.ResultError;
import shapeways.api.models.reservation.RESTAPIReservation;
import shapeways.api.models.reservation.RESTAPIReservationV1;

/**
 * TODO: Add docs
 *
 * @author Alan Hudson
 */
public class ModelUploader {
    private String host;

    private String CONSUMER_KEY;
    private String CONSUMER_SECRET;
    private String ACCESS_TOKEN;
    private String ACCESS_SECRET;

    public ModelUploader(String host, String consumerKey, String consumerSecret, String accessToken, String accessSecret) {
        this.host = host;
        this.CONSUMER_KEY = consumerKey;
        this.CONSUMER_SECRET = consumerSecret;
        this.ACCESS_TOKEN = accessToken;
        this.ACCESS_SECRET = accessSecret;
    }

    public void uploadModel(Integer modelId, byte[] model, String filename, float scale) {
        System.out.println("Uploading model: " + modelId + " to host: " + host);
        System.out.println("Upload Model ConsumerKey: " + CONSUMER_KEY + " access_token: " + CONSUMER_SECRET);
        System.out.println("Upload Model AccessKey: " + ACCESS_TOKEN + " access_token: " + ACCESS_SECRET);
        RESTAPIReservation reserve = new RESTAPIReservationV1(host, CONSUMER_KEY, CONSUMER_SECRET, ACCESS_TOKEN, ACCESS_SECRET);
        reserve.setDebug(true);

        int retry_count = 3;
        int delay = 100;
        System.out.println("Retry Count: " + retry_count + " model length: " + model.length);
        Result result = null;

        while(retry_count > 0) {
            System.out.println("Sending model: " + filename);
            result = reserve.update(modelId,model, filename, scale,true,true);
            System.out.println("Upload Result: " + result);
            if (!(result instanceof ResultError)) {
                break;
            }

            retry_count--;

            try {
                Thread.sleep(delay);
                delay = 2 * delay;
            } catch(InterruptedException ie) {
                ie.printStackTrace();
            }
        }

        if (result instanceof ResultError) {
            System.out.println("Failed to upload model: " + result);
            return;
        }

        System.out.println("Final Upload Result: " + result);
    }

}
