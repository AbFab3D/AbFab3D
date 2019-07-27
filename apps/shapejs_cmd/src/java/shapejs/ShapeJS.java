package shapejs;

import abfab3d.shapejs.ImageSetup;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;

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

    public void execute(ParamContainer params) {
        CommandBackend backend = null;

        if (params.getBackend() == null) {
            backend = new CPUBackend();
        } else {
            backend = new NamedBackend(params.getBackend());
        }

        String out = params.getOutput();
        String format = FilenameUtils.getExtension(out);

        printf("Execute: %s  out: %s\n",params.getCommand(),out);
        try (
            FileOutputStream fos = new FileOutputStream(out);
            BufferedOutputStream bos = new BufferedOutputStream(fos);
        ) {
            switch (params.getCommand()) {
                case renderImage:
                    backend.renderImage(params, bos, format);
                    break;
                case renderTriangle:
                    backend.renderTriangle(params, bos, format);
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
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
                } else if(arg.equals("-backend")) {
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
