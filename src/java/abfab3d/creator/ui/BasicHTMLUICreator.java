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

package abfab3d.creator.ui;

// External Imports
import java.util.*;
import java.io.*;

// Internal Imports
import abfab3d.creator.*;

/**
 * Create a user interface for an editor using with an HTML 2.0 interface
 * Expects to talk with a server-side hosted editor.
 *
 * @author Alan Hudson
 */
public class BasicHTMLUICreator {
    /** The type of editors */
    private enum Editors {TEXTFIELD, TEXTAREA,COMBOBOX, FILE_DIALOG};

    private HashMap<Integer,String> indentCache;

    private GeometryKernal kernel;

    /** The number of steps */
    private List<Step> steps;

    public BasicHTMLUICreator() {
        indentCache = new HashMap<Integer,String>();
    }

    /**
     * Create a user interface for a kernel.
     *
     * @param dir The directory to place the files
     * @param genParams Parameters for generation
     * @param kernel The kernel
     * @param remove The parameters to remove
     */
    public void createInterface(String packageName, String className, String title, String dir, List<Step> steps, Map<String,String> genParams, GeometryKernal kernel, Set<String> remove) {
        this.kernel = kernel;
        this.steps = new ArrayList<Step>();
        this.steps.addAll(steps);

        try {
            File f = new File(dir);

            if (!f.exists()) {
                System.out.println("Directory does not exist");
                return;
            }

            // TODO: Create path if needed
            FileOutputStream fos = new FileOutputStream(f.toString() + "/" + className + ".html");
            BufferedOutputStream bos = new BufferedOutputStream(fos);
            PrintStream ps = new PrintStream(bos);

            Map<String,Parameter> params = kernel.getParams();

            ps.print("<HTML><BODY><FORM name=");
            ps.print(className);
            ps.println("\" method=\"GET\" action=\"http://localhost:8080/hosted_creator/CreatorHost\" target = \"Right frame\">");

            ps.println("<input type=\"hidden\" name=\"creatorID\" value=\"ImageEditor\">");

            ArrayList sorted_params = new ArrayList();
            sorted_params.addAll(params.values());

            java.util.Collections.sort(sorted_params);

            Iterator<Parameter> itr = sorted_params.iterator();

            itr = sorted_params.iterator();
            int curr_step = -1;

            ps.println("<TABLE>");
            while(itr.hasNext()) {
                Parameter p = itr.next();
                if (remove.contains(p.getName())) {
                    continue;
                }

                if (curr_step != p.getStep()) {
                    // Add step line
                    curr_step = p.getStep();
                    Step step = steps.get(curr_step);

                    if (curr_step != 0) {
                        //ps.println("<HR />");
                    }
                }

                addParameterUI(ps, p);
            }
            ps.println("</TABLE>");
            ps.println("<INPUT TYPE=SUBMIT VALUE=\"Submit\">");
            ps.println("</FORM>");
            ps.println("</BODY>");
            ps.println("</HEAD>");

            ps.flush();
            fos.close();
        } catch(IOException ioe) {
            ioe.printStackTrace();
        }

        System.out.println("Done creating editor");
    }

    /**
     * Add a user interface element for an item.
     *
     * @param ps The stream to print too
     * @param p The parameter
     */
    private void addParameterUI(PrintStream ps, Parameter p) {
System.out.println("Adding param: " + p.getName());

        switch(getEditor(p)) {
            case COMBOBOX:
                ps.print("<TR><TD>");
                ps.print(p.getNameDesc());
                ps.print("</TD><TD>");
                ps.print("<SELECT name=\"");
                ps.print(p.getName());
                ps.print("\" selected=\"");
                ps.print(p.getDefaultValue());
                ps.print("\" \">");

                String[] vals = null;

                if (p.getDataType() == Parameter.DataType.BOOLEAN) {
                    vals = new String[] {"true", "false"};
                } else {
                    vals = p.getEnumValues();
                }

                for(int i=0; i < vals.length; i++) {
                    ps.print("<OPTION value=\"");
                    ps.print(vals[i]);
                    ps.print("\">");
                    ps.print(vals[i]);
                    ps.println("</OPTION>");
                }

                ps.println("</SELECT><TD/></TR>");
                break;
            case FILE_DIALOG:
/*
                if (p.getDefaultValue() == null) {
                    ps.println(indent(8) + "String dir = \".\";");
                } else {
                    ps.println(indent(8) + "String dir = \"" + p.getDefaultValue() + "\";");
                }
                ps.println(indent(8) + p.getName() + "Dialog = new JFileChooser(new File(dir));");
                ps.println(indent(8) + p.getName() + "Button = new JButton(\"Browse\");");
                ps.println(indent(8) + p.getName() + "Button.addActionListener(this);");
*/
                break;
            case TEXTAREA:
                ps.print("<TR><TD>");
                ps.print(p.getNameDesc());
                ps.print("</TD><TD>");
                ps.print("<TEXTAREA cols=\"20\" rows=\"2\" name=\"");
                ps.print(p.getName());
                ps.print("\">");
                ps.print(p.getDefaultValue());
                ps.println("</TEXTAREA>");
                ps.println("<TD/></TR>");
                break;
            default:
                ps.print("<TR><TD>");
                ps.print(p.getNameDesc());
                ps.print("</TD><TD>");
                ps.print("<INPUT type=\"text\" name=\"");
                ps.print(p.getName());
                ps.print("\" value=\"");
                ps.print(p.getDefaultValue());
                ps.println("\" />");
                ps.println("<TD/></TR>");
        }

/*
        ps.println(indent(8) + "getContentPane().add(" + p.getName() + "Editor);");

        // Determine third column content
        switch(getEditor(p)) {
            case FILE_DIALOG:
                ps.println(indent(8) + "getContentPane().add(" + p.getName() + "Button);");
                break;
            default:
                ps.println(indent(8) + "getContentPane().add(new JLabel(\"\"));");
        }

        ps.println();
*/
    }

    /**
     * Add user interface elements for Global Buttons
     *
     * @param ps The stream to print too
     * @param p The parameter
     */
    private void addGlobalButtons(PrintStream ps) {
        ps.println(indent(8) + "submitButton = new JButton(\"Submit\");");
        ps.println(indent(8) + "getContentPane().add(submitButton);");
        ps.println(indent(8) + "submitButton.addActionListener(this);");

        ps.println(indent(8) + "getContentPane().add(new JLabel(\"\"));");

        ps.println(indent(8) + "uploadButton = new JButton(\"Upload\");");
        ps.println(indent(8) + "getContentPane().add(uploadButton);");
        ps.println(indent(8) + "uploadButton.addActionListener(this);");
    }

    private void addActions(PrintStream ps, Map<String,Parameter> params, Set<String> remove) {

        // submit button
        ps.println(indent(4) + "public void actionPerformed(ActionEvent e) {");
        ps.println(indent(8) + "if (e.getSource() == submitButton) {");
        ps.println(indent(12) + "// Get all params to global string vars");

        Iterator<Parameter> itr = params.values().iterator();

        while(itr.hasNext()) {
            Parameter p = itr.next();
            if (remove.contains(p.getName())) {
                continue;
            }

            switch(getEditor(p)) {
                case TEXTFIELD:
                    ps.println(indent(12) + p.getName() + " = ((JTextField)" + p.getName() + "Editor).getText();");
                    break;
                case FILE_DIALOG:
                    ps.println(indent(12) + p.getName() + " = ((JTextField)" + p.getName() + "Editor).getText();");
                    break;
                case COMBOBOX:
                    ps.println(indent(12) + p.getName() + " = (String) ((JComboBox)" + p.getName() + "Editor).getSelectedItem();");
                    break;
                default:
                    System.out.println("Unhandled action for editor: " + getEditor(p));
            }

        }

        ps.println();

        // Create Kernal
        String class_name = kernel.getClass().getName();
        ps.println(indent(12) + class_name + " kernel = new " + class_name + "();");

        // Put params into a map

        ps.println(indent(12) + "HashMap<String,String> params = new HashMap<String,String>();");
        itr = params.values().iterator();

        while(itr.hasNext()) {
            Parameter p = itr.next();
            if (remove.contains(p.getName())) {
                continue;
            }

            ps.println(indent(12) + "params.put(\"" + p.getName() + "\", " + p.getName() + ");");
        }

        ps.println(indent(12) + "Map<String,Object> parsed_params = ParameterUtil.parseParams(kernel.getParams(), params);");

        // Generate Geometry
        ps.println(indent(12) + "try {");
        ps.println(indent(16) + "FileOutputStream fos = new FileOutputStream(\"out.x3db\");");
        ps.println(indent(16) + "BufferedOutputStream bos = new BufferedOutputStream(fos);");
        ps.println(indent(16) + "kernel.generate(parsed_params, bos);");
        ps.println(indent(16) + "fos.close();");
        ps.println(indent(12) + "} catch(IOException ioe) { ioe.printStackTrace(); }");
        ps.println(indent(12) + "System.out.println(\"Model Done\");");

        ps.println(indent(8) + "} else if (e.getSource() == uploadButton) {");
        ps.println(indent(12) + "System.out.println(\"Uploading Model\");");
        //ps.println(indent(8) + "}");


        itr = params.values().iterator();
        boolean more_elses = false;

        while(itr.hasNext()) {
            Parameter p = itr.next();
            if (remove.contains(p.getName())) {
                continue;
            }

            if (getEditor(p) == Editors.FILE_DIALOG) {
                ps.println(indent(8) + "} else if (e.getSource() == " + p.getName() + "Button) {");

                ps.println(indent(12) + "int returnVal = " + p.getName() + "Dialog" + ".showOpenDialog(this);");

                ps.println(indent(12) + "if (returnVal == JFileChooser.APPROVE_OPTION) {");
                ps.println(indent(16) + "File file = " + p.getName() + "Dialog" + ".getSelectedFile();");
                ps.println(indent(16) + " ((JTextField)" + p.getName() + "Editor).setText(file.toString());");
                ps.println(indent(12) + "}");
                more_elses = true;
            }
        }

        if (more_elses) {
            ps.println(indent(8) + "}");
        }

        ps.println(indent(4) + "}");
    }

    /**
     * Add global variables.
     */
    private void addGlobalVars(PrintStream ps, Map<String,Parameter> params, Set<String> remove) {
            Iterator<Parameter> itr = params.values().iterator();

        ps.println("JButton submitButton;");
        ps.println("JButton uploadButton;");

        while(itr.hasNext()) {
            Parameter p = itr.next();
            if (remove.contains(p.getName())) {
                continue;
            }

            ps.println(indent(4) + "/** " + p.getDesc() + " Field */");
            ps.println(indent(4) + "protected String " + p.getName() + ";");
            ps.println(indent(4) + "/** " + p.getDesc() + " Editor */");
            ps.println(indent(4) + "protected JComponent " + p.getName() + "Editor;");
            ps.println();

            switch(getEditor(p)) {
                case FILE_DIALOG:
                    ps.println(indent(4) + "protected JButton " + p.getName() + "Button;");
                    ps.println(indent(4) + "protected JFileChooser " + p.getName() + "Dialog;");
                    break;
            }
        }

        ps.println();
    }

    private String indent(int spaces) {
        Integer key = new Integer(spaces);

        String ret_val = indentCache.get(key);

        if (ret_val == null) {
            ret_val = "";
            for(int i=0; i < spaces; i++) {
                ret_val = ret_val + " ";
            }

            indentCache.put(key, ret_val);
        }

        return ret_val;
    }

    /**
     * Get the editor type to use.
     */
    private Editors getEditor(Parameter p) {
        if (p.getEditorType() == Parameter.EditorType.FILE_DIALOG) {
            return Editors.FILE_DIALOG;
        }

        if (p.getDataType() == Parameter.DataType.ENUM) {
            return Editors.COMBOBOX;
        }

        if (p.getDataType() == Parameter.DataType.BOOLEAN) {
            return Editors.COMBOBOX;
        }

        if (p.getEditorType() == Parameter.EditorType.TEXT_AREA) {
            return Editors.TEXTAREA;
        }

        return Editors.TEXTFIELD;
    }
}
