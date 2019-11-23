/*
 * ***************************************************************************
 *                   Shapeways, Inc Copyright (c) 2019
 *                                Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 * ***************************************************************************
 */

package abfab3d.io.cli;

import junit.framework.Assert;
import org.apache.tools.ant.filters.StringInputStream;
import org.junit.Test;

import java.io.FileInputStream;
import java.io.IOException;

import static abfab3d.core.Output.printf;

public class TestCLIScanner {

    @Test
    public void testTokenizer() throws IOException {

        String src = "$$HEADERSTART\n" +
                "$$ASCII\n" +
                "$$UNITS/0.100000\n" +
                "$$HEADEREND\n" +
                "$$GEOMETRYSTART\n" +
                "\n" +
                "$$LAYER/0.00000\n" +
                "\n" +
                "$$LAYER/1.00000\n" +
                "$$POLYLINE/1,1,2,66.00000,43.80000,66.20000,47.90000\n";

        StringInputStream sis = new StringInputStream(src);

        CLIScanner scanner = new CLIScanner(sis);
        int cnt = 0;
        String cmd;

        while((cmd = scanner.nextCommand()) != null) {
            //printf("Cmd: %s\n",cmd);
            if (cmd.charAt(cmd.length()-1) == '/') {
                byte[] data = scanner.nextData();
                //printf("data: %s\n",new String(data));
            }
            cnt++;
        }

        Assert.assertEquals("Count wrong",8,cnt);
    }

    @Test
    public void testComments() throws IOException {

        String src = "$$HEADERSTART\n" +
                "$$ASCII\n" +
                "$$UNITS/0.100000//val comment\n" +
                "$$HEADEREND\n" +
                "$$GEOMETRYSTART\n" +
                "// Full line comment\n" +
                "//$$HEADERSTART\n" +
                "$$LAYER/0.00000\n" +
                "\n" +
                "$$LAYER/1.00000\n" +
                "$$POLYLINE/1,1,2,66.00000,43.80000,66.20000,47.90000  // Complex value\n";

        StringInputStream sis = new StringInputStream(src);

        CLIScanner scanner = new CLIScanner(sis);
        int cnt = 0;
        String cmd;

        while((cmd = scanner.nextCommand()) != null) {
            printf("Cmd: %s\n",cmd);
            if (cmd.charAt(cmd.length()-1) == '/') {
                byte[] data = scanner.nextData();
                printf("data: %s\n",new String(data));
            }
            cnt++;
        }

        Assert.assertEquals("Count wrong",8,cnt);
    }

    @Test
    public void testFile() throws IOException {
        String file = "test/slices/gyroplane.cli";

        FileInputStream fis = new FileInputStream(file);

        CLIScanner scanner = new CLIScanner(fis);
        int cnt = 0;
        String cmd;

        while((cmd = scanner.nextCommand()) != null) {
            printf("Cmd: %s\n",cmd);
            if (cmd.charAt(cmd.length()-1) == '/') {
                byte[] data = scanner.nextData();
                printf("data: %s\n",new String(data));
            }
            cnt++;
        }

        printf("cnt: %d\n",cnt);
    }
}
