/** @author dracus

 * This class contains the major crawler functionalities
 * */

package SimpleCrawler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URL;
import java.util.ArrayList;

import javax.net.ssl.HttpsURLConnection;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**use of Milinda's Neo4j Storage class*/

import org.neo4j.graphdb.Node;

import Neo4StoredProcedures.Neo4jStoredProcedure;

//import java.util.List;

/**
 * Create Class "URLOb for ease of handling degrees and associated links.
 * Simplifies the process
 * */

class URLOb {
	/* Using public members for now! Take care!! */
	public URL Url;
	public int Degree;

	/* Overloading the constructor for future versatility. */
	public URLOb(URL url, int degree) {
		Url = url;
		Degree = degree;
	}

	public URLOb(URL url) {
		Url = url;
		Degree = 0;
	}

	public URLOb(String url, int deg) throws MalformedURLException {
		Url = new URL(url);
		Degree = deg;
	}

	public URLOb(String url) throws MalformedURLException {
		Url = new URL(url);
		Degree = 0;
	}

}

public class SimpleWikiCrawler {

	private ArrayList<URLOb> OutGoing;

	int degree = 0;
	boolean done;
	private int maxDegree;// maximum degree that the crawler will crawl to.
	private Neo4jStoredProcedure DB_Handler;
	String DB_NAME;

	public void setDBName(String dbn) {
		/**
		 * Use this method to set the Name of the Neo4j Database you want the
		 * graph to be stored to
		 */
		this.DB_NAME = dbn;
		DB_Handler.SetDatabase(dbn);
	}

	/* Overloaded constructors */
	public SimpleWikiCrawler(String DB) {
		// visited = new ArrayList<URLOb>();
		OutGoing = new ArrayList<URLOb>();
		this.DB_NAME = DB;

		DB_Handler = new Neo4jStoredProcedure();
		DB_Handler.SetDatabase(DB);
		// DB_Handler.CreateDatabase();
	}

	public SimpleWikiCrawler(int MaxDegs) {
		// visited = new ArrayList<URLOb>();
		OutGoing = new ArrayList<URLOb>();
		this.maxDegree = MaxDegs;
		DB_Handler = new Neo4jStoredProcedure();

	}

	public void SetMaxDegree(int max) {
		// visited = new ArrayList<URLOb>();
		OutGoing = new ArrayList<URLOb>();
		this.maxDegree = max;
	}

	private String getTitle(URLOb urlob) {
		String temp = urlob.Url.toString();
		String lastTitle = temp.substring((temp.lastIndexOf("/") + 1));
		return lastTitle;
	}

	public void navigate(URLOb url) {
		System.out.println(url.Url.toString() + " Degree: " + url.Degree);
		// visited.add(url);
		int tempdeg = url.Degree;
		if (tempdeg > this.maxDegree){
			return;
		}
		String urstring = url.Url.toString();
		try {

			//System.setProperty("https.proxyHost", "cache.mrt.ac.lk");
			//System.setProperty("https.proxyPort", "3128");
			//System.setProperty("http.proxyHost", "cache.mrt.ac.lk");
			//System.setProperty("http.proxyPort", "3128");

			Document document = null;
			boolean state = true;
			while (state) {
				try {
					document = Jsoup.connect(urstring).get();
					state = false;
				} catch (Exception e) {
				 	e.printStackTrace();
					try {
						Thread.sleep(2000);
					} catch (InterruptedException e1) {
						e1.printStackTrace();
					}
				}
			}
			Element content = document.getElementById("mw-content-text");

			Elements temp = content.getElementsByTag("p");
			int found = 0;
			for (Element t : temp) {
				Elements linx;
				if (found == 1)
					break;
				linx = t.getElementsByTag("a");
				if (t.getElementsByTag("b").size() == 0
						|| t.getElementsByTag("a").size() == 0) {
					// System.out.println("whoa ");
					continue;
				}
				// Element lx;
				for (Element ln : linx) {
					if (!ln.attr("title").startsWith("Help:")
							&& !ln.attr("title").contains(":")
							&& !ln.attr("href").startsWith("#")
							&& ln.attr("href").startsWith("/wiki")
							&& !IsInParantheses(ln.text(), t.text())) {
						// System.out.println(ln.text() + " " + ln.attr("href")
						// + " Degree: " + (tempdeg + 1));
						if (ln.attr("href").length() != 0)
							found = 1;
						URLOb tempNext = new URLOb(
								("https://en.wikipedia.org" + ln.attr("href")),
								(tempdeg + 1));
						OutGoing.add(tempNext);
						addNodeToGraph(getTitle(url), getTitle(tempNext));
					}

				}
				// break;
			}

			System.out.println("*************************");

			URLOb next = OutGoing.remove(0);

			while (next.Degree > this.maxDegree) {
				try {
					next = OutGoing.remove(0);
				} catch (IndexOutOfBoundsException e) {
					System.out.println("Finished!!!");
					done = true;
					return;
				}
			}

			if (next.Degree <= this.maxDegree) {

				navigate(next);

			} else
				return;

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private boolean IsInParantheses(String Title, String Text) {
		boolean flag = false;
		int pcount = 0;
		for (int i = 0; i < Text.length(); i++) {
			if (Text.charAt(i) == '(') {
				pcount++;
			}

			if (Text.charAt(i) == ')') {
				pcount--;
			}

			try {
				if (Text.substring(i, i + Title.length()).equals(Title)) {
					if (pcount != 0)
						flag = true;
					else
						flag = false;
					return flag;
				}
			} catch (Exception e) {
				return flag;
			}

		}

		return flag;
	}

	public void addNodeToGraph(String s_text, String d_text) {
		DB_Handler.addPageToDatabase(s_text, d_text);
	}

	// The main method is for trying out the crawler.
	public static void main(String[] args) {

		SimpleWikiCrawler htp = new SimpleWikiCrawler(2);

		htp.setDBName("lib/neo4j-community-1.9.2/data/WikiEx.db");
		// create an instance of Database
		htp.DB_Handler.CreateDatabase();

		try {
			htp.navigate(new URLOb(
					"http://en.wikipedia.org/wiki/Campaign_of_Danture", 0));
//          Node leaf = htp.DB_Handler.getLeafNode();
//			while (leaf != null) {
//				System.out.println(leaf.toString());
//				System.out
//						.println("Iteration Finished. Looking for leaf nodes...");
//				String url = "https://en.wikipedia.org/wiki/"
//						+ leaf.getProperty("title").toString();
//				htp.navigate(new URLOb(url, 0));
//				System.out.println("Navigation Iteration Finished......");
//				leaf = htp.DB_Handler.getLeafNode();
//			}

		} catch (MalformedURLException e) {
			e.printStackTrace();
		}

	}
}
