package shapeways.api.robocreator;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.CreateQueueRequest;
import com.amazonaws.services.sqs.model.QueueAttributeName;

import java.util.Collections;

/**
 * Task for creating SQS queues.
 *
 * @author Alan Hudson
 */
public class SQSCreateQueueTask implements Runnable {
    private AmazonSQS sqs;
    private String name;
    private Integer visibilityTimeout;
    private SQSQueueListener listener;

    public SQSCreateQueueTask(AmazonSQS sqs, String name, Integer visibilityTimeout, SQSQueueListener listener) {
        this.sqs = sqs;
        this.name = name;
        this.visibilityTimeout = visibilityTimeout;
        this.listener = listener;
    }

    public void run() {
        try {
            CreateQueueRequest createQueueRequest = new CreateQueueRequest(name).withAttributes(Collections.singletonMap(
                    QueueAttributeName.VisibilityTimeout.name(), String.valueOf(visibilityTimeout)));
            String url = sqs.createQueue(createQueueRequest).getQueueUrl();

            if (listener != null) {
                listener.queueCreated(name,url);
            }
        } catch(Exception e) {
            e.printStackTrace();

            if (listener != null) {
                listener.queueFailed(name, e);
            }
        }
    }
}
