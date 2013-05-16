package shapeways.api.robocreator;

import com.amazonaws.services.sqs.model.Message;

import java.util.List;

/**
 * TODO: Add docs
 *
 * @author Alan Hudson
 */
public interface SQSQueueListener {
    public void queueCreated(String name, String url);
    public void queueFailed(String name, Exception e);
    public void messagesReceived(List<Message> messages);
}
