package kindleExtender.models;

public class Book extends Record {
    private String title;
    private int wordCount;

    public Book(String id, String title, int wordCount) {
        super(id);
        this.title = title;
        this.wordCount = wordCount;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getWordCount() {
        return wordCount;
    }

    public void setWordCount(int wordCount) {
        this.wordCount = wordCount;
    }
}
