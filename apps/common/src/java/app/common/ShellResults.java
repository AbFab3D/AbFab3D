package app.common;

import abfab3d.mesh.WingedEdgeTriangleMesh;

/**
 * Results from a getLargestShell call
 *
 * @author Alan Hudson
 */
public class ShellResults {
    private WingedEdgeTriangleMesh largestShell;
    private int shellsRemoved;

    public ShellResults(WingedEdgeTriangleMesh largestShell, int shellsRemoved) {
        this.largestShell = largestShell;
        this.shellsRemoved = shellsRemoved;
    }

    public WingedEdgeTriangleMesh getLargestShell() {
        return largestShell;
    }

    public int getShellsRemoved() {
        return shellsRemoved;
    }
}
