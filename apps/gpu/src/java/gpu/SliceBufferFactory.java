package gpu;

import abfab3d.grid.NIOAttributeGridByte;
import com.jogamp.opencl.CLBuffer;
import com.jogamp.opencl.CLContext;
import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;

import java.nio.ByteBuffer;

/**
 * Created by giles on 1/6/2015.
 */
public class SliceBufferFactory extends BasePooledObjectFactory<SliceBuffer> {
    private CLContext context;
    private int nx,ny,nz;
    private double vs;

    public SliceBufferFactory(int nx, int ny, int nz, double vs, CLContext context) {
        this.nx = nx;
        this.ny = ny;
        this.nz = nz;
        this.vs = vs;
        this.context = context;
    }

    public SliceBuffer create() {
        NIOAttributeGridByte grid = new NIOAttributeGridByte(nx,ny,nz,vs,vs);
        return new SliceBuffer(context,grid);
    }

    public PooledObject<SliceBuffer> wrap(SliceBuffer buff) {
        return new DefaultPooledObject<SliceBuffer>(buff);
    }
}
