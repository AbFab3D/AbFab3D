package volumesculptor.shell;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextFactory;

/**
 * Created by giles on 3/16/2015.
 */
public class SandboxContextFactory extends ContextFactory {
    @Override
    protected Context makeContext() {
        Context cx = super.makeContext();
        cx.setWrapFactory(new SandboxWrapFactory());
        return cx;
    }
}
