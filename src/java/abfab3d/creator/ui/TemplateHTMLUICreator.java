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
public class TemplateHTMLUICreator {
    /** The type of editors */
    private enum Editors {TEXTFIELD, TEXTAREA,COMBOBOX, FILE_DIALOG};

    private HashMap<Integer,String> indentCache;

    private GeometryKernel kernel;

    /** The number of steps */
    private List<Step> steps;

    public TemplateHTMLUICreator() {
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
    public void createInterface(String template, String kernelPath, String kernelName, String templateID,
        String dir, List<Step> steps, GeometryKernel kernel, Set<String> remove) {

        this.kernel = kernel;
        this.steps = new ArrayList<Step>();
        this.steps.addAll(steps);

        try {
            File f = new File(dir);

            if (!f.exists()) {
                f.mkdirs();
                return;
            }

            // TODO: Create path if needed
            FileOutputStream fos = new FileOutputStream(f.toString() + "/" + "index.html");
            BufferedOutputStream bos = new BufferedOutputStream(fos);

            Map<String,Parameter> params = kernel.getParams();


            // Fill in the following params
            // kernel, kernelName, dataEntry, getParameters, submit, templateID

            if (templateID == null) {
                templateID = "";
            }

            HashMap<String,String> template_params = new HashMap<String,String>();
            template_params.put("kernel", kernelPath);
            template_params.put("kernelName", kernelName);
            template_params.put("templateID", templateID);


            // Replace template vars with params

            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);

            ArrayList sorted_params = new ArrayList();
            sorted_params.addAll(params.values());

            java.util.Collections.sort(sorted_params);

            Iterator<Parameter> itr = sorted_params.iterator();

            int curr_step = -1;

            pw.println("<TABLE>");

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

                addParameterUI(pw, p);
            }

            pw.println("</TABLE>");

            pw.flush();

            template_params.put("dataEntry", sw.toString());

            sw = new StringWriter();
            pw = new PrintWriter(sw);

            itr = sorted_params.iterator();
            curr_step = -1;

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

                addParameterPull(pw, p);
            }

            pw.flush();

            template_params.put("getParameters", sw.toString());

            sw = new StringWriter();
            pw = new PrintWriter(sw);

            itr = sorted_params.iterator();
            curr_step = -1;

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

                addSubmit(pw, p);
            }

            pw.flush();

            template_params.put("submit", sw.toString());

            sw = new StringWriter();
            pw = new PrintWriter(sw);

            itr = sorted_params.iterator();
            curr_step = -1;

            addProcessX3D(pw,kernelPath,itr);

            pw.flush();

            template_params.put("3dGenerate", sw.toString());

            sw = new StringWriter();
            pw = new PrintWriter(sw);

            itr = sorted_params.iterator();
            curr_step = -1;

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

                addGlobalVar(pw, p);
            }

            pw.flush();

            template_params.put("globalVars", sw.toString());


            FileInputStream fis = new FileInputStream(template);
            String out = fillTemplate(fis, template_params);

            pw = new PrintWriter(bos);
            pw.println(out);

            pw.flush();
            fos.close();
        } catch(IOException ioe) {
            ioe.printStackTrace();
        }

        System.out.println("Done creating editor");
    }

    /**
     * Pull the paramter from the form to local vars.  Perform any transformation
     * on the variables necessary.
     *
     * @param ps The stream to print too
     * @param p The parameter
     */
    private void addGlobalVar(PrintWriter pw, Parameter p) {
        pw.print("var p_");
        pw.print(p.getName());
        pw.print(" = \"");
        pw.print(p.getDefaultValue());
        pw.println("\";");
    }

    /**
     * Pull the paramter from the form to local vars.  Perform any transformation
     * on the variables necessary.
     *
     * @param ps The stream to print too
     * @param p The parameter
     */
    private void addParameterPull(PrintWriter pw, Parameter p) {
        pw.print("p_");
        pw.print(p.getName());
        pw.print(" = ");
        pw.print("document.getElementById(\"");
        pw.print("p_");
        pw.print(p.getName());
        pw.println("\").value;");
    }

    /**
     * Add submit items
     *
     * @param ps The stream to print too
     * @param p The parameter
     */
    private void addSubmit(PrintWriter pw, Parameter p) {
        pw.print("p_");
        pw.print(p.getName());
        pw.print(" : ");
        pw.print("p_");
        pw.print(p.getName());
        pw.println(",");
    }

    private void addProcessX3D(PrintWriter pw, String kernelPath, Iterator<Parameter> params) {
    //var url = "/creator-kernels/${kernel}/3d/generate?p_fontStyle="+font+"&p_text="+encodeURIComponent(text)+"&p_material='White Strong & Flexible'";
        pw.print("var url = \"/creator-kernels/");
        pw.print(kernelPath);
        pw.print("/3d/generate?");

        boolean first = true;

        while(params.hasNext()) {
            Parameter param = params.next();

            if (first) {
                pw.print("p_");
                pw.print(param.getName());
                pw.print("=\"");
                first = false;
            } else {
                pw.print("+\"&p_");
                pw.print(param.getName());
                pw.print("=\"");
            }

            pw.print("+");
            pw.print("encodeURIComponent(");
            pw.print("p_");
            pw.print(param.getName());
            pw.print(")");
        }
    }

    /**
     * Add a user interface element for an item.
     *
     * @param ps The stream to print too
     * @param p The parameter
     */
    private void addParameterUI(PrintWriter pw, Parameter p) {
System.out.println("Adding param: " + p.getName());

        switch(getEditor(p)) {
            case COMBOBOX:
                pw.print("<TR><TD>");
                pw.print(p.getNameDesc());
                pw.print("</TD><TD>");
                pw.print("<SELECT name=\"");
                pw.print("p_");
                pw.print(p.getName());
                pw.print("\" id=\"p_");
                pw.print(p.getName());
                pw.print("\" selected=\"");
                pw.print(p.getDefaultValue());
                pw.print("\">");

                String[] vals = null;

                if (p.getDataType() == Parameter.DataType.BOOLEAN) {
                    vals = new String[] {"true", "false"};
                } else {
                    vals = p.getEnumValues();
                }

                for(int i=0; i < vals.length; i++) {
                    pw.print("<OPTION value=\"");
                    pw.print(vals[i]);
                    pw.print("\">");
                    pw.print(vals[i]);
                    pw.println("</OPTION>");
                }

                pw.println("</SELECT><TD/></TR>");
                break;
            case FILE_DIALOG:
/*
                if (p.getDefaultValue() == null) {
                    pw.println(indent(8) + "String dir = \".\";");
                } else {
                    pw.println(indent(8) + "String dir = \"" + p.getDefaultValue() + "\";");
                }
                pw.println(indent(8) + p.getName() + "Dialog = new JFileChooser(new File(dir));");
                pw.println(indent(8) + p.getName() + "Button = new JButton(\"Browse\");");
                pw.println(indent(8) + p.getName() + "Button.addActionListener(this);");
*/
                break;
            case TEXTAREA:
                pw.print("<TR><TD>");
                pw.print(p.getNameDesc());
                pw.print("</TD><TD>");
                pw.print("<TEXTAREA cols=\"20\" rows=\"2\" name=\"");
                pw.print("p_");
                pw.print(p.getName());
                pw.print("\" id=\"p_");
                pw.print(p.getName());
                pw.print("\">");
                pw.print(p.getDefaultValue());
                pw.println("</TEXTAREA>");
                pw.println("<TD/></TR>");
                break;
            default:
                pw.print("<TR><TD>");
                pw.print(p.getNameDesc());
                pw.print("</TD><TD>");
                pw.print("<INPUT type=\"text\" name=\"");
                pw.print("p_");
                pw.print(p.getName());
                pw.print("\" id=\"p_");
                pw.print(p.getName());
                pw.print("\" value=\"");
                pw.print(p.getDefaultValue());
                pw.println("\" />");
                pw.println("<TD/></TR>");
        }

/*
        pw.println(indent(8) + "getContentPane().add(" + p.getName() + "Editor);");

        // Determine third column content
        switch(getEditor(p)) {
            case FILE_DIALOG:
                pw.println(indent(8) + "getContentPane().add(" + p.getName() + "Button);");
                break;
            default:
                pw.println(indent(8) + "getContentPane().add(new JLabel(\"\"));");
        }

        pw.println();
*/
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

    /**
     * Fill in a template with values.  Template contains ${...} references to names
     *
     * @param templateStr The template to use
     * @param params The params to use
     */
    private String fillTemplate(InputStream templateStr, Map params) throws IOException {
        int idx1, idx2;
        int pos = 0;


        BufferedInputStream bis = new BufferedInputStream(templateStr);
        BufferedReader br = new BufferedReader(new InputStreamReader(bis));

        String st;
        StringBuilder bldr = new StringBuilder();

        while((st = br.readLine()) != null) {
            bldr.append(st);
            bldr.append("\n");
        }

        String template = bldr.toString();
        String param_name;
        String param;

        bldr.setLength(0);
        idx1 = template.indexOf("${", pos);
        while(idx1 > -1) {
            idx2 = template.indexOf("}", idx1);
            param_name = template.substring(idx1+2,idx2);
            param = (String) params.get(param_name);

            if (param != null) {
                bldr.append(template.substring(pos, idx1));
                bldr.append(param);
                pos = idx2+1;
            } else {
                bldr.append(template.substring(pos, idx1+2));
                pos = idx1+2;
            }

            idx1 = template.indexOf("${", pos);
        }

        bldr.append(template.substring(pos));

        return bldr.toString();
    }
}
