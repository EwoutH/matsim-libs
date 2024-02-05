package org.matsim.contrib.drt.teleportation;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Named;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.locationtech.jts.geom.prep.PreparedGeometry;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.population.Activity;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.PlanElement;
import org.matsim.api.core.v01.population.Route;
import org.matsim.contrib.drt.extension.estimator.DrtEstimator;
import org.matsim.contrib.drt.extension.estimator.DrtInitialEstimator;
import org.matsim.contrib.drt.routing.*;
import org.matsim.contrib.drt.run.DrtConfigGroup;
import org.matsim.contrib.drt.run.DrtControlerCreator;
import org.matsim.contrib.dvrp.router.*;
import org.matsim.contrib.dvrp.run.AbstractDvrpModeModule;
import org.matsim.contrib.dvrp.run.DvrpMode;
import org.matsim.contrib.dvrp.run.DvrpModes;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigGroup;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.controler.AbstractModule;
import org.matsim.core.controler.Controler;
import org.matsim.core.modal.ModalProviders;
import org.matsim.core.population.PopulationUtils;
import org.matsim.core.router.*;
import org.matsim.core.router.costcalculators.TravelDisutilityFactory;
import org.matsim.core.router.speedy.SpeedyALTFactory;
import org.matsim.core.router.util.LeastCostPathCalculatorFactory;
import org.matsim.core.router.util.TravelTime;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.core.utils.collections.QuadTrees;
import org.matsim.core.utils.io.IOUtils;
import org.matsim.core.utils.timing.TimeInterpretation;
import org.matsim.examples.ExamplesUtils;
import org.matsim.facilities.Facility;
import org.matsim.pt.transitSchedule.api.TransitScheduleReader;
import org.matsim.testcases.MatsimTestUtils;
import org.matsim.utils.gis.shp2matsim.ShpGeometryUtils;
import org.matsim.utils.objectattributes.attributable.Attributes;

import java.net.URL;
import java.util.*;

class Test2{

	@RegisterExtension public MatsimTestUtils utils = new MatsimTestUtils();

	@org.junit.jupiter.api.Test void test1() {

		URL url = IOUtils.extendUrl( ExamplesUtils.getTestScenarioURL( "mielec" ), "empty_config.xml" );
		Config config = ConfigUtils.loadConfig( url );

		config.network().setInputFile( "network.xml" );
		config.plans().setInputFile( "plans_only_drt_1.0.xml.gz" );

		config.controller().setOutputDirectory( utils.getOutputDirectory() );
		config.controller().setLastIteration( 0 );

		Scenario scenario = DrtControlerCreator.createScenarioWithDrtRouteFactory( config );
		ScenarioUtils.loadScenario( scenario );

		Controler controler = new Controler( scenario );

		// install the drt routing stuff, but not the mobsim stuff!

		controler.addOverridingModule( new AbstractModule(){
			@Override public void install(){
				this.addRoutingModuleBinding( "drt" ).to( DrtEstimatingRoutingModule.class );
			}
		} );

		controler.run();

	}

	private static class DrtEstimatingRoutingModule implements RoutingModule {

		private final RoutingModule drtRoutingModule;
		private DrtEstimatingRoutingModule( TripRouter tripRouter ) {
			this.drtRoutingModule = tripRouter.getRoutingModule( "drt" );
		}



		@Override
		public List<? extends PlanElement> calcRoute(RoutingRequest request) {
			List<? extends PlanElement> route = drtRoutingModule.calcRoute( request );

			DrtRouteCreator creator = null;
			creator.createRoute( departureTime, accessActLink, egressActLink, person, tripAttributes, routeFactories );

			DrtRouteFactory factory = null;
			Route drtRoute = factory.createRoute( startLinkId, endLinkId );

			// correct the attributes of the route as we need them

			DrtInitialEstimator estimator = new DrtInitialEstimator(){
			};

			DrtEstimator.Estimate estimate = estimator.estimate( route, 12. * 3600 );

			estimate.travelTime();
			estimate.distance();

			return route;
		}

	}

}
