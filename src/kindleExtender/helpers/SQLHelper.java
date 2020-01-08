package kindleExtender.helpers;

import kindleExtender.models.Book;
import kindleExtender.models.LookUp;
import kindleExtender.models.Word;

import java.sql.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class SQLHelper {

    public SQLHelper(String fileURL) {
        _conn = connect(fileURL);
        try {
            _conn.setAutoCommit(false);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        hasUnsavedChanges = false;
    }

    // Indicates if any (uncomitted) changes to database have been made
    public boolean hasUnsavedChanges;

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

    public List<Word> getWords() {
        String sql = "SELECT * FROM WORDS";
        List<Word> wordList = new ArrayList<Word>();

        try {
            Statement stmt = _conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            // loop through the result set
            while (rs.next()) {
                wordList.add(new Word(rs.getString("id"), rs.getString("word"), getLookUpsCountOnWordKey(rs.getString("id")), rs.getString("stem")));
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
            return new Word(rs.getString("id"), rs.getString("word"), getLookUpsCountOnWordKey(rs.getString("id")), rs.getString("stem"));
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return null;
        }
    }

    public List<Book> getBooks() {
        String sql = "SELECT * FROM BOOK_INFO";
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
        String sql = "SELECT * FROM LOOKUPS INNER JOIN WORDS ON LOOKUPS.word_key = WORDS.id INNER JOIN BOOK_INFO ON LOOKUPS.book_key=BOOK_INFO.id";
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

    // Merges two words by removing one of them (wordKey2) and setting new value (for wordKey2)
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
}
