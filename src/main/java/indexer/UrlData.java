package indexer;

public class UrlData {
    public String URL;
    public String FilePath;
    public double popularity;

    public UrlData(String URL, String filePath, double popularity) {
        this.URL = URL;
        FilePath = filePath;
        this.popularity = popularity;
    }
}
