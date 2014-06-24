package org.iceburg.home.actors;

import java.util.ArrayList;

import org.iceburg.home.main.StaticFunctions;

public class Race {
	String id, name, description;
	ArrayList<String> names;
	// trait boosts (other than health, range from 0-10)
	int healthTotal;
	// bonus station skill for Crewmen
	int energyFields, navigation, analysis, powerDistribution;

	public Race() {
		names = new ArrayList<String>();
	}
	@Override
	public String toString() {
		return name;
	}
	public String getRandName() {
		int r = StaticFunctions.randRange(0, names.size() - 1);
		return names.get(r);
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public ArrayList<String> getNames() {
		return names;
	}
	public void setNames(ArrayList<String> names) {
		this.names = names;
	}
	public int getHealthTotal() {
		return healthTotal;
	}
	public void setHealthTotal(int healthTotal) {
		this.healthTotal = healthTotal;
	}
	public int getEnergyFields() {
		return energyFields;
	}
	public void setEnergyFields(int energyFields) {
		this.energyFields = energyFields;
	}
	public int getNavigation() {
		return navigation;
	}
	public void setNavigation(int navigation) {
		this.navigation = navigation;
	}
	public int getAnalysis() {
		return analysis;
	}
	public void setAnalysis(int analysis) {
		this.analysis = analysis;
	}
	public int getPowerDistribution() {
		return powerDistribution;
	}
	public void setPowerDistribution(int powerDistribution) {
		this.powerDistribution = powerDistribution;
	}
	public String getId() {
		return id;
	}
	public void setId(String iD) {
		id = iD;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
}
