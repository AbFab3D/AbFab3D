package shapeways.api.robocreator;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.SendMessageRequest;

/**
 * Add an item to a SQS queue.  This could wait a fair bit of IO so use multithreads to handle.
 *
 * @author Alan Hudson
 */
public class SQSEnqueueTask implements Runnable {
    private static final boolean DEBUG = true;

    private String queueUrl;
    private String msg;
    protected AmazonSQS sqs;

    public SQSEnqueueTask(AmazonSQS sqs, String queueUrl, String msg) {
        this.sqs = sqs;
        this.queueUrl = queueUrl;
        this.msg = msg;
    }

    public void run() {
        try {
            if (DEBUG) {
                System.out.println("Sending message: " + msg + " to que: " + queueUrl);
            }
            sqs.sendMessage(new SendMessageRequest(queueUrl, msg));
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}
