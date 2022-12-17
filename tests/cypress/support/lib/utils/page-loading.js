/// <reference types="cypress" />

export default {
    waitForLoading() {
        return cy.get('[data-testid="loading"]').should("not.exist");
    },

    waitForSpinner() {
        return cy.get('[class^="wv-spinner"]').should("not.exist");
    }
};
