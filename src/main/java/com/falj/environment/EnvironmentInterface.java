/**
 * EnvironmentInterface.java
 * interface for environment
 * @version0.1
 * @see EnvironmentInterface
 * @see Agent
 * @author Frederic JEAN
 * @copyright (C) Frederic JEAN 2015
 * @date 01/07/2015
 * @notes  you can redistribute it and/or modify it under the terms of the 
 * Licence Creative Commons Attribution 4.0 International.
 * http://creativecommons.org/licenses/by/4.0/
 */


package com.falj.environment;

import java.util.Set;

import com.falj.agent.Agent;
import com.falj.agent.Interaction;


public abstract class EnvironmentInterface {

	public Agent agent;

	public Set<Interaction> interactions;

	public abstract Interaction enact(String a);

	public abstract void step();

}