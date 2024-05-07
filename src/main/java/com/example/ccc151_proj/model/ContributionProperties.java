package com.example.ccc151_proj.model;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

/**
 * Information of the contributions. Use on for TableView.
 */
public class ContributionProperties {
    private final SimpleStringProperty contribution_code;
    private final SimpleStringProperty contribution_sem;
    private final SimpleIntegerProperty contribution_amount;

    public ContributionProperties(String contribution_code, String contribution_sem, Integer contribution_amount) {
        this.contribution_code = new SimpleStringProperty(contribution_code);
        this.contribution_sem = new SimpleStringProperty(contribution_sem);
        this.contribution_amount = new SimpleIntegerProperty(contribution_amount);
    }

    /*
    Setters and Getters.
     */

    public String getContribution_code() {
        return contribution_code.get();
    }

    public void setContribution_code(String contribution_code) {
        this.contribution_code.set(contribution_code);
    }

    public SimpleStringProperty contribution_codeProperty() {
        return contribution_code;
    }

    public String getContribution_sem() {
        return contribution_sem.get();
    }

    public void setContribution_sem(String contribution_sem) {
        this.contribution_sem.set(contribution_sem);
    }

    public SimpleStringProperty contribution_semProperty() {
        return contribution_sem;
    }

    public int getContribution_amount() {
        return contribution_amount.get();
    }

    public void setContribution_amount(int contribution_amount) {
        this.contribution_amount.set(contribution_amount);
    }

    public SimpleIntegerProperty contribution_amountProperty() {
        return contribution_amount;
    }
}
