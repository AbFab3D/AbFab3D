/*****************************************************************************
 *                        Shapeways, Inc Copyright (c) 2013
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package doclet;

import com.sun.javadoc.*;
import com.sun.tools.doclets.formats.html.*;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.util.*;

/**
 * Generate the ShapeJS reference guide.  The guide is a combination of manual pages and auto-generated content
 * from the Java source files.
 *
 * @author Alan Hudson
 */
public class RefGuideDoclet {
    private static final int MAX_LINES = 1200;
    private static final String REF_GUIDE_DIR = "docs/refguide";
    private static final String USER_GUIDE = "apps/volumesculptor/docs/manual/overview.html";
    private static final HashMap<String,String> packageTOCName;
    private static HashSet<String> ignoreMethods = new HashSet();


    static {
        packageTOCName = new HashMap<String,String>();
        packageTOCName.put("abfab3d.datasources","Data Sources");
        packageTOCName.put("abfab3d.transforms","Transformations");
    }

    public RefGuideDoclet() {
        super();
    }


    public static boolean start(RootDoc root) {

        boolean generateParts = isGenerateParts(root.options());
        int partNum = 1;

        System.out.printf("Generate parts: %b\n",generateParts);
        File dir = new File(REF_GUIDE_DIR);
        dir.mkdirs();

        FileOutputStream fos = null;
        BufferedOutputStream bos = null;
        LineCountWriter lcw = null;
        PrintWriter pw = null;


        List<Heading> toc_list = new ArrayList<Heading>();
        Map<String, Heading> toc_map = new HashMap<String, Heading>();
        try {
            if (generateParts) {
                fos = new FileOutputStream(REF_GUIDE_DIR + "/index_part_" + partNum + ".html");
            } else {
                fos = new FileOutputStream(REF_GUIDE_DIR + "/index.html");
            }
            bos = new BufferedOutputStream(fos);
            if (generateParts) {
                lcw = new LineCountWriter(bos);
                pw = new PrintWriter(lcw);
            } else {
                pw = new PrintWriter(bos);
            }
            if (!generateParts) writePreamble(pw);

            List<List<Heading>> headings = getHeadings(USER_GUIDE,1);
            System.out.println("Headings:");
            Heading user_guide = new Heading("User Guide","User Guide");

            int level = 0;
            toc_list.add(user_guide);
            for(List<Heading> list : headings) {
                for(Heading h : list) {
                    System.out.println("ID: " + h.getId() + " Text: " + h.getText());
                    user_guide.add(h);
                }
                level++;
            }


            for (ClassDoc classd : root.classes()) {
                if (classd.isAbstract() || classd.isEnum() || classd.isEnumConstant()) {
                    continue;
                }

                PackageDoc pack = classd.containingPackage();
                String pname = pack.name();

                String display = packageTOCName.get(pname);
                if (display != null) {
                    pname = display;
                }
                System.out.println("package name:" + pname);
                Heading classes = toc_map.get(pname);
                if (classes == null) {
                    classes = new Heading(pname,pname);
                    toc_map.put(pname, classes);

                    toc_list.add(classes);
                }
                classes.add(new Heading(classd.name(),classd.name()));
            }


            writeTOC(pw, toc_list);


            String current_package = null;

            pw.println("<div class=\"span-9 last right\">");
            writeStaticFile(pw, USER_GUIDE);

            for (ClassDoc classd : root.classes()) {
                if (classd.isAbstract() || classd.isEnum() || classd.isEnumConstant()) {
                    continue;
                }

                if (ignoreMethods.size() == 0) {
                    initIgnoreMethods(classd.findClass("Object"));
                }

                PackageDoc pack = classd.containingPackage();
                String pname = pack.name();

                copyDocFiles(pname);
                String display = packageTOCName.get(pname);
                if (display != null) {
                    pname = display;
                }

                if (current_package == null || !current_package.equals(pname)) {
                    if (current_package != null) {
                        pw.println("</div>");
                    }

                    pw.println("<div class=\"domain-container\" id=\"" + pname + "\">");
                    pw.println("<div class=\"api-domain-header\">" + pname + "</div>");
                    pw.println("<div class=\"api-domain-desc\" ></div>");
                }

                current_package = display;

                pw.println("   <div class=\"domain-endpoint\" id=\"" + classd.name() + "\">");
                pw.println("<div class=\"api-endpoint-header\">" + classd.name() + "</div>");

                pw.println("<div class=\"api-endpoint-desc\">");

                String upd_comment = updateURL(classd.commentText(), pack.name().replace(".","/") + "/" + "doc-files");
                pw.println(upd_comment);
                pw.println("</div>");

                ConstructorDoc[] constructors = classd.constructors();
                System.out.println(classd);
                System.out.println("Constructors:");

                for(ConstructorDoc cd : constructors) {
                    pw.println("<div class=\"api-endpoint-parameters\">");
                    StringBuilder sb = new StringBuilder();
                    sb.append(cd.name());
                    sb.append("(");
                    Parameter[] params = cd.parameters();
                    for(int i=0; i < params.length; i++) {
                        Parameter param = params[i];
                        System.out.println("      Param: " + param.name() + " type: " + param.type());
                        sb.append(param.name());
                        if (i != params.length - 1) {
                            sb.append(",");
                        }
                    }
                    sb.append(")\n");
                    if (cd.commentText() != null) {
                        // TODO: make a different css style?
                        sb.append("<div class=\"api-endpoint-desc\">");
                        sb.append(cd.commentText());
                        sb.append("</div>");
                    }
                    pw.println(sb.toString());

                    AnnotationDesc[] annots = cd.annotations();
                    System.out.println(java.util.Arrays.toString(annots));

                    ParamTag[] tags = cd.paramTags();

                    pw.println("   <table class=\"api-endpoint-parameters-table\">");
                    for(int i=0; i < params.length; i++) {
                        /*
                        <tr class="api-endpoint-parameters-table-row">
                        <td class="api-endpoint-parameters-table-name-col">x</td>
                        <td class="api-endpoint-parameters-table-type-col">double</td>
                        <td class="api-endpoint-parameters-table-desc-col">
                                The center x</td>
                        </tr>
                        */
                        Parameter param = params[i];
                        System.out.println("      Param: " + param.name() + " type: " + param.type());
                        pw.println("      <tr class=\"api-endpoint-parameters-table-row\">");
                        pw.println("         <td class=\"api-endpoint-parameters-table-name-col\">" + param.typeName() + " " + param.name()+ "</td>");
                        //pw.println("         <td class=\"api-endpoint-parameters-table-type-col\">" + param.typeName() + "</td>");
                        pw.println("         <td class=\"api-endpoint-parameters-table-desc-col\">");
                        ParamTag comment = getComment(tags,param.name());
                        if (comment != null) {
                            pw.println(comment.parameterComment());
                        }
                        pw.println("         </td>");
                        pw.println("      </tr>");
                    }

                    pw.println("</table>");
                    pw.println("</div>");
                }

                System.out.println("Methods:");

                for(MethodDoc method : classd.methods()) {
                    writeMethod(pw, method, classd);
                    if (generateParts) {
                        if (lcw.getCount() >= MAX_LINES) {
                            partNum++;
                            if (pw != null) pw.close();
                            if (bos != null) bos.close();
                            if (fos != null) fos.close();

                            fos = new FileOutputStream(REF_GUIDE_DIR + "/index_part_" + partNum + ".html");
                            bos = new BufferedOutputStream(fos);
                            lcw = new LineCountWriter(bos);
                            pw = new PrintWriter(lcw);
                        }
                    }
                }


                ClassDoc parent = classd.superclass();
                while(parent != null) {
                    System.out.println("Parent: " + parent);
                    System.out.println("Parent is: " + parent + " methods: " + parent.methods().length);
                    for(MethodDoc method : parent.methods()) {
                        writeMethod(pw, method, classd);
                        if (generateParts) {
                            if (lcw.getCount() >= MAX_LINES) {
                                partNum++;
                                if (pw != null) pw.close();
                                if (bos != null) bos.close();
                                if (fos != null) fos.close();

                                fos = new FileOutputStream(REF_GUIDE_DIR + "/index_part_" + partNum + ".html");
                                bos = new BufferedOutputStream(fos);
                                lcw = new LineCountWriter(bos);
                                pw = new PrintWriter(lcw);
                            }
                        }
                    }
                    parent = parent.superclass();

                    if (parent != null && parent.name().equals("Object")) {
                        break;
                    }
                }

                pw.println("   </div>");    // domain endpoint
            }

            pw.println("</div>");
            if (!generateParts) writePostamble(pw);

            if (generateParts) {
                if (pw != null) pw.close();
                if (bos != null) bos.close();
                if (fos != null) fos.close();

                for (int i = 1; i < partNum + 1; i++) {
                    String file = REF_GUIDE_DIR + "/index_part_" + i + ".html";
                    System.out.printf("Replacing doc-files reference: %s\n",file);
                    String content = IOUtils.toString(new FileInputStream(file));
                    content = content.replaceAll("abfab3d/datasources/doc-files/", "/rrstatic/img/shapejs/");
                    content = content.replaceAll("abfab3d/transforms/doc-files/", "/rrstatic/img/shapejs/");
                    IOUtils.write(content, new FileOutputStream(file));
                }
            }
        } catch(IOException ioe) {
            ioe.printStackTrace();
        } finally {
            try {
                if (pw != null) pw.close();
                if (bos != null) bos.close();
                if (fos != null) fos.close();
            } catch(IOException ioe) {
                ioe.printStackTrace();
            }
        }
        return true;
    }

    /**
     * Add methods to the ignore list.
     * @param obj The class obj
     */
    private static void initIgnoreMethods(ClassDoc obj) {
        for(MethodDoc method : obj.methods()) {
            ignoreMethods.add(method.name());
        }
    }

    private static void writeMethod(PrintWriter pw, MethodDoc method, ClassDoc classd) {
        if (ignoreMethods.contains(method.name())) {
            return;
        }
        Tag[] tags = method.tags("noRefGuide");

        System.out.println("WriteMethod: " + method.name() + " tags: " + java.util.Arrays.toString(tags));
        if (tags != null && tags.length > 0) {
            System.out.println("Skipping method: " + classd.name() + "." + method.name());
            return;
        }

        pw.println("<div class=\"api-endpoint-parameters\">");
        StringBuilder sb = new StringBuilder();
        sb.append(method.name());
        sb.append("(");
        Parameter[] params = method.parameters();
        for(int i=0; i < params.length; i++) {
            Parameter param = params[i];
            System.out.println("      Param: " + param.name() + " type: " + param.type());
            sb.append(param.name());
            if (i != params.length - 1) {
                sb.append(",");
            }
        }
        sb.append(")\n");
        if (method.commentText() != null) {
            // TODO: add new css style?
            sb.append("<div class=\"api-endpoint-desc\">");
            sb.append(method.commentText());
            sb.append("</div>");
        }
        pw.println(sb.toString());
        System.out.println("Comment: " + method.commentText());

        System.out.println("Printing method annots");
        AnnotationDesc[] annots = method.annotations();
        System.out.println(java.util.Arrays.toString(annots));

        ParamTag[] ptags = method.paramTags();

        pw.println("   <table class=\"api-endpoint-parameters-table\">");
        for(int i=0; i < params.length; i++) {
                    /*
                    <tr class="api-endpoint-parameters-table-row">
                    <td class="api-endpoint-parameters-table-name-col">x</td>
                    <td class="api-endpoint-parameters-table-type-col">double</td>
                    <td class="api-endpoint-parameters-table-desc-col">
                            The center x</td>
                    </tr>
                    */
            Parameter param = params[i];
            System.out.println("      Param: " + param.name() + " type: " + param.type());
            pw.println("      <tr class=\"api-endpoint-parameters-table-row\">");
            pw.println("         <td class=\"api-endpoint-parameters-table-name-col\">" + param.typeName() + " " + param.name()+ "</td>");
/*
                    pw.println("         <td class=\"api-endpoint-parameters-table-name-col\">" + param.name()+ "</td>");
                    pw.println("         <td class=\"api-endpoint-parameters-table-type-col\">" + param.typeName() + "</td>");
*/
            pw.println("         <td class=\"api-endpoint-parameters-table-desc-col\">");
            ParamTag mcomment = getComment(ptags,param.name());
            if (mcomment != null) {
                pw.println(mcomment.parameterComment());
            }
            pw.println("         </td>");
            pw.println("      </tr>");
        }

        pw.println("</table>");
        pw.println("</div>");

        System.out.println("   " + method);
    }

    /**
     * Write a static file into the stream.  Only write the BODY content.
     * @param pw
     * @param file
     * @throws IOException
     */
    private static void writeStaticFile(PrintWriter pw, String file) throws IOException {
        String st = FileUtils.readFileToString(new File(file));

        int s_idx = st.indexOf("<BODY>");
        if (s_idx == -1) {
            s_idx = st.indexOf("<body");
        }
        int e_idx = st.indexOf("</BODY>");
        if (e_idx == -1) {
            e_idx = st.indexOf("</body");
        }

        if (s_idx != -1 && e_idx != -1) {
            st = st.substring(s_idx + 6, e_idx);
        }

        pw.println("<!-- Begin static file: " + file + " -->");
        // Strip out
        pw.println(st);
        pw.println("<!-- End static file: " + file + " -->");
    }

    /**
     * Get the headings out of an HTML file.  This is rather dodgy but I don't want to use a full HTML parser
     * @param file
     * @param maxLevels The maximum levels we support
     * @return Each level of headings.  ie with H2 and H3 it would be [3][n] array.
     */
    private static List<List<Heading>> getHeadings(String file, int maxLevels) throws IOException {
        List<List<Heading>> ret_val = new ArrayList<List<Heading>>();
        String st = FileUtils.readFileToString(new File(file));

        for(int i=0; i < 5; i++) {
            int level = i+1;
            ArrayList<Heading> list = new ArrayList<Heading>();
            // replace all lowercase with upper for easier parsing
            st = st.replace("<h" + level,"<H" + level);
            st = st.replace("</h" + level,"</H" + level);
            int pos = 0;
            int idx = st.indexOf("<H"+level,pos);

            System.out.println("Initial idx for: " + ("<H" + level) + " is: " + idx);
            while(idx != -1) {
                String id = null;
                int b_idx = st.indexOf(">",idx);  // find end of H start tag
                int e_idx = st.indexOf("</H",idx+4);  // find begin of H end tag

                System.out.println("Start of H: " + b_idx + " end: " + e_idx + " Tag: " + st.substring(b_idx+1,e_idx));

                int id_idx = st.indexOf("id=",idx);
                System.out.println("ID index: " + id_idx);
                if (id_idx != -1 && id_idx < e_idx) {
                    int qs_idx = st.indexOf("\"",id_idx);  // assume attributes use "
                    int qe_idx = st.indexOf("\"",qs_idx+1);

                    System.out.println("end of H tag: " + e_idx + " qs: " + qs_idx + " qe: " + qe_idx);
                    if (qe_idx > -1 && qe_idx < e_idx) {
                        id = st.substring(qs_idx+1,qe_idx);
                    } else {
                        id = null;
                    }
                }
                String heading = st.substring(b_idx+1,e_idx);
                if (id != null) {
                    list.add(new Heading(id,heading));
                }
                System.out.println("Heading: " + heading + " id: " + id + " level: " + (i+1));
                pos = e_idx+1;
                idx = st.indexOf("<H" + level,pos);

            }

            if (list.size() > 0) {
                ret_val.add(list);

                if (ret_val.size() == maxLevels) {
                    return ret_val;
                }
            }
        }

        return ret_val;
    }


    /**
     * Update any doc-file urls to package/doc-files form
     * @param src The src text
     * @param path
     * @return
     */
    private static String updateURL(String src, String path) {
        String replace = "src=\"" + path;
        // search for src="doc-files" attributes
        String ret_val = src.replace("src=\"doc-files", replace);

        System.out.println("Update URL: " + src + " --> " + ret_val);
        return ret_val;
    }

    private static void copyDocFiles(String pack) {
        System.out.println("Copy dir for: " + pack);
        String pdir = pack.replace(".",File.separator);
        File src_dir = new File("src/java/" + pdir + File.separator + "doc-files");
        System.out.println("Src dir is: " + src_dir);
        File[] files = src_dir.listFiles();

        if (files != null) {
            System.out.println("Doc files: " + files.length);
            File dest_dir = new File(REF_GUIDE_DIR + File.separator + pdir + File.separator + "doc-files");
            System.out.println("Dest Dir: " + dest_dir);
            dest_dir.mkdirs();
            try {
                FileUtils.copyDirectory(src_dir,dest_dir);
            } catch(IOException ioe) {
                ioe.printStackTrace();
            }
        }
    }

    private static ParamTag getComment(ParamTag[] tags, String name) {
        for(int i=0; i < tags.length; i++) {
            if (tags[i].parameterName().equals(name)) {
                return tags[i];
            }
        }

        return null;
    }

    public static void writeTOC(PrintWriter pw,List<Heading> package_list) {
        pw.println("<div class=\"span-3\">");
        pw.println("<div class=\"api-toc\" id=\"toc\">");

        for(Heading heading : package_list) {
            String id = heading.getId();
            pw.println("<div class=\"api-toc-domain-container\" id=\"" + id + "_toc\">");
            pw.println("   <div class=\"api-toc-domain-name\"><a href=\"#" + id + "\">" + id + "</a></div>");

            List<Heading> children = heading.getChildren();
            System.out.println("Heading has: " + children.size());
            for(Heading h2 : children) {
                pw.println("   <div class=\"api-toc-endpoint\" id=\"" + h2.getId() + "_toc\"><a href=\"#" + h2.getId() + "\">" + h2.getText() + "</a></div>");
            }
            pw.println("</div>");
        }
        pw.println("</div></div>");
    }

    private static void writePreamble(PrintWriter pw) {

        String txt = "<!DOCTYPE html>\n" +
                "\n" +
                "<html>\n" +
                "<head>\n" +
                "\n" +
                "    <meta charset=\"utf-8\">\n" +
                "    <meta name=\"Author\" content=\"Shapeways Inc.\" />\n" +
                "    <meta name=\"viewport\" content=\"width=1024\" />\n" +
                "    <title>ShapeJS Developer Documentation</title>\n" +
                "    <link rel=\"shortcut icon\" href=\"/favicon.ico?tag=2013080701\" />\n" +
                "    <link rel=\"icon\" href=\"/favicon.ico\" type=\"image/png\"/>\n" +
                "    <meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\"/>\n" +
                "\n" +
                "    <link rel=\"stylesheet\" type=\"text/css\" href=\"https://static1.sw-cdn.net/rrstatic/stylesheets/screen.css?tag=2013080701\">\n" +
                "    <link rel=\"stylesheet\" type=\"text/css\" href=\"https://static1.sw-cdn.net/rrstatic/stylesheets/developers.css?tag=2013080701\">\n" +
                "    <link rel=\"stylesheet\" type=\"text/css\" href=\"https://static1.sw-cdn.net/rrstatic/stylesheets/styleguide.css?tag=2013080701\">\n" +
                "\n" +
                "    <script type=\"text/javascript\">\n" +
                "        $(document).ready(function() {\n" +
                "            prettyPrint();\n" +
                "        });\n" +
                "\n" +
                "\n" +
                "\n" +
                "    </script>\n" +
                "</head>\n" +
                "<body class=\"developers\" data-spy=\"scroll\" data-target=\"#toc\" data-offset=\"10\">\n" +
                "\n" +
                "<div class=\"container\">\n" +
                "\n" +
                "<div class=\"span-12 last\">\n" +
                "<div class=\"span-12 last\">\n" +
                "    <div class=\"section\">\n" +
                "        <h1>ShapeJS Developer Documentation</h1>\n" +
                "        <p>\n" +
                "            Browse the ShapeJS Documentation below." +
                "        </p>\n" +
                "    </div>  </div>\n";

        pw.println(txt);
    }

    private static void writePostamble(PrintWriter pw) {
        String txt = "</body>\n" +
                "</html>";

        pw.println(txt);
    }

    public static int optionLength(String option) {
        System.out.println("OL called: " + option);
        if (option.equals("-generateParts")) {
            return 2;
        }

        return HtmlDoclet.optionLength(option);
    }

    private static boolean isGenerateParts(String[][] options) {
        String genParts = null;
        for (int i = 0; i < options.length; i++) {
            String[] opt = options[i];
            if (opt[0].equals("-generateParts")) {
                genParts = opt[1];
            }
        }

        return Boolean.parseBoolean(genParts);
    }

    public static boolean validOptions(String[][] options,
                                       DocErrorReporter reporter) {

        System.out.println("VO called");

        for(int i=0; i < options.length; i++) {
            String[] opt = options[i];
            if(opt[0].equals("-generateParts")) {
                return true;
            }
        }

        return HtmlDoclet.validOptions(options, reporter);
    }

    public static LanguageVersion languageVersion() {
        return HtmlDoclet.languageVersion();
    }
}


