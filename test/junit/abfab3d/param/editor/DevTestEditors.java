package abfab3d.param.editor;

import abfab3d.param.*;
import java.awt.Point;
import java.awt.Dimension;


import javax.swing.*;

import abfab3d.datasources.Box;
import abfab3d.datasources.Sphere;
import abfab3d.datasources.Union;
import abfab3d.datasources.VolumePatterns;


import java.util.ArrayList;
import java.util.Vector;

import static java.awt.AWTEvent.WINDOW_EVENT_MASK;
import static abfab3d.core.Output.printf;

public class DevTestEditors extends JFrame implements ParamChangedListener {

    public DevTestEditors() {
        
        super("Test Parameter Editor");
        
        this.add(new JPanel());
        this.setSize(300,100);
        this.setLocation(40,40);
        
        JPopupMenu.setDefaultLightWeightPopupEnabled(false);        
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        this.setVisible(true);
    }

    void testUnion(){
        
        //ArrayList<Parameterizable> list = new ArrayList<>();
        //list.add();
        ParamPanel seditor = new ParamPanel(makeUnion());
        seditor.addParamChangedListener(this);
        seditor.setVisible(true);
        Point p = getLocation();
        Dimension dim = getSize();
        seditor.setLocation(p.x + dim.width, p.y);
        
    }

    void testGroups(){

        String groupP = "groupP";
        String groupQ = "groupQ";
        String groupPQ = "groupPQ";

        Parameter p0 = new IntParameter("p0", 0);p0.setGroup(groupP);
        Parameter p1 = new DoubleParameter("p1", 0);p1.setGroup(groupQ);
        Parameter p2 = new IntParameter("p2", 0);p2.setGroup(groupP);
        Parameter p3 = new DoubleParameter("p3", 0.);p3.setGroup(groupP);
        Parameter p4 = new DoubleParameter("p4", 0.);p4.setGroup(groupP);
        Parameter p5 = new DoubleParameter("p5", 0.);p5.setGroup(groupPQ);
        Parameter p6 = new DoubleParameter("p6", 0.);p6.setGroup(groupP);
        Parameter p7 = new DoubleParameter("p7", 0.);p7.setGroup(groupPQ);
        Parameter q0 = new IntParameter("q0", 3);q0.setGroup(groupQ);
        Parameter q1 = new DoubleParameter("q1", 0.);q1.setGroup(groupQ);
        Parameter q2 = new DoubleParameter("q2", 0.);q2.setGroup(groupQ);
        Parameter q3 = new DoubleParameter("q3", 0.);q3.setGroup(groupQ);
        Parameter q4 = new DoubleParameter("q4", 0.);q4.setGroup(groupPQ);
        Parameter q5 = new DoubleParameter("q5", 0.);q5.setGroup(groupQ);
        Parameter q6 = new IntParameter("q6", 7);q6.setGroup(groupPQ);
        Parameter q7 = new IntParameter("q7", 8);q7.setGroup(groupPQ);


        Parameter param[] = new Parameter[]{
            p0, p1, p2, p3, p4, p5, p6, p7, 
            q0, q1, q2, q3, q4, q5, q6, q7};
        
        Vector<ParamPanel.ParamGroup> groups = ParamPanel.makeGroups(param);
        printf("groups: %d\n", groups.size());
        for(int i = 0; i < groups.size(); i++){
            ParamPanel.ParamGroup group = groups.get(i);
            printf("grop[%d] = %s count: %d\n",i, group.name, group.param.size());
            for(int k = 0; k < group.param.size(); k++){
                printf("  par[%d] = %s\n",k, group.param.get(k).getName());
            }
        }
        ParamPanel seditor = new ParamPanel("groups", param);
        seditor.addParamChangedListener(this);
        seditor.setVisible(true);
        Point p = getLocation();
        Dimension dim = getSize();
        seditor.setLocation(p.x + dim.width, p.y);
        
    }

    void testSimple(){

        String groupP = "groupP";
        String groupQ = "groupQ";
        String groupPQ = "groupPQ";

        Parameter p0 = new IntParameter("p0", 15);p0.setGroup(groupP);
        Parameter p1 = new DoubleParameter("p1", 0.123456789);p1.setGroup(groupP);
        Parameter param[] = new Parameter[]{ p0, p1};
        
        Vector<ParamPanel.ParamGroup> groups = ParamPanel.makeGroups(param);
        printf("groups: %d\n", groups.size());
        for(int i = 0; i < groups.size(); i++){
            ParamPanel.ParamGroup group = groups.get(i);
            printf("grop[%d] = %s count: %d\n",i, group.name, group.param.size());
            for(int k = 0; k < group.param.size(); k++){
                printf("  par[%d] = %s\n",k, group.param.get(k).getName());
            }
        }
        ParamPanel seditor = new ParamPanel("groups", param);
        seditor.addParamChangedListener(this);
        seditor.setVisible(true);
        Point p = getLocation();
        Dimension dim = getSize();
        seditor.setLocation(p.x + dim.width, p.y);
        
    }
    
    @Override
        public void paramChanged(Parameter param) {
        printf("paramChanged: %s\n",param.getValue());
    }
    
    Parameterizable makeBox(){
        return new Box();
    }
    
    Parameterizable makeUnion(){
        return new Union(new Box(), new Sphere(), new VolumePatterns.Gyroid());
    }
    
    public static final void main(String[] args) {
               
        DevTestEditors tester = new DevTestEditors();
        //tester.testUnion();
        tester.testSimple();
        //tester.testGroups();

    }
}
