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
import abfab3d.core.Units;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Scanner;

import static abfab3d.core.Output.printf;
import static abfab3d.core.Units.MM;
import static abfab3d.io.cli.CLIScanner.*;
import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Common Layer Slice Reader
 *
 * Spec used:
 * http://web.archive.org/web/19970617041930/http://www.cranfield.ac.uk/aero/rapid/CLI/cli_v20.html
 */
public class CLISliceReader extends BaseSliceReader {
    private enum State {
        BeforeHeader, Header, Geometry, PostGeometry
    }
    private static final boolean DEBUG = true;

    private State state;
    private double units = 1.0;
    private ArrayList<SliceLayer> layers = new ArrayList<>();


    public CLISliceReader() {

    }

    public CLISliceReader(InputStream is) throws IOException {
        load(is);

    }

    public CLISliceReader(String path) throws IOException {
        load(path);
    }

    public void load(InputStream is) throws IOException {
        state = State.BeforeHeader;
        boolean binary = false;

        /*
        hexDump(is,65);
        if (1==1) return;
*/

        CLIScanner scan = new CLIScanner(is);

        try {
            String cmd = null;
            while ((cmd = scan.nextCommand()) != null) {
                switch (state) {
                    case BeforeHeader:
                        if (cmd.startsWith(HEADER_START)) {
                            state = State.Header;
                        }
                        break;
                    case Header:
                        if (cmd.startsWith(BINARY)) binary = true;
                        if (cmd.startsWith(ASCII)) binary = false;
                        if (cmd.startsWith(GEOMETRY_START)) state = State.Geometry;
                        if (cmd.startsWith(UNITS)) {
                            String val = new String(scan.nextData());
                            units = parseReal(val,0);
                        }
                        if (cmd.startsWith(HEADER_END)) {
                            state = State.Geometry;
                            if (binary) loadBinary(is);
                        }

                        break;
                    case Geometry:
                        if (binary) throw new IllegalArgumentException("Should not be there in this state");
                        loadAscii(scan);

                        break;
                    case PostGeometry:
                        break;
                }
            }
        } finally {
            IOUtils.closeQuietly(scan);
        }

        if (DEBUG) {
            printf("Units: %f\n",units);
            printf("Layers: %d\n",layers.size());
            printf("Layer0: %s\n",layers.get(1).getPolyLines()[0].toString(MM));

        }
    }

    private void loadAscii(CLIScanner scan) throws IOException {
        SliceLayer current = new SliceLayer();

        String cmd;
        while((cmd = scan.nextCommand()) != null) {
            if (cmd.startsWith(POLYLINE)) parsePolyline(new String(scan.nextData()), 0,current);
            if (cmd.startsWith(HATCHES)) {
                parseHatches(new String(scan.nextData()),0,current);
            }
            if (cmd.startsWith(LAYER)) {
                double h = parseReal(new String(scan.nextData()),0) * units * MM;

                current = new SliceLayer(h);
                layers.add(current);
            }
            if (cmd.startsWith(GEOMETRY_END)) return;
        }
    }

    protected void loadBinary(InputStream is) throws IOException {

        DataInputStream dis = null;

        if (is instanceof  DataInputStream) {
            dis = (DataInputStream) is;
        } else {
            dis = new DataInputStream(is);
        }
        SliceLayer current = new SliceLayer();

        try {
            while(true) {
                //printf("Avail: %d\n",dis.available());
                int b1 = dis.readUnsignedByte();
                int b2 = dis.readUnsignedByte();
                int cmd  = (b2 << 8 | b1);

                //if (DEBUG) printf("cmd: %d\n",cmd);
                switch (cmd) {
                    case CMD_START_LAYER_SHORT:
                        if (DEBUG) printf("Start Layer Short\n");
                        int hi = parseUnsignedIntegerBinary(dis);
                        double h = hi * units * MM;
                        current = new SliceLayer(h);
                        layers.add(current);
                        break;
                    case CMD_START_LAYER_LONG:
                        double height = parseRealBinary(dis);
                        height = height * units * MM;
                        if (DEBUG) printf("Start Layer Long.  height: %f mm\n",height/MM);
                        current = new SliceLayer(height);
                        layers.add(current);
                        break;
                    case CMD_START_POLY_LINE_SHORT:
                        if (DEBUG) printf("Start Polyline short\n");
                        parsePolylineShortBinary(dis, current);
                        break;
                    case CMD_START_POLY_LINE_LONG:
                        parsePolylineLongBinary(dis, current);
                        break;
                    case CMD_START_HATCHES_SHORT:
                        if (DEBUG) printf("Start Hatches short\n");
                        parseHatchesShortBinary(dis, current);
                        break;
                    case CMD_START_HATCHES_LONG:
                        parseHatchesLongBinary(dis, current);
                        break;
                    default:
                        printf("Unknown cmd: %d\n",cmd);
                }
            }
        } catch(EOFException e) {
            // expected
        }
    }



    private static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();
    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = HEX_ARRAY[v >>> 4];
            hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }
        return new String(hexChars);
    }


    private double parseReal(String line, int pos) {
        String val = line.substring(pos);
        return Double.parseDouble(val);
    }

    private void parsePolyline(String line, int pos, SliceLayer current) {
        /*
        Syntax : $$POLYLINE/id,dir,n,p1x,p1y,...pnx,pny

         */


        int state = 0;  // 0 =id,1=dir,2=n,3=points
        int id = 0;
        int dir = 0;
        int n = 0;

        int idx = line.indexOf(",",pos);
        String st;

        double[] points = null;
        int pidx = 0;

        while(idx != -1) {

            switch(state) {
                case 0:
                    st = line.substring(pos,idx);
                    id = Integer.parseInt(st);
                    pos = idx + 1;
                    state++;
                    break;
                case 1:
                    st = line.substring(pos,idx);
                    dir = Integer.parseInt(st);
                    pos = idx + 1;
                    state++;
                    break;
                case 2:
                    st = line.substring(pos,idx);
                    n = Integer.parseInt(st);
                    points = new double[n*2];  // x,y array
                    pos = idx + 1;
                    state++;
                    break;
                case 3:
                    st = line.substring(pos,idx);
                    double p = Double.parseDouble(st) * units * MM;  // Keep values as m internally
                    points[pidx++] = p;
                    pos = idx + 1;
                    break;
            }

            idx = line.indexOf(",",pos);
        }

        double lp = Double.parseDouble(line.substring(line.lastIndexOf(",")+1));
        points[pidx] = lp * units * MM;

        PolyLine pl = new PolyLine(id,dir,points);
        current.addPolyLine(pl);
    }

    private void parseHatches(String line, int pos, SliceLayer current) {
        /*
        Syntax : $$HATCHES/id,n,p1sx,p1sy,p1ex,p1ey,...pnex,pney

         */


        int state = 0;  // 0 =id,2=n,3=points
        int id = 0;
        int n = 0;

        int idx = line.indexOf(",",pos);
        String st;

        double[] points = null;
        int pidx = 0;

        while(idx != -1) {

            switch(state) {
                case 0:
                    st = line.substring(pos,idx);
                    id = Integer.parseInt(st);
                    pos = idx + 1;
                    state++;
                    break;
                case 1:
                    st = line.substring(pos,idx);
                    n = Integer.parseInt(st);
                    points = new double[n*4];  // sx,sy,ex,ey array
                    pos = idx + 1;
                    state++;
                    break;
                case 2:
                    st = line.substring(pos,idx);
                    double p = Double.parseDouble(st) * units * MM;  // Keep values as m internally
                    points[pidx++] = p;
                    pos = idx + 1;
                    break;
            }

            idx = line.indexOf(",",pos);
        }

        double lp = Double.parseDouble(line.substring(line.lastIndexOf(",")+1));
        points[pidx] = lp * units * MM;

        Hatches pl = new Hatches(id,points);
        current.addHatches(pl);
    }

    private void parsePolylineLongBinary(DataInputStream dis,SliceLayer current) throws IOException {
        /*
        Syntax : $$POLYLINE/id,dir,n,p1x,p1y,...pnx,pny

         */

        int id = parseLongBinary(dis);
        int dir = parseLongBinary(dis);
        int n = parseLongBinary(dis);

        double[] points = new double[n*2];

        for(int i=0; i < n * 2; i++) {
            points[i] = parseRealBinary(dis) * units * MM;
        }

        PolyLine pl = new PolyLine(id,dir,points);
        current.addPolyLine(pl);
    }

    private void parsePolylineShortBinary(DataInputStream dis,SliceLayer current) throws IOException {
        int id = parseUnsignedIntegerBinary(dis);
        int dir = parseUnsignedIntegerBinary(dis);
        int n = parseUnsignedIntegerBinary(dis);
        double[] points = new double[n*2];

        for(int i=0; i < n * 2; i++) {
            points[i] = parseUnsignedIntegerBinary(dis) * units * MM;
        }

        PolyLine pl = new PolyLine(id,dir,points);
        current.addPolyLine(pl);
    }

    private void parseHatchesLongBinary(DataInputStream dis,SliceLayer current) throws IOException {
        int id = parseLongBinary(dis);
        int n = parseLongBinary(dis);

        double[] points = new double[n*4];

        for(int i=0; i < n * 4; i++) {
            points[i] = parseRealBinary(dis) * units * MM;
        }

        Hatches pl = new Hatches(id,points);
        current.addHatches(pl);
    }

    private void parseHatchesShortBinary(DataInputStream dis,SliceLayer current) throws IOException {
        int id = parseUnsignedIntegerBinary(dis);
        int n = parseUnsignedIntegerBinary(dis);
        double[] points = new double[n*4];

        for(int i=0; i < n * 4; i++) {
            points[i] = parseUnsignedIntegerBinary(dis) * units * MM;
        }

        Hatches pl = new Hatches(id,points);
        current.addHatches(pl);
    }

    @Override
    public SliceLayer getSlice(int idx) {
        return layers.get(idx);
    }

    @Override
    public SliceLayer[] getSlices() {
        return (SliceLayer[]) layers.toArray(new SliceLayer[layers.size()]);
    }

    @Override
    public int getNumSlices() {
        return layers.size();
    }

    @Override
    public double getUnits() {
        return units;
    }

    @Override
    public Bounds getBounds() {
        return null;
    }
}

