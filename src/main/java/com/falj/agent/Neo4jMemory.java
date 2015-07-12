/**
* Neo4jmemory.java
* structure of the memory - interface with the neo4j database
* @version0.1
* @author Frederic JEAN
* @copyright (C) Frederic JEAN 2015
* @date 01/07/2015
* @notes  you can redistribute it and/or modify it under the terms of the 
* Licence Creative Commons Attribution 4.0 International.
* http://creativecommons.org/licenses/by/4.0/
*/

package com.falj.agent;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.DynamicLabel;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.graphdb.schema.IndexCreator;
import org.neo4j.graphdb.schema.IndexDefinition;
import org.neo4j.graphdb.schema.Schema;
import org.neo4j.helpers.collection.IteratorUtil;
import org.neo4j.io.fs.FileUtils;

class Neo4jMemory {

	private static final String DB_PATH = "target/neo4j-";
	
	// parameters for the organization of the memory
	private static final Long MIN_NTRIALS = 10L;
	private static final double MIN_PROBA = 0.1;
	private static final double DEFAULT_PROBABILITY = 0.5;
	private static final double MIN_PROBABILITY_TO_ENACT = 0.5;
	private static final double MIN_PROBABILITY_FOR_LEARNING = 0.95;
	private static final double MIN_TRIALS_PROBABILITY_TO_KEEP = 0.001;
	private static final double MAX_PROCLIVITY_TOLERANCE = 0.1;

	GraphDatabaseService graphDb;
	Node nodeage;

	private Long age = 0L;

	private String defaultInteraction;

	private static enum RelTypes implements RelationshipType
	{
		LEADS_TO
	}

	public Neo4jMemory(Set<Interaction> interactions, String path) throws IOException {
		
		// "000" stands for the tests. The database is deleted
		if(path.compareTo("000") == 0) {
			FileUtils.deleteRecursively( new File( DB_PATH + path ) );
			age = 0L;
		}

		graphDb = new GraphDatabaseFactory().newEmbeddedDatabase( DB_PATH + path );
		registerShutdownHook( graphDb, age );

		try ( Transaction tx = graphDb.beginTx() )
		{
			Schema schema = graphDb.schema();
			IndexCreator index = schema.indexFor(DynamicLabel.label( "interaction" ));
			if(index == null ) {
				schema.indexFor( DynamicLabel.label( "interaction" ) )
						.on( "name" )
						.create();
			}
			IndexCreator index2 = schema.indexFor(DynamicLabel.label( "age" ));
			if(index2 == null ) {
				schema.indexFor( DynamicLabel.label( "age" ) )
						.on( "name" )
						.create();
			}	
			nodeage = graphDb.findNode(DynamicLabel.label( "age" ), "name", "age");
			if( nodeage == null ) {
				nodeage = graphDb.createNode(DynamicLabel.label( "age" ));
				nodeage.setProperty("age",0L);
				nodeage.setProperty("name","age");
				age = 0L;
			} else {
				age = (Long) nodeage.getProperty("age");
			}
			tx.success();
		}
		for (Interaction interaction : interactions) {
			addNode(interaction.getHash(), interaction.getValence());
		}
		
		defaultInteraction = interactions.iterator().next().getHash();
	}

	private boolean addNode(String hash, Double valence) {
		boolean result = false;
		try ( Transaction tx = graphDb.beginTx() )
		{
			Node n = graphDb.findNode(DynamicLabel.label("interaction"), "name", hash);
			if( n == null ) {
				result = true;
				Node node = graphDb.createNode(DynamicLabel.label( "interaction" ));
				node.setProperty("name", hash);
				node.setProperty("valence", valence);
				node.setProperty("tried", 0L );
				node.setProperty("success", 0L );
				node.setProperty("birth", age);
				Iterable<Node> nodes = IteratorUtil.asIterable(graphDb.findNodes(DynamicLabel.label( "interaction" )));
				for (Node node2 : nodes) {
					Relationship r0 = node.createRelationshipTo(node2, RelTypes.LEADS_TO);
					r0.setProperty("nTrials", 0L);
					r0.setProperty("nSuccess", 0L);
					r0.setProperty("probability", DEFAULT_PROBABILITY);
					r0.setProperty("proclivity", DEFAULT_PROBABILITY * (Double) node2.getProperty("valence")); 
					if( !node.equals(node2) ) {
						Relationship r1 = node2.createRelationshipTo(node, RelTypes.LEADS_TO);
						r1.setProperty("nTrials", 0L);
						r1.setProperty("nSuccess", 0L);
						r1.setProperty("probability", DEFAULT_PROBABILITY);
						r1.setProperty("proclivity", DEFAULT_PROBABILITY * (Double) node.getProperty("valence")); 
					}
				}
			}
			tx.success();
		}
		return result;

	}

	public String chooseInteraction(Interaction previousInteraction) {
		String result = defaultInteraction;
		Node previousNode;
		age ++;

		try ( Transaction tx = graphDb.beginTx() )
		{
			previousNode  = graphDb.findNode(DynamicLabel.label("interaction"), "name", previousInteraction.getHash());
			tx.success();
		}
		if( previousNode == null ) {
			addNode(previousInteraction.getHash(), previousInteraction.getValence());
			try ( Transaction tx = graphDb.beginTx() )
			{
				previousNode  = graphDb.findNode(DynamicLabel.label("interaction"), "name", previousInteraction.getHash());
				tx.success();
			}	
		}

		try ( Transaction tx = graphDb.beginTx() )
		{
			Iterable<Relationship> rel = previousNode.getRelationships(Direction.OUTGOING);
			Map<String,Double> possibleResponses = new HashMap<>();
			List<String> response = new ArrayList<String>();
			double proclivityMax = -Double.MAX_VALUE;
			for (Relationship relationship : rel) {

				double probability = (Double) relationship.getProperty("probability");
				if( probability >= MIN_PROBABILITY_TO_ENACT ) {
					double proclivity = (Double) relationship.getProperty("proclivity");
					if( proclivity > proclivityMax ) {
						proclivityMax = proclivity;
					}
					possibleResponses.put((String) relationship.getEndNode().getProperty("name"), proclivity);
				}
			}

			proclivityMax -= MAX_PROCLIVITY_TOLERANCE * Math.abs(proclivityMax);

			// find best
			for (String key : possibleResponses.keySet()) {
				if( proclivityMax <= possibleResponses.get(key)) {
					response.add(key);
				}
			}
			if(!response.isEmpty()) {
				result = response.get((int) Math.floor(Math.random()*response.size()));
			}
			nodeage.setProperty("age",age);

			tx.success();
		}
		return result;
	}

	public void learn(Interaction previousInteraction, Interaction result,
			String interactionHash) {
		Node previousNode;
		Node resultNode;
		Node triedNode;

		try ( Transaction tx = graphDb.beginTx() )
		{
			previousNode = graphDb.findNode(DynamicLabel.label("interaction"), "name", previousInteraction.getHash());
			resultNode = graphDb.findNode(DynamicLabel.label("interaction"), "name", result.getHash());
			triedNode = graphDb.findNode(DynamicLabel.label("interaction"), "name", interactionHash);
			tx.success();
		}
		// check if result is known
		if(resultNode == null) {
			addNode(result.getHash(), result.getValence());
			try ( Transaction tx = graphDb.beginTx() )
			{
				resultNode = graphDb.findNode(DynamicLabel.label("interaction"), "name", result.getHash());
				tx.success();
			}

		}
		
		try ( Transaction tx = graphDb.beginTx() )
		{
			// add tried and success properties
			resultNode.setProperty("tried", (Long)resultNode.getProperty("tried") + 1L );
			resultNode.setProperty("success", (Long)resultNode.getProperty("success") + 1L );

			// check error
			if(!resultNode.equals(triedNode)) {
				Iterable<Relationship> rel = previousNode.getRelationships(Direction.OUTGOING);
				for (Relationship relationship : rel) {
					if( triedNode.equals(relationship.getEndNode())) {
						relationship.setProperty("nTrials", (Long)relationship.getProperty("nTrials") + 1L);
						updateRelationShip(relationship);
					}
				}
			}
		
			Iterable<Relationship> rel = previousNode.getRelationships(Direction.OUTGOING);
			for (Relationship relationship : rel) {
				if( resultNode.equals(relationship.getEndNode())) {
					relationship.setProperty("nTrials", (Long)relationship.getProperty("nTrials") + 1L);
					relationship.setProperty("nSuccess", (Long)relationship.getProperty("nSuccess") + 1L);
					updateRelationShip(relationship);
				}
			}
			tx.success();
		}

	}

	private void updateRelationShip(Relationship relationship) {
		Double probability;
		Double proclivity;
		probability = (double) (Long) relationship.getProperty("nSuccess") / (double) (Long) relationship.getProperty("nTrials");
		if(  (Long) relationship.getProperty("nTrials") < MIN_NTRIALS ) {
			probability = DEFAULT_PROBABILITY;
		}
		proclivity = probability * (Double)relationship.getEndNode().getProperty("valence");

		relationship.setProperty("probability", probability );
		relationship.setProperty("proclivity", proclivity);
	}

	private static void registerShutdownHook( final GraphDatabaseService graphDb, final Long age )
	{
		// Registers a shutdown hook for the Neo4j instance so that it
		// shuts down nicely when the VM exits (even if you "Ctrl-C" the
		// running application).
		Runtime.getRuntime().addShutdownHook( new Thread()
		{
			@Override
			public void run()
			{
				Node nodeage = graphDb.findNode(DynamicLabel.label( "age" ), "name", "age");
				if( nodeage != null ) {
					try ( Transaction tx = graphDb.beginTx() ) {
						nodeage.setProperty("age", age);
						tx.success();
					}
				}
				graphDb.shutdown();
			}
		} );
	}

	public void sleep() {
		int numberOfRelationDeleted = 0;
		int numberOfNodeLearned = 0;
		int numberOfNodeForgot = 0;
		Set<Interaction> toAdd = new HashSet<>();
		try ( Transaction tx = graphDb.beginTx() )
		{
			Iterable<Node> nodes = IteratorUtil.asIterable(graphDb.findNodes(DynamicLabel.label( "interaction" )));
			for (Node node : nodes) {
				// refrain from altering primitive interactions
				if( (Long)node.getProperty("birth") > 0L ) {
					Iterable<Relationship> relationships = node.getRelationships(Direction.OUTGOING);
					for (Relationship relationship : relationships) {
						double probability = (Double) relationship.getProperty("probability");
						if( probability <= MIN_PROBA) {
							relationship.delete();
							numberOfRelationDeleted++;
						} else {
							if((Long) relationship.getProperty("nTrials") <= MIN_TRIALS_PROBABILITY_TO_KEEP * (age - (Long) node.getProperty("birth"))) {
								relationship.delete();
							}
						}

						if( probability >= MIN_PROBABILITY_FOR_LEARNING) {
							String newHash = node.getProperty("name") + "/" + relationship.getEndNode().getProperty("name");
							Node newNode = graphDb.findNode(DynamicLabel.label("interaction"), "name", newHash);
							if( newNode == null ) {
								Interaction interaction = new Interaction(
										newHash, 
										(Double)node.getProperty("valence") + (Double)relationship.getEndNode().getProperty("valence") );
								toAdd.add(interaction);
							}
						}					
					}
				}
			}
			tx.success();
		}
		try ( Transaction tx = graphDb.beginTx() )
		{	
			Iterable<Node> nodes = IteratorUtil.asIterable(graphDb.findNodes(DynamicLabel.label( "interaction" )));

			for (Node node : nodes) {
				int incoming = node.getDegree(Direction.INCOMING);
				int outgoing = node.getDegree(Direction.OUTGOING);
				if((outgoing + incoming )== 0) {
					node.delete();
					numberOfNodeForgot++;
				}
			}
			tx.success();
		}

		for (Interaction interaction : toAdd) {
			if( addNode(interaction.getHash(), interaction.getValence()) ) {
				numberOfNodeLearned++;
			}
		}
		System.out.println("relationShip deleted : " + numberOfRelationDeleted + "\n" +
				"nodes forgotten      : " + numberOfNodeForgot + "\n" + 
				"nodes learned        : " + numberOfNodeLearned );

	}
}