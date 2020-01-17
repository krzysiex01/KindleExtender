package kindleExtender.helpers;

import kindleExtender.models.Book;
import kindleExtender.models.LookUp;
import kindleExtender.models.Word;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.sql.*;
import java.util.*;

public class SQLHelper {

    public SQLHelper(String fileURL) {
        this.fileURL = fileURL;
        _conn = connect(fileURL);
        try {
            _conn.setAutoCommit(false);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        hasUnsavedChanges = false;
        currentLanguageFilters = getListOfLanguages();
    }

    // Indicates if any (uncomitted) changes to database have been made
    public boolean hasUnsavedChanges;

    // Contains list of currently applied Language filters, stored as language codes
    private List<String> currentLanguageFilters;
    // URL to currently open database file
    private String fileURL;
    // Stores connection to currently open database
    private Connection _conn;

    private static Connection connect(String fileURL) {
        String url = "jdbc:sqlite:" + fileURL;
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(url);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return conn;
    }

    private String getCurrentFilters() {
        StringBuilder result = new StringBuilder();
        for (var langCode: currentLanguageFilters){
            result.append("lang=").append("\'").append(langCode).append("\'").append(" OR ");
        }
        if (result.length() == 0)
            result.append("lang=").append("empty").append(" OR ");

        return result.substring(0,result.length()-4);
    }

    private String getCurrentFiltersWithColumnName() {
        StringBuilder result = new StringBuilder();
        for (var langCode: currentLanguageFilters){
            result.append("WORDS.lang=").append("\'").append(langCode).append("\'").append(" OR ");
        }
        if (result.length() == 0)
            result.append("lang=").append("empty").append(" OR ");

        return result.substring(0,result.length()-4);
    }

    private List<String> getListOfLanguages() {
        String sql = "SELECT DISTINCT lang FROM WORDS";
        List<String> langList = new ArrayList<>();

        try {
            Statement stmt = _conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            // loop through the result set
            while (rs.next()) {
                langList.add(rs.getString("lang"));
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return langList;
    }

    public List<Word> getWords() {
        String sql = "SELECT * FROM WORDS WHERE " + getCurrentFilters();
        List<Word> wordList = new ArrayList<Word>();

        try {
            Statement stmt = _conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            // loop through the result set
            while (rs.next()) {
                wordList.add(new Word(rs.getString("id"), rs.getString("word"), getLookUpsCountOnWordKey(rs.getString("id")), rs.getString("stem"), rs.getString("lang")));
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return wordList;
    }

    public Word getWord(String id) {
        String sql = "SELECT * FROM WORDS WHERE id='" + id + "'";
        try {
            Statement stmt = _conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            return new Word(rs.getString("id"), rs.getString("word"), getLookUpsCountOnWordKey(rs.getString("id")), rs.getString("stem"), rs.getString("lang"));
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return null;
        }
    }

    public List<Book> getBooks() {
        String sql = "SELECT * FROM BOOK_INFO WHERE " + getCurrentFilters();
        List<Book> bookList = new ArrayList<Book>();

        try {
            Statement stmt = _conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            // loop through the result set
            while (rs.next()) {
                bookList.add(new Book(rs.getString("id"), rs.getString("title"), getLookUpsCountOnBookKey(rs.getString("id"))));
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return bookList;
    }

    public Book getBook(String id) {
        String sql = "SELECT * FROM BOOK_INFO WHERE id='" + id + "'";

        try {
            Statement stmt = _conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            return new Book(rs.getString("id"), rs.getString("title"), 0);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return null;
        }
    }

    public List<LookUp> getLookUps() {
        String sql = "SELECT * FROM LOOKUPS INNER JOIN WORDS ON LOOKUPS.word_key = WORDS.id INNER JOIN BOOK_INFO ON LOOKUPS.book_key=BOOK_INFO.id WHERE " + getCurrentFiltersWithColumnName();
        List<LookUp> lookUpList = new ArrayList<>();

        try {
            Statement stmt = _conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            // loop through the result set
            while (rs.next()) {
                lookUpList.add(new LookUp(rs.getString("id"), rs.getString("word"), rs.getString("usage"), rs.getString("title"), rs.getLong("timestamp")));
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return lookUpList;
    }

    private int getLookUpsCountOnWordKey(String wordKey) {
        String sql = "SELECT COUNT(*) AS total FROM LOOKUPS WHERE word_key = '" + wordKey + "'";

        try {
            Statement stmt = _conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            rs.next();
            return rs.getInt("total");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return 0;
        }
    }

    private int getLookUpsCountOnBookKey(String bookKey) {
        String sql = "SELECT COUNT(*) AS total FROM LOOKUPS WHERE book_key = '" + bookKey + "'";

        try {
            Statement stmt = _conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            rs.next();
            return rs.getInt("total");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return 0;
        }
    }

    public void removeWord(String wordKey) {
        String sql = "DELETE FROM WORDS WHERE id = ?";
        try {
            PreparedStatement stmt = _conn.prepareStatement(sql);
            stmt.setString(1,wordKey);
            stmt.executeUpdate();
            hasUnsavedChanges = true;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public void removeLookUp(String lookUpKey){
        String sql = "DELETE FROM LOOKUPS WHERE id = ?";
        try {
            PreparedStatement stmt = _conn.prepareStatement(sql);
            stmt.setString(1,lookUpKey);
            stmt.executeUpdate();
            hasUnsavedChanges = true;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public void updateWord(String wordKey, String newValue) {
        String sql = "UPDATE WORDS SET word = ?"
                + "WHERE id = ?";
        try {
             PreparedStatement stmt = _conn.prepareStatement(sql);
            stmt.setString(1, newValue);
            stmt.setString(2, wordKey);
            stmt.executeUpdate();
            hasUnsavedChanges = true;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    // Merges two words by removing one of them (wordKey2) and setting new value for second one (for wordKey1)
    public void mergeWords(String wordKey1, String wordKey2, String newValue) {
        // Update LOOKUPS table
        String sql = "UPDATE LOOKUPS SET wordKey = ? "
                + "WHERE word_key = ?";
        try {
            PreparedStatement stmt = _conn.prepareStatement(sql);
            stmt.setString(1, wordKey1);
            stmt.setString(2, wordKey2);
            stmt.executeUpdate();
            hasUnsavedChanges = true;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        // Sets new common value for two merged records
        updateWord(wordKey1, newValue);
        // Remove unused foreign key in words table
        removeWord(wordKey2);
    }

    public void commit() {
        try {
            _conn.commit();
            hasUnsavedChanges = false;
        } catch (SQLException e) {
            System.out.println(e.getMessage());

        }
    }

    public void rollback() {
        try {
            _conn.rollback();
            hasUnsavedChanges = false;
        } catch (SQLException e) {
            System.out.println(e.getMessage());

        }
    }

    public void close() {
        try {
            if (_conn != null) {
                _conn.close();
            }
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
        }
    }

    public void exportDatabase(String fullPath) {
        // Copy template database from app resources
        try {
            var source = new File("src/resources/vocabTemplate.db");
            var dest = new File(fullPath);

            Files.copy(source.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }
        catch (Exception e) {
            System.out.println(e.getMessage());
        }

        String sqlAttach = "ATTACH DATABASE ? AS newDatabase";
        String sqlInsertBooks = "INSERT INTO newDatabase.BOOK_INFO SELECT * FROM main.BOOK_INFO";
        String sqlInsertWords = "INSERT INTO newDatabase.WORDS SELECT * FROM main.WORDS";
        String sqlInsertLookUps = "INSERT INTO newDatabase.LOOKUPS SELECT * FROM main.LOOKUPS";
        String sqlInsertVersion = "INSERT INTO newDatabase.VERSION SELECT * FROM main.VERSION";
        String sqlInsertDict = "INSERT INTO newDatabase.DICT_INFO SELECT * FROM main.DICT_INFO";
        String sqlInsertMetaData = "INSERT INTO newDatabase.METADATA SELECT * FROM main.METADATA";
        String sqlDetach = "DETACH newDatabase";

        try {
            // Attach database
            PreparedStatement stmt = _conn.prepareStatement(sqlAttach);
            stmt.setString(1,fullPath);
            stmt.execute();

            stmt = _conn.prepareStatement(sqlInsertBooks);
            stmt.executeUpdate();

            stmt = _conn.prepareStatement(sqlInsertDict);
            stmt.executeUpdate();

            stmt = _conn.prepareStatement(sqlInsertMetaData);
            stmt.executeUpdate();

            stmt = _conn.prepareStatement(sqlInsertVersion);
            stmt.executeUpdate();

            stmt = _conn.prepareStatement(sqlInsertWords);
            stmt.executeUpdate();

            stmt = _conn.prepareStatement(sqlInsertLookUps);
            stmt.executeUpdate();
            // Commit changes
            commit();
            stmt.close();
            // Reconnect - because of bug with detaching database
            _conn.close();
            _conn = connect(fileURL);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public void addLanguage(String code) {
        if (!currentLanguageFilters.contains(code))
            currentLanguageFilters.add(code);
    }

    public void removeLanguage(String code) {
        currentLanguageFilters.remove(code);
    }

    public List<String> getCurrentLanguageFilters() {
        return currentLanguageFilters;
    }
}
