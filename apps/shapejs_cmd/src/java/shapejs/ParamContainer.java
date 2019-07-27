package shapejs;

import abfab3d.shapejs.*;

import javax.vecmath.Matrix4d;
import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3f;

public class ParamContainer {
    public enum Commands {renderImage,renderTriangle,renderPolyJet,exec};
    private static final int DEFAULT_WIDTH = 512;
    private static final int DEFAULT_HEIGHT = 512;

    private Commands command;
    private String project;
    private String variant;
    private String script;
    private String output;
    private String backend;

    // RenderImage params
    private ImageSetup imageSetup;
    private Camera camera;
    private Matrix4d objectMatrix;

    // RenderTriangle params
    private double meshErrorFactor = Scene.DEFAULT_ERROR_FACTOR;
    private double meshSmoothingWidth = Scene.DEFAULT_SMOOTHING_WIDTH;
    private int maxPartsCount = Scene.DEFAULT_MAX_PARTS_COUNT;

    public ParamContainer() {
        // Setup a sensible default values

        Matrix4f identity = new Matrix4f();
        identity.setIdentity();

//        imageSetup = new ImageSetup(DEFAULT_WIDTH, DEFAULT_HEIGHT, identity, ImageSetup.IMAGE_JPEG, 0.5f, AntiAliasingType.NONE, false, 0f, 1);
        imageSetup = new ImageSetup(DEFAULT_WIDTH, DEFAULT_HEIGHT, identity, ImageSetup.IMAGE_JPEG, 1.0f, AntiAliasingType.SUPER_3X3, true, 1f, 1);

        Matrix4f view = getView(-6);
        camera = new MatrixCamera(view);
    }

    public Commands getCommand() {
        return command;
    }

    public void setCommand(Commands command) {
        this.command = command;
    }

    public String getProject() {
        return project;
    }

    public void setProject(String project) {
        this.project = project;
    }

    public String getVariant() {
        return variant;
    }

    public void setVariant(String variant) {
        this.variant = variant;
    }

    public String getScript() {
        return script;
    }

    public void setScript(String script) {
        this.script = script;
    }

    public String getOutput() {
        return output;
    }

    public void setOutput(String output) {
        this.output = output;
    }

    public double getMeshErrorFactor() {
        return meshErrorFactor;
    }

    public void setMeshErrorFactor(double meshErrorFactor) {
        this.meshErrorFactor = meshErrorFactor;
    }

    public double getMeshSmoothingWidth() {
        return meshSmoothingWidth;
    }

    public void setMeshSmoothingWidth(double meshSmoothingWidth) {
        this.meshSmoothingWidth = meshSmoothingWidth;
    }

    public int getMaxPartsCount() {
        return maxPartsCount;
    }

    public void setMaxPartsCount(int maxPartsCount) {
        this.maxPartsCount = maxPartsCount;
    }

    public ImageSetup getImageSetup() {
        return imageSetup;
    }

    public void setImageSetup(ImageSetup imageSetup) {
        this.imageSetup = imageSetup;
    }

    public Camera getCamera() {
        return camera;
    }

    public void setCamera(Camera camera) {
        this.camera = camera;
    }

    public Matrix4d getObjectMatrix() {
        return objectMatrix;
    }

    public void setObjectMatrix(Matrix4d objectMatrix) {
        this.objectMatrix = objectMatrix;
    }

    public String getBackend() {
        return backend;
    }

    public void setBackend(String backend) {
        this.backend = backend;
    }

    public static Matrix4f getView() {
        return getView(-3);
    }

    public static Matrix4f getView(double pos) {
        float[] DEFAULT_TRANS = new float[]{0, 0, (float)pos};
        float z = DEFAULT_TRANS[2];
        float rotx = 0;
        float roty = 0;

        Vector3f trans = new Vector3f();
        Matrix4f tmat = new Matrix4f();
        Matrix4f rxmat = new Matrix4f();
        Matrix4f rymat = new Matrix4f();

        trans.z = z;
        tmat.set(trans, 1.0f);

        rxmat.rotX(rotx);
        rymat.rotY(roty);

        Matrix4f mat = new Matrix4f();
        mat.mul(tmat, rxmat);
        mat.mul(rymat);

        return mat;
    }
}
