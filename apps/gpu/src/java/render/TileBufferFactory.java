package render;

import com.jogamp.opencl.CLBuffer;
import com.jogamp.opencl.CLContext;
import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;

import static abfab3d.util.Output.printf;

/**
 * Created by giles on 1/6/2015.
 */
public class TileBufferFactory extends BasePooledObjectFactory<CLBuffer<IntBuffer>> {
    private CLContext context;
    private int width, height;

    public TileBufferFactory(int width, int height, CLContext context) {
        this.width = width;
        this.height = height;
        this.context = context;
    }

    public CLBuffer<IntBuffer> create() {
        IntBuffer buff = ByteBuffer.allocateDirect((height * width * 4)).order(ByteOrder.nativeOrder()).asIntBuffer();
        return context.createBuffer(buff, CLBuffer.Mem.WRITE_ONLY, CLBuffer.Mem.ALLOCATE_BUFFER);
    }

    public PooledObject<CLBuffer<IntBuffer>> wrap(CLBuffer<IntBuffer> buff) {
        return new DefaultPooledObject<CLBuffer<IntBuffer>>(buff);
    }

    public void cleanup() {
        context = null;
    }
}
