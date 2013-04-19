package shapeways.api.robocreator;

/**
 * Task for keeping SQS queues alive.  A queue must be accessed once every 30 days or it may be deleted.  Since we
 * expect some kernels will not be accessed very often this task will run each week and ping GetQueueAttributes to
 * keep it alive.
 *
 * @author Alan Hudson
 */
public class SQSKeepAliveTask implements Runnable {
    public void run() {

    }
}
