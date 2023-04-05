/*
 * *********************************************************************** *
 * project: org.matsim.*
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2023 by the members listed in the COPYING,        *
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
 * *********************************************************************** *
 */

package org.matsim.contrib.ev.discharging;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.vehicles.Vehicle;

/**
 * @author Michal Maciejewski (michalm)
 */
public class DrivingEnergyConsumptionEvent extends AbstractEnergyConsumptionEvent {
	public static final String EVENT_TYPE = "drivingEnergyConsumption";

	public DrivingEnergyConsumptionEvent(double time, Id<Vehicle> vehicleId, Id<Link> linkId, double energy, double endCharge) {
		super(time, vehicleId, linkId, energy, endCharge);
	}

	@Override
	public String getEventType() {
		return EVENT_TYPE;
	}
}
