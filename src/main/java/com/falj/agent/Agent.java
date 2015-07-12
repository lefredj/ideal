/**
* Agent.java
* Core control of an ideal agent
* @version0.1
* @see Interaction
* @see Neo4jMemory
* @author Frederic JEAN
* @copyright (C) Frederic JEAN 2015
* @date 01/07/2015
* @notes  you can redistribute it and/or modify it under the terms of the 
* Licence Creative Commons Attribution 4.0 International.
* http://creativecommons.org/licenses/by/4.0/
*/

package com.falj.agent;


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.falj.environment.Environment;
import com.falj.environment.EnvironmentInterface;


public class Agent {

	Environment environment;
	Interaction previousInteraction;
	Neo4jMemory memory;
	private Long age = 1L;

	
	public Agent( EnvironmentInterface e, String path) throws IOException {
		environment = (Environment) e;

		// Initialization of the neo4j database with e.interactions
		memory = new Neo4jMemory(e.interactions, path);
		previousInteraction = e.interactions.iterator().next();
	}

	public String step() {
		String description;
		
		// choose next interaction to try considering the previous interaction
		String nextInteraction = memory.chooseInteraction(previousInteraction);

		// enact
		Interaction result = enact(nextInteraction);

		// print (previousInteraction)/(resultInteraction)/(triedInteraction) - age
//		System.out.print("("+previousInteraction.getHash()+")/("+result.getHash()+")/("+nextInteraction + ") - " + age + "\r");
		description = "\n("+previousInteraction.getHash()+")/("+result.getHash()+")/("+nextInteraction + ")\nage : " + age +"\n\n";
		// learn
		memory.learn(previousInteraction, result, nextInteraction);

		// remember last previousInteraction
		previousInteraction = result;

		// regularly organize memory (during sleep)
//		if( age % 500 == 0 ) {
//			System.out.println("sleep " + age);
			description += memory.sleep();
//			System.out.println();
//		}

		age ++;
		return description;
	}

	/**
	* enact(String interaction)
	* enact the composed interaction
	* @return      actual enacted interaction
	* @param       hash of the interaction to be enacted
	* @see EnvironmentInterface#enact
	*/
	private Interaction enact(String interaction) {

		// Experiment of the complete composed interaction
		String hash = null;
		double valence = 0;
		for(String h : interaction.split("/")) {
			Interaction result = environment.enact(h);
			if(hash == null) {
				hash = result.getHash();
			} else {
				hash += "/" + result.getHash();
			}
			valence += result.getValence();

			if( h.compareTo(result.getHash()) != 0 ) {
				break;
			}
		}

		return new Interaction(hash, valence);
	}

}