

package doclet;

import com.sun.javadoc.*;
import com.sun.tools.doclets.formats.html.*;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RefGuideDoclet {
    private static final String REF_GUIDE_DIR = "docs/refguide";

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
                int idx = pname.lastIndexOf(".");
                pname = pname.substring(idx+1);
                System.out.println("package name:" + pname);
                List<String> classes = package_list.get(pname);
                if (classes == null) {
                    classes = new ArrayList<String>();
                    package_list.put(pname, classes);
                }
                classes.add(classd.name());
            }

            writeTOC(pw, package_list);
            // TODO: need to copy doc-files
            for (ClassDoc classd : root.classes()) {
                System.out.println("Class: " + classd.name());
                System.out.println("Comment: " + classd.commentText());

                ConstructorDoc[] constructors = classd.constructors();
                System.out.println(classd);
                System.out.println("Constructors:");
                for(ConstructorDoc cd : constructors) {
                    System.out.println("   " + cd);
                    System.out.println("      Name: " + cd.name());
                    for(Parameter param : cd.parameters()) {
                        System.out.println("      Param: " + param.name() + " type: " + param.type());
                    }
                    System.out.println("Comment: " + cd.commentText());
                }

                // uncommented methods will not be displayed
                System.out.println("Methods:");
                for(MethodDoc method : classd.methods()) {
                    String comment = method.commentText();
                    Tag[] tags = method.tags("noRefGuide");

                    if (tags != null && tags.length > 0) {
                        System.out.println("Skipping method: " + classd.name() + "." + method.name());
                        continue;
                    }
                    System.out.println("   " + method);
                }
            }

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

    public static void writeTOC(PrintWriter pw,Map<String, List<String>> package_list) {
    /*
        <div class="span-3">

    <div class="api-toc" id="toc">
        <div class="api-toc-domain-container"
             id="OAuth1toc">
            <div class="api-toc-domain-name"><a href="#DataSources">Data Sources</a></div>
            <div class="api-toc-endpoint" id="GET_-oauth1-request_token-v1toc"><a href="#Box">Box</a></div>
        </div>
    </div>
</div>

         */

        pw.println("<div class=\"span-3\">");
        pw.println("<div class=\"api-toc\" id=\"toc\">");
        for(Map.Entry<String, List<String>> entry : package_list.entrySet()) {
            String id = entry.getKey();
            pw.println("<div class=\"api-toc-domain-container\" id=\"" + id + "\">");
            pw.println("   <div class=\"api-toc-domain-name\"><a href=\"#" + id + "\">" + id + "</a></div>");

            List<String> classes = entry.getValue();
            System.out.println("Package has: " + classes.size());
            for(String st : classes) {
                pw.println("   <div class=\"api-toc-endpoint\" id=\"" + st + "\"><a href=\"#" + st + "\">" + st + "</a></div>");
            }
            pw.println("</div>");
        }
        pw.println("</div></div>");
    }

    public static void writePreamble(PrintWriter pw) {

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

    public static void writePostamble(PrintWriter pw) {
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
