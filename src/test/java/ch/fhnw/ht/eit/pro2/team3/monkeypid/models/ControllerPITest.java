package ch.fhnw.ht.eit.pro2.team3.monkeypid.models;

import static org.junit.Assert.*;

import org.apache.commons.math3.complex.Complex;
import org.junit.Test;

/**
 * Test the ControllerPI
 */
public class ControllerPITest {
	double delta = 1.5e-4;

    /**
     * Test the Consturctor
     */
	@Test
	public void testPIControllerConstructor()  throws Exception {
		//[Tn, Kr] = p2_zellweger_pi_tu_tg(45-180,2,6,1)
		//Tn = 3.3122, Kr =	0.9840
		double Kr = 0.9840;
		double Tn = 3.3122;
		ControllerPI myControllerPI = new ControllerPI("myPI", Kr, Tn);
		
		//expected values
		//Br = Kr*[Tn 1];
		//Ar = [Tn 0];
		double[] NumeratorExpected = {3.2591, 0.9840};
		double[] DenominatorExpected = {3.3122, 0.0};
		assertArrayEquals(NumeratorExpected, myControllerPI.getTransferFunction().getNumeratorCoefficients(),delta);
		assertArrayEquals(DenominatorExpected, myControllerPI.getTransferFunction().getDenominatorCoefficients(),delta);
	}

    /**
     * Test setParams
     */
	@Test
	public void testPIControllerSetParameters()  throws Exception {
		//[Tn, Kr] = p2_zellweger_pi_tu_tg(45-180,2,6,1)
		//Tn = 3.3122, Kr =	0.9840
		double Kr = 0.9840;
		double Tn = 3.3122;
		
		ControllerPI myControllerPI = new ControllerPI("myPI", 1.2, 5.5); //set some random values
		myControllerPI.setParameters(Kr, Tn);
		
		//expected values
		//Br = Kr*[Tn 1];
		//Ar = [Tn 0];
		double[] NumeratorExpected = {3.2591, 0.9840};
		double[] DenominatorExpected = {3.3122, 0.0};
		assertArrayEquals(NumeratorExpected, myControllerPI.getTransferFunction().getNumeratorCoefficients(),delta);
		assertArrayEquals(DenominatorExpected, myControllerPI.getTransferFunction().getDenominatorCoefficients(),delta);
	}

}
