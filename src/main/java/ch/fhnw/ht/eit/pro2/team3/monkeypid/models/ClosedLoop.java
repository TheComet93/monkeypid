package ch.fhnw.ht.eit.pro2.team3.monkeypid.models;

import ch.fhnw.ht.eit.pro2.team3.monkeypid.interfaces.IController;
import ch.fhnw.ht.eit.pro2.team3.monkeypid.services.MathStuff;
import org.apache.commons.math3.complex.Complex;
import org.jfree.data.xy.XYSeries;

import java.util.Arrays;

public class ClosedLoop {

    private Plant plant;
    private IController IController;

    public ClosedLoop(Plant plant, IController IController) {
        this.plant = plant;
        this.IController = IController;
    }

    public XYSeries calculateStepResponse() {
        /*
        B = MathStuff.conv(hS.getNumeratorCoefficients(), hR.getNumeratorCoefficients());
        A = MathStuff.conv(hS.getDenominatorCoefficients(), hR.getDenominatorCoefficients());

        for (int i = 0; i < B.length ; i++) {
            A[A.length - B.length + i] += B[i];
        }

        schrittIfft(new TransferFunction(A, B), 100, 10);

        double fs = 100;
        int N = 1024;

        double [] omega = MathStuff.linspace(0, fs * Math.PI, N / 2);

        // calculate frequency response
        Complex[] H = MathStuff.freqs(g, omega);

        // calculate impulse response
        H = MathStuff.symmetricMirrorConjugate(H);
        Complex[] h = MathStuff.ifft(H);

        // calculate step response - note that h doesn't have an
        // imaginary part, so we can use conv as if it were a double
        double[] y = MathStuff.conv(MathStuff.real(h), MathStuff.ones(N+1));

        // cut away mirrored part
        y = Arrays.copyOfRange(y, 0, y.length / 2);

        // generate time axis
        double[] t = MathStuff.linspace(0, (y.length-1)/fs, y.length);

        // create XY data series for jfreechart
        XYSeries series = new XYSeries(""); // TODO name?
        for(int i = 0; i < t.length; i++) {
            series.add(t[i], y[i]);
        }

        return series;*/

        return null;
    }

    public XYSeries exampleCalculate() {

        double[][] data = {{0, 1, 2, 3}, {3, 5, 4, 6}};

        // construct XY dataset from the loaded data
        XYSeries series = new XYSeries("Test");
        for(int i = 0; i < data[0].length; i++) {
            series.add(data[0][i], data[1][i]);
        }

        return series;
    }
}