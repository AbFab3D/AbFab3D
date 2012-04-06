package abfab3d.grid;

import com.sun.japex.JapexDriver;
import com.sun.japex.TestCase;

import java.util.HashMap;

/**
 * TODO: Add docs
 *
 * @author Alan Hudson
 */
public class PerfTester {
    public static final void main(String[] args) {
//        ArrayGridDriver driver1 = new ArrayGridDriver();
        JapexDriver driver1 = new BlockBasedGridDriver();

        Test tc = new Test();

//        tc.setParam("input","WriteTorusShell");
//        tc.setParam("input","ReadYXZ");
        tc.setParam("input","ReadExterior");
        
System.out.println("input: " + tc.getParam("input"));
//        tc.setParam("input","ReadXZY");
        tc.setParam("type","cpu");
        tc.setParam("width","512");
        tc.setParam("height","512");
        tc.setParam("depth","512");

        driver1.prepare(tc);

        int WARMUP = 6;
        int TIMES = 20;

        // WARMUP
        for(int i=0; i < WARMUP; i++) {
            driver1.run(tc);
        }
        
        System.out.println("Warmup done");
        long start = System.currentTimeMillis();
        
        for(int i=0; i < TIMES; i++) {
            driver1.run(tc);
        }
        
        System.out.println("Time: " + (System.currentTimeMillis() - start));
        driver1.finish(tc);
    }
}

class Test implements TestCase {
    private HashMap<String,String> params;
    
    public Test() {
        params = new HashMap();
    }
    
    @Override
    public String getName() {
        return "hardcoded";
    }

    @Override
    public boolean hasParam(String s) {
        return params.containsKey(s);
    }

    @Override
    public void setParam(String s, String s1) {
        params.put(s,s1);
    }

    @Override
    public String getParam(String s) {
        return params.get(s);
    }

    @Override
    public void setBooleanParam(String s, boolean b) {
        params.put(s, new Boolean(b).toString());
    }

    @Override
    public boolean getBooleanParam(String s) {
        return Boolean.parseBoolean(params.get(s));
    }

    @Override
    public void setIntParam(String s, int i) {
        params.put(s, new Integer(i).toString());
    }

    @Override
    public int getIntParam(String s) {
        return Integer.parseInt(params.get(s));
    }

    @Override
    public void setLongParam(String s, long l) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public long getLongParam(String s) {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void setDoubleParam(String s, double v) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public double getDoubleParam(String s) {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }
}