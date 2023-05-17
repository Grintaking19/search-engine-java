package indexer;

import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@ToString
public class SearchWord {
    public String word;
    public int df;
    public List<WordData> data = new ArrayList<>();
}
