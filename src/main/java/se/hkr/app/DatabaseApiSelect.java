package se.hkr.app;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;

public class DatabaseApiSelect {

    public enum RetrieveMode {JOURNAL_ENTRY, MOOD_TENSION}

    // Convert ResultSet to List of Data objects
    public static ArrayList<Data> parseToDataList (ResultSet rs) {
        ArrayList<Data> returnList = new ArrayList<>();
        try {
            while (rs.next()) {
                switch(rs.getMetaData().getColumnCount()) {
                    case 3:
                        String daytimeJE = rs.getString(1);
                        String entryJE = rs.getString(2);
                        LocalDate dateJE = LocalDate.parse(rs.getString(3));
                        Data journalEntry = new JournalEntry(dateJE, daytimeJE, entryJE);
                        returnList.add(journalEntry);
                        break;
                    case 4:
                        String daytimeMT = rs.getString(1);
                        int moodMT = (int) rs.getDouble(2);
                        int tensionMT = (int) rs.getDouble(3);
                        LocalDate dateMT = LocalDate.parse(rs.getString(4));
                        Data moodTensionEntry = new MoodTension(dateMT, daytimeMT, moodMT, tensionMT);
                        returnList.add(moodTensionEntry);
                        break;
                    default:
                        break;
                }
            }
        } catch (SQLException e) {
            return null;
        }
        return returnList;
    }


    // Retrieve data by day or by time period, depending on the given arguments
    public static ArrayList<Data> getData(Connection con, RetrieveMode mode, LocalDate date, String personnummer) throws SQLException {
        String sql;
        switch (mode) {
            case JOURNAL_ENTRY:
                sql = """
                SELECT
                    determineTimeOfDay(Journal_entry.date) AS timeOfDay,
                    Journal_entry.journal_entry,
                    DATE(Journal_entry.date) AS date
                FROM Journal_entry
                WHERE DATE(Journal_entry.date) LIKE ? AND Journal_entry.personnummer LIKE ?
                ORDER BY Journal_entry.date;
                """;
                break;
            case MOOD_TENSION:
                sql = """
                SELECT
                    determineTimeOfDay(Tension.date) AS timeOfDay,
                    AVG(Mood.rating) AS mood,
                    AVG(Tension.rating) AS tension,
                    MAX(DATE(Mood.date)) AS date
                FROM Mood
                JOIN Tension ON Mood.date = Tension.date
                WHERE DATE(Tension.date) = ? AND Mood.personnummer LIKE ?
                GROUP BY timeOfDay;
                """;
                break;
            default:
                sql = "";
        }
        PreparedStatement stmt = con.prepareStatement(sql);
        stmt.setString(1, date.toString());
        stmt.setString(2, personnummer);
        ResultSet rs = stmt.executeQuery();
        return parseToDataList(rs);
    }

    public static ArrayList<Data> getData(Connection con, RetrieveMode mode, LocalDate startDate, LocalDate endDate, String personnummer) throws SQLException {
        String sql;
        switch (mode) {
            case JOURNAL_ENTRY:
                sql = """
                SELECT
                    determineTimeOfDay(Journal_entry.date) AS timeOfDay,
                    Journal_entry.journal_entry,
                    DATE(Journal_entry.date) AS date
                FROM Journal_entry
                WHERE DATE(Journal_entry.date) >= ? AND DATE(Journal_entry.date) <= ? AND Journal_entry.personnummer LIKE ?
                ORDER BY Journal_entry.date;
                """;
                break;
            case MOOD_TENSION:
                sql = """
                SELECT
                    determineTimeOfDay(Tension.date) AS timeOfDay,
                    AVG(Mood.rating) AS mood,
                    AVG(Tension.rating) AS tension,
                    MAX(DATE(Mood.date)) AS date
                FROM Mood
                JOIN Tension ON Mood.date = Tension.date
                WHERE DATE(Tension.date) >= ? AND DATE(Tension.date) <= ? AND Mood.personnummer LIKE ?
                GROUP BY timeOfDay;
                """;
                break;
            default:
                sql = "";
        }
        PreparedStatement stmt = con.prepareStatement(sql);
        stmt.setString(1, startDate.toString());
        stmt.setString(2, endDate.toString());
        stmt.setString(3, personnummer);
        ResultSet rs = stmt.executeQuery();
        return parseToDataList(rs);
    }

    // Retrieve journal entries by rating range
    public static ArrayList<Data> getJournalEntriesByMoodRange(Connection con, int moodLower, int moodUpper, String personnummer) throws SQLException {
        String sql = """
                SELECT DISTINCT
                    determineTimeOfDay(Journal_entry.date) AS timeOfDay,
                    Journal_entry.journal_entry,
                    DATE(Journal_entry.date) AS date
                FROM Journal_entry
                JOIN Mood ON DATE(Journal_entry.date) = DATE(Mood.date) AND Journal_entry.personnummer = Mood.personnummer
                WHERE Mood.rating >= ? AND Mood.rating <= ? AND Journal_entry.personnummer LIKE ?
                ORDER BY Journal_entry.date;
                """;
        PreparedStatement stmt = con.prepareStatement(sql);
        stmt.setInt(1, moodLower);
        stmt.setInt(2, moodUpper);
        stmt.setString(3, personnummer);
        ResultSet rs = stmt.executeQuery();
        return parseToDataList(rs);
    }

    public static ResultSet checkPersonnummer(Connection con, String personnummer) throws SQLException {
        String getPersonnummer = """
            SELECT email FROM User WHERE  personnummer = ? """;
        PreparedStatement stmt = con.prepareStatement(getPersonnummer);
        stmt.setString(1, personnummer);

        return stmt.executeQuery();
    }

    public static ResultSet checkEmail(Connection con, String email) throws SQLException {
        String getEmail = """
            SELECT personnummer FROM User WHERE  email = ? """;
        PreparedStatement stmt = con.prepareStatement(getEmail);
        stmt.setString(1, email);

        return stmt.executeQuery();
    }

    // Check login
    public static String getLogin = """
            SELECT
                personnummer, email, name
            FROM User
            WHERE email= ? and password = SHA1( ? );
            """;


}
