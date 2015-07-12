/**
* Interaction.java
* structure of the interactions
* @version0.1
* @author Frederic JEAN
* @copyright (C) Frederic JEAN 2015
* @date 01/07/2015
* @notes  you can redistribute it and/or modify it under the terms of the 
* Licence Creative Commons Attribution 4.0 International.
* http://creativecommons.org/licenses/by/4.0/
*/

package com.falj.agent;

public class Interaction {

	private String hash;

	private Double valence;

	public Interaction() {
	}
	
	public Interaction(String h, Double v) { 
		hash = h;
		valence = v;
	}

	public Double getValence() {
		return valence;
	}

	public void setValence(double valence) {
		this.valence = valence;
	}

	public String getHash() {
		return hash;
	}

	public void setHash(String hash) {
		this.hash = hash;
	}
}