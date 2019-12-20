package kindleExtender.models;

public class Word extends Record {
    public Word(String id, String word, int count) {
        super(id);
        this.word = word;
        this.count = count;
    }

    private String word;
    private int count;

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
