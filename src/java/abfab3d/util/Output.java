/*****************************************************************************
 *                        Copyright Shapeways, Inc (c) 2012
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package abfab3d.util;


import java.io.PrintStream;

/**
   utility class to simplify formatted C-style output 
   
   @author Vladimir Bulatov
   
 */
public class Output{

    public static PrintStream out = System.out;
    public static PrintStream err = System.err;
    
    public static void setOut(PrintStream ps){
        out = ps;
    }
    public static void setErr(PrintStream ps){
        err = ps;
    }
    
    public static void print(String s){
        out.print(s);   
    }
    
    public static void println(String s){
        out.println(s);
    }
    
    public static void printf(String s,Object... args){
        try {
            out.printf(s, args);
        } catch(Exception e) {
            println("bad format string in printf(): " + s );
            for(int k = 0; k < args.length; k++){                
                print(" " + args[k].toString());
            }
            println("");
        }
    }
    
    public static void printf(PrintStream ps, String s,Object... args){
        
        ps.printf(s, args);
        
    }
    
    public static String fmt(String s,Object... args){
        return String.format(s, args);
    }
    
    public static void printarray(String s, double a[]){
        for(int i=0; i < a.length; i++){
            println(s + ": " + a[i]);
        }
    }

    public static long time(){
        return System.currentTimeMillis();
    }

    public static void printMemory(){
        
        Runtime rt = Runtime.getRuntime();
        long total = rt.totalMemory();
        long free = rt.freeMemory();
        printf("memory used: %d MB (%d bytes)\n", (total - free)/1000000, (total - free));
    }

}
