package org.aksw.mlqa.analyzer.entityType;

import static org.junit.Assert.*;

import org.junit.Test;

import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;

public class EntityTypeTest {

	@Test
	public void PersonTest1() {
		EntityPerson personana = new EntityPerson();
		FastVector fvWekaAttributes = new FastVector();
		fvWekaAttributes.addElement(personana.getAttribute());
		Instances testinstances = new Instances("Test", fvWekaAttributes, 1 );
		Instance testinstance = new Instance(fvWekaAttributes.size());
		testinstance.setValue(personana.getAttribute(), (String) personana.analyze("Bart is a person."));
		assertTrue(testinstance.stringValue(personana.getAttribute()).equals("containsPerson"));
	}
	
	@Test
	public void PersonTest2() {
		EntityPerson personana = new EntityPerson();
		FastVector fvWekaAttributes = new FastVector();
		fvWekaAttributes.addElement(personana.getAttribute());
		Instances testinstances = new Instances("Test", fvWekaAttributes, 1 );
		Instance testinstance = new Instance(fvWekaAttributes.size());
		testinstance.setValue(personana.getAttribute(), (String) personana.analyze("Berlin is a city."));
		assertTrue(testinstance.stringValue(personana.getAttribute()).equals("containsNoPerson"));
	}
	
	@Test
	public void LocationTest1() {
		EntityLocation locationana = new EntityLocation();
		FastVector fvWekaAttributes = new FastVector();
		fvWekaAttributes.addElement(locationana.getAttribute());
		Instances testinstances = new Instances("Test", fvWekaAttributes, 1 );
		Instance testinstance = new Instance(fvWekaAttributes.size());
		testinstance.setValue(locationana.getAttribute(), (String) locationana.analyze("Berlin is a city."));
		assertTrue(testinstance.stringValue(locationana.getAttribute()).equals("containsLocation"));
	}
	
	@Test
	public void LocationTest2() {
		EntityLocation locationana = new EntityLocation();
		FastVector fvWekaAttributes = new FastVector();
		fvWekaAttributes.addElement(locationana.getAttribute());
		Instances testinstances = new Instances("Test", fvWekaAttributes, 1 );
		Instance testinstance = new Instance(fvWekaAttributes.size());
		testinstance.setValue(locationana.getAttribute(), (String) locationana.analyze("Bart is a person."));
		assertTrue(testinstance.stringValue(locationana.getAttribute()).equals("containsNoLocation"));
	}
	
	@Test
	public void OrganizationTest1() {
		EntityOrganization organa = new EntityOrganization();
		FastVector fvWekaAttributes = new FastVector();
		fvWekaAttributes.addElement(organa.getAttribute());
		Instances testinstances = new Instances("Test", fvWekaAttributes, 1 );
		Instance testinstance = new Instance(fvWekaAttributes.size());
		testinstance.setValue(organa.getAttribute(), (String) organa.analyze("The United Nations are an organization."));
		assertTrue(testinstance.stringValue(organa.getAttribute()).equals("containsOrganization"));
	}
	
	@Test
	public void OrganizationTest2() {
		EntityOrganization organa = new EntityOrganization();
		FastVector fvWekaAttributes = new FastVector();
		fvWekaAttributes.addElement(organa.getAttribute());
		Instances testinstances = new Instances("Test", fvWekaAttributes, 1 );
		Instance testinstance = new Instance(fvWekaAttributes.size());
		testinstance.setValue(organa.getAttribute(), (String) organa.analyze("Bart is a person."));
		assertTrue(testinstance.stringValue(organa.getAttribute()).equals("containsNoOrganization"));
	}
	
	@Test
	public void MoneyTest1() {
		EntityMoney monana = new EntityMoney();
		FastVector fvWekaAttributes = new FastVector();
		fvWekaAttributes.addElement(monana.getAttribute());
		Instances testinstances = new Instances("Test", fvWekaAttributes, 1 );
		Instance testinstance = new Instance(fvWekaAttributes.size());
		testinstance.setValue(monana.getAttribute(), (String) monana.analyze("One Dollar is worth more than one Yen."));
		assertTrue(testinstance.stringValue(monana.getAttribute()).equals("containsMoney"));
	}
	
	@Test
	public void MoneyTest2() {
		EntityMoney monana = new EntityMoney();
		FastVector fvWekaAttributes = new FastVector();
		fvWekaAttributes.addElement(monana.getAttribute());
		Instances testinstances = new Instances("Test", fvWekaAttributes, 1 );
		Instance testinstance = new Instance(fvWekaAttributes.size());
		testinstance.setValue(monana.getAttribute(), (String) monana.analyze("Bart lives in Berlin."));
		assertTrue(testinstance.stringValue(monana.getAttribute()).equals("containsNoMoney"));
	}

	@Test
	public void PercentTest1() {
		EntityPercent percana = new EntityPercent();
		FastVector fvWekaAttributes = new FastVector();
		fvWekaAttributes.addElement(percana.getAttribute());
		Instances testinstances = new Instances("Test", fvWekaAttributes, 1 );
		Instance testinstance = new Instance(fvWekaAttributes.size());
		testinstance.setValue(percana.getAttribute(), (String) percana.analyze("5% of 100 equal 5."));
		assertTrue(testinstance.stringValue(percana.getAttribute()).equals("containsPercent"));
	}

	@Test
	public void PercentTest2() {
		EntityPercent percana = new EntityPercent();
		FastVector fvWekaAttributes = new FastVector();
		fvWekaAttributes.addElement(percana.getAttribute());
		Instances testinstances = new Instances("Test", fvWekaAttributes, 1 );
		Instance testinstance = new Instance(fvWekaAttributes.size());
		testinstance.setValue(percana.getAttribute(), (String) percana.analyze("Bart lives in Berlin."));
		assertTrue(testinstance.stringValue(percana.getAttribute()).equals("containsNoPercent"));
	}

	@Test
	public void TimeTest1() {
		EntityTime timeana = new EntityTime();
		FastVector fvWekaAttributes = new FastVector();
		fvWekaAttributes.addElement(timeana.getAttribute());
		Instances testinstances = new Instances("Test", fvWekaAttributes, 1 );
		Instance testinstance = new Instance(fvWekaAttributes.size());
		testinstance.setValue(timeana.getAttribute(), (String) timeana.analyze("4PM is a time."));
		assertTrue(testinstance.stringValue(timeana.getAttribute()).equals("containsTime"));
	}
	
	@Test
	public void TimeTest2() {
		EntityTime timeana = new EntityTime();
		FastVector fvWekaAttributes = new FastVector();
		fvWekaAttributes.addElement(timeana.getAttribute());
		Instances testinstances = new Instances("Test", fvWekaAttributes, 1 );
		Instance testinstance = new Instance(fvWekaAttributes.size());
		testinstance.setValue(timeana.getAttribute(), (String) timeana.analyze("Bart is a person."));
		assertTrue(testinstance.stringValue(timeana.getAttribute()).equals("containsNoTime"));
	}
	
	
}
