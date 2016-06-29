/*****************************************************************************
 *                        Shapeways, Inc Copyright (c) 2011
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
/**
   representation of Complex numbers operations 

   @author Vladimir Bulatov
   

 */

import java.util.StringTokenizer;
import java.util.Vector;

import static abfab3d.core.Output.fmt;
import static abfab3d.core.Output.printf;


public final class Complex {

    public static final double EPSILON = 1.e-12;
    
    public static final double TODEG = 180./Math.PI;

    public static final double EPSILON2 = EPSILON*EPSILON;
    public static final double INF = Double.POSITIVE_INFINITY;
    public static final String SINF = "INF";

    public static final Complex ZERO = new Complex(0,0);
    
    public static final Complex _2  = new Complex(2, 0);
    public static final Complex _1  = new Complex(1, 0);
    public static final Complex ONE  = _1;
    
    public static final Complex MINUSONE  = new Complex(-1, 0);
    public static final Complex I = new Complex(0.,1.);
    public static final Complex _2I  = new Complex(0., 2.);
    public static final Complex MINUS_I = new Complex(0.,-1.);
    
    public static final Complex EXP_PI_3 = new Complex(Math.cos(Math.PI/3),Math.sin(Math.PI/3));
    
    
    public double re,im;
    
    public Complex(double x,double y){
        re = x;
        im = y;
    }

    
    
    public Complex(Complex z){
        this.re = z.re;
        this.im = z.im;
    }
    
    public Complex(double x){
        this.re = x;
        this.im = 0.;
    }
    
    public Complex(){
        this(0,0);
    }
    
    public static Complex fromArg(double r, double arg){
        
        return new Complex(r*Math.cos(arg), r*Math.sin(arg));
        
    }

    public static Complex getInf(){
        
        return new Complex(INF, 0);
        
    }
    
    public boolean isZero(){
        return this.equals(ZERO);
    }
    
    public boolean isInf(){
        if(Double.isInfinite(re))
            return true;
        else
            return false;
    }
    
    public Complex add(Complex a){
        
        return new Complex(re + a.re, im + a.im);
        
    }
    
    public Complex add(double a){
        
        return new Complex(re + a, im);
        
    }
    
    public Complex addSet(Complex a,Complex b){
        re = a.re + b.re;
        im = a.im + b.im;
        return this;
    }
    
    public Complex addSet(Complex a){
        re += a.re;
        im += a.im;
        return this;
    }
    
    public Complex addSet(double a){
        re += a;
        return this;
    }
    
    public Complex addSet(double a,double b){
        re += a;
        im += b;
        return this;
    }
    
    public Complex sub(Complex a){
        return new Complex(re - a.re, im - a.im);
    }
    
    public Complex sub(double a){
        return new Complex(re - a, im);
    }
    
    public Complex subSet(Complex a){
        re -= a.re;
        im -= a.im;
        return this;
    }
    
    public Complex subSet(double a){
        re -= a;
        return this;
    }
    
    public Complex subSet(Complex a, Complex b){
        re = a.re - b.re;
        im = a.im - b.im;
        return this;
    }
    
    public Complex conj(){
        return new Complex(re, -im);
    }
    
    public Complex conjSet(){
        im = -im;
        return this;
    }
    
    public Complex conj(Complex a){
        re = a.re;
        im = -a.im;
        return this;
    }
    
    public Complex neg(){
        re = -re;
        im = -im;
        return this;
    }
    
    public Complex neg(Complex a){
        re = -a.re;
        im = -a.im;
        return this;
    }
    
    public Complex divSet(Complex b){
        
        if(b.isZero()){
            
            set(getInf());
            
        } else {
            double d = b.re*b.re + b.im*b.im;
            
            double x = (re*b.re + im*b.im)/d;
            double y = (im*b.re - re*b.im)/d;
            
            re = x;
            im = y; 
        }
        
        return this;
        
    }
    
    public Complex divSet(double b){
        
        if(b*b < EPSILON2){
            
            set(INF,0);
            
        } else {
            
            re /= b;
            im /= b; 
        }
        return this;
        
    }
    
    public Complex divSet(Complex a, Complex b){
        if( b.isZero()){
            set(INF,0);
        } else {
            double d = b.re*b.re+b.im*b.im;
            re = (a.re*b.re+a.im*b.im)/d;
            im = (a.im*b.re-a.re*b.im)/d;
        }
        return this;
    }
    
    
    public Complex div(Complex a){
        if(a.isZero()){
            return getInf(); 
        } else {
            double d = a.re*a.re+a.im*a.im;
            double t = (re*a.re+im*a.im)/d;
            return new Complex(t, (im*a.re-re*a.im)/d);
        }
    }
    
    
    public Complex div(double b){
        
        return new Complex(re / b, im/b);
        
    }
    
    public Complex mul(Complex a){
        
        return new Complex(re * a.re - im * a.im, re * a.im + im * a.re);
        
    }
    
    public Complex mul(double a){
        
        if(isInf())
            return getInf();
        return new Complex(re * a , im * a);
        
    }
    
    public Complex mulSet(Complex a,Complex b){
        
        if(a.isInf() || b.isInf()){
            set(getInf());
        } else {
            
            re = a.re * b.re - a.im * b.im;
            im = a.re * b.im + a.im * b.re;
        }
        return this;
    }
    
    public Complex mulSet(Complex a, double b){
        
        if(a.isInf()){
            set(getInf());      
        } else {
            im = a.im * b;
            re = a.re * b;
        }
        return this;
    }
    
    public Complex mulSet(double b){
        if(isInf()){
            set(getInf());
        } else {
            im *= b;
            re *= b;
        }
        return this;
    }
    
    public Complex mulSet(Complex b){
        if(isInf() || b.isInf()){
            set(getInf());
        } else {
            double t = re * b.re - im * b.im;
            im = re * b.im + im * b.re;
            re = t; 
        }
        return this;
    }
    
    public Complex set(Complex a){
        re = a.re;
        im = a.im;
        return this;
    }
    
    public Complex set(double x, double y){
        re = x;
        im = y;
        return this;
    }
    
    public double abs2(){
        return re*re+im*im;
    }
    
    public double dot(Complex z2){
        return re*z2.re + im*z2.im;
    }
    
    public double abs(){
        return Math.sqrt(abs2());
    }
    
    public double arg(){
        return Math.atan2(im,re);
    }
    
    public Complex normalize(){
        double r = abs();
        re /= r;
        im /= r;
        return this;
    }
    
    public Complex invert(){
        
        double t = abs2();
        if(t != 0.0){
            re = re/t;
            im = -im/t;      
        } else {
            re = INF;
            im = 0;
        }
        return this;
    }
    
    public Complex getInverse(){
        double t = abs2();
        return new Complex(re/t, -im/t);
    }
    
    static final boolean DOROUND = true;
    
    public String toString(){
        if(isInf())
            return SINF;
        
        if(DOROUND)
            return 
                round(re)+","+round(im);
        else 
            return String.valueOf(re) + "," + String.valueOf(im);
    }
    
    public String toString(int width, int digits){
        if(isInf()){
            return "[INF]";
        }
        String format = "%%"+width + "."+digits+"f";

        return "[" + fmt(format, chop(re)) + "," + fmt(format, chop(im)) + "]";
        
    }
    
    public String toString(String format){
        if(isInf()){
            return "[INF]";
        }
        return "[" + fmt(format,re) + "," + fmt(format, im) + "]";    
    }
    
    static final double NORM = 1.e15;
    static double round(double v){
        return Math.floor((NORM*v) + 0.5)/NORM;
    }
    
    public static Complex sum(Complex[] z){
        double x = 0, y = 0;
        for(Complex c: z){
            x += c.re;
            y += c.im;
        }
        return new Complex(x, y);
    }
    
    public double distance(Complex z){
        
        return distance(this, z);
        
    }
    
    public static double distance(Complex z1, Complex z2){
        
        double d2 = distance2(z1,z2);
        if(d2 == INF)
            return INF;
        else 
            return Math.sqrt(d2);
        
    }
    
    
    public double distance2(Complex z2){
        return distance2(this, z2);
    }
    
    public static double distance2(Complex z1, Complex z2){
        
        if(z1.isInf() && z2.isInf()){
            return 0;
        } 
        if(z1.isInf() || z2.isInf()){
            return INF;
        } 
        
        double re = z1.re-z2.re;
        double im = z1.im-z2.im;
        return re*re + im*im;
        
    }
    
    public double distance2(double x, double y){
        
        double dx = this.re - x;
        double dy = this.im - y;
        return dx*dx + dy*dy;
        
    }
    
    
    /**
     *  returns linear interpolation between points
     */
    public static Complex lerp(Complex z1, Complex z2, double t){
        
        double t1 = 1-t;
        
        return new Complex(z1.re*t1 + z2.re*t, z1.im*t1 + z2.im*t);
        
    }
    
    /**
       return z component of cross product of z1 and z2 considered as 3D vectors 
    */
    public static double cross(Complex z1, Complex z2){
        
        return z1.re*z2.im - z1.im*z2.re;
        
    }
    
    /**
     *
     *
     *
     */
    public Complex sqrt(){
        
        return sqrt(this);
        
    }
    
    /**
     *
     *
     *
     */
    public static Complex sqrt(Complex z){
        
        
        double r = Math.sqrt(z.abs());
        double arg = z.arg()/2;
        return Complex.fromArg(r, arg);
        
        //double x = Math.sqrt((r + z.re)/2); 
        //double y = z.re /(2*x);   // this will break if x == 0
        //return new Complex(x,y);
        
    }
    
    
    /**
     *
     *
     *
     */
    public Complex log(){
        
        return log(this);
        
    }
    
    /**
     *
     *
     *
     */
    public static Complex power(Complex c, Complex t){
        
        // exp( t*log(c))
        if(t.im == 0.)
            return power(c, t.re);
        else 
            return Complex.exp(Complex.log(c).mulSet(t));   
    }
    
    /**
     *
     *
     *
     */
    public static Complex power(Complex c, double t){
        
        double a = Math.pow( c.abs(), t);
        double phi = t*c.arg();
        return fromArg(a, phi);   
    }
    
    /**
     *
     *
     *
     */
    public Complex power(double t){
        
        double a = Math.pow( abs(), t);
        double phi = t*arg();
        return fromArg(a, phi);   
    }
    
    /**
     *
     *
     *
     */
    public Complex pow(int n){
        
        if(n != 0){
            Complex m = new Complex(this);
            
            if(n < 0){
                n = -n;
                m.invert();        
            }
            Complex m0 = new Complex(m);
            while(n > 1){
                m.mulSet(m0);
                n--;
            }
            return m;
        } else {
            return new Complex(1,0);      
        }
    }
    
    
    /**
     *
     *
     *
     */
    public static Complex log(Complex z){
        
        return new Complex(0.5*Math.log(z.abs2()), z.arg());
        
    }
    
    public static Complex exp(Complex z){
        
        double r = Math.exp(z.re);
        
        return new Complex(r*Math.cos(z.im), r*Math.sin(z.im));
        
    }
    
    public static Complex sin(Complex z){
        
        Complex e = exp(z.mul(Complex.I));
        return e.sub(Complex.ONE.div(z)).div(_2I);
        
    }
    
    /**
     *
     *
     *
     */
    public static Complex cos(Complex z){
        
        Complex e = exp(z.mul(Complex.I));
        return e.add(Complex.ONE.div(z)).div(2.0);
        
    }
    
    /**
     *
     *
     *
     */
    public static Complex tan(Complex z){
        
        Complex e = exp(z.mul(Complex.I));
        Complex e1 = Complex.ONE.div(z);
        return e.sub(e1).div(e.add(e1)).mul(Complex.MINUS_I);
        
    }
    
    /**
     *
     *  arcsin(x) = -i * ln( ix + sqrt(1-x^2))
     *
     *   recommended method of calculations
     *
     *  arcsin(x) = arctan(x/sqrt(1-x^2))
     */
    public static Complex arcsin(Complex z){
        
        return arctan(z.div(sqrt(Complex.ONE.sub(z.mul(z)))));
        
    } 
    
    /**
     *
     *  arctan(x) = i/2 *(ln(1 - i*x) - ln(1 + i*x))
     *
     *  it is possible to use continued fractions in complex plane 
     */
    public static Complex arctan(Complex z){
        
        Complex num = new Complex(1+z.im, -z.re);
        Complex den = new Complex(1-z.im, z.re);
        // this may be bad
        //return log(num.div(den)).mul(I).div(2);
        
        return log(num).sub(log(den)).mul(new Complex(0, 0.5));
        
    } 
    
    public static Complex sinh(Complex z){
        
        Complex e = exp(z);
        return e.sub(Complex.ONE.div(z)).mul(0.5);
        
    }
    
    public static Complex cosh(Complex z){
        
        Complex e = exp(z);
        return e.add(Complex.ONE.div(z)).mul(0.5);
        
    }
    
    public static Complex tanh(Complex z){
        
        Complex e = exp(z);
        Complex e1 = Complex.ONE.div(e);
        return e.sub(e1).div(e.add(e1));
        
    }
    
    /**
     *
     *  arctanh(z) = 1/2 *(ln(1 + z) - ln(1 - z))
     *
     */
    public static Complex arctanh(Complex z){
        
        Complex t1 = new Complex(1+z.re, z.im);
        Complex t2 = new Complex(1-z.re, -z.im);
        
        return log(t1).subSet(log(t2)).mulSet(0.5);
        
    } 
    
    
    /**
     *
     *  solves equation x^2 + b*x + c = 0;
     *  return result in x1 and x2
     */
    public static  Complex[] solveQuadratic(Complex b, Complex c){
        
        Complex s = sqrt(b.mul(b).sub(c.mul(4)));
        return new Complex[]{b.mul(-1).add(s).div(2), 
                             b.mul(-1).sub(s).div(2)};
    }
    
    /**
     *
     *  solves equation a*x^2 + b*x + c = 0;
     *  return result in x1 and x2
     */
    public static Complex[] solveQuadratic(Complex a, Complex b, Complex c){
        
        if( a.isZero()){
            
            if(b.isZero())
                return new Complex[]{getInf()};
            else
                return new Complex[]{c.mul(-1).div(b), getInf()};
            
        } else {
            
            b = b.div(a);
            c = c.div(a);
            return solveQuadratic(b, c);
        }
        
    }
    
    /**
     * return signed area of polygon formad by points. 
     * for closed polygon last point should to be equal to first point 
     *
     */
    public static double polygonArea(Complex p[]){
        double c = 0; 
        if(p == null)
            return 0;
        for(int i = 0; i < p.length-1; i++){
            Complex p1 = p[i], p2 = p[i+1];
            c += (p1.re*p2.im - p1.im * p2.re);
        }
        
        return 0.5*c;
    }
    
    static final double TOLERANCE = 1.e-9;
    static final double TOLERANCE2 =  TOLERANCE*TOLERANCE;
    
    public boolean equals(Object o){
        
        if(o == null)
            return false;
        
        Complex c = (Complex)o;    
        if(distance2(c, this) < TOLERANCE2)
            return true;
        else
            return false; 
        
    }
    
    public int hashCode(){
        
        return (int)((331345.563*re)+ (412345.891*im) + 12345*Math.PI*Math.E);
        
    }
    
    static public double chop(double d){
        
        double i = Math.floor(d+0.5);
        if(Math.abs(i - d) < EPSILON)
            return i;
        
        return d;
        
    }
    
    public static Complex[] parseArray(String s){
        
        StringTokenizer st = new StringTokenizer(s, " \n\r\t,;", false);
        Vector<Complex> v = new Vector<Complex>();
        
        double x = 0, y = 0;
        int count = 0;
        while(st.hasMoreTokens()){
            String token = st.nextToken();      
            try {
                double d = Double.parseDouble(token);
                switch(count){
                case 0: 
                    x = d; 
                    count = 1; 
                    break;
                    
                case 1: 
                    v.add(new Complex(x,d)); 
                    count = 0;   
                    break;
                }
            } catch(Exception e){
                e.printStackTrace();
            }
        }
        
        return (Complex[])v.toArray(new Complex[v.size()]);
        
    }
}
