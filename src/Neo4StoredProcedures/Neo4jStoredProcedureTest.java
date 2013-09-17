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
	public void tetsAddPageToDataBase(){
		boolean state;
	    state=db.addPageToDatabase("computer_science","Science","http://en.wikipedia.org/wiki/Computer_science","http://en.wikipedia.org/wiki/Science");
		assertEquals(true, state);
		state=db.addPageToDatabase("computer_science","Computation","http://en.wikipedia.org/wiki/Computer_science","http://en.wikipedia.org/wiki/Computation");
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
//
}
