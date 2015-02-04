package render;

import com.jogamp.opencl.CLBuffer;
import com.jogamp.opencl.CLContext;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;

/**
 * Reusable buffer for transmitting results from a tile
 *
 * @author Alan Hudson
 */
public class TileBuffer {
    private CLBuffer<IntBuffer> dest;

    public TileBuffer(int width, int height,CLContext context) {
        IntBuffer buff = ByteBuffer.allocateDirect(height * width * 4).order(ByteOrder.nativeOrder()).asIntBuffer();
        dest = context.createBuffer(buff, CLBuffer.Mem.WRITE_ONLY, CLBuffer.Mem.ALLOCATE_BUFFER);
    }

    public CLBuffer<IntBuffer> getBufferDest() {
        return dest;
    }
}
