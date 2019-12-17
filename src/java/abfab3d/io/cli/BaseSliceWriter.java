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

import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Write slices to a Common Layer Interface file.
 * <p>
 * * Spec used:
 * * http://web.archive.org/web/19970617041930/http://www.cranfield.ac.uk/aero/rapid/CLI/cli_v20.html
 *
 * @author Alan Hudson
 */
public class BaseSliceWriter {
    protected void writeUnsignedIntegerBinary(DataOutputStream dos, int val) throws IOException {
        dos.write((byte) (0xff & val));
        dos.write((byte) (0xff & (val >> 8)));
    }

    protected void writeRealBinary(DataOutputStream dos, double val) throws IOException {
        int value = Float.floatToIntBits((float) val);

        dos.write((byte) (0xff & value));
        dos.write((byte) (0xff & (value >> 8)));
        dos.write((byte) (0xff & (value >> 16)));
        dos.write((byte) (0xff & (value >> 24)));
    }

    protected void writeLongBinary(DataOutputStream dos, int val) throws IOException {
        dos.write((byte) (0xff & val));
        dos.write((byte) (0xff & (val >> 8)));
        dos.write((byte) (0xff & (val >> 16)));
        dos.write((byte) (0xff & (val >> 24)));
    }

}
