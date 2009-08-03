/* *********************************************************************** *
 * project: org.matsim.*
 * DgDeltaScoreIncomeChart
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2009 by the members listed in the COPYING,        *
 *                   LICENSE and WARRANTY file.                            *
 * email           : info at matsim dot org                                *
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *   See also COPYING, LICENSE and WARRANTY file                           *
 *                                                                         *
 * *********************************************************************** */
package playground.dgrether.analysis.population;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.matsim.core.population.PlanImpl.Type;
import org.matsim.core.utils.charts.XYLineChart;
import org.matsim.core.utils.collections.Tuple;

import playground.dgrether.utils.DgChartUtils;



public class DgAvgDeltaScoreIncomeGroupModeSwitchChart {
	
	private static final Logger log = Logger.getLogger(DgAvgDeltaScoreIncomeGroupModeSwitchChart.class);
	
	private DgAnalysisPopulation ana;
	
	private int numberOfClasses = 10;

	private double minIncome = Double.POSITIVE_INFINITY;
	private double maxIncome = Double.NEGATIVE_INFINITY;
	
	public DgAvgDeltaScoreIncomeGroupModeSwitchChart(DgAnalysisPopulation ana) {
		this.ana = ana;
		this.calculateMinMaxIncome();
	}

	private void calculateMinMaxIncome() {
		double y;
		for (DgPersonData d : ana.getPersonData().values()) {
			y = d.getIncome().getIncome();
			if (y< minIncome) {
				this.minIncome = y;
			}
			
			if (y > maxIncome) {
				this.maxIncome = y;
			}
			
		}
	}

	private static final class IncomeClass {		
		double min;
		double max;
		String title;
		
		public IncomeClass(double min, double max){
			this.min = min; 
			this.max = max;
			this.title = this.min + " - " + this.max;
		}
		
	}
	
	public void writeFile(String filename) {
		XYLineChart chart = new XYLineChart("", "Income groups", "Delta Utility");

		List<DgAnalysisPopulation> popsPerModeSwitch = new ArrayList<DgAnalysisPopulation>();
		DgAnalysisPopulation car2carPop = new DgAnalysisPopulation();
		DgAnalysisPopulation pt2ptPop = new DgAnalysisPopulation();
		DgAnalysisPopulation pt2carPop = new DgAnalysisPopulation();
		DgAnalysisPopulation car2ptPop = new DgAnalysisPopulation();
		popsPerModeSwitch.add(car2carPop);
		popsPerModeSwitch.add(pt2ptPop);
		popsPerModeSwitch.add(pt2carPop);
		popsPerModeSwitch.add(car2ptPop);
		
		for (DgPersonData d : ana.getPersonData().values()) {
			DgPlanData planDataRun1 = d.getPlanData().get(DgAnalysisPopulation.RUNID1);
			DgPlanData planDataRun2 = d.getPlanData().get(DgAnalysisPopulation.RUNID2);
			
			if (planDataRun1.getPlan().getType().equals(Type.CAR) && planDataRun2.getPlan().getType().equals(Type.CAR)) {
				car2carPop.getPersonData().put(d.getPersonId(), d);
			}
			else if (planDataRun1.getPlan().getType().equals(Type.PT) && planDataRun2.getPlan().getType().equals(Type.PT)) {
				pt2ptPop.getPersonData().put(d.getPersonId(), d);
			}
			else if (planDataRun1.getPlan().getType().equals(Type.PT) && planDataRun2.getPlan().getType().equals(Type.CAR)) {
				pt2carPop.getPersonData().put(d.getPersonId(), d);
			}
			else if (planDataRun1.getPlan().getType().equals(Type.CAR) && planDataRun2.getPlan().getType().equals(Type.PT)){
				car2ptPop.getPersonData().put(d.getPersonId(), d);
			}
		}
		
		
		
		
		
		for (DgAnalysisPopulation population : popsPerModeSwitch) {
			// calculate thresholds for income classes
			IncomeClass[] incomeThresholds = new IncomeClass[this.numberOfClasses];
			DgAnalysisPopulation[] groups = new DgAnalysisPopulation[this.numberOfClasses];
			
			double deltaY = this.maxIncome / (this.numberOfClasses -1);
			for (int i = 0; i < incomeThresholds.length; i++) {
				incomeThresholds[i] = new IncomeClass(i *deltaY, i+1 * deltaY);
				groups[i] = new DgAnalysisPopulation();
			}
			
			for (DgPersonData d : population.getPersonData().values()) {
				double y = d.getIncome().getIncome();
				int pos = (int) (y / deltaY);
				IncomeClass c = incomeThresholds[pos];
				if (!(c.min <= y) && (y <= c.max)) {
					throw new IllegalStateException();
				}
				groups[pos].getPersonData().put(d.getPersonId(), d);
			}
			
			String[] groupDescriptions = new String[groups.length];
			double[] xvalues = new double[groups.length];
			double[] yvalues = new double[groups.length];
			
			List<Tuple<Double, Double>> values = new ArrayList<Tuple<Double, Double>>();
			
			
			
			for (int i = 0; i < groups.length; i++) {
//				groupDescriptions[i] = incomeThresholds[i].title;
//				xvalues[i] = incomeThresholds[i].max;
//				yvalues[i] = calcAverageScoreDifference(groups[i]);
				Double avgScore = calcAverageScoreDifference(groups[i]);
				if (avgScore != null) {
					values.add(new Tuple<Double, Double>(incomeThresholds[i].max, avgScore));
				}

			}
			Tuple<double[], double[]> data = DgChartUtils.createArray(values);
			
			chart.addSeries("avg delta utility", data.getFirst(), data.getSecond());	
		}
		
		
		
		
		chart.saveAsPng(filename, 1000, 600);
		log.info("DgAvgDeltaScoreIncomeGroupChart written to : " +filename);
	}

	
	private Double calcAverageScoreDifference(DgAnalysisPopulation group) {
		Double deltaScoreSum = 0.0;
		for (DgPersonData d : group.getPersonData().values()){
			DgPlanData planDataRun1 = d.getPlanData().get(DgAnalysisPopulation.RUNID1);
			DgPlanData planDataRun2 = d.getPlanData().get(DgAnalysisPopulation.RUNID2);
			deltaScoreSum += planDataRun2.getScore() - planDataRun1.getScore();
		}
		Double avg = null;
		if (group.getPersonData().size() > 1) {
			avg = deltaScoreSum/group.getPersonData().size()  - 0.03;
		}
		return avg;
	}

	public int getNumberOfClasses() {
		return numberOfClasses;
	}

	
	public void setNumberOfClasses(int numberOfClasses) {
		this.numberOfClasses = numberOfClasses;
	}
	


}
