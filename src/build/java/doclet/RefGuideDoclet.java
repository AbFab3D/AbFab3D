

package doclet;

import com.sun.javadoc.*;
import com.sun.tools.doclets.formats.html.*;
import org.apache.commons.io.FileUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RefGuideDoclet {
    private static final String REF_GUIDE_DIR = "docs/refguide";
    private static final HashMap<String,String> packageTOCName;

    static {
        packageTOCName = new HashMap<String,String>();
        packageTOCName.put("abfab3d.datasources","Data Sources");
        packageTOCName.put("abfab3d.transforms","Transformations");
    }

    public RefGuideDoclet() {
        super();
    }


    public static boolean start(RootDoc root) {


        File dir = new File(REF_GUIDE_DIR);
        dir.mkdirs();

        FileOutputStream fos = null;
        BufferedOutputStream bos = null;
        PrintWriter pw = null;

        HashMap<String, List<String>> package_list = new HashMap<String, List<String>>();
        try {
            fos = new FileOutputStream(REF_GUIDE_DIR + "/index.html");
            bos = new BufferedOutputStream(fos);
            pw = new PrintWriter(bos);

            writePreamble(pw);

            for (ClassDoc classd : root.classes()) {
                PackageDoc pack = classd.containingPackage();
                String pname = pack.name();

                String display = packageTOCName.get(pname);
                if (display != null) {
                    pname = display;
                }
                System.out.println("package name:" + pname);
                List<String> classes = package_list.get(pname);
                if (classes == null) {
                    classes = new ArrayList<String>();
                    package_list.put(pname, classes);
                }
                classes.add(classd.name());
            }

            writeTOC(pw, package_list);
            String current_package = null;

            pw.println("<div class=\"span-9 last right\">\n");

            for (ClassDoc classd : root.classes()) {
                System.out.println("Class: " + classd.name());

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

                System.out.println("Here");
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
                    System.out.println("Comment: " + cd.commentText());

                    System.out.println("Printing method annots");
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
                    String comment = method.commentText();
                    Tag[] tags = method.tags("noRefGuide");

                    if (tags != null && tags.length > 0) {
                        System.out.println("Skipping method: " + classd.name() + "." + method.name());
                        continue;
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

                pw.println("   </div>");    // domain endpoint
            }

            pw.println("</div>");
            writePostamble(pw);

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
     * Update any doc-file urls to package/doc-files form
     * @param src The src text
     * @param path
     * @return
     */
    private static String updateURL(String src, String path) {
        String replace = "src=\"" + path;
        // search for src="doc-files" attributes
        String ret_val = src.replace("src=\"doc-files",replace);

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
        System.out.println("Checking for: " + name);
        for(int i=0; i < tags.length; i++) {
            System.out.println("   checking: " + tags[i].parameterName());
            if (tags[i].parameterName().equals(name)) {
                return tags[i];
            }
        }

        return null;
    }

    public static void writeTOC(PrintWriter pw,Map<String, List<String>> package_list) {
        pw.println("<div class=\"span-3\">");
        pw.println("<div class=\"api-toc\" id=\"toc\">");
        for(Map.Entry<String, List<String>> entry : package_list.entrySet()) {
            String id = entry.getKey();
            pw.println("<div class=\"api-toc-domain-container\" id=\"" + id + "_toc\">");
            pw.println("   <div class=\"api-toc-domain-name\"><a href=\"#" + id + "\">" + id + "</a></div>");

            List<String> classes = entry.getValue();
            System.out.println("Package has: " + classes.size());
            for(String st : classes) {
                pw.println("   <div class=\"api-toc-endpoint\" id=\"" + st + "_toc\"><a href=\"#" + st + "\">" + st + "</a></div>");
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
                "    <title>Shapeways Developer API Documentation</title>\n" +
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
                "        <h1>Shapeways API Documentation</h1>\n" +
                "        <p>\n" +
                "            Browse the Shapeways API Documentation below or check out our <a href='https://api.shapeways.com'>JSON API Discovery</a> -\n" +
                "            <a class=\"note\" href=\"https://www.google.com/search?q=JSONView\">Get the JSON View addon for your browser</a>\n" +
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
        return HtmlDoclet.optionLength(option);
    }

    public static boolean validOptions(String[][] options,
                                       DocErrorReporter reporter) {

        System.out.println("Here");
        return HtmlDoclet.validOptions(options, reporter);
    }

    public static LanguageVersion languageVersion() {
        System.out.println("Here2");
        return HtmlDoclet.languageVersion();
    }
}
