/// <reference types="cypress" />

import { SignInActions } from "../start-screen/sign-in-action";
import { deleteArticles } from "../utils/cleanup/delete-articles";
import { deleteBillboards } from "../utils/cleanup/delete-billboards";
import { deleteAllCategories } from "../utils/cleanup/delete-categories";
import { deleteSpaces } from "../utils/cleanup/delete-spaces";
import { adminUser, peggyConfig, testUrl } from "./setup-import-users";

export const beforeTest = before(() => {
    cy.log(Cypress.env());
    if (Cypress.env().harRecord) {
        cy.recordHar();
    }

    if (Cypress.env().cleanUp) {
        cy.log("------- Running Clean Up -------");
        const signIn = new SignInActions();
        signIn.gaSignIn(testUrl, adminUser, peggyConfig.loginVersion);
        deleteAllCategories();
        deleteArticles();
        deleteSpaces();
        deleteBillboards();
        Cypress.env("cleanUp", false);
    }
});

export const afterTest = after(() => {
    if (Cypress.env().harRecord) {
        cy.saveHar({ outDir: Cypress.env().harOutputDir });
    }
});
