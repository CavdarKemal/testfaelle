package cte.testfaelle.rohdatenverwendung;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class UsageOfCrefo {
    private final String fileName;
    private final List<UsedInTestScenario> usedInScenarios;
    private boolean availableInP1;
    private boolean availableInP2;

    public UsageOfCrefo(String fileName) {
        this.fileName = fileName;
        this.usedInScenarios = new ArrayList<>();
    }

    public void setAvailableInP1() {
        this.availableInP1 = true;
    }

    public void setAvailableInP2() {
        this.availableInP2 = true;
    }

    public void addUsedInScenario(UsedInTestScenario usedInScenario) {
        if (usedInScenario != null) {
            usedInScenarios.add(usedInScenario);
        }
    }

    public int getNumberOfScenarios() {
        return usedInScenarios.size();
    }

    public List<UsedInTestScenario> getUsedInScenarios() {
        return Collections.unmodifiableList(usedInScenarios);
    }
}
