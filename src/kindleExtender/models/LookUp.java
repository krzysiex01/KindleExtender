package kindleExtender.models;

import javafx.beans.property.SimpleBooleanProperty;

public class LookUp extends Record {
    public LookUp(String id, String word, String usage, String book, long timestamp) {
        super(id);
        this.usage = usage;
        this.word = word;
        this.book = book;
        this.timestamp = timestamp;
        this.delete = new SimpleBooleanProperty(false);
    }

    private String usage;
    private String word;
    private String book;
    private long timestamp;
    private final SimpleBooleanProperty delete;

    public SimpleBooleanProperty deleteProperty() {
        return delete;
    }

    public boolean isDelete() {
        return delete.get();
    }

    public void setDelete(boolean delete) {
        this.delete.set(delete);
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

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
