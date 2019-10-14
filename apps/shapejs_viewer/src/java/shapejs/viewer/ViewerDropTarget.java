/******************************************************************************
 *                        Shapeways, Inc Copyright (c) 2012-2019
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package shapejs.viewer;

import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetListener;
import java.awt.dnd.DropTargetAdapter;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DnDConstants;

import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;

import java.util.Iterator;
import java.io.File;
import java.io.IOException;


public class ViewerDropTarget extends DropTargetAdapter {
    
    BaseVolumeViewer m_viewer;

    public ViewerDropTarget(BaseVolumeViewer viewer){
        m_viewer = viewer;
    }
    public void drop(DropTargetDropEvent dropTargetDropEvent){
        System.out.println("drop (" + dropTargetDropEvent + ")");
        
        try
            {
                Transferable tr = dropTargetDropEvent.getTransferable();
                if (tr.isDataFlavorSupported (DataFlavor.javaFileListFlavor))
                    {
                        dropTargetDropEvent.acceptDrop (DnDConstants.ACTION_COPY_OR_MOVE);
                        java.util.List fileList = (java.util.List)
                            tr.getTransferData(DataFlavor.javaFileListFlavor);
                        Iterator iterator = fileList.iterator();
                        if (iterator.hasNext()) // we only load first file 
                            {
                                File file = (File)iterator.next();
                                final String cpath = file.getCanonicalPath().replace('\\','/');
                                m_viewer.loadFile(cpath, true);
                            }
                        dropTargetDropEvent.getDropTargetContext().dropComplete(true);
                    } else {
                    System.err.println ("Rejected");
                    dropTargetDropEvent.rejectDrop();
                }
            } catch (IOException io) {
            io.printStackTrace();
            dropTargetDropEvent.rejectDrop();
        } catch (UnsupportedFlavorException ufe) {
            ufe.printStackTrace();
            dropTargetDropEvent.rejectDrop();
        }            
    }
}
