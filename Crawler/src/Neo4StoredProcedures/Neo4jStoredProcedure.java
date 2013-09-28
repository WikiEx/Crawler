/**
 * @author Milinda Fernando.
 * 10/09/2013
 * 06:09 PM
 *
 */
package Neo4StoredProcedures;

import java.io.File;
import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.neo4j.cypher.javacompat.ExecutionEngine;
import org.neo4j.cypher.javacompat.ExecutionResult;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.kernel.EmbeddedGraphDatabase;

public class Neo4jStoredProcedure {

	private GraphDatabaseFactory graphDbFactory;
	private EmbeddedGraphDatabase graphDb;
	private String databasePath;
	private File dbFolder;
	private Node startNode;
	private Date date;
	private ExecutionEngine execute_eng;
	private ExecutionResult result;

	public void SetDatabase(String databasePath) {

		this.databasePath = databasePath;
		dbFolder = new File(databasePath);
		graphDbFactory = new GraphDatabaseFactory();
		date = new Date();

	}

	public void CreateDatabase() {
		if (dbFolder.exists() && dbFolder.list().length != 0) {
			// Database Exits.
			graphDb = (EmbeddedGraphDatabase) graphDbFactory
					.newEmbeddedDatabaseBuilder(this.databasePath)
					.loadPropertiesFromFile(
							this.databasePath + "/neo4j.properties")
					.newGraphDatabase();
		} else {

			graphDb = (EmbeddedGraphDatabase) graphDbFactory
					.newEmbeddedDatabase(databasePath);
			this.createStartNode();
			execute_eng = new ExecutionEngine(this.graphDb);
		}

		registerShutdownHook(graphDb);// To Ensure that the database is shutdown
		// properly when JVM Exits
	}

	public void StopDatabase() {

		try {

			graphDb.shutdown();
		} catch (Exception e) {
			System.out
					.println("Error Occurred while Shutting down the database");
		}

	}

	private static void registerShutdownHook(final EmbeddedGraphDatabase graphDb) {
		// Registers a shutdown hook for the Neo4j instance so that it
		// shuts down nicely when the VM exits (even if you "Ctrl-C" the
		// running example before it's completed)
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				graphDb.shutdown();
			}
		});
	}

	private void createStartNode() {

		Transaction tx = this.graphDb.beginTx();
		try {
			this.startNode = graphDb.createNode();
			startNode.setProperty("message",
					"This is the start node of the database");
			startNode.setProperty("timeStamp", this.date.getTime());
			tx.success();
		} catch (Exception e) {
			System.out.println("Error Occurred while creating the node.");
			tx.failure();
		} finally {

			tx.finish();
		}

	}

	public boolean addPageToDatabase(String p_source_title,
			String p_destination_title, String p_source_link,
			String p_destination_link) {

		boolean state = false;
		Long source_id;
		Long destination_id;

		source_id = this.isExists(p_source_title);
		destination_id = this.isExists(p_destination_title);
		Node source;
		Node destination;
		Relationship rel;
		if (source_id == null && destination_id == null) {

			Transaction tx = this.graphDb.beginTx();
			try {

				source = this.graphDb.createNode();
				source.setProperty("link", p_source_link);
				source.setProperty("title", p_source_title);
				destination = this.graphDb.createNode();
				destination.setProperty("link", p_destination_link);
				destination.setProperty("title", p_destination_title);
				rel = source
						.createRelationshipTo(destination, RelTypes.LeadsTo);
				rel.setProperty("timestamp",
						new Timestamp(new Date().getTime()).toString());
				rel = this.startNode.createRelationshipTo(source,
						RelTypes.LeadsTo);
				rel.setProperty("timestamp",
						new Timestamp(new Date().getTime()).toString());
				tx.success();
				state = true;
			} catch (Exception e) {
				System.out.println("Error occured in the addPageToDatabase");
				tx.failure();
				state = false;
			} finally {
				tx.finish();
			}

		} else if (source_id == null && destination_id != null) {

			Transaction tx = this.graphDb.beginTx();

			try {
				source = this.graphDb.createNode();
				source.setProperty("link", p_source_link);
				source.setProperty("title", p_source_title);
				destination = this.getNodeByID(destination_id);
				rel = source
						.createRelationshipTo(destination, RelTypes.LeadsTo);
				rel = this.startNode.createRelationshipTo(source,
						RelTypes.LeadsTo);
				rel = source
						.createRelationshipTo(destination, RelTypes.LeadsTo);
				tx.success();
				state = true;
			} catch (Exception e) {
				System.out.println("Error occured in the addPageToDatabase");
				tx.failure();
				state = false;

			} finally {

				tx.finish();
			}

		} else if (destination_id == null && source_id != null) {

			Transaction tx = this.graphDb.beginTx();
			try {

				destination = this.graphDb.createNode();
				destination.setProperty("link", p_destination_link);
				destination.setProperty("title", p_destination_title);
				source = this.getNodeByID(source_id);
				rel = source
						.createRelationshipTo(destination, RelTypes.LeadsTo);
				tx.success();
				state = true;

			} catch (Exception e) {
				System.out.println("Error occured in the addPageToDatabase");
				tx.failure();
				state = false;

			} finally {

				tx.finish();
			}

		} else  {

			source = this.getNodeByID(source_id);
			destination = this.getNodeByID(destination_id);
			if (!this.isRelationshipExists(source, destination)) {
				Transaction tx = graphDb.beginTx();
				try {
					source.createRelationshipTo(destination, RelTypes.LeadsTo);
					tx.success();
					state = true;
				} catch (Exception e) {
					System.out
							.println("Error occured in the addPageToDatabase");
					tx.failure();
					state = false;

				} finally {

					tx.finish();

				}
			}

		}

		return state;
	}

	public boolean addPageToDatabase(String p_source_title,
			String p_destination_title) {

		boolean state = false;
		Long source_id;
		Long destination_id;

		source_id = this.isExists(p_source_title);
		destination_id = this.isExists(p_destination_title);
		Node source;
		Node destination;
		Relationship rel;
		if (source_id == null && destination_id == null) {

			Transaction tx = this.graphDb.beginTx();
			try {

				source = this.graphDb.createNode();
				source.setProperty("title", p_source_title);
				destination = this.graphDb.createNode();
				destination.setProperty("title", p_destination_title);
				rel = source
						.createRelationshipTo(destination, RelTypes.LeadsTo);
				rel.setProperty("timestamp",
						new Timestamp(new Date().getTime()).toString());
				rel = this.startNode.createRelationshipTo(source,
						RelTypes.LeadsTo);
				rel.setProperty("timestamp",
						new Timestamp(new Date().getTime()).toString());
				tx.success();
				state = true;
			} catch (Exception e) {
				System.out.println("Error occured in the addPageToDatabase");
				tx.failure();
				state = false;
			} finally {
				tx.finish();
			}

		} else if (source_id == null && destination_id != null) {

			Transaction tx = this.graphDb.beginTx();

			try {
				source = this.graphDb.createNode();
				source.setProperty("title", p_source_title);
				destination = this.getNodeByID(destination_id);
				rel = this.startNode.createRelationshipTo(source,
						RelTypes.LeadsTo);
				rel.setProperty("timestamp",
						new Timestamp(new Date().getTime()).toString());
				rel = source
						.createRelationshipTo(destination, RelTypes.LeadsTo);
				rel.setProperty("timestamp",
						new Timestamp(new Date().getTime()).toString());
				tx.success();
				state = true;
			} catch (Exception e) {
				System.out.println("Error occured in the addPageToDatabase");
				tx.failure();
				state = false;

			} finally {

				tx.finish();
			}

		} else if (destination_id == null && source_id != null) {

			Transaction tx = this.graphDb.beginTx();
			try {

				destination = this.graphDb.createNode();
				destination.setProperty("title", p_destination_title);
				source = this.getNodeByID(source_id);
				rel = source
						.createRelationshipTo(destination, RelTypes.LeadsTo);
				rel.setProperty("timestamp",
						new Timestamp(new Date().getTime()).toString());
				tx.success();
				state = true;

			} catch (Exception e) {
				System.out.println("Error occured in the addPageToDatabase");
				tx.failure();
				state = false;

			} finally {

				tx.finish();
			}

		} else  {
			
			source=this.getNodeByID(source_id);
			destination = this.getNodeByID(destination_id);

			Transaction tx = this.graphDb.beginTx();
			try {
				rel=source.createRelationshipTo(destination, RelTypes.LeadsTo);
				rel.setProperty("timestamp",
						new Timestamp(new Date().getTime()).toString());
				tx.success();
				state = true;
			} catch (Exception e) {
				System.out
						.println("Error occured in the addPageToDatabase");
				tx.failure();
				state = false;

			} finally {

				tx.finish();

			}

			
//			source = this.getNodeByID(source_id);
//			destination = this.getNodeByID(destination_id);
//			if (!this.isRelationshipExists(source, destination)) {
//				Transaction tx = this.graphDb.beginTx();
//				try {
//					rel=source.createRelationshipTo(destination, RelTypes.LeadsTo);
//					rel.setProperty("timestamp",
//							new Timestamp(new Date().getTime()).toString());
//					tx.success();
//					state = true;
//				} catch (Exception e) {
//					System.out
//							.println("Error occured in the addPageToDatabase");
//					tx.failure();
//					state = false;
//
//				} finally {
//
//					tx.finish();
//
//				}
//			}

		}

		return state;
	}

	public boolean isRelationshipExists(Node p_source, Node p_destination) {

		Map<String, Object> params = new HashMap<String, Object>();
		params.put("s_id", p_source.getId());
		params.put("d_id", p_destination.getId());
		String cypherquerry = "START s=Node({s_id}) ,d=Node({d_id}) MATCH p=allShortestPaths(s-[:LeadsTo* .. 1]->d) RETURN p;";
		result = execute_eng.execute(cypherquerry, params);
		Iterator<Node> iterator = result.columnAs("p");
		if (iterator.hasNext()) {
			return true;
		} else {
			return false;
		}

	}

	private static enum RelTypes implements RelationshipType {
		LeadsTo

	}

	public Long isExists(String p_source) {

		Map<String, Object> params = new HashMap<String, Object>();
		params.clear();
		params.put("start_node_id", this.startNode.getId());
		params.put("p_source", p_source);
		String cypherquerry = "START n=NODE({start_node_id}) MATCH n-[:LeadsTo*]->data WHERE data.title={p_source} RETURN data;";
		result = execute_eng.execute(cypherquerry, params);
		Iterator<Node> iterator = result.columnAs("data");
		Node data;
		if (iterator.hasNext()) {

			data = iterator.next();
			return data.getId();

		} else {
			return null;
		}

	}

	public Node getLeafNode() {

		String cypherquerry = "START n=NODE(1) MATCH n-[:LeadsTo*]->leaf WHERE NOT(leaf-->()) RETURN DISTINCT leaf AS leaf;";
		result = execute_eng.execute(cypherquerry);
		Iterator<Node> iterator = result.columnAs("leaf");
		if (iterator.hasNext()) {
			Node temp = iterator.next();
			return temp;
		} else {
			return null;
		}
	}

	public Node getNodeByID(Long p_id) {

		Map<String, Object> params = new HashMap<String, Object>();
		params.clear();
		params.put("node_id", p_id);
		String cypherquerry = "START n=NODE({node_id}) RETURN n AS data;";
		result = execute_eng.execute(cypherquerry, params);
		Iterator<Node> iterator = result.columnAs("data");

		if (iterator.hasNext()) {
			return iterator.next();
		} else {
			return null;
		}
	}

}
