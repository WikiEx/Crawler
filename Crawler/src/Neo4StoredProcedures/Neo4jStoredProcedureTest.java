package Neo4StoredProcedures;

import static org.junit.Assert.*;

import java.io.File;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;


public class Neo4jStoredProcedureTest {

private Neo4jStoredProcedure db;
	
	@Before
	public void setup() throws Exception  {
	    db=new Neo4jStoredProcedure();
        db.SetDatabase("/home/milinda/Desktop/WikiEx");
        db.CreateDatabase();

		
	}

	
	@Test
	public void testAddPageToDataBase(){
		boolean state;
	    state=db.addPageToDatabase("computer_science","science");
		assertEquals(true, state);
		state=db.addPageToDatabase("computer_science","computation");
		assertEquals(true, state);
		state=db.addPageToDatabase("computer_science","computer");
 		assertEquals(true, state);
 		state=db.addPageToDatabase("computation","computer");
 		assertEquals(true, state);
 		state=db.addPageToDatabase("computation","science");
 		assertEquals(true, state);
		
	}
	
   
	
//	@After
//	public void tearDown()  {
//		
//		 db.StopDatabase();
//
//	        File file = new File("/home/milinda/Desktop/WikiEx/index");
//	        String[] myFiles;
//	        if (file.isDirectory()) {
//	            myFiles = file.list();
//	            for (int i = 0; i < myFiles.length; i++) {
//	                File myFile = new File(file, myFiles[i]);
//	                myFile.delete();
//	            }
//	        }
//	        file.delete();
//
//	        file = new File("/home/milinda/Desktop/WikiEx");
//
//	        if (file.isDirectory()) {
//	            myFiles = file.list();
//	            for (int i = 0; i < myFiles.length; i++) {
//	                File myFile = new File(file, myFiles[i]);
//	                myFile.delete();
//	            }
//	        }
//
//
//
//		
//	}

}
