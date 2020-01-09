package kindleExtender.helpers;

import javafx.collections.ObservableList;
import javafx.scene.chart.XYChart;
import kindleExtender.models.Book;
import kindleExtender.models.LookUp;
import kindleExtender.models.Word;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

public class StatsHelper {
    public List<Book> getTopBooks(int count, List<Book> books) {
        List<Book> result = new ArrayList<>();
        books.stream()
                .sorted(Comparator.comparingInt(Book::getWordCount).reversed())
                .limit(count)
                .forEach((book) -> {
                    result.add(book);
                });
        return result;
    }

    public List<Word> getTopWords(int count, List<Word> words,String langCode) {
        List<Word> result = new ArrayList<>();
        words.stream()
                .filter(word -> word.getLanguage().equals(langCode))
                .sorted(Comparator.comparingInt(Word::getCount).reversed())
                .limit(count)
                .forEach((word) -> {
                    result.add(word);
                });
        return result;
    }

    public List<XYChart.Data> getLookUpTimeLine(int numberOfIntervals, List<LookUp> lookUpsList) {
        List<XYChart.Data> result = new ArrayList<>();
        if (lookUpsList == null || lookUpsList.isEmpty())
            return result;

        lookUpsList.sort(Comparator.comparingLong(LookUp::getTimestamp));

        long min = lookUpsList.get(0).getTimestamp();
        long max = lookUpsList.get(lookUpsList.size() - 1).getTimestamp();

        long intervalValue = (max - min) / (long) numberOfIntervals;

        Iterator<LookUp> it = lookUpsList.iterator();
        long tmpMax =  min + intervalValue;
        int count = 0;
        for (int i = 0; i < numberOfIntervals; i++) {
            while (it.hasNext() && it.next().getTimestamp() < tmpMax){
                count++;
            }
            result.add(new XYChart.Data<Long,Integer>((tmpMax - intervalValue / 2), count));
            tmpMax += intervalValue;
            count = 0;
        }

        return result;
    }

}
