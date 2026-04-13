package cte.testfaelle.db_tools;

import static cte.testfaelle.db_tools.CteDeliveryKonstanten.HIBERNATE.HIB_DIALECT_HSQLDB;
import static cte.testfaelle.db_tools.CteDeliveryKonstanten.HIBERNATE.HIB_DIALECT_ORA;
import static cte.testfaelle.db_tools.CteDeliveryKonstanten.HIBERNATE.HIB_DIALECT_POSTGRESQLDB;
import static cte.testfaelle.db_tools.CteDeliveryKonstanten.JDBC.JDBC_DRIVER_NAME_HSQLDB;
import static cte.testfaelle.db_tools.CteDeliveryKonstanten.JDBC.JDBC_DRIVER_NAME_ORACLE;
import static cte.testfaelle.db_tools.CteDeliveryKonstanten.JDBC.JDBC_DRIVER_NAME_POSTGRESQLDB;

public enum DatabaseVendor {

    HSQLDB(JDBC_DRIVER_NAME_HSQLDB, HIB_DIALECT_HSQLDB),
    ORA(JDBC_DRIVER_NAME_ORACLE, HIB_DIALECT_ORA),
    POSTGRESQL(JDBC_DRIVER_NAME_POSTGRESQLDB, HIB_DIALECT_POSTGRESQLDB);
    private final String jdbcDriverName;
    private final String hibernateDialect;

    DatabaseVendor(String jdbcDriverName, String hibernateDialect) {
        this.jdbcDriverName = jdbcDriverName;
        this.hibernateDialect = hibernateDialect;
    }

    public String getJdbcDriverName() {
        return jdbcDriverName;
    }

    public String getHibernateDialect() {
        return hibernateDialect;
    }

}
