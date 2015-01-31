package datasources;

import abfab3d.param.Parameterizable;

import java.util.Stack;

/**
 * Created by giles on 1/29/2015.
 */
public class InstNode {
    boolean start;
    boolean end;
    public Parameterizable node;
    public Stack<Parameterizable> chain;
    public Parameterizable parent;

    public InstNode(Parameterizable node, Parameterizable parent, Stack<Parameterizable> chain) {
        this.node = node;
        this.parent = parent;
        this.chain = new Stack<Parameterizable>();
        this.chain.addAll(chain);
    }

    public boolean isStart() {
        return start;
    }

    public void setStart(boolean start) {
        this.start = start;
    }

    public boolean isEnd() {
        return end;
    }

    public void setEnd(boolean end) {
        this.end = end;
    }
}
