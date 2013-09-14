import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import Neo4StoredProcedures.Neo4jStoredProcedure;

//import java.util.List;

class URLOb {

	public URL Url;
	public int Degree;
	

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

class SimpleWikiCrawler {

	private ArrayList<URLOb> visited, OutGoing;

	int degree = 0;
	boolean done;
	private int maxDegree;
	private Neo4jStoredProcedure DB_Handler;
	String DB_NAME;
	
	public void setDBName(String dbn){
		
		this.DB_NAME=dbn;
		DB_Handler.SetDatabase(dbn);
	}

	public SimpleWikiCrawler(String DB) {
		visited = new ArrayList<URLOb>();
		OutGoing = new ArrayList<URLOb>();
		this.DB_NAME=DB;
		
		DB_Handler=new Neo4jStoredProcedure();
		DB_Handler.SetDatabase(DB);
	}

	public SimpleWikiCrawler(int MaxDegs) {
		visited = new ArrayList<URLOb>();
		OutGoing = new ArrayList<URLOb>();
		this.maxDegree = MaxDegs;
		DB_Handler=new Neo4jStoredProcedure();
	}

	public void SetMaxDegree(int max) {
		visited = new ArrayList<URLOb>();
		OutGoing = new ArrayList<URLOb>();
		this.maxDegree = max;
	}

	public void navigate(URLOb url) {
		System.out.println(url.Url.toString()+" Degree: "+url.Degree);
		visited.add(url);
		int tempdeg = url.Degree;
		if (done||tempdeg > this.maxDegree)
			return;
		String urstring = url.Url.toString();
		try {
			Document document = Jsoup.connect(urstring).get();
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
//						System.out.println(ln.text() + " " + ln.attr("href")
//								+ " Degree: " + (tempdeg + 1));
						if (ln.attr("href").length() != 0)
							found = 1;
						OutGoing.add(new URLOb(("https://en.wikipedia.org" + ln
								.attr("href")), (tempdeg + 1)));
						//TODO addNodeToGraph();
					}

				}
				// break;
			}

			System.out.println("*************************");

			URLOb next = OutGoing.remove(0);

			while (next.Degree > this.maxDegree) {
				try{
					next = OutGoing.remove(0);
				}catch(IndexOutOfBoundsException e){
					System.out.println("Finished!!!");
					done=true;
					System.exit(0);
				}
			}

			if (next.Degree <= this.maxDegree) {

				navigate(next);

				// add to graph and navigate next

				/*
				 * while(visited.contains(next)){ } next=OutGoing.remove(0); }
				 * if(!visited.contains(next)){ navigate(next); }else {
				 * System.out.println("End!"); }
				 */

			} else
				return;

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public boolean IsInParantheses(String Title, String Text) {
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

	public void addNodeToGraph(String s_text,String s_link, String d_text, String d_link) {
		
	}

	public static void main(String[] args) {

		SimpleWikiCrawler htp = new SimpleWikiCrawler(2);
		try {
			htp.navigate(new URLOb(
					"http://en.wikipedia.org/wiki/Campaign_of_Danture", 0));
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
	}
}
