package shapeways.api.robocreator;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.DeleteMessageRequest;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * TODO: Add docs
 *
 * @author Alan Hudson
 */
public class ModelGeneratorRunner implements Runnable {
    private static final boolean DEBUG = false;

    private ModelGenerator modelGenerator;
    private Map params;
    private X3DEncodingType encoding;
    private byte[] model;
    private ModelUploader uploader;
    private String filename;
    private Integer modelId;
    private String receiptHandle;
    private AmazonSQS sqs;
    private String qurl;

    public ModelGeneratorRunner(ModelGenerator mg, Map params, Integer modelId, String filename,
                                X3DEncodingType encoding) {

        this(mg,params,null,modelId,filename,encoding,null,null,null);
    }

    public ModelGeneratorRunner(ModelGenerator mg, Map params, ModelUploader uploader, Integer modelId, String filename,
                                X3DEncodingType encoding) {

        this(mg,params,uploader,modelId,filename,encoding,null,null,null);
    }

    public ModelGeneratorRunner(ModelGenerator mg, Map params, ModelUploader uploader, Integer modelId, String filename,
                                X3DEncodingType encoding, AmazonSQS sqs, String qurl, String receiptHandle) {
        modelGenerator = mg;
        this.params = params;
        this.encoding = encoding;
        this.uploader = uploader;
        this.filename = filename;
        this.modelId = modelId;
        this.receiptHandle = receiptHandle;
        this.qurl = qurl;
        this.sqs = sqs;
    }

    @Override
    public void run() {
        if (DEBUG) {
            System.out.println("Generating model: " + params);
        }
        model = modelGenerator.generateModel(params,encoding);

        if (uploader != null) {
            if (DEBUG) {
                System.out.println("Uploading Model: " + modelId + " filename: " + filename);
            }
            uploader.uploadModel(modelId, model, filename, 1.0f);
        }

        if (sqs != null) {
            if (DEBUG) {
                System.out.println("Deleting SQS msg: " + sqs);
            }

            sqs.deleteMessage(new DeleteMessageRequest(qurl, receiptHandle));
        }
    }

    public byte[] getModel() {
        return model;
    }
}
