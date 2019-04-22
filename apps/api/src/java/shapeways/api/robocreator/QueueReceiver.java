package shapeways.api.robocreator;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.google.gson.Gson;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Listens to a queue for new messages.
 *
 * @author Alan Hudson
 */
public class QueueReceiver extends Thread {
    private static final boolean DEBUG = false;

    /** Polling frequency to SQS.  A per message cost is incurred so don't make too small */
    private int pollFrequency;
    private int threads;
    private String queueUrl;
    private volatile boolean terminate;
    private AmazonSQS sqs;
    private ThreadPoolExecutor threadPool;
    private SQSQueueListener listener;

    public QueueReceiver(int threads, AmazonSQS sqs, String queueUrl, int pollFrequency, ThreadPoolExecutor threadPool, SQSQueueListener listener) {
        this.threads = threads;
        this.queueUrl = queueUrl;
        this.sqs = sqs;
        this.pollFrequency = pollFrequency;
        this.threadPool = threadPool;
        this.listener = listener;

        terminate = false;
    }

    public void run() {
        if (DEBUG) {
            System.out.println("Starting listening for SQS messages: " + queueUrl);
        }

        while(!terminate) {

            int count = threads - threadPool.getActiveCount();

            if (count < 0) {
                try {
                    Thread.sleep(100);
                } catch(InterruptedException ie) {}

                continue;
            }

            ReceiveMessageRequest receiveMessageRequest = new ReceiveMessageRequest(queueUrl);
            receiveMessageRequest.setMaxNumberOfMessages(count);

            List<Message> messages = sqs.receiveMessage(receiveMessageRequest).getMessages();


            if (messages.size() > 0) {
                if (DEBUG) {
                    System.out.println("Received messages: " + messages.size());
                }

                listener.messagesReceived(messages);
            }

            try {
                Thread.sleep(pollFrequency);
            } catch(InterruptedException ie) {}
        }
    }

    public void setTerminate(boolean val) {
        terminate = val;
    }
}
