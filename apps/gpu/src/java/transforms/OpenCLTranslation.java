/*****************************************************************************
 *                        Shapeways, Inc Copyright (c) 2015
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package transforms;

import org.apache.commons.io.IOUtils;
import program.ProgramLoader;

import java.io.IOException;
import java.io.InputStream;

/**
 * OpenCL representation of a translation
 *
 * @author Alan Hudson
 */
public class OpenCLTranslation extends OpenCLTransform {
    @Override
    public String getCode(String version) throws IOException {

        InputStream is = ProgramLoader.getStreamFor("translation_" + version + ".cl");
        return IOUtils.toString(is, "UTF-8");
    }
}
