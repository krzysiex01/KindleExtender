package kindleExtender.models;

public class Word extends Record {
    public Word(String id, String word, int count, String stem, String lang) {
        super(id);
        this.word = word;
        this.count = count;
        this.stem = stem;
        this.lang = lang;
    }

    private String lang;
    private String word;
    private String stem;
    private int count;

    private String translationValue;
    private String translationTo;
    private String partOfSpeech;

    public String getPartOfSpeech() {
        return partOfSpeech;
    }

    public void setPartOfSpeech(String partOfSpeech) {
        this.partOfSpeech = partOfSpeech;
    }

    public String getTranslationValue() {
        return translationValue;
    }

    public String getTranslationTo() {
        return translationTo;
    }

    public void setTranslationTo(String translationTo) {
        this.translationTo = translationTo;
    }

    public void setTranslationValue(String translationValue) {
        this.translationValue = translationValue;
    }

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

    public String getLanguage() {
        return lang;
    }
}
