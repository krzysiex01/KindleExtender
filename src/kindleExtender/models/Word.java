package kindleExtender.models;

public class Word extends Record {
    public Word(String id, String word, int count,String stem) {
        super(id);
        this.word = word;
        this.count = count;
        this.stem = stem;
    }

    private String word;
    private String stem;
    private int count;

    public String getStem() {
        return stem;
    }

    public int getCount() {
        return count;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }
}
