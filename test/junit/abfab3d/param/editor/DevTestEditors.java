package abfab3d.param.editor;

import abfab3d.param.*;

import javax.swing.*;
import javax.vecmath.Vector3d;

import abfab3d.datasources.Box;
import abfab3d.datasources.Sphere;
import abfab3d.datasources.Union;
import abfab3d.datasources.VolumePatterns;



import static abfab3d.util.Units.MM;
import static java.awt.AWTEvent.WINDOW_EVENT_MASK;
import static abfab3d.util.Output.printf;

public class DevTestEditors extends JFrame implements ParamChangedListener {

    public DevTestEditors() {
        super("Parameter Editor");
        
        int width = 100;
        int height = 100;

        enableEvents(WINDOW_EVENT_MASK);
        JPopupMenu.setDefaultLightWeightPopupEnabled(false);
        
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        
        setSize(width, height);
        setVisible(true);
        
        ParamPanel seditor = new ParamPanel(makeUnion());
        seditor.addParamChangedListener(this);
        seditor.setVisible(true);
        
    }
    
    @Override
        public void paramChanged(Parameter param) {
        printf("Val Changed: %s\n",param);
    }
    
    Parameterizable makeBox(){
        return new Box();
    }
    
    Parameterizable makeUnion(){
        return new Union(new Box(), new Sphere(), new VolumePatterns.Gyroid());
    }
    
    public static final void main(String[] args) {
        DevTestEditors tester = new DevTestEditors();
    }
}
