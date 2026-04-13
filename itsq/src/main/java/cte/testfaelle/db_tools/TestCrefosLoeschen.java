package cte.testfaelle.db_tools;

import cte.testfaelle.domain.TestCustomer;
import cte.testfaelle.domain.TestSupportClientKonstanten;
import cte.testfaelle.domain.TimelineLogger;
import cte.testfaelle.extender.ExtendArchivBestandCrefos;
import cte.testfaelle.extender.ITSQTestFaelleUtil;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;
import org.apache.commons.io.FileUtils;

/*
  Tabellendefinition im Projekt cte_sql_scripte: src/main/sql/test-util/test_crefos_install.sql
  siehe src/main/SQL/Statements for CTE_EXPORT_PROTOKOLL.sql für SQL's
*/

public class TestCrefosLoeschen {

    private static boolean DELETE_RECORDS = false;
    private static final int NUM_CREFOS_TO_KEEP = 5000;
    private static final long CLZ_RANGE_MIN_412 = 4120000000L; // 4120000000L;
    private static final long CLZ_RANGE_MIN_912 = 9120000000L;  // 9120000000L;
    private static DBEnvironmentsEnum dbEnvironmentsEnum;
    private static final String[][] TABLE_COLUMN_MAP = new String[][]
            {
                    {"STAGING_IKAROS_AUFTRAG", "CREFONUMMER"},
                    {"STAGING_ENTSCHEIDUNGS_TR", "CREFO_FIRMA"},
                    {"STAGING_ENTSCHEIDUNGS_TR", "CREFO_ENTSCHEIDUNGS_TR"},
                    {"STAGING_ENTSCHEIDUNGS_TR", "DIREKT_BETEILIGT_AN_FIRMA"},
                    {"STAGING_FIRMEN_BETEILIGUNG", "CREFO_BETEILIGTER"},
                    {"STAGING_FIRMEN_BETEILIGUNG", "BETEILIGT_AN_CREFO"},
                    {"STAGING_ARCHIVBESTAND", "CREFONUMMER"},
                    {"STAGING_IMPORT_EVENT", "CREFONUMMER"},
                    {"KORREKTUR_LIEFERUNG_EVENT", "CREFONUMMER"},
                    {"BESTAND_ENTSCHEIDUNGS_TR", "CREFO_FIRMA"},
                    {"CREFODATEN_BTLG", "CREFO_BETEILIGTER"},
                    {"CREFODATEN_BTLG", "BETEILIGT_AN_CREFO"},
                    {"CREFODATEN", "CREFO"},
                    {"DSGVO_STATUS_CREFO", "CREFONUMMER"},
                    {"CTE_EXPORT_PROTOKOLL", "CREFONUMMER"},
                    {"ABFRELEVANZPRUEFERGEBNIS", "CREFONUMMER"},
                    {"ABFEXPORT_XML_HASH", "CREFONUMMER"},
                    {"ATFRELEVANZPRUEFERGEBNIS", "CREFONUMMER"},
                    {"ATFEXPORT_XML_HASH", "CREFONUMMER"},
                    {"ACBRELEVANZPRUEFERGEBNIS", "CREFONUMMER"},
                    {"ACBEXPORT_XML_HASH", "CREFONUMMER"},
                    {"BVDRELEVANZPRUEFERGEBNIS", "CREFONUMMER"},
                    {"BDREXPORT_XML_HASH", "CREFONUMMER"},
                    {"BVDRELEVANZPRUEFERGEBNIS", "CREFONUMMER"},
                    {"BVDEXPORT_XML_HASH", "CREFONUMMER"},
                    {"CEFRELEVANZPRUEFERGEBNIS", "CREFONUMMER"},
                    {"CEFEXPORT_XML_HASH", "CREFONUMMER"},
                    {"CTCRELEVANZPRUEFERGEBNIS", "CREFONUMMER"},
                    {"CTCEXPORT_XML_HASH", "CREFONUMMER"},
                    {"DRDRELEVANZPRUEFERGEBNIS", "CREFONUMMER"},
                    {"DRDEXPORT_XML_HASH", "CREFONUMMER"},
                    {"FVDRELEVANZPRUEFERGEBNIS", "CREFONUMMER"},
                    {"FVDEXPORT_XML_HASH", "CREFONUMMER"},
                    {"FSURELEVANZPRUEFERGEBNIS", "CREFONUMMER"},
                    {"FSUEXPORT_XML_HASH", "CREFONUMMER"},
                    {"GDLRELEVANZPRUEFERGEBNIS", "CREFONUMMER"},
                    {"GDLEXPORT_XML_HASH", "CREFONUMMER"},
                    {"IKARELEVANZPRUEFERGEBNIS", "CREFONUMMER"},
                    {"IKAEXPORT_XML_HASH", "CREFONUMMER"},
                    {"INSORELEVANZPRUEFERGEBNIS", "CREFONUMMER"},
                    {"INSO_P2_XML_HASH", "CREFONUMMER"},
                    {"INSO_P1_PRE_PRODUCT", "CREFONUMMER"},
                    {"ISMRELEVANZPRUEFERGEBNIS", "CREFONUMMER"},
                    {"ISMEXPORT_XML_HASH", "CREFONUMMER"},
                    {"K25RELEVANZPRUEFERGEBNIS", "CREFONUMMER"},
                    {"K25EXPORT_XML_HASH", "CREFONUMMER"},
                    {"EHRELEVANZPRUEFERGEBNIS", "CREFONUMMER"},
                    {"EHEXPORT_XML_HASH", "CREFONUMMER"},
                    {"K26RELEVANZPRUEFERGEBNIS", "CREFONUMMER"},
                    {"K26EXPORT_XML_HASH", "CREFONUMMER"},
                    {"CRMRELEVANZPRUEFERGEBNIS", "CREFONUMMER"},
                    {"CRMEXPORT_XML_HASH", "CREFONUMMER"},
                    {"K27RELEVANZPRUEFERGEBNIS", "CREFONUMMER"},
                    {"K27EXPORT_XML_HASH", "CREFONUMMER"},
                    {"FOORELEVANZPRUEFERGEBNIS", "CREFONUMMER"},
                    {"FOOEXPORT_XML_HASH", "CREFONUMMER"},
                    {"LENRELEVANZPRUEFERGEBNIS", "CREFONUMMER"},
                    {"LENEXPORT_XML_HASH", "CREFONUMMER"},
                    {"MICRELEVANZPRUEFERGEBNIS", "CREFONUMMER"},
                    {"MICEXPORT_XML_HASH", "CREFONUMMER"},
                    {"MIPRELEVANZPRUEFERGEBNIS", "CREFONUMMER"},
                    {"MIPEXPORT_XML_HASH", "CREFONUMMER"},
                    {"NIMRELEVANZPRUEFERGEBNIS", "CREFONUMMER"},
                    {"NIMEXPORT_XML_HASH", "CREFONUMMER"},
                    {"RTNRELEVANZPRUEFERGEBNIS", "CREFONUMMER"},
                    {"RTNEXPORT_XML_HASH", "CREFONUMMER"},
                    {"SDFRELEVANZPRUEFERGEBNIS", "CREFONUMMER"},
                    {"SDFEXPORT_XML_HASH", "CREFONUMMER"},
                    {"VSHRELEVANZPRUEFERGEBNIS", "CREFONUMMER"},
                    {"VSHEXPORT_XML_HASH", "CREFONUMMER"},
                    {"ZEWRELEVANZPRUEFERGEBNIS", "CREFONUMMER"},
                    {"ZEWEXPORT_XML_HASH", "CREFONUMMER"},
            };

    public static void main(String[] args) throws IOException {
        if (args != null && args.length == 1) {
            DELETE_RECORDS = args[0].startsWith("true");
        }
        Connection connection = null;
        try {
            File workDir = new File(System.getProperty("user.dir"));
            if (!TimelineLogger.configure(workDir, "DelteTestCrefos.log", "DelteTestCrefos-Actions.log")) {
                throw new RuntimeException("Exception beim Konfigurieren der LOG-Dateien!\n");
            }

            connection = getConnection("ENE", "eneadmin");
            long processId = getNextProcessId(connection);

            processForTestRange(connection, processId, CLZ_RANGE_MIN_412);
            // ORA-01000: Maximale Anzahl offener Cursor überschritten, deswegen schließe ich die Connection und öffne sie wieder
            connection.close();
            connection = getConnection("ENE", "eneadmin");
            processForTestRange(connection, processId, CLZ_RANGE_MIN_912);

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            TimelineLogger.close();
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException sqlEx) {
                    TimelineLogger.error(TestCrefosLoeschen.class, "Fehler beim Schliessen der Datenbank-Connection zum Test der Parameter", sqlEx);
                }
            }
        }
    }

    private static void processForTestRange(Connection connection, long processId, long startCrefo) throws SQLException, IOException {
        // ermittle die kleinere aus den maximalen Crefonummer aus STAGING_ARCHIVBESTAND und CREFODATEN
        long maxCrefo = getMaxCrefo(connection, startCrefo);
        TimelineLogger.info(TestCrefosLoeschen.class, "Ermittle die kleinere aus den maximalen Crefonummer aus STAGING_ARCHIVBESTAND und CREFODATEN zwischen " + startCrefo + " und " + maxCrefo + "...");
        dumpProcessInfo(connection, processId, startCrefo, maxCrefo);
        if (DELETE_RECORDS) {
            // Erzeuge TEST_CREFOS_PROTOKOLL-Datensätze für den Stand vor dem Löschen
            writeProtocoll(connection, processId, startCrefo, maxCrefo);
            for (int i = 0; i < TABLE_COLUMN_MAP.length; i++) {
                // Lösche die Test-Crefos in den ermittelten Bereichen
                deleteFromTable(connection, TABLE_COLUMN_MAP[i][0], TABLE_COLUMN_MAP[i][1], startCrefo, maxCrefo);
            }
        }
    }

    private static void writeProtocoll(Connection connection, long processId, long startCrefo, long maxCrefo) throws SQLException {
        TimelineLogger.info(TestCrefosLoeschen.class, "Schreibe das Protokoll...");
        for (int i = 0; i < TABLE_COLUMN_MAP.length; i++) {
            TableInfo tableInfo = checkProcessingTable(connection, TABLE_COLUMN_MAP[i][0], TABLE_COLUMN_MAP[i][1], startCrefo, maxCrefo);
            if (tableInfo.getNumCrefos() > 0) {
                String strInfo = String.format("%d Testcrefos im Bereich %d bis %d gelöscht (Spalte %s)", tableInfo.getNumCrefos(), tableInfo.getMinCrefo(), tableInfo.getMaxCrefo(), tableInfo.getColumnName());
                String sqlTemplate = "INSERT INTO TEST_CREFOS_PROTOKOLL ( TEST_CREFOS_PROTOKOLL_ID, PROCESS_ID, DATUM, TABLE_NAME, MIN_CREFO, MAX_CREFO, ANZAHL_GELOESCHT, INFO ) VALUES ( seqTEST_CREFOS_PROTOKOLL.nextval, %d, CURRENT_TIMESTAMP, '%s', %d, %d, %d, '%s' )";
                Statement stmt = connection.createStatement();
                String sql = String.format(sqlTemplate, processId, tableInfo.getTableName(), tableInfo.getMinCrefo(), tableInfo.getMaxCrefo(), tableInfo.getNumCrefos(), strInfo);
                ResultSet resultSet = stmt.executeQuery(sql);
                if (!resultSet.next()) {
                    throw new RuntimeException("SQL " + sql + " liefert kein Ergebnis!");
                }
            } else {
                TimelineLogger.info(TestCrefosLoeschen.class, "Nothing to delete for the table '" + tableInfo.getTableName() + "'!");
            }
        }
    }

    private static long getNextProcessId(Connection connection) throws SQLException {
        String sql = "SELECT MAX(PROCESS_ID) AS NEXT_ID FROM TEST_CREFOS_PROTOKOLL";
        Statement stmt = connection.createStatement();
        ResultSet resultSet = stmt.executeQuery(sql);
        if (!resultSet.next()) {
            throw new RuntimeException("SQL " + sql + " liefert kein Ergebnis!");
        }
        long nextId = resultSet.getLong("NEXT_ID");
        if (nextId < 1) {
            return 1;
        }
        return nextId + 1;
    }

    private static void dumpProcessInfo(Connection connection, long processId, long startCrefo, long maxCrefo) throws SQLException, IOException {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(getProzessInfo(connection));
        stringBuilder.append("\nTabelle;Spalte;Min;Max;Anzahl");
        for (int i = 0; i < TABLE_COLUMN_MAP.length; i++) {
            TableInfo tableInfo = checkProcessingTable(connection, TABLE_COLUMN_MAP[i][0], TABLE_COLUMN_MAP[i][1], startCrefo, maxCrefo);
            if (tableInfo.getNumCrefos() > 0) {
                stringBuilder.append(tableInfo.formatForFile());
            }
        }
        stringBuilder.append("\n--------------------------------------------");
        File infoFile = new File(System.getProperty("user.dir"), startCrefo + "-" + NUM_CREFOS_TO_KEEP + "-Table-Analyse-ProzessID-" + processId + ".csv");
        FileUtils.writeStringToFile(infoFile, stringBuilder.toString(), "UTF-8");
        TimelineLogger.info(TestCrefosLoeschen.class, "Prozessinfo gespeichert unter " + infoFile.getAbsolutePath());
    }

    private static String getProzessInfo(Connection connection) throws SQLException {
        StringBuilder stringBuilder = new StringBuilder("Lösch-Prookoll bisher");
        stringBuilder.append("\nPROCESS_ID;ANZAHL_GLOESCHT");

        Statement stmt = connection.createStatement();
        String sql = "SELECT PROCESS_ID, SUM(ANZAHL_GELOESCHT) AS ANZAHL_GLOESCHT FROM TEST_CREFOS_PROTOKOLL GROUP BY PROCESS_ID";
        ResultSet resultSet = stmt.executeQuery(sql);
        while (resultSet.next()) {
            long prozessID = resultSet.getLong("PROCESS_ID");
            long numDeleted = resultSet.getLong("ANZAHL_GLOESCHT");
            stringBuilder.append("\n" + prozessID);
            stringBuilder.append(";" + numDeleted);
        }
        stringBuilder.append("\n\n");
        return stringBuilder.toString();
    }

    private static TableInfo checkProcessingTable(Connection connection, String tableName, String columnName, long minCrefo, long maxCrefo) throws SQLException {
        String sql = "SELECT MIN(" + columnName + ") AS MINX, MAX(" + columnName + ") AS MAXX, COUNT(*) AS CNT" + " FROM " + tableName + " WHERE " + columnName + " BETWEEN " + minCrefo + " AND " + maxCrefo;
        TimelineLogger.info(TestCrefosLoeschen.class, "Prüfe Tabelle " + tableName + ": " + sql);
        Statement stmt = connection.createStatement();
        ResultSet resultSet = stmt.executeQuery(sql);
        if (!resultSet.next()) {
            throw new RuntimeException("SQL " + sql + " liefert kein Ergebnis!");
        }
        long min = resultSet.getLong("MINX");
        if (min < 1) {
            min = minCrefo;
        }
        long max = resultSet.getLong("MAXX");
        if (max < 1) {
            max = maxCrefo;
        }
        long cnt = resultSet.getLong("CNT");
        TableInfo tableInfo = new TableInfo(tableName, columnName, columnName, min, max, cnt);
        return tableInfo;
    }

    private static void deleteFromTable(Connection connection, String tableName, String columnName, long minCrefo, long maxCrefo) throws SQLException {
        String sql = "DELETE FROM " + tableName + " WHERE " + columnName + " BETWEEN " + minCrefo + " AND " + maxCrefo;
        TimelineLogger.info(TestCrefosLoeschen.class, "Lösche Crefos aus der Tabelle " + tableName + ": " + sql);
        Statement stmt = connection.createStatement();
        ResultSet resultSet = stmt.executeQuery(sql);
        if (resultSet.rowDeleted()) {
            TimelineLogger.info(TestCrefosLoeschen.class, "Datensätze im Bereich von " + minCrefo + " bis " + maxCrefo + " aus der Tabelle " + tableName + " wurden gelöscht.");
        }
    }

    private static long getMaxCrefo(Connection connection, long minCrefoRange) throws SQLException {
        TableInfo tableInfoCREFODATEN = getMaxCrefoFromTable(connection, "CREFODATEN", "CREFO", minCrefoRange);
        TimelineLogger.info(TestCrefosLoeschen.class, tableInfoCREFODATEN.toString());
        TableInfo tableInfoSTAGING = getMaxCrefoFromTable(connection, "STAGING_ARCHIVBESTAND", "CREFONUMMER", minCrefoRange);
        TimelineLogger.info(TestCrefosLoeschen.class, tableInfoSTAGING.toString());
        long maxCrefo = (tableInfoCREFODATEN.getMaxCrefo() < tableInfoSTAGING.getMaxCrefo()) ? tableInfoCREFODATEN.getMaxCrefo() : tableInfoSTAGING.getMaxCrefo();
        return maxCrefo;
    }

    private static TableInfo getMaxCrefoFromTable(Connection connection, String tableName, String columnName, long minCrefoRange) throws SQLException {
        Statement stmt = connection.createStatement();
        long maxCrefoRange = ((minCrefoRange / 10000000L) + 1) * 10000000L;
        String sqlTemplate = "SELECT MIN(%s) AS MIN_CREFO, (MAX(%s)-%d) AS MAX_CREFO, COUNT(%s) AS NUM_CREFOS FROM %s WHERE %s BETWEEN %d AND %d";
        String sql = String.format(sqlTemplate, columnName, columnName, NUM_CREFOS_TO_KEEP, columnName, tableName, columnName, minCrefoRange, maxCrefoRange);
        ResultSet resultSet = stmt.executeQuery(sql);
        if (!resultSet.next()) {
            throw new RuntimeException("SQL " + sqlTemplate + " liefert kein Ergebnis!");
        }
        long minCrefo = resultSet.getLong("MIN_CREFO");
        if (minCrefo < 1) {
            minCrefo = minCrefoRange;
        }
        long maxCrefo = resultSet.getLong("MAX_CREFO");
        if (maxCrefo < 1) {
            maxCrefo = maxCrefoRange;
        }
        long numCrefos = resultSet.getLong("NUM_CREFOS");
        TableInfo tableInfo = new TableInfo(tableName, sql, tableName, minCrefo, maxCrefo, numCrefos);
        return tableInfo;
    }

    private static Connection getConnection(String env, String password) {
        try {
            dbEnvironmentsEnum = DBEnvironmentsEnum.locate(env);
            DatabaseVendor dbSystem = dbEnvironmentsEnum.getDatabaseVendor();
            String dbDriver = dbSystem.getJdbcDriverName();
            Connection testConnection = null;
            try {
                Class.forName(dbDriver);
                testConnection = DriverManager.getConnection(dbEnvironmentsEnum.getDbUrl(), dbEnvironmentsEnum.getDbUser(), password);
                return testConnection;
            } catch (ClassNotFoundException cnfEx) {
                throw new IllegalStateException("Oracle JDBC-Treiber fehlt im Classpath", cnfEx);
            } catch (SQLException openEx) {
                throw new IllegalStateException("Parameter für die lesende (!) Datenbank-Connection der Umgebung '" + dbEnvironmentsEnum.name() + "' falsch, User: " + dbEnvironmentsEnum.getDbUser(), openEx);
            }
        } catch (Exception e) {
            throw new RuntimeException("Fehler bei der Initialisierung von " + TestCrefosLoeschen.class.getSimpleName(), e);
        }
    }

    static class TableInfo {
        final String tableName;
        final String columnName;
        final String sql;
        final long minCrefo;
        final long maxCrefo;
        final long numCrefos;

        TableInfo(String tableName, String columnName, String sql, long minCrefo, long maxCrefo, long numCrefos) {
            this.tableName = tableName;
            this.columnName = columnName;
            this.sql = sql;
            this.minCrefo = minCrefo;
            this.maxCrefo = maxCrefo;
            this.numCrefos = numCrefos;
        }

        public String getTableName() {
            return tableName;
        }

        public String getColumnName() {
            return columnName;
        }

        public long getMinCrefo() {
            return minCrefo;
        }

        public long getMaxCrefo() {
            return maxCrefo;
        }

        public long getNumCrefos() {
            return numCrefos;
        }

        @Override
        public String toString() {
            return "TableInfo{" +
                    "tableName='" + tableName + '\'' +
                    "columnName='" + columnName + '\'' +
                    ", minCrefo=" + minCrefo +
                    ", maxCrefo=" + maxCrefo +
                    ", numCrefos=" + numCrefos +
                    ", SQL=" + sql +
                    '}';
        }

        public String formatForFile() {
            return String.format("\n%s;%s;%d;%d;%d", tableName, columnName, minCrefo, maxCrefo, numCrefos);
        }
    }
}
