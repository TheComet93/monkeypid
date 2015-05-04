package ch.fhnw.ht.eit.pro2.team3.monkeypid.models;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.math3.transform.DftNormalization;
import org.apache.commons.math3.transform.FastFourierTransformer;
import org.apache.commons.math3.transform.TransformType;

public class TransferFunctionClosedLoop {

	private double[] B;
	private double[] A;
	
	public TransferFunctionClosedLoop(TransferFunction hS, TransferFunction hR) {
		B = conv(hS.getB(), hR.getB());
		A = conv(hS.getA(), hR.getA());
		
		/*System.out.println("As"+hS.getA()[0]);
		System.out.println("Bs"+hS.getB()[0]);
		System.out.println("Br"+hR.getB()[0]);
		System.out.println("Ar"+hR.getA()[0]);*/
		
		System.out.println("laenge von A " + A.length + " laenge von B " + B.length);
		
		for (int i = 0; i < B.length ; i++) {
			A[A.length - B.length + i] += B[i];
		}
		
		//Test 
		/*for (int i = 0; i < 10; i++) {
			System.out.println(linspace(0,11, 10)[i]);
		}*/
		
		schrittIfft(new TransferFunction(A, B), 100, 10);
		
				
	}
	public double[] getA(){
		return A;
	}
	public double[] getB(){
		return B;
	}

	public static final double[] conv(double[] a, double[] b){
		double[] res = new double[a.length +b.length - 1];
		for (int n = 0; n < res.length; n++) {
			for (int i=Math.max(0, n - a.length + 1); i <= Math.min(b.length - 1, n); i++) {
				res[n] += b[i] * a[n - i];
			}
		}
		return res;
	}
	
	public double[][] schrittIfft(TransferFunction g, double fs, int N){
		double t = 1/fs;
		
		double [] w = linspace(0, fs*Math.PI, N/2);
		
		Complex[] H = freqs(g, w);
		
		Complex[] HmirrCon = new Complex[H.length];
		
		for (int i = 0; i < HmirrCon.length; i++) {
			HmirrCon[i] = new Complex(H[i].re, H[i].im);
		}
				
		//mirror
		for (int i = 0; i < HmirrCon.length/2; i++) {
			Complex temp = HmirrCon[i];
			HmirrCon[i] = HmirrCon[HmirrCon.length - i -1];
			HmirrCon[HmirrCon.length - i -1] = temp;
		}
		
		//conj
		for (int i = 0; i < HmirrCon.length; i++) {
			HmirrCon[i].im = -HmirrCon[i].im;
		}
				
		//H2 = ArrayUtils.remove(H2, H2.length-1);
		//H2 = ArrayUtils.remove(H2, H2.length-1);
		
		//Complex[] H0 = new Complex[1];
		//H0[0] = new Complex();
		
		//H=(Complex[]) ArrayUtils.addAll(H, H0, H2);
		
		Complex[] HCon = new Complex[H.length];
		
		for (int i = 0; i < H.length/2; i++) {
			HCon[i]=H[i];
		}
		HCon[H.length/2]=new Complex();
		for (int j = HCon.length/2+1; j < HCon.length; j++) {
			HCon[j] = HmirrCon[j-(HCon.length/2+1)];
		}
		

		System.out.println("H");
		for (int i = 0; i < H.length; i++) {
			System.out.println("Real " + H[i].re + " Imag " + H[i].im);
		}
		System.out.println("HmirrCon");
		for (int i = 0; i < HmirrCon.length; i++) {
			System.out.println("Real " + HmirrCon[i].re + " Imag " + HmirrCon[i].im);
		}
		
		FastFourierTransformer myIFFT = new FastFourierTransformer(DftNormalization.STANDARD);
		
		
		return null;
	}
	
	
	
	private double[] linspace(double startValue, double endValue, int nValues){
		double step = (endValue - startValue)/(nValues-1);
		
		double[] res = new double[nValues];
		
		for (int i = 0; i < nValues; i++) {
			res[i] = step * i;
		}
		return res;
	}
	/**
	 * Berechnet den Frequenzgang aufgrund von Z�hler- und Nennerpolynom b resp.
	 * a sowie der Frequenzachse f.
	 * 
	 * @param b
	 *            Z�hlerpolynom
	 * @param a
	 *            Nennerpolynom
	 * @param f
	 *            Frequenzachse
	 * @return Komplexwertiger Frequenzgang.
	 */
	public static final Complex[] freqs(TransferFunction g, double[] w) {
		Complex[] res = new Complex[w.length];

		for (int i = 0; i < res.length; i++) {
			Complex jw = new Complex(0.0, w[i]);
			Complex zaehler = polyVal(g.getB(), jw);
			Complex nenner = polyVal(g.getA(), jw);
			res[i] = zaehler.div(nenner);
		}
		return res;
	}

	public static final Complex polyVal(double[] poly, Complex x) {

		Complex res = new Complex();

		for (int i = 0; i < poly.length; i++) {
			res=res.add(Complex.pow(x, poly.length - i - 1).mul(poly[i]));
		}
		return res;
	}
}