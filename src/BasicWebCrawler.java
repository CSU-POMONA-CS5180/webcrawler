import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.FileWriter;

import java.io.IOException;
import java.util.HashSet;

public class BasicWebCrawler {
	
    private HashSet<String> links;
    private int limit;
    
    public BasicWebCrawler() {
        links = new HashSet<String>();
        limit = 0;
    }

    BasicWebCrawler(int limit){
        links = new HashSet<String>();
    	this.limit = limit;
    }
    
    public void getPageLinks(String URL) throws InterruptedException {
        //4. Check if you have already crawled the URLs 
        //(we are intentionally not checking for duplicate content in this example)
        if (!links.contains(URL)) {
            try {
                //4. (i) If not add it to the index
                if (links.size() < limit && links.add(URL) ) {
                    System.out.println(URL);
                }
                else {
                	return;
                }
                
                //2. Fetch the HTML code
                Document document = Jsoup.connect(URL).get();
                producePage(document);
                
                //To set a delay for accessing the same 
            	//Thread.sleep(5*1000); 

                //3. Parse the HTML to extract links to other URLs
                Elements linksOnPage = document.select("a[href]");
                //Find the number of outlinks in current URL for report
                System.out.println(linksOnPage.size());

                //Find the number of images in current URL for report
                Elements imagesOnPage = document.select("img[src]");
                System.out.println(imagesOnPage.size());
                
                //Get Response Status
                Response response = Jsoup.connect(URL).followRedirects(false).execute();
                System.out.println(response.statusCode());
                
                //5. For each extracted URL... go back to Step 4.
                for (Element page : linksOnPage) {
                		getPageLinks(page.attr("abs:href"));
                }
            } catch (IOException e) {
                System.err.println("For '" + URL + "': " + e.getMessage());
            } /*catch (InterruptedException e) {
                System.err.println("For '" + URL + "': " + e.getMessage());
            }*/
        }
    }

    //Produce html file of current URL
    public void producePage(Document Doc) throws IOException {
    	//Replace the destination & output file name
    	//Will need different filename for each html
    	File file = new File("/Users/wilsenkosasih/desktop/test.html");
    	String html = Doc.html();
        
		FileWriter fileWriter = new FileWriter(file);
		fileWriter.write(html);
		
		fileWriter.flush();
		fileWriter.close();
    }
    
    public static void main(String[] args) throws InterruptedException {
        //1. Pick a URL from the frontier
    	BasicWebCrawler BWC = new BasicWebCrawler(1000);
    	BWC.getPageLinks("http://www.google.com/");
    }
}