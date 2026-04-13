package cte.testfaelle.rohdatenverwendung;

class UsedInTestScenario {
    private final String kundenKuerzel;
    private final String path;
    private final String propertyFileName;
    private final String scenarioName;

    public UsedInTestScenario(String kundenKuerzel, String path, String propertyFileName, String scenarioName) {
        this.kundenKuerzel = kundenKuerzel;
        this.path = path;
        this.propertyFileName = propertyFileName;
        this.scenarioName = scenarioName;
    }

    public String getKundenKuerzel() {
        return kundenKuerzel;
    }

}
