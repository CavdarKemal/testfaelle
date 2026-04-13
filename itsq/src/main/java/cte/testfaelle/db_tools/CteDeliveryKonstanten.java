package cte.testfaelle.db_tools;

/**
 * Container für die Definition von Konstanten in CTE_Delivery
 */
public class CteDeliveryKonstanten {

    /**
     * Keine Instanzen dieser Klasse
     */
    private CteDeliveryKonstanten() {
    }

    /**
     * Konstanten mit Bezug zu JDBC
     */
    public static class JDBC {
        /**
         * Klassen-Name des JDBC-Treibers, Oracle-DB
         */
        public static final String JDBC_DRIVER_NAME_ORACLE = "oracle.jdbc.driver.OracleDriver";
        /**
         * Klassen-Name des JDBC-Treibers, HSQL-DB
         */
        public static final String JDBC_DRIVER_NAME_HSQLDB = "org.hsqldb.jdbcDriver";
        /**
         * Klassen-Name des JDBC-Treibers, POSTGRESSQL
         */
        public static final String JDBC_DRIVER_NAME_POSTGRESQLDB = "org.postgresql.Driver";
        /**
         * JDBC-URL der ENE-Datenbank
         */
        public static final String DB_URL_ENE = "jdbc:oracle:thin:@(DESCRIPTION=(ENABLE=BROKEN)(ADDRESS_LIST=(ADDRESS=(PROTOCOL=TCP)(HOST=ent-db-rac-scan.ecofis.de)(PORT=1521)))(CONNECT_DATA=(SERVICE_NAME=cteene.creditreform.de)))";
        /**
         * JDBC-URL der ENE-Postgre SQL Datenbank
         */
        public static final String DB_URL_ENEPG = "jdbc:postgresql://rhscted001.ecofis.de/cteene?currentSchema=eneadmin";
        /**
         * JDBC-URL der lokalen PostgreSQL Datenbank
         */
        public static final String DB_URL_LOCALPG = "jdbc:postgresql://localhost:5432/cteene";

        /**
         * Keine Instanzen dieser Klasse
         */
        private JDBC() {
        }

    }

    /**
     * Konstanten mit Bezug zu Hibernate
     */
    public static class HIBERNATE {
        public static final String HIB_DIALECT_HSQLDB = "de.creditreform.crefoteam.technischebasis.dbaccess.CTHSQL3Dialect";
        public static final String HIB_DIALECT_ORA = "de.creditreform.crefoteam.technischebasis.dbaccess.CTOracle3Dialect";
        public static final String HIB_DIALECT_POSTGRESQLDB = "org.hibernate.dialect.PostgreSQL9Dialect";
    }

}
