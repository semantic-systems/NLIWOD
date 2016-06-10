package org.aksw.hawk.number;

interface IUnitLanguage {
	/**
	 * Converts all occurring natural language numerals to digits. If a unit is
	 * given, depending numeral will be converted to a base unit.
	 * 
	 * <pre>
	 * "$80 million" -> "$ 80000000"
	 * "10 miles" -> "1609.344 m"
	 * </pre>
	 * 
	 * @param q any string which may or may not contain numerals or units to
	 *            convert.
	 * @return Same string with all numerals as digits and all units converted
	 *         to a base unit.
	 */
	String convert(String q);

}
