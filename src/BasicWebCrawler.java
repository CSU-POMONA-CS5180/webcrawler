import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.BufferedReader;
import java.io.BufferedWriter;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.SocketException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

public class BasicWebCrawler {
	
    private HashSet<String> links; // data structure to keep unique to get information/crawl from
    private HashSet<String> disallowed_links; // data structure to keep list of disallowed links
    private HashSet<String> checked_base; //data structure to keep a list of hosts whose robots.txt have been checked
    private int limit; // # of links to crawl
    private ArrayList<Site> sites; // list of Site objects 
    private String scope; // restrict domain

	//Constructors
    public BasicWebCrawler() {
    	links = new HashSet<String>();
    	disallowed_links =  new HashSet<String>();
    	checked_base = new HashSet<String>();
    	limit = 0;
    	sites = new ArrayList<Site>();
    	scope = "";
    }

    public BasicWebCrawler(int limit){
    	links = new HashSet<String>();
    	disallowed_links =  new HashSet<String>();
    	checked_base = new HashSet<String>();
    	this.limit = limit;
    	sites = new ArrayList<Site>();
    	scope = "";
    }
    
    public BasicWebCrawler(int limit, String scope){
    	links = new HashSet<String>();
    	disallowed_links =  new HashSet<String>();
    	checked_base = new HashSet<String>();
    	this.limit = limit;
    	sites = new ArrayList<Site>();
    	this.scope = scope;
    }
	
    //Function to crawl a site, given its URL
    public void getPageLinks(String URL) throws InterruptedException, IOException {
    	//Check if we have acquired enough number of links specified by input from csv files 
    	//and if the URL contains specific character "?" (input) and "#" (section), and whether it's within scope
    	if(links.size() >= limit || !passPrimaryConstraint(URL))
    		return;
	    
    	//Form strings to avoid duplicate links formed by different protocol (http vs https) & directories "/", 
		URL url = new URL(URL);
    	String[] URLS = createDuplicateURLs(URL);
    		
    	//Check if the URL host's robots.txt has been checked
    	if(!checked_base.contains(url.getHost())){
    		handleRobotstxt(URL, url);
    	}
    	
    	//Check if the URL is a duplicate of an crawled link and whether or not it's disallowed based on the collected rules        	
    	if (passSecondaryConstraint(URL, URLS)) {
          try {                       
        	  //Get Response Status & Check file type 
        	  Response response = Jsoup.connect(URL).followRedirects(false).ignoreHttpErrors(true).ignoreContentType(true).execute();
        	  int status = response.statusCode();
        	  
                
        	  //If number of pages crawled is still under the limit & it is an accepted type, add it to the list to be crawled
        	  if (!passLastConstraint(response)) {
                	return;
        	  } 
                                
        	  //Fetch the HTML code
        	  Document document = Jsoup.connect(URL).ignoreHttpErrors(true)./*timeout(15*1000).*/get();
              links.add(URL);

              System.out.println(URL);
        	  System.out.println(links.size() + ": Retrieved the page");
        	  
        	  //Produce html document
        	  producePage(document);
        	  System.out.println(links.size() + ": Produced the page");

        	  //Create entry for report.html
        	  createReportEntry(URL, document, status);
        	  
        	  //Parse the HTML to extract links to other URLs
        	  Elements linksOnPage = document.select("a[href]");

        	                  
        	  //For each extracted URL... crawl them
        	  for (Element page : linksOnPage) {
        		  getPageLinks(page.attr("abs:href"));
        	  }        
            } catch (IOException e) {
                System.err.println("For '" + URL + "': " + e.getMessage());
            } 
        }
    }

    //Produce html file of current URL
    public void producePage(Document Doc) throws IOException {
    	//Replace the destination & output file name
    	File file = new File("/Users/wilsenkosasih/desktop/repository/html_"+ links.size() + ".html");
    	//File file = new File("C:\\Users\\Vincent\\Desktop\\repository\\html_"+ links.size() + ".html");
    	//File file = new File("C:\\Users\\snowf\\Desktop\\repository\\html_"+ links.size() + ".html");
    	
    	String html = Doc.html();
        
		FileWriter fileWriter = new FileWriter(file);
		fileWriter.write(html);
		
		fileWriter.flush();
		fileWriter.close();
    }
    
    public void printToHTML(BasicWebCrawler bwc) {
    	
    	String html = "<div><h1>Welcome to our Web-Crawler Page!</h1><p>Results are shown below...";
    	
    	//File f = new File("C:\\Users\\Vincent\\Desktop\\report.html");
    	File f = new File("/Users/wilsenkosasih/desktop/report.html");
    	//File f = new File("/Users/snowf/Desktop/report.html");

    	try{
            //1. clickable link to crawled URL.
            //2. link to downloaded page in repo folder.
            //3. HTTP status code
            //4. number of outlinks for crawled URL.
            //5. number of images
            BufferedWriter bw = new BufferedWriter(new FileWriter(f));
            bw.write(html + "<br><br>");
            
            html = "<table style=\"width:100%\"><tr><th style=\"; border: 1px solid black\">Clickable Link</th><th style=\"; border: 1px solid black\">Link to Repo Folder File</th>"
                    + "<th style=\"; border: 1px solid black\">HTTP status</th><th style=\"; border: 1px solid black\">No. outlinks</th><th style=\"; border: 1px solid black\">No. images</th></tr>";                    
            bw.write(html);
            html = "";
           //Access
        	for(int i = 0; i < bwc.sites.size(); i++) {
        		Site a = bwc.sites.get(i);
        		System.out.println();
        		html = "<tr>";
        		System.out.println(a.getUrl());
        		html += "<td style=\"; border: 1px solid black\">" + "<a href=\"" + a.getUrl() + "\">" + a.getUrl() + "</a></td>";
        		System.out.println(a.getDir());
        		html += "<td style=\"; border: 1px solid black\">" + "<a href=\"" + a.getDir() + "\">" + a.getDir() + "</a></td>";
        		System.out.println(a.getStatus());
        		html += "<td style=\"; border: 1px solid black\">" + a.getStatus() + "</td>";
        		System.out.println(a.getOutlink());
        		html += "<td style=\"; border: 1px solid black\">" + a.getOutlink() + "</td>";
        		System.out.println(a.getImages());
        		html += "<td style=\"; border: 1px solid black\">" + a.getImages() + "</td>";
        		html += "</tr>";
        		bw.write(html);
        		html = "";
        	}
            
            bw.close();
        }
        catch(IOException e){
            e.printStackTrace();
        }
    }
    
	
	/**
	 * loads all parameter input into the links HashSet. This is primarily 
	 * used to initially load disallowed addresses from robots.txt
	 * 
	 * @param input String[] 
	 */
	public void loadLinks(String[] input) {
		
		for(int i = 0; i < input.length ; ++i) {
			disallowed_links.add(input[i]);
		}
		System.out.println("** New Host & Robots.txt: Adding new disallowed links **");
		System.out.println(disallowed_links.toString());				// used to check disallowed_links data	
	}
	
	
	
	/**
	 * fetches the disallowed addresses of a website and returns in String form
	 * 
	 * @param URL String. ex. "http://www.google.com"
	 * @return String[] all disallowed addresses
	 * @throws IOException 
	 */
	public String[] fetchRobotRules(String URL) throws IOException, SocketException {

		ArrayList<String> roboRules = new ArrayList<String>();
		
		String[] disallowedURL = null;
		//Check if robots.txt exist
		//URL url = new URL(URL);
		
		//Previously needed this to avoid crash if robots.txt doesn't exist
		/*try {
			Response response = Jsoup.connect("https://" + url.getHost() + "/robots.txt").followRedirects(false).ignoreHttpErrors(true).execute();
			int status = response.statusCode();        
					
			if (status == 404) {
        		return disallowedURL;
			}
		} catch (SocketException ex) {
			System.out.println("continue");
		}*/
        
		
		try(BufferedReader input = new BufferedReader(
				new InputStreamReader(new URL( URL + "/robots.txt").openStream())))	// getting all input from robots.txt 
		{
			String line = null;
			loop: while((line = input.readLine()) != null) {		// going through robots.txt file as long as next line exists
				
				if(line.equalsIgnoreCase("user-agent: *")) {		// looking for user-agent: * for all web-crawl agents
					while((line = input.readLine()) != null) {		// while the next line exists
						
						if(line.toLowerCase().contains("user-agent") || line.toLowerCase().startsWith("#")
								|| line.toLowerCase().contains("sitemap:")) { // arrived at another set of agent rules, or a comment, or sitemap info 
							break loop;											// so break loop
						}
						if(line.toLowerCase().contains("disallow: /")) {
							roboRules.add(line);						// add line to 
						}		
					}			
				}
			}
			disallowedURL = new String[roboRules.size()];
			
			for(int i = 0; i < roboRules.size(); ++i) {
					
				disallowedURL[i] = URL + roboRules.get(i).substring(10);
			}
			
		}
		catch(Exception e) {
			System.out.println("** Robots.txt of new host not found **");						// file not found. only yahoo.com did this when testing for some reason
		}
		return disallowedURL;
	}
	
	/**
	 * Form strings to avoid duplicate links formed by different protocol (http vs https) & directories "/", 
	 * e.g. Given URL http://www.google.com/about, to check if
	 * URL[0] = https://www.google.com/about
	 * URL[1] = http://www.google.com/about/
	 * URL[2] = https://www.google.com/about/
	 * has already been crawled before
	 */
	public String[] createDuplicateURLs(String URL) throws MalformedURLException {
		String[] URLS = new String[3];
		
		URL url = new URL(URL);
		String URL2, URL3, URL4 = "";
		
	   	if((url.getProtocol().equals("http"))) {
	    		URL2 = "https://" + url.getHost() + url.getPath();
	    		if(URL.endsWith("/")) {
	    			URL3 = URL.substring(0, URL.length()-1);
	   				URL4 = URL2.substring(0, URL2.length()-1);
	    		}
	    		else {
	    			URL3 = URL + "/";
	    			URL4 = URL2 + "/";
	    		}
	   		}
	   	else {
	   		URL2 = "http://" + url.getHost() + url.getPath();
	   		if(URL.endsWith("/")) {
	   			URL3 = URL.substring(0, URL.length()-1);
	   			URL4 = URL2.substring(0, URL2.length()-1);
	   		}
	   		else {
	   			URL3 = URL + "/";
	   			URL4 = URL2 + "/";
	   		}
	   	}		
	   	URLS[0] = URL2;
	   	URLS[1] = URL3;
	   	URLS[2] = URL4;
		return URLS;
		
	}
	/** 
	 * Given URL, Document, and status of HTTP request to the URL, create a site object for report.html entry
	 * 
	 * @param URL
	 * @param document
	 * @param status
	 */
	public void createReportEntry(String URL, Document document, int status) {
		//Parse the HTML to extract links to other URLs
		Elements linksOnPage = document.select("a[href]");

  	  	//Find the number of outlinks in current URL for report
		int outlink = linksOnPage.size();

  	  	//Find the number of images in current URL for report
  	  	Elements imagesOnPage = document.select("img[src]");
  	  	int image = imagesOnPage.size();
        
  	  	//Create name for new file
  	  	String directory = "repository/html_" + (links.size()) + ".html";
        
  	  	Site newsite = new Site(URL, directory, status, outlink, image);
  	  	sites.add(newsite);
	}
	
	/**
	 * Given the URL, handles robots.txt of the host site and add it to the list
	 * @param URL
	 * @param url
	 * @throws IOException
	 */
	public void handleRobotstxt (String URL, URL url) throws IOException {
		System.out.println(URL);
		// fetch all robots.txt rules for user-agent:
		String[] fetchRobotRules = fetchRobotRules("https://" + url.getHost());		

		// load robots.txt rules into links, if it exists
		if(fetchRobotRules != null)
			loadLinks(fetchRobotRules);		
		// Add the host URL into the hashset to avoid repetitive rechecking 
		checked_base.add(url.getHost());
	}
	
	public boolean passPrimaryConstraint(String URL) {
		String[] list = new String[] {"?", "#", ".pdf", ".jpg", "jpeg", "png", "mailto:"};
		ArrayList <String> checker = new ArrayList<String>();
		checker.addAll(Arrays.asList(list));
		
		boolean sectionOrForm = false;
		for (String el: checker) {
			if(URL.contains(el))
					sectionOrForm = true;
		}
		
		boolean withinScope = URL.contains(scope);
    	
		boolean validProtocol = URL.contains("https://") || URL.contains("http://");			
		
    	return validProtocol && !sectionOrForm && withinScope;
	}
	
	public boolean passSecondaryConstraint(String URL, String[] URLS) throws MalformedURLException {
		boolean notDupe = !links.contains(URL) && !links.contains(URLS[0]) &&!links.contains(URLS[1]) &&!links.contains(URLS[2]);    			
		boolean notForbidden = true;
	
		for(String str : disallowed_links) {			
			//System.out.println(str);
			if(str != null) {
				str = str.replace("?","\\?");
				str = str.replace("*",".*");
			}
			if(URL.matches(".*" + str + ".*") || URLS[0].matches(".*" + str + ".*") || URLS[1].matches(".*" + str + ".*") || URLS[2].matches(".*" + str + ".*")) {
				notForbidden = false;
				break;
			}
		}

		//System.out.println(notForbidden);
		//System.out.println(URL);
    	return notDupe && notForbidden;
	}
	
	public boolean passLastConstraint(Response response) {
		String contentType = response.contentType();
		//System.out.println("Content type:" + contentType);
		if(contentType == null)
			return true;
		
		boolean accepted_content_type = contentType.contains("text/") || 
          								contentType.contains("application/xml") ||
          								contentType.contains("application/xhtml+xml");
		
		return accepted_content_type;
	}
	
	/**
	 * Clean repository directory before producing new html files
	 */
	public static void cleanDirectory() {
		for(File file: new java.io.File("/Users/wilsenkosasih/desktop/repository").listFiles()) 
		    if (!file.isDirectory()) 
		        file.delete();
	}
	
    public static void main(String[] args) throws InterruptedException, IOException {		
    		String csvFile = "/Users/wilsenkosasih/desktop/source.csv";
    		BufferedReader br = new BufferedReader(new FileReader(csvFile));
    		String line = br.readLine();
    		//1. Pick a URL from the frontier
    		String[] input = line.split(",",-1);
    		br.close();
    		    		
    		System.out.println("Seed: " + input[0]);
    		System.out.println("Size: " + input[1]);
    		System.out.println("Scope: " + input[2]);
    		
    		cleanDirectory();
    		
    		BasicWebCrawler BWC = new BasicWebCrawler(Integer.parseInt(input[1]), input[2]);
        	
    		BWC.getPageLinks(input[0]);
    		BWC.printToHTML(BWC);
    		
    		System.out.println("List of checked robots.txt: " + BWC.checked_base);
    }
}
