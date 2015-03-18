package shapejs;

import org.mozilla.javascript.Context;

import static abfab3d.util.Output.printf;

/**
 * Created by giles on 3/16/2015.
 */
public class SandboxContextFactory extends ContextFactory {
    @Override
    protected Context makeContext() {
        printf("***Making context\n");
        Context cx = super.makeContext();
        cx.setWrapFactory(new SandboxWrapFactory());
        return cx;
    }
}
