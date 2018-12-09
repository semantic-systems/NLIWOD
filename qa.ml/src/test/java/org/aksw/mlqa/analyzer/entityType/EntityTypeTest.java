package org.aksw.mlqa.analyzer.entityType;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;

import org.junit.Test;

import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;

public class EntityTypeTest {

	@Test
	public void PersonTest1() {
		EntityPerson personana = new EntityPerson();
		ArrayList<Attribute> fvWekaAttributes = new ArrayList<Attribute>();
		fvWekaAttributes.add(personana.getAttribute());
		new Instances("Test", fvWekaAttributes, 1);
		Instance testinstance = new DenseInstance(fvWekaAttributes.size());
		testinstance.setValue(personana.getAttribute(), (String) personana.analyze("Bart is a person."));
		assertTrue(testinstance.stringValue(personana.getAttribute()).equals("Person"));
	}

	@Test
	public void PersonTest2() {
		EntityPerson personana = new EntityPerson();
		ArrayList<Attribute> fvWekaAttributes = new ArrayList<Attribute>();
		fvWekaAttributes.add(personana.getAttribute());
		new Instances("Test", fvWekaAttributes, 1);
		Instance testinstance = new DenseInstance(fvWekaAttributes.size());
		testinstance.setValue(personana.getAttribute(), (String) personana.analyze("Berlin is a city."));
		assertTrue(testinstance.stringValue(personana.getAttribute()).equals("NoPerson"));
	}

	@Test
	public void LocationTest1() {
		EntityLocation locationana = new EntityLocation();
		ArrayList<Attribute> fvWekaAttributes = new ArrayList<Attribute>();
		fvWekaAttributes.add(locationana.getAttribute());
		new Instances("Test", fvWekaAttributes, 1);
		Instance testinstance = new DenseInstance(fvWekaAttributes.size());
		testinstance.setValue(locationana.getAttribute(), (String) locationana.analyze("Berlin is a city."));
		assertTrue(testinstance.stringValue(locationana.getAttribute()).equals("Location"));
	}

	@Test
	public void LocationTest2() {
		EntityLocation locationana = new EntityLocation();
		ArrayList<Attribute> fvWekaAttributes = new ArrayList<Attribute>();
		fvWekaAttributes.add(locationana.getAttribute());
		new Instances("Test", fvWekaAttributes, 1);
		Instance testinstance = new DenseInstance(fvWekaAttributes.size());
		testinstance.setValue(locationana.getAttribute(), (String) locationana.analyze("Bart is a person."));
		assertTrue(testinstance.stringValue(locationana.getAttribute()).equals("NoLocation"));
	}

	@Test
	public void OrganizationTest1() {
		EntityOrganization organa = new EntityOrganization();
		ArrayList<Attribute> fvWekaAttributes = new ArrayList<Attribute>();
		fvWekaAttributes.add(organa.getAttribute());
		new Instances("Test", fvWekaAttributes, 1);
		Instance testinstance = new DenseInstance(fvWekaAttributes.size());
		testinstance.setValue(organa.getAttribute(), (String) organa.analyze("The United Nations are an organization."));
		assertTrue(testinstance.stringValue(organa.getAttribute()).equals("Organization"));
	}

	@Test
	public void OrganizationTest2() {
		EntityOrganization organa = new EntityOrganization();
		ArrayList<Attribute> fvWekaAttributes = new ArrayList<Attribute>();
		fvWekaAttributes.add(organa.getAttribute());
		new Instances("Test", fvWekaAttributes, 1);
		Instance testinstance = new DenseInstance(fvWekaAttributes.size());
		testinstance.setValue(organa.getAttribute(), (String) organa.analyze("Bart is a person."));
		assertTrue(testinstance.stringValue(organa.getAttribute()).equals("NoOrganization"));
	}

	@Test
	public void MoneyTest1() {
		EntityMoney monana = new EntityMoney();
		ArrayList<Attribute> fvWekaAttributes = new ArrayList<Attribute>();
		fvWekaAttributes.add(monana.getAttribute());
		new Instances("Test", fvWekaAttributes, 1);
		Instance testinstance = new DenseInstance(fvWekaAttributes.size());
		testinstance.setValue(monana.getAttribute(), (String) monana.analyze("One Dollar is worth more than one Yen."));
		assertTrue(testinstance.stringValue(monana.getAttribute()).equals("Money"));
	}

	@Test
	public void MoneyTest2() {
		EntityMoney monana = new EntityMoney();
		ArrayList<Attribute> fvWekaAttributes = new ArrayList<Attribute>();
		fvWekaAttributes.add(monana.getAttribute());
		new Instances("Test", fvWekaAttributes, 1);
		Instance testinstance = new DenseInstance(fvWekaAttributes.size());
		testinstance.setValue(monana.getAttribute(), (String) monana.analyze("Bart lives in Berlin."));
		assertTrue(testinstance.stringValue(monana.getAttribute()).equals("NoMoney"));
	}

	@Test
	public void PercentTest1() {
		EntityPercent percana = new EntityPercent();
		ArrayList<Attribute> fvWekaAttributes = new ArrayList<Attribute>();
		fvWekaAttributes.add(percana.getAttribute());
		new Instances("Test", fvWekaAttributes, 1);
		Instance testinstance = new DenseInstance(fvWekaAttributes.size());
		testinstance.setValue(percana.getAttribute(), (String) percana.analyze("5% of 100 equal 5."));
		assertTrue(testinstance.stringValue(percana.getAttribute()).equals("Percent"));
	}

	@Test
	public void PercentTest2() {
		EntityPercent percana = new EntityPercent();
		ArrayList<Attribute> fvWekaAttributes = new ArrayList<Attribute>();
		fvWekaAttributes.add(percana.getAttribute());
		new Instances("Test", fvWekaAttributes, 1);
		Instance testinstance = new DenseInstance(fvWekaAttributes.size());
		testinstance.setValue(percana.getAttribute(), (String) percana.analyze("Bart lives in Berlin."));
		assertTrue(testinstance.stringValue(percana.getAttribute()).equals("NoPercent"));
	}

	@Test
	public void DateTest1() {
		EntityDate dateana = new EntityDate();
		ArrayList<Attribute> fvWekaAttributes = new ArrayList<Attribute>();
		fvWekaAttributes.add(dateana.getAttribute());
		new Instances("Test", fvWekaAttributes, 1);
		Instance testinstance = new DenseInstance(fvWekaAttributes.size());
		testinstance.setValue(dateana.getAttribute(), (String) dateana.analyze("The olympic games in 1992 were the best."));
		assertTrue(testinstance.stringValue(dateana.getAttribute()).equals("Date"));
	}

	@Test
	public void DateTest2() {
		EntityDate dateana = new EntityDate();
		ArrayList<Attribute> fvWekaAttributes = new ArrayList<Attribute>();
		fvWekaAttributes.add(dateana.getAttribute());
		new Instances("Test", fvWekaAttributes, 1);
		Instance testinstance = new DenseInstance(fvWekaAttributes.size());
		testinstance.setValue(dateana.getAttribute(), (String) dateana.analyze("Who fucked up?"));
		assertTrue(testinstance.stringValue(dateana.getAttribute()).equals("NoDate"));
	}

}
