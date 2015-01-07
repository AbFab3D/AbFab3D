package gpu;

import abfab3d.grid.NIOAttributeGridByte;
import com.jogamp.opencl.CLBuffer;
import com.jogamp.opencl.CLContext;

import java.nio.ByteBuffer;

/**
 * Created by giles on 1/6/2015.
 */
public class SliceBuffer {
    private NIOAttributeGridByte grid;
    private ByteBuffer fbDest;
    private CLBuffer<ByteBuffer> bufferDest;

    public SliceBuffer(CLContext context, NIOAttributeGridByte grid) {
        this.grid = grid;

        fbDest = (ByteBuffer) grid.getBuffer();
        bufferDest = context.createBuffer(fbDest, CLBuffer.Mem.WRITE_ONLY, CLBuffer.Mem.ALLOCATE_BUFFER);
    }

    public NIOAttributeGridByte getGrid() {
        return grid;
    }

    public ByteBuffer getFbDest() {
        return fbDest;
    }

    public CLBuffer<ByteBuffer> getBufferDest() {
        return bufferDest;
    }
}
