/* *********************************************************************** *
 * project: org.matsim.*
 * RandomizingTimeDistanceTravelDisutilityFactory.java
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2007 by the members listed in the COPYING,        *
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

package org.matsim.core.router.costcalculators;

import org.apache.log4j.Logger;
import org.matsim.core.config.groups.PlanCalcScoreConfigGroup;
import org.matsim.core.config.groups.PlanCalcScoreConfigGroup.ScoringParameterSet;
import org.matsim.core.router.util.TravelDisutility;
import org.matsim.core.router.util.TravelTime;

import java.util.Collections;
import java.util.Set;

/**
 * A factory for a disutility that leads to randomized Pareto search. Starting
 * point is to have something like disutility(link) = alpha * time + beta *
 * money. Evidently, for beta=0 one obtains the time-minimal path, and for
 * alpha=0 the money-minimal path. The current version will randomize the
 * prefactor of the monetary term. It is implemented in a way that the random
 * number is drawn once per routing call, i.e. the route is computed with a
 * constant tradeoff between money and time, but with the next call it will be a
 * different trade-off. <br/>
 * The idea is to come up with different routes, with the hope that one of them
 * ends up being a good one for the scoring function. See, e.g., <a href=
 * "https://arxiv.org/abs/1002.4330v1">https://arxiv.org/abs/1002.4330v1</a> for
 * how to generate route alternatives (where Pareto routing is one option), and
 * <a href=
 * "https://doi.org/10.1016/j.procs.2014.05.488">https://doi.org/10.1016/j.procs.2014.05.488</a>
 * for a paper testing the approach.
 */
public class RandomizingTimeDistanceTravelDisutilityFactory implements TravelDisutilityFactory {
	private static final Logger log = Logger.getLogger(RandomizingTimeDistanceTravelDisutilityFactory.class);

	private static int wrnCnt = 0;
	private static int normalisationWrnCnt = 0;

	private final String mode;
	private double sigma = 0.;
	private final PlanCalcScoreConfigGroup cnScoringGroup;

	public RandomizingTimeDistanceTravelDisutilityFactory(final String mode, PlanCalcScoreConfigGroup cnScoringGroup) {
		this.mode = mode;
		this.cnScoringGroup = cnScoringGroup;
	}

	@Override
	public TravelDisutility createTravelDisutility(final TravelTime travelTime) {
		// Decision regarding handling of parameters per subpopulation: given
		// randomization, being very exact is not very important.
		// Thus, focus is on keeping one value for cost of time and of distance in the
		// disutility object,
		// while maximizing diversity in the outcomes
		logWarningsIfNecessary(cnScoringGroup);

		double minMarginalCostOfTime_s = Double.POSITIVE_INFINITY;
		double maxMarginalCostOfDistance_m = Double.NEGATIVE_INFINITY;

		for (ScoringParameterSet scoringParams : cnScoringGroup.getScoringParametersPerSubpopulation().values()) {
			final PlanCalcScoreConfigGroup.ModeParams params = scoringParams.getModes().get( mode ) ;
			if ( params == null ) {
				// it is OK if the mode is not in all subpopulations
				continue;
			}

			/* Usually, the travel-utility should be negative (it's a disutility) but the cost should be positive. Thus negate the utility.*/
			final double marginalCostOfTime_s = (-params.getMarginalUtilityOfTraveling() / 3600.0) + (scoringParams.getPerforming_utils_hr() / 3600.0);
			final double marginalCostOfDistance_m = - params.getMonetaryDistanceRate() * scoringParams.getMarginalUtilityOfMoney() 
					- params.getMarginalUtilityOfDistance() ;

			if (marginalCostOfDistance_m > maxMarginalCostOfDistance_m) maxMarginalCostOfDistance_m = marginalCostOfDistance_m;
			if (marginalCostOfTime_s < minMarginalCostOfTime_s) minMarginalCostOfTime_s = marginalCostOfTime_s;

		}

		if (Double.isInfinite(minMarginalCostOfTime_s) || Double.isInfinite(maxMarginalCostOfDistance_m)) {
			throw new RuntimeException("could not determine routing parameters for mode "+mode+
			". Make sure that this mode has parameters in the planCalcScore config group for at least one subpopulation");
		}

		double normalization = 1;
		if ( sigma != 0. ) {
			normalization = 1. / Math.exp(this.sigma * this.sigma / 2);
			if (normalisationWrnCnt < 10) {
				normalisationWrnCnt++;
				log.info(" sigma: " + this.sigma + "; resulting normalization: " + normalization);
			}
		}

		return new RandomizingTimeDistanceTravelDisutility(
				travelTime,
				minMarginalCostOfTime_s,
				maxMarginalCostOfDistance_m,
				normalization,
				sigma);
	}

	private void logWarningsIfNecessary(final PlanCalcScoreConfigGroup cnScoringGroup) {
		if ( wrnCnt < 1 ) {
			wrnCnt++ ;
			for (ScoringParameterSet scoringParams : cnScoringGroup.getScoringParametersPerSubpopulation().values()) {
				if ( scoringParams.getModes().get( mode ).getMonetaryDistanceRate() > 0. ) {
					log.warn("Monetary distance cost rate needs to be NEGATIVE to produce the normal " +
							"behavior; just found positive.  Continuing anyway.") ;
				}
			}

			final Set<String> monoSubpopKeyset = Collections.singleton( null );
			if ( !cnScoringGroup.getScoringParametersPerSubpopulation().keySet().equals( monoSubpopKeyset ) ) {
				log.warn( "Scoring parameters are defined for different subpopulations." +
						" The routing disutility will only consider the ones of the default subpopulation.");
				log.warn( "This warning can safely be ignored if disutility of traveling only depends on travel time.");
			}
		}
	}

	public RandomizingTimeDistanceTravelDisutilityFactory setSigma(double val ) {
		this.sigma = val ;
		return this;
	}
}
