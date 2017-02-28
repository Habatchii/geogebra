package org.geogebra.common.gui.view.data;

import java.util.ArrayList;

import org.apache.commons.math.MathException;
import org.apache.commons.math.distribution.NormalDistributionImpl;
import org.apache.commons.math.distribution.TDistributionImpl;
import org.apache.commons.math.stat.StatUtils;
import org.apache.commons.math.stat.inference.TTestImpl;
import org.geogebra.common.kernel.Kernel;
import org.geogebra.common.kernel.arithmetic.ExpressionNodeConstants;
import org.geogebra.common.kernel.arithmetic.NumberValue;
import org.geogebra.common.main.Localization;

public class OneVarModel {
	private TTestImpl tTestImpl;
	private NormalDistributionImpl normalDist;
	private TDistributionImpl tDist;
	public double testStat, P, df, lower, upper, mean, se, me, N;
	// input fields
	public double confLevel = .95, hypMean = 0, sigma = 1;
	// test type (tail)
	public static final String tail_left = "<";
	public static final String tail_right = ">";
	public static final String tail_two = ExpressionNodeConstants.strNOT_EQUAL;
	public String tail = tail_two;
	public int selectedPlot = StatisticsModel.INFER_TINT;
	public void evaluate(double[] sample) {
		mean = StatUtils.mean(sample);
		N = sample.length;

		try {
			switch (selectedPlot) {

			default:
				// do nothing
				break;
			case StatisticsModel.INFER_ZTEST:
			case StatisticsModel.INFER_ZINT:
				normalDist = new NormalDistributionImpl(0, 1);
				se = sigma / Math.sqrt(N);
				testStat = (mean - hypMean) / se;
				P = 2.0 * normalDist.cumulativeProbability(-Math.abs(testStat));
				P = adjustedPValue(P, testStat, tail);

				double zCritical = normalDist
						.inverseCumulativeProbability((confLevel + 1d) / 2);
				me = zCritical * se;
				upper = mean + me;
				lower = mean - me;
				break;

			case StatisticsModel.INFER_TTEST:
			case StatisticsModel.INFER_TINT:
				if (tTestImpl == null) {
					tTestImpl = new TTestImpl();
				}
				se = Math.sqrt(StatUtils.variance(sample) / N);
				df = N - 1;
				testStat = tTestImpl.t(hypMean, sample);
				P = tTestImpl.tTest(hypMean, sample);
				P = adjustedPValue(P, testStat, tail);

				tDist = new TDistributionImpl(N - 1);
				double tCritical = tDist
						.inverseCumulativeProbability((confLevel + 1d) / 2);
				me = tCritical * se;
				upper = mean + me;
				lower = mean - me;
				break;
			}

		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (MathException e) {
			e.printStackTrace();
		}

	}

	private static double adjustedPValue(double p, double testStatistic,
			String tail) {

		// two sided test
		if (tail.equals(OneVarModel.tail_two)) {
			return p;
		} else if ((tail.equals(OneVarModel.tail_right) && testStatistic > 0)
				|| (tail.equals(OneVarModel.tail_left) && testStatistic < 0)) {
			return p / 2;
		} else {
			return 1 - p / 2;
		}
	}

	public double evaluateExpression(Kernel kernel, String expr) {

		NumberValue nv;

		try {
			nv = kernel.getAlgebraProcessor().evaluateToNumeric(expr, false);
		} catch (Exception e) {
			e.printStackTrace();
			return Double.NaN;
		} catch (Error e) {
			e.printStackTrace();
			return Double.NaN;
		}
		return nv.getDouble();
	}

	public ArrayList<String> getNameList(Localization loc) {
		ArrayList<String> nameList = new ArrayList<String>();

		switch (selectedPlot) {
		default:
			// do nothing
			break;
		case StatisticsModel.INFER_ZTEST:
			nameList.add(loc.getMenu("PValue"));
			nameList.add(loc.getMenu("ZStatistic"));
			nameList.add(loc.getMenu(""));
			nameList.add(loc.getMenu("Length.short"));
			nameList.add(loc.getMenu("Mean"));

			break;

		case StatisticsModel.INFER_TTEST:
			nameList.add(loc.getMenu("PValue"));
			nameList.add(loc.getMenu("TStatistic"));
			nameList.add(loc.getMenu("DegreesOfFreedom.short"));
			nameList.add(loc.getMenu("StandardError.short"));
			nameList.add(loc.getMenu(""));
			nameList.add(loc.getMenu("Length.short"));
			nameList.add(loc.getMenu("Mean"));
			break;

		case StatisticsModel.INFER_ZINT:
			nameList.add(loc.getMenu("Interval"));
			nameList.add(loc.getMenu("LowerLimit"));
			nameList.add(loc.getMenu("UpperLimit"));
			nameList.add(loc.getMenu("MarginOfError"));
			nameList.add(loc.getMenu(""));
			nameList.add(loc.getMenu("Length.short"));
			nameList.add(loc.getMenu("Mean"));
			break;

		case StatisticsModel.INFER_TINT:
			nameList.add(loc.getMenu("Interval"));
			nameList.add(loc.getMenu("LowerLimit"));
			nameList.add(loc.getMenu("UpperLimit"));
			nameList.add(loc.getMenu("MarginOfError"));
			nameList.add(loc.getMenu("DegreesOfFreedom.short"));
			nameList.add(loc.getMenu("StandardError.short"));
			nameList.add(loc.getMenu(""));
			nameList.add(loc.getMenu("Length.short"));
			nameList.add(loc.getMenu("Mean"));
			break;
		}
		return nameList;
	}

}
