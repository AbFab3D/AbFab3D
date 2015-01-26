package viewer;

import render.Instruction;

import javax.media.opengl.FPSCounter;
import javax.media.opengl.GLEventListener;
import java.awt.*;
import java.util.*;

/**
 * Canvas for rendering, responsible for handling windows events and navigation.
 *
 * @author Alan Hudson
 */
public interface RenderCanvas extends GLEventListener {
    public void setRenderVersion(String st);

    public void setStatusBar(StatusBar status);

    public Component getComponent();

    public FPSCounter getCounter();

    public void setAntialiasingSteps(int numSteps);

    public void setSteps(int numSteps);

    public void setShadowSteps(int numSteps);

    public void setScene(String scene, java.util.List<Instruction> instructions, float worldScale);

    public long getLastRenderTime();

    public long getLastKernelTime();

    public void setNavigator(Navigator nav);

    public void terminate();
}
