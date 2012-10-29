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

package ringpopper;

// External Imports
import java.util.*;

// Local Imports
import abfab3d.creator.ui.*;
import abfab3d.creator.*;

public class UICreator {
    public static final void main(String[] args) {
        RingPopperKernel kernel = new RingPopperKernel();

System.out.println("Creating Java User Interface for imageeditor");
        JavaStandaloneUICreator uic = new JavaStandaloneUICreator();

        ArrayList<Step> steps = new ArrayList<Step>();
        steps.add(new Step(0, "Image", "Select your Image"));
        steps.add(new Step(1, "Size", "Select your size"));
        steps.add(new Step(2, "Engraving", "Select your text"));
        steps.add(new Step(3, "Material", "Select your Material"));
        steps.add(new Step(4, "Advanced", "Advanced Parameters"));

        uic.createInterface("ringpopper.ui", "Editor", "AbFab3D Ring Creator","src/java/ringpopper/ui", steps, new HashMap<String,String>(), kernel, new HashSet<String>());

System.out.println("Creating HTML User Interface for imageeditor");
        BasicHTMLUICreator uic2 = new BasicHTMLUICreator();

        uic2.createInterface("ringpopper.ui", "Editor", "AbFab3D Image Creator","src/html/ringpopper", steps, new HashMap<String,String>(), kernel, new HashSet<String>());

    }
}
