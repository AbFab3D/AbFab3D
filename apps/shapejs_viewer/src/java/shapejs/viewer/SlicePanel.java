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

import abfab3d.core.DataSource;
import abfab3d.core.Units;
import abfab3d.core.Vec;
import abfab3d.grid.op.SliceMaker;
import abfab3d.param.*;
import abfab3d.param.editor.ParamPanel;
import abfab3d.shapejs.Scene;
import abfab3d.util.ColorMapperDistance;

import javax.swing.*;
import javax.vecmath.Vector3d;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;

import static abfab3d.core.MathUtil.clamp;
import static abfab3d.core.MathUtil.iround;
import static abfab3d.core.Output.fmt;
import static abfab3d.core.Output.printf;
import static abfab3d.core.Units.MM;

/**
 * Panel for viewing distance slices
 *
 * @author Alan Hudson
 * @author Vladimir Bulatov
 */
public class SlicePanel extends JFrame implements ActionListener, MouseMotionListener, MouseListener, MouseWheelListener, Runnable {

    static final int SLEEP_TIME = 10; // frame sleep time in ms
    static final int DEFAULT_IMAGE_SIZE = 504;
    static final double MIN_GRID_CELL = 1.e-3;
    static final double MAX_GRID_CELL = 100;
    static final int MIN_GRID_DISTANCE = 5; // min pixels between grid lines 

    private SingleImagePanel m_sliceCanvas;
    private Scene m_scene;
    private int m_imgWidth = DEFAULT_IMAGE_SIZE;
    private int m_imgHeight = DEFAULT_IMAGE_SIZE;
    private BufferedImage m_img;
    private int m_imgData[];
    private double m_data[];
    DataSource m_source;
    // display units
    String m_vUnitsName = "mm";
    double m_vUnits = Units.MM;
    String m_cUnitsName = "mm";
    double m_cUnits = Units.MM;
    int m_axis = AXIS_ZP;
    double m_band = 1 * MM;
    double m_bandOffset = 0 * MM;
    boolean m_showCross = false;
    protected Color m_crossColor = new Color(127, 127, 255);
    protected Color m_centerColor = new Color(0, 0, 255);


    protected JLabel status;

    // pointer location and value 
    Vector3d m_lastPnt = new Vector3d();
    double m_lastValue = -1;
    double m_centerValue = -1;
    Vector3d m_mouseDownPnt = new Vector3d();

    private transient boolean m_needToRender = false;
    private transient boolean m_terminate = false;
    static final int
            AXIS_XP = 0,  // view from X+
            AXIS_YP = 1,  // view from Y+
            AXIS_ZP = 2,  // view from Z+
            AXIS_XM = 3,  // view from X-
            AXIS_YM = 4,  // view from Y-
            AXIS_ZM = 5;  // view from Z-

    SliceMaker m_sliceMaker;
    // center of slice
    Vector3d m_center = new Vector3d(0, 0, 0);
    double m_pixelSize = 0.1 * MM; // slice pixel size
    private double m_wheelIncrement = 1; // wheel increment in pixels 
    private double m_zoomIncrement = 0.1;

    private Thread m_runner;

    DoubleParameter mp_band = new DoubleParameter("band", m_band / m_vUnits);
    DoubleParameter mp_bandOffset = new DoubleParameter("bandOffset", m_bandOffset / m_vUnits);

    static final String sm_axisNames[] = new String[]{"X", "Y", "Z"};
    EnumParameter mp_axis = new EnumParameter("axis", sm_axisNames, sm_axisNames[m_axis]);
    DoubleParameter mp_centerX = new DoubleParameter("center-X", 0);
    DoubleParameter mp_centerY = new DoubleParameter("center-Y", 0);
    DoubleParameter mp_centerZ = new DoubleParameter("center-Z", 0);
    DoubleParameter mp_pixelSize = new DoubleParameter("pixel size", m_pixelSize / m_cUnits);
    BooleanParameter mp_showCenter = new BooleanParameter("show center", true);
    BooleanParameter mp_showAxis = new BooleanParameter("show axis", true);
    BooleanParameter mp_showGrid = new BooleanParameter("show grid", true);
    DoubleParameter mp_gridCellX = new DoubleParameter("grid cell X", 1., MIN_GRID_CELL, MAX_GRID_CELL);
    DoubleParameter mp_gridCellY = new DoubleParameter("grid cell Y", 1., MIN_GRID_CELL, MAX_GRID_CELL);
    DoubleParameter mp_gridCellZ = new DoubleParameter("grid cell Z", 1., MIN_GRID_CELL, MAX_GRID_CELL);

    Parameter a_params[] = new Parameter[]{
            mp_axis,
            mp_band,
            mp_bandOffset,
            mp_centerX,
            mp_centerY,
            mp_centerZ,
            mp_pixelSize,
            mp_showCenter,
            mp_showAxis,
            mp_showGrid,
            mp_gridCellX,
            mp_gridCellY,
            mp_gridCellZ,
    };

    public SlicePanel() {
        super("Slice View");

        initBuffers(DEFAULT_IMAGE_SIZE, DEFAULT_IMAGE_SIZE);

        status = new JLabel("Status me");
        status.setFont(new Font("Courier New", Font.PLAIN, 11));
        m_sliceCanvas = new SingleImagePanel();
        m_sliceCanvas.setImage(m_img);
        m_sliceCanvas.addMouseMotionListener(this);
        m_sliceCanvas.addMouseWheelListener(this);
        m_sliceCanvas.addMouseListener(this);
        m_sliceCanvas.addComponentListener(new CanvasSizeListener());

        setLayout(new BorderLayout());

        add(m_sliceCanvas, BorderLayout.CENTER);
        add(status, BorderLayout.SOUTH);

        JPanel sidePanel = new JPanel();
        sidePanel.setLayout(new BorderLayout());
        sidePanel.add(ParamPanel.makePanel(a_params, new ParamListener()), BorderLayout.NORTH);

        this.add(sidePanel, BorderLayout.WEST);

        setLocation(590, 550);
        pack();
        setVisible(false);
        m_runner = new Thread(this);
        m_runner.start();

    }

    public void setScene(Scene scene) {

        m_scene = scene;
        m_needToRender = true;
    }

    public void initBuffers(int width, int height) {

        //printf("initBuffers(%d, %d)\n", width, height);
        m_imgWidth = width;
        m_imgHeight = height;
        m_img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        DataBufferInt db = (DataBufferInt) m_img.getRaster().getDataBuffer();
        m_imgData = db.getData();
        m_data = new double[width * height];
    }

    /**
     * action coming from top UI
     *
     * @Override
     */
    public void actionPerformed(ActionEvent e) {

        String action = e.getActionCommand();
        if (action.equalsIgnoreCase("none")) {
            setVisible(false);
        } else {
            m_needToRender = true;
            setVisible(true);
        }
    }

    // thread for image updates 
    public void run() {
        while (!m_terminate) {
            try {
                Thread.sleep(SLEEP_TIME);
            } catch (InterruptedException ie) {
            }
            if (m_needToRender && isVisible()) {
                m_needToRender = false;
                updateImage();
            }
        }
    }

    /**
     * image rendering routine
     * It is called from runner thread
     */
    private void updateImage() {
        m_needToRender = false;

        boolean initCanvas = false;
        if (m_imgWidth != m_sliceCanvas.getWidth() || m_imgHeight != m_sliceCanvas.getHeight()) {
            // canvas size was changed 
            initBuffers(m_sliceCanvas.getWidth(), m_sliceCanvas.getHeight());
            initCanvas = true;
        }

        DataSource source = ((abfab3d.param.Shape) m_scene.getSource().get(0)).getSource();
        m_source = source;

        Vector3d origin = null;
        Vector3d pntu = null;
        Vector3d pntv = null;

        double sx = m_imgWidth * m_pixelSize / 2;
        double sy = m_imgHeight * m_pixelSize / 2;
        switch (m_axis) {
            default:
            case AXIS_ZP:
                origin = new Vector3d(m_center.x - sx, m_center.y - sy, m_center.z);
                pntu = new Vector3d(m_center.x + sx, m_center.y - sy, m_center.z);
                pntv = new Vector3d(m_center.x - sx, m_center.y + sy, m_center.z);
                break;
            case AXIS_XP:
                origin = new Vector3d(m_center.x, m_center.y - sy, m_center.z + sx);
                pntu = new Vector3d(m_center.x, m_center.y - sy, m_center.z - sx);
                pntv = new Vector3d(m_center.x, m_center.y + sy, m_center.z + sx);
                break;
            case AXIS_YP:
                origin = new Vector3d(m_center.x - sx, m_center.y, m_center.z + sy);
                pntu = new Vector3d(m_center.x + sx, m_center.y, m_center.z + sy);
                pntv = new Vector3d(m_center.x - sx, m_center.y, m_center.z - sy);
                break;
        }
        if (m_sliceMaker == null)
            m_sliceMaker = new SliceMaker();
        int channelID = 0;
        m_sliceMaker.renderSlice(m_imgWidth, m_imgHeight, origin, pntu, pntv, source, channelID, new ColorMapperDistance(m_band, m_bandOffset), m_imgData, m_data);

        m_lastValue = getValueWorld(m_lastPnt);
        m_centerValue = getValueWorld(m_center);

        if (initCanvas)
            m_sliceCanvas.setImage(m_img);
        else
            m_sliceCanvas.repaint();
        updateLabel();
    }

    private void updateLabel() {

        status.setText(fmt(" cntr: [%6.3f,%6.3f,%6.3f] value:%6.3f; curs:[%6.3f,%6.3f,%6.3f] value:%6.3f",
                m_center.x / m_cUnits, m_center.y / m_cUnits, m_center.z / m_cUnits,
                m_centerValue / m_vUnits,
                m_lastPnt.x / m_cUnits, m_lastPnt.y / m_cUnits, m_lastPnt.z / m_cUnits,
                m_lastValue / m_vUnits
        ));

    }

    void updateUI() {

        mp_pixelSize.setValue(m_pixelSize / m_cUnits);
        mp_centerX.setValue(m_center.x / m_cUnits);
        mp_centerY.setValue(m_center.y / m_cUnits);
        mp_centerZ.setValue(m_center.z / m_cUnits);

    }


    /**
     * return value for given screen coordinate
     */
    double getValueWorld(Vector3d wpnt) {
        Vector3d spnt = new Vector3d();
        getScreenCoord(wpnt, spnt);
        return getValue(iround(spnt.x), iround(spnt.y));
    }

    double getValue(int xs, int ys) {
        xs = clamp(xs, 0, m_imgWidth - 1);
        ys = clamp(ys, 0, m_imgHeight - 1);
        if (m_data != null)
            return m_data[xs + m_imgWidth * ys];
        else
            return 0;
    }

    @Override
    public void mouseDragged(MouseEvent e) {

        getWorldCoord(e.getX(), e.getY(), m_lastPnt);
        Vector3d delta = new Vector3d(m_lastPnt);
        delta.sub(m_mouseDownPnt);
        m_center.sub(delta);

        updateUI();
        m_needToRender = true;
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        // 
        double x = (e.getX() + 0.5);
        double y = (e.getY() + 0.5);

        if (m_sliceMaker == null) return;

        getWorldCoord(e.getX(), e.getY(), m_lastPnt);
        m_lastValue = getValue(e.getX(), e.getY());

        updateLabel();
        m_sliceCanvas.repaint();
    }


    @Override
    public void mouseClicked(MouseEvent e) {
        Vector3d pnt = new Vector3d();
        getWorldCoord(e.getX(), e.getY(), pnt);
        Vec p = new Vec(3);
        Vec v = new Vec(4);
        p.set(pnt);
        m_source.getDataValue(p, v);
        String msg = fmt("pnt:(%10.6f %10.6f %10.6f)mm->(%10.6fmm, %10.6f %10.6f %10.6f)\n",
                pnt.x / m_cUnits, pnt.y / m_cUnits, pnt.z / m_cUnits, v.v[0] / m_vUnits, v.v[1], v.v[2], v.v[3]);

        ViewerConfig.getInstance().print(msg);
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        m_showCross = true;
    }

    @Override
    public void mouseExited(MouseEvent e) {
        m_showCross = false;
        m_sliceCanvas.repaint();
    }

    @Override
    public void mousePressed(MouseEvent e) {
        getWorldCoord(e.getX(), e.getY(), m_mouseDownPnt);
    }

    @Override
    public void mouseReleased(MouseEvent e) {
    }


    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {

        int mod = e.getModifiersEx();
        if ((mod & e.CTRL_DOWN_MASK) == e.CTRL_DOWN_MASK) {
            // z-shift            
            double d = e.getWheelRotation() * m_pixelSize * m_wheelIncrement;
            switch (m_axis) {
                default:
                case AXIS_XP:
                    m_center.x -= d;
                    break;
                case AXIS_YP:
                    m_center.y -= d;
                    break;
                case AXIS_ZP:
                    m_center.z -= d;
                    break;
                case AXIS_XM:
                    m_center.x += d;
                    break;
                case AXIS_YM:
                    m_center.y += d;
                    break;
                case AXIS_ZM:
                    m_center.z += d;
                    break;
            }
            getWorldCoord(e.getX(), e.getY(), m_lastPnt);

            updateUI();

        } else {
            // zoom in 
            getWorldCoord(e.getX(), e.getY(), m_lastPnt);
            double zoom = Math.exp(e.getWheelRotation() * m_zoomIncrement);
            Vector3d delta = new Vector3d(m_center);
            delta.sub(m_lastPnt);
            delta.scale(zoom);
            delta.add(m_lastPnt);
            m_center.set(delta);
            m_pixelSize *= zoom;

            updateUI();

        }
        m_needToRender = true;
        //updateImage();
        //updateLabel();
    }

    void getWorldCoord(int x, int y, Vector3d pnt) {

        switch (m_axis) {
            default:
            case AXIS_ZP:
                pnt.x = (x + 0.5 - 0.5 * m_imgWidth) * m_pixelSize + m_center.x;
                pnt.y = ((m_imgHeight - 1 - y) + 0.5 - 0.5 * m_imgHeight) * m_pixelSize + m_center.y;
                pnt.z = m_center.z;
                break;
            case AXIS_XP:
                pnt.x = m_center.x;
                pnt.y = ((m_imgHeight - 1 - y) + 0.5 - 0.5 * m_imgHeight) * m_pixelSize + m_center.y;
                pnt.z = ((m_imgWidth - 1 - x) + 0.5 - 0.5 * m_imgWidth) * m_pixelSize + m_center.z;
                break;
            case AXIS_YP:
                pnt.x = (x + 0.5 - 0.5 * m_imgWidth) * m_pixelSize + m_center.x;
                pnt.y = m_center.y;
                pnt.z = (y + 0.5 - 0.5 * m_imgHeight) * m_pixelSize + m_center.z;
                break;
        }
    }

    /**
     * convert worls coordinates into screen coordinates
     */
    void getScreenCoord(Vector3d wpnt, Vector3d spnt) {

        switch (m_axis) {
            default:

            case AXIS_ZP:
                spnt.x = (wpnt.x - m_center.x) / m_pixelSize + 0.5 * m_imgWidth - 0.5;
                spnt.y = m_imgHeight - 1 - ((wpnt.y - m_center.y) / m_pixelSize + 0.5 * m_imgHeight - 0.5);
                spnt.z = (wpnt.z - m_center.z) / m_pixelSize;
                break;

            case AXIS_XP:
                spnt.x = m_imgWidth - 1 - ((wpnt.z - m_center.z) / m_pixelSize + 0.5 * m_imgWidth - 0.5);
                spnt.y = m_imgHeight - 1 - ((wpnt.y - m_center.y) / m_pixelSize + 0.5 * m_imgHeight);
                spnt.z = (wpnt.x - m_center.x) / m_pixelSize;
                break;

            case AXIS_YP:
                spnt.x = (wpnt.x - m_center.x) / m_pixelSize + 0.5 * m_imgWidth - 0.5;
                spnt.y = (wpnt.z - m_center.z) / m_pixelSize + 0.5 * m_imgHeight - 0.5;
                spnt.z = (wpnt.y - m_center.y) / m_pixelSize;
                break;

        }
    }

    /**
     * listener for param changes
     */
    class ParamListener implements ParamChangedListener {

        public void paramChanged(Parameter param) {
            if (param == mp_axis) {
                m_axis = mp_axis.getIndex();
            } else if (param == mp_band) {
                m_band = mp_band.getValue() * m_vUnits;
            } else if (param == mp_bandOffset) {
                m_bandOffset = mp_bandOffset.getValue() * m_vUnits;
            } else if (param == mp_pixelSize) {
                m_pixelSize = mp_pixelSize.getValue() * m_cUnits;
            } else if (param == mp_centerX) {
                m_center.x = mp_centerX.getValue() * m_cUnits;
            } else if (param == mp_centerY) {
                m_center.y = mp_centerY.getValue() * m_cUnits;
            } else if (param == mp_centerZ) {
                m_center.z = mp_centerZ.getValue() * m_cUnits;
            }

            m_needToRender = true;

            printf("param changed: %s\n", param);
        }

    }

    class CanvasSizeListener extends ComponentAdapter {

        public void componentResized(ComponentEvent e) {

            int w = m_sliceCanvas.getWidth();
            int h = m_sliceCanvas.getHeight();
            if (w != m_imgWidth || h != m_imgHeight) {
                //printf("[%d x %d]\n", w, h);
                m_needToRender = true;
            }
        }
    }

    class SingleImagePanel extends JPanel {

        private BufferedImage m_img;

        public SingleImagePanel() {
            setPreferredSize(new Dimension(DEFAULT_IMAGE_SIZE, DEFAULT_IMAGE_SIZE));
        }

        public void setImage(BufferedImage img) {
            m_img = img;
            repaint();
        }

        protected void paintComponent(Graphics g) {

            super.paintComponent(g);

            if (m_img == null) return;

            g.drawImage(m_img, 0, 0, null);

            if (mp_showGrid.getValue()) {
                g.setColor(Color.darkGray);
                switch (m_axis) {
                    case AXIS_ZP: {
                        double dx = mp_gridCellX.getValue() * m_cUnits;
                        double dy = mp_gridCellY.getValue() * m_cUnits;
                        Vector3d wp0 = new Vector3d(0, 0, 0);
                        Vector3d wp1 = new Vector3d(0, 0, 0);
                        getWorldCoord(0, m_imgHeight, wp0);
                        getWorldCoord(m_imgWidth, 0, wp1);

                        int x0 = (int) Math.floor(wp0.x / dx);
                        int x1 = (int) Math.ceil(wp1.x / dx);
                        int y0 = (int) Math.floor(wp0.y / dy);
                        int y1 = (int) Math.ceil(wp1.y / dy);


                        Vector3d wpnt = new Vector3d();
                        Vector3d spnt = new Vector3d();
                        if ((x1 - x0) < m_imgWidth / MIN_GRID_DISTANCE) {
                            for (int x = x0; x <= x1; x++) {
                                wpnt.x = x * dx;
                                getScreenCoord(wpnt, spnt);
                                int ix = iround(spnt.x);
                                g.drawLine(ix, 0, ix, m_imgHeight);
                                if ((x % 10) == 0) {
                                    g.drawLine(ix + 1, 0, ix + 1, m_imgHeight);
                                    g.drawLine(ix - 1, 0, ix - 1, m_imgHeight);
                                }
                            }
                        }
                        if ((y1 - y0) < m_imgHeight / MIN_GRID_DISTANCE) {
                            for (int y = y0; y <= y1; y++) {
                                wpnt.y = y * dy;
                                getScreenCoord(wpnt, spnt);
                                int iy = iround(spnt.y);
                                g.drawLine(0, iy, m_imgWidth, iy);
                                if ((y % 10) == 0) {
                                    g.drawLine(0, iy + 1, m_imgWidth, iy + 1);
                                    g.drawLine(0, iy - 1, m_imgWidth, iy - 1);
                                }
                            }
                        }
                    }
                }
            } //if(mp_showGrid.getValue()){

            if (mp_showAxis.getValue()) {
                int w = 100;
                int x = 5;
                int y = 5;
                g.setColor(new Color(0xFF, 0xFF, 0xFF, 0xB0));
                g.fillRect(x, y, w, w);
                g.setColor(new Color(0, 0, 0));
                //g.drawRect(x,y,w, w);
                int off = 5;
                int th = 10;
                int tw = 10;
                switch (m_axis) {
                    case AXIS_ZP:
                        g.drawString("Y", 2 * x, 2 * y + th);
                        g.drawString("X", w - tw, w);
                        g.drawLine(x, y + w, x + w, y + w);
                        g.drawLine(x, y + w, x, y);
                        break;
                    case AXIS_XP:
                        g.drawString("Y", w + x - tw, 2 * y + th);
                        g.drawString("Z", 2 * x, w);
                        g.drawLine(x, y + w, x + w, y + w);
                        g.drawLine(x + w, y + w, x + w, y);
                        break;
                    case AXIS_YP:
                        g.drawString("X", w + x - tw, 2 * y + th);
                        g.drawString("Z", 2 * x, w);
                        g.drawLine(x, y, x + w, y);
                        g.drawLine(x, y + w, x, y);
                        break;

                }
            } // if(mp_showAxis.getValue()){

            if (mp_showCenter.getValue()) {
                int x = m_imgWidth / 2;
                int y = m_imgHeight / 2;
                int len = 20;
                g.setColor(m_centerColor);
                g.drawLine(x - len, y, x + len, y);
                g.drawLine(x, y - len, x, y + len);
                g.drawOval(x - len / 2, y - len / 2, len, len);
            }

            if (m_showCross) {
                Vector3d spnt = new Vector3d();
                getScreenCoord(m_lastPnt, spnt);
                int x = (int) (spnt.x);
                int y = (int) (spnt.y);
                g.setColor(m_crossColor);
                g.drawLine(0, y, m_imgWidth - 1, y);
                g.drawLine(x, 0, x, m_imgHeight - 1);
            }

        }
    } // class SingleImagePanel 

} // class SlicePanel 

