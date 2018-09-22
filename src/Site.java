public class Site {
	private String url, dir;
	private int status, outlink, images;
	
	public Site(String url, String dir, int status, int outlink, int images) {
		this.url = url;
		this.dir = dir;
		this.status = status;
		this.outlink = outlink;
		this.images = images;
	}
	
	public String getUrl() {
		return url;
	}
	
	public String getDir() {
		return dir;
	}
	
	public int getStatus() {
		return status;
	}

	public int getOutlink() {
		return outlink;
	}
	
	public int getImages() {
		return images;
	}
}