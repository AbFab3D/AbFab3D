package shapejs;

import abfab3d.shapejs.*;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;

import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static abfab3d.core.Output.printf;

/**
 * Perform operations on ShapeJS scripts
 *
 * Usage patterns
 *
 * shapejs renderImage -project prj.zip -variant variants/foo.zip -output foo.png -width 256 -height 256 -backend viewer.ShapeJSBackend
 * shapejs renderTriangle -project prj.zip -variant variants/foo.zip -output foo.stl -meshErrorFactor 0.1
 * shapejs renderPolyjet -project prj.zip -variant variants/foo.zip -output foo.stl
 * shapejs exec -project prj.zip -variant variants/foo.zip
 */
public class ShapeJS {
    private List<String> libDirs = new ArrayList<>();

    public void setLibDirs(List<String> libs) {
        libDirs.clear();
        libDirs.addAll(libs);
    }

    public void execute(ParamContainer params) {
        ShapeJSExecutor backend = null;

        if (params.getBackend() == null) {
            backend = new ShapeJSExecutorCpu();
        } else {
            backend = new ShapeJSExecutorImpl(params.getBackend());
        }

        String out = params.getOutput();
        String format = FilenameUtils.getExtension(out);

        try (
            FileOutputStream fos = new FileOutputStream(out);
            BufferedOutputStream bos = new BufferedOutputStream(fos);
        ) {
            Scene scene = loadContent(params.getVariant(),params.getScript());
            switch (params.getCommand()) {
                case renderImage:
                    Camera camera = params.getCamera();
                    ImageSetup setup = params.getImageSetup();
                    backend.renderImage(scene,camera, setup,bos, format);
                    break;
                case renderTriangle:
                    backend.renderTriangle(scene, bos, format);
                    break;
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            backend.shutdown();
        }
    }

    /**
     * Load the content from the params.  Content can either be Project/Variant based or just a script.
     * If both are provided it uses the Project/Variant
     * @param variant The variant
     * @param script The script.  If both provided the variant takes precedence
     */
    public Scene loadContent(String variant, String script) throws IOException {

        if (variant == null) {
            if (script == null) throw new IllegalArgumentException("No variant or script content provided");

            return loadScript(script,null,false);
        }

        Variant currVariant = new Variant();
        try {
            currVariant.readDesign(libDirs, variant, true);

            Scene scene = currVariant.getScene();

            return scene;
        } catch(NotCachedException nce) {
            nce.printStackTrace();
        } catch(InvalidScriptException ise) {
            ise.printStackTrace();
        }

        throw new IllegalArgumentException("Invalid content");
    }

    /**
     * Load a script into a Scene object.
     * @param scriptPath The path to the script
     * @param params The initial params
     * @param sandboxed Whether to sandbox for security
     * @return The Scene
     * @throws IOException
     */
    private Scene loadScript(String scriptPath, Map<String,Object> params, boolean sandboxed) throws IOException {
        String baseDir = null;

        ScriptManager sm = ScriptManager.getInstance();

        String jobID = UUID.randomUUID().toString();

        String script = IOUtils.toString(new FileInputStream(scriptPath));

        ScriptResources sr = sm.prepareScript(jobID, baseDir, script, params, sandboxed);

        sm.executeScript(sr);

        Scene scene = (Scene) sr.evaluatedScript.getResult();

        return scene;
    }

    public static final void main(String[] args) {
        ShapeJS runner = new ShapeJS();

        ParamContainer params = new ParamContainer();
        ImageSetup imageSetup = new ImageSetup();
        params.setImageSetup(imageSetup);

        try {
            params.setCommand(ParamContainer.Commands.valueOf(args[0]));

            for(int i = 1; i < args.length; i++){
                String arg = args[i];

                if(arg.charAt(0) != '-'){
                    printf("invalid key:%s\n",arg);
                }

                if(arg.equals("-project")) {
                    params.setProject(args[++i]);
                } else if(arg.equals("-variant")){
                    params.setVariant(args[++i]);
                } else if(arg.equals("-script")){
                    params.setScript(args[++i]);
                } else if(arg.equals("-output")){
                    params.setOutput(args[++i]);
                } else if(arg.equals("-width")){
                    imageSetup.setWidth(Integer.parseInt(args[++i]));
                } else if(arg.equals("-height")) {
                    imageSetup.setHeight(Integer.parseInt(args[++i]));
                } else if(arg.equals("-impl")) {
                        params.setBackend(args[++i]);
                } else {

                    System.out.println("Unknown parameter: " + arg);
                    System.exit(-1);
                }
            }
        } catch(Exception e){
            e.printStackTrace(System.out);
            System.exit(-1);
        }

        runner.execute(params);
    }
}
