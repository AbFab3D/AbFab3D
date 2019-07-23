/*****************************************************************************
 *                        Shapeways, Inc Copyright (c) 2011
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package volumesculptor;

// External Imports
import java.util.*;

// Local Imports
import abfab3d.creator.ui.*;
import abfab3d.creator.*;

public class UICreator {
    public static final void main(String[] args) {
        VolumeSculptorKernel kernel = new VolumeSculptorKernel();

System.out.println("Creating Java User Interface for imageeditor");
        JavaStandaloneUICreator uic = new JavaStandaloneUICreator();

        ArrayList<Step> steps = new ArrayList<Step>();
        steps.add(new Step(0, "3D model", "Select 3D model"));
        steps.add(new Step(1, "3D Pattern", "Select Volume Patterns"));
        steps.add(new Step(2, "Material", "Select your Material"));
        steps.add(new Step(3, "Advanced", "Advanced Parameters"));
        steps.add(new Step(4, "Advanced2", "Advanced Parameters2"));

        uic.createInterface("volumesculptor.ui", "Editor", "AbFab3D Volume Sculptor","src/java/volumesculptor/ui", steps, new HashMap<String,String>(), kernel, new HashSet<String>());

System.out.println("Creating HTML User Interface for imageeditor");
        BasicHTMLUICreator uic2 = new BasicHTMLUICreator();

        uic2.createInterface("volumesculptor.ui", "Editor", "AbFab3D Volume Sculptor","src/html/volumesculptor", steps, new HashMap<String,String>(), kernel, new HashSet<String>());

    }
}
