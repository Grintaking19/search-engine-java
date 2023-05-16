package indexer;/*
package indexer;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.util.ArrayList;

class DocIndexer implements Runnable {

    private String docText;
    private int docIndex;

    public DocIndexer(String docText, int docIndex) {
        this.docText = docText;
        this.docIndex = docIndex;
    }

    public void run() {

        Document doc = Jsoup.parse(docText);

        ArrayList<Thread> threads = new ArrayList<Thread>();

        String title = doc.getElementsByTag("title").text();

        if (!title.equals("")) {
            ArrayList<String> str = Indexer.removeStopwordsAndStem(title);
            threads.add(new Thread(new Indexer(docIndex, "title", 0, str)));
            threads.get(threads.size() - 1).start();
            Indexer_main.docSize[docIndex] += str.size();
        }

        int j;
        org.jsoup.select.Elements ele;

        for (int h = 1; h < 7; ++h) {
            ele = doc.select("h" + h);
            j = 0;
            for (Element s : ele) {
                ArrayList<String> str = Indexer.removeStopwordsAndStem(s.text());
                Indexer ind = new Indexer(docIndex, "h" + h, j, str);
                Runnable r = ind;
                threads.add(new Thread(r));
                threads.get(threads.size() - 1).start();
                ++j;
                Indexer_main.docSize[docIndex] += str.size();
            }
        }

        ele = doc.select("p");
        j = 0;
        for (Element s : ele) {
            ArrayList<String> str = Indexer.removeStopwordsAndStem(s.text());
            Indexer ind = new Indexer(docIndex, "p", j, str);
            Runnable r = ind;
            threads.add(new Thread(r));
            threads.get(threads.size() - 1).start();
            ++j;
            Indexer_main.docSize[docIndex] += str.size();
        }

        for (Thread t : threads) {
            try {
                t.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

}
*/
