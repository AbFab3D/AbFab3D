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

import abfab3d.core.Bounds;

import java.io.PrintStream;
import java.io.OutputStream;
import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Closeable;

import java.util.Date;

import static abfab3d.core.Output.printf;
import static abfab3d.core.Units.MM;

/**
 * Write slices to a Common Layer Interface file.
 *
 *  * Spec used:
 *  * http://web.archive.org/web/19970617041930/http://www.cranfield.ac.uk/aero/rapid/CLI/cli_v20.html
 *
 * @author Alan Hudson
 */
public class CLISliceWriter extends BaseSliceWriter implements Closeable {

    static final boolean DEBUG = true;

    private OutputStream os;
    private PrintStream ps;
    private DataOutputStream dos;
    private boolean binary;
    private double units;
    private double conv;
    private String numberFormat = "%.3f";

    private boolean geomStarted = false;


    /**
     *
     * @param file
     * @param binary
     * @param units 
     * @throws IOException
     */
    public CLISliceWriter(String file, boolean binary, double units) throws IOException {
        FileOutputStream fos = new FileOutputStream(file);
        BufferedOutputStream bos = new BufferedOutputStream(fos);

        init(bos,binary,units);
    }

    /**
     *
     * @param os
     * @param binary
     * @param units 1.0 for mm
     */
    public CLISliceWriter(OutputStream os, boolean binary, double units) {
        init(os,binary,units);
    }

    /**
     *
     * @param os
     * @param binary
     * @param units 
     */
    private void init(OutputStream os, boolean binary,double units) {
        this.os = os;
        this.ps = new PrintStream(os);
        this.binary = binary;
        this.units = units;
        this.conv = 1.0/(units);  // bake the output conversion factor

        ps.print(CLIScanner.HEADER_START);
        ps.print('\n');
        if (binary) {
            ps.print(CLIScanner.BINARY);
            ps.print('\n');
        } else {
            ps.print(CLIScanner.ASCII);
            ps.print('\n');
        }

        ps.print(CLIScanner.UNITS);
        ps.format("%.4f",units/MM); // output in MM 
        ps.print('\n');
    }

    public void setSigDigits(int digits) {
        //sigDigits = digits;
        this.numberFormat  = "%."+digits+"f";
    }

    public void setVersion(int v) {
        ps.print(CLIScanner.VERSION);
        ps.print(v);
        ps.print('\n');
    }

    public void setDate(Date d) {
        /*
        Command : file was built on date
        Syntax : $$DATE/d
        Parameter d : integer
        d will be interpreted in the sequence DDMMYY.
        $$DATE/070493                       // 7. April 1993 //
         */
        ps.print(CLIScanner.DATE);
        ps.print("TBD");  // TODO: Implement date converter
        ps.print('\n');
    }

    public void setBounds(Bounds b) {}
    public void setAlign(boolean align) {}

    public void addLabel(int id, String label) {
        ps.print(CLIScanner.LABEL);
        ps.print(id);
        ps.print(",");
        ps.print(label);
        ps.print('\n');
    }
    public void addUserData(String id, String data) {
        ps.print(CLIScanner.USERDATA);
        ps.print(id);
        ps.print(",");
        ps.print(data);
        ps.print('\n');
    }

    public void addLayer(SliceLayer layer) throws IOException {
        startLayer(layer.getLayerHeight());
        PolyLine[] lines = layer.getPolyLines();

        for(PolyLine line : lines) {
            addPolyLine(line);
        }
        Hatches[] hatches = layer.getHatches();

        for(Hatches hatch : hatches) {
            addHatches(hatch);
        }
        endLayer();
    }

    public void startLayer(double height) throws IOException {

        if (!geomStarted) {
            if (binary) {
                ps.print(CLIScanner.HEADER_END);
                ps.flush();
                dos = new DataOutputStream(os);
            } else {
                if(DEBUG)printf("%s\n",CLIScanner.HEADER_END);
                if(DEBUG)printf("%s\n",CLIScanner.GEOMETRY_START);
                ps.print(CLIScanner.HEADER_END);
                ps.print('\n');
                ps.print(CLIScanner.GEOMETRY_START);
                ps.print('\n');
            }
            geomStarted = true;
        }

        if (binary) {
            writeUint16(dos,CLIScanner.CMD_START_LAYER_LONG);
            writeFloat32(dos,height*conv);
        } else {
            ps.print(CLIScanner.LAYER);
            ps.format(numberFormat,(height*conv));
            ps.print('\n');
        }
    }

    public void addPolyLine(PolyLine pl) throws IOException {

        if(DEBUG)printf("CLISliceWritewr.addPolyLine()\n"); 
        double[] points = pl.getPoints();
        if (binary) {
            writeUint16(dos,CLIScanner.CMD_START_POLY_LINE_LONG);
            writeInt32(dos,pl.getId());
            writeInt32(dos,pl.getDir());
            writeInt32(dos,points.length / 2);
            for(int i=0; i < points.length; i++) {
                writeFloat32(dos,points[i] * conv);
            }
        } else {
            
            if(DEBUG)printf("polyline: %d, %d, %d\n",pl.getId(), pl.getDir(),points.length / 2);
            ps.print(CLIScanner.POLYLINE);
            ps.print(pl.getId());
            ps.print(",");
            ps.print(pl.getDir());
            ps.print(",");
            ps.print(points.length / 2);
            ps.print(",");

            for (int i = 0; i < points.length - 1; i++) {
                ps.format(numberFormat,points[i] * conv);
                ps.print(",");

            }
            ps.format(numberFormat, points[points.length - 1] * conv);
            ps.print('\n');
        }
    }

    public void addHatches(Hatches h) throws IOException {
        double[] points = h.getCoords();
        if (binary) {
            writeUint16(dos,CLIScanner.CMD_START_HATCHES_LONG);
            writeInt32(dos,h.getId());
            writeInt32(dos,points.length / 4);
            for(int i=0; i < points.length; i++) {
                writeFloat32(dos,points[i] * conv);
            }
        } else {
            ps.print(CLIScanner.HATCHES);
            ps.print(h.getId());
            ps.print(",");
            ps.print(points.length / 4);
            ps.print(",");

            for (int i = 0; i < points.length - 1; i++) {
                ps.format(numberFormat,ps, points[i] * conv);
                ps.print(",");
            }
            ps.format(numberFormat, points[points.length - 1] * conv);
            ps.print('\n');
        }
    }
    public void endLayer() {
        if (!binary) ps.print('\n');
    }

    public void close() {
        try {
            if (binary) {
                dos.close();
            } else {
                if(DEBUG)printf("%s\n",CLIScanner.GEOMETRY_END);
                ps.print(CLIScanner.GEOMETRY_END);
                //ps.print('\n');
            }
            os.close();
        } catch(IOException ioe) {
            ioe.printStackTrace();
        }
    }
}
