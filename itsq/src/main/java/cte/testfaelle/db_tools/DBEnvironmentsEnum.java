package cte.testfaelle.db_tools;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Enum mit den Eckdaten der im Test unterstützten Datenbank-Environments
 */
public enum DBEnvironmentsEnum {

    LOCALPG("ENE", "postgres", CteDeliveryKonstanten.JDBC.DB_URL_LOCALPG, DatabaseVendor.POSTGRESQL),

    ENE("ENE", "eneadmin", CteDeliveryKonstanten.JDBC.DB_URL_ENE, DatabaseVendor.ORA),
    ENEPG("ENE", "eneadmin", CteDeliveryKonstanten.JDBC.DB_URL_ENEPG, DatabaseVendor.POSTGRESQL),

    GEE("GEE", "geereed", "jdbc:oracle:thin:@(DESCRIPTION=(ENABLE=BROKEN)(ADDRESS_LIST=(ADDRESS=(PROTOCOL=TCP)(HOST=ent-db-rac-scan.ecofis.de)(PORT=1521)))(CONNECT_DATA=(SERVICE_NAME=abe.creditreform.de)))", DatabaseVendor.ORA),
    ABE("ABE", "ABEREAD", "jdbc:oracle:thin:@(DESCRIPTION=(ENABLE=BROKEN)(ADDRESS_LIST=(ADDRESS=(PROTOCOL=TCP)(HOST=ent-db-rac-scan.ecofis.de)(PORT=1521)))(CONNECT_DATA=(SERVICE_NAME=abe.creditreform.de)))", DatabaseVendor.ORA),

    PRE("PRE", "preread", "jdbc:oracle:thin:@(DESCRIPTION=(ENABLE=BROKEN)(ADDRESS=(PROTOCOL=TCP)(HOST=ctz-prd-db-rac-scan.ecofis.de)(PORT=1521))(LOAD_BALANCE=no)(CONNECT_DATA=(SERVER=DEDICATED)(SERVICE_NAME=pre.creditreform.de)))", DatabaseVendor.ORA),
    PRE_BCK("PRE", "preread", "jdbc:oracle:thin:@(DESCRIPTION=(ENABLE=BROKEN)(ADDRESS=(PROTOCOL=TCP)(HOST=ctz-dg-db-rac-scan.ecofis.de)(PORT=1521))(LOAD_BALANCE=no)(CONNECT_DATA=(SERVER=DEDICATED)(SERVICE_NAME=ctedg.creditreform.de)))", DatabaseVendor.ORA),

    REE("PRE", "preread", "jdbc:oracle:thin:@(DESCRIPTION=(ENABLE=BROKEN)(ADDRESS=(PROTOCOL=TCP)(HOST=ref-db-rac-scan.ecofis.de)(PORT=1521))(LOAD_BALANCE=no)(CONNECT_DATA=(SERVER=DEDICATED)(SERVICE_NAME=cteref.creditreform.de)))", DatabaseVendor.ORA);

    private final List<String> validNames;
    private final String adminUserPrefix;
    private final String dbUser;
    private final String dbUrl;
    private final DatabaseVendor databaseVendor;
    DBEnvironmentsEnum(String adminUserPrefix, String dbUser, String dbUrl, DatabaseVendor databaseVendor, String... otherValidNames) {
        this.adminUserPrefix = String.valueOf(adminUserPrefix).toUpperCase();
        this.dbUser = dbUser;
        this.dbUrl = dbUrl;
        this.databaseVendor = databaseVendor;
        this.validNames = new ArrayList<>();
        this.validNames.add(name());
        if (otherValidNames != null && otherValidNames.length > 0) {
            this.validNames.addAll(Arrays.asList(otherValidNames));
        }
    }

    public static DBEnvironmentsEnum locate(String environment) {
        if (environment != null) {
            final String upr = environment.trim().toUpperCase();
            for (DBEnvironmentsEnum e : DBEnvironmentsEnum.values()) {
                if (e.isValidName(upr)) {
                    return e;
                }
            }
        }
        throw new IllegalArgumentException("Umgebung " + environment + " wird nihct unterstützt");
    }

    private boolean isValidName(String nameToCheck) {
        return this.validNames.contains(nameToCheck);
    }

    public String getAdminUserPrefix() {
        return adminUserPrefix;
    }

    public String getDbUser() {
        return dbUser;
    }

    public String getDbUrl() {
        return dbUrl;
    }

    public DatabaseVendor getDatabaseVendor() {
        return databaseVendor;
    }

}
