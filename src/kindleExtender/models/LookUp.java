package kindleExtender.models;

public class LookUp extends Record {
    public LookUp(String id, String word, String usage, String book) {
        super(id);
        this.usage = usage;
        this.word = word;
        this.book = book;
    }

    private String usage;
    private String word;
    private String book;

    public String getUsage() {
        return usage;
    }
    public void setUsage(String context) {
        this.usage = context;
    }

    public String getWord() {
        return word;
    }
    public void setWord(String word) {
        this.word = word;
    }

    public void setBook(String book) {
        this.book = book;
    }

    public String getBook() {
        return book;
    }
}
