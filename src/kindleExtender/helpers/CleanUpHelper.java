package kindleExtender.helpers;

import kindleExtender.models.Word;
import java.util.HashMap;
import java.util.Map;

public class CleanUpHelper {
    public SQLHelper sqlHelper;

    public CleanUpHelper(SQLHelper sqlHelper) {
        this.sqlHelper = sqlHelper;
    }

    // Removes all non alphanumerical characters form word.
    public int removeNonAlphabeticCharacters(){
        int editedWordsCount = 0;
        var wordsObservableList = sqlHelper.getWords();
        for (int i = 0; i < wordsObservableList.size(); i++) {
            var word = wordsObservableList.get(i);
            for (int j = 0 ; j < word.getWord().length(); j++)
            {
              if(!Character.isLetterOrDigit(word.getWord().charAt(j))) {
                    String editedWord = word.getWord().replaceAll("[^a-zA-Z0-9\\-]", "");
                    editedWord = editedWord.toLowerCase();
                    // Update word in database
                    sqlHelper.updateWord(word.id, editedWord);
                    editedWordsCount++;
                    break;
                }
            }
        }
        return editedWordsCount;
    }

    // Removes words with no corresponding key in LookUps table
    public int removeInconsistentData() {
        var wordsObservableList = sqlHelper.getWords();
        int removedWordsCount = 0;
        for (int i = 0; i < wordsObservableList.size(); i++) {
            if (wordsObservableList.get(i).getCount() == 0) {
                sqlHelper.removeWord(wordsObservableList.get(i).id);
                removedWordsCount++;
            }
        }
        return removedWordsCount;
    }

    // Merges words with different key but same stem value
    public int mergeDuplicates() {
        var wordsList = sqlHelper.getWords();
        int mergedWordsCount = 0;
        final Map<String,String> wordsMap = new HashMap<>();
        final Map<String,String> wordsMapDuplicates = new HashMap<>();

        for (Word word : wordsList) {
            String oldWord;
            if ((oldWord = wordsMap.put(word.getStem(),word.id)) != null) {
                sqlHelper.mergeWords(oldWord, word.id, word.getStem());
                mergedWordsCount++;
            }
        }
        return mergedWordsCount;
    }
}
