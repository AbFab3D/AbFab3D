package prototype;

/*
 * Created on Tuesday, September 07 2010 21:33
 */

import com.jogamp.common.JogampRuntimeException;
import com.jogamp.opencl.CLPlatform;
import com.jogamp.opencl.JoclVersion;

import javax.swing.*;
import java.awt.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.jogamp.common.JogampRuntimeException;
import com.jogamp.opencl.CLPlatform;
import com.jogamp.opencl.JoclVersion;
import java.awt.Container;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.UIManager;

/**
 * Displays OpenCL information in a table.
 * @author Michael Bien
 */
public class CLInfo {

    public static void main(String[] args) {

        Logger logger = Logger.getLogger(CLInfo.class.getName());

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ex) {
            logger.log(Level.INFO, null, ex);
        }

        final JoclVersion joclVersion = JoclVersion.getInstance();
        logger.info("\n" + joclVersion.getAllVersions(null).toString());

        try{
            CLPlatform.initialize();
        }catch(JogampRuntimeException ex) {
            logger.log(Level.SEVERE, null, ex);
            return;
        }

        JFrame frame = new JFrame("OpenCL Info");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        Container contentPane = frame.getContentPane();

        JEditorPane area = new JEditorPane();
        area.setContentType("text/html");
        area.setEditable(false);

        contentPane.add(new JScrollPane(area));

        area.setText(joclVersion.getOpenCLHtmlInfo(null).toString());

        frame.setSize(800, 600);
        frame.setVisible(true);

    }

}
