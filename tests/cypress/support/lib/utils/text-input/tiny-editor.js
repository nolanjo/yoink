/// <reference types="cypress" />

import { longTime, TEXT } from "../../constants";
import { DRAG, uploadOnPageChain } from "../upload-image";

export const TINY_TOOLBAR_OPTION = {
    uploadImage: {
        selector: '[title="Insert/edit image"]'
    },
    insertTemplate: {
        selector: '[aria-label="Insert template"]'
    }
};

export const TAB_LIST_OPTIONS = {
    general: {
        text: "General"
    },
    upload: {
        text: "Upload"
    }
};

export const TINY_TEMPLATES = {
    basicAlert: {
        text: "Basic Alert (Primary)"
    },
    titledAlertDanger: {
        text: "Titled Alert (Danger)"
    },
    textCard: {
        text: "Text Card"
    },
    deckOfCards4: {
        text: "Deck of 4 Cards"
    },
    linkButton: {
        text: "Link Button"
    }
};

const DIAG_BOX = '[class="tox-dialog__body"]';
const DIAG_FOOTER = '[class="tox-dialog__footer-end"]';
const IFRAME_IMAGE = '[class="tox-dropzone"]';
const IFRAME_TOOLBAR = '[class="tox-toolbar"]';

export default {
    clickToxSave() {
        return cy
            .get(DIAG_FOOTER)
            .contains(TEXT.SAVE)
            .should("be.enabled")
            .click({ timeout: longTime });
    },

    clickTinyMenuOption(option) {
        return cy
            .get(IFRAME_TOOLBAR)
            .find(option.selector)
            .click();
    },

    clickUploadOption(option) {
        return cy
            .get('[role="tablist"]')
            .contains(option.text)
            .click();
    },

    tinyUploadChain() {
        return cy.get(IFRAME_IMAGE);
    },

    interceptTemplate() {
        return cy.intercept({ method: "GET", path: /js\/templates/, times: 1 }).as("templateLoad");
    },

    waitForTemplate() {
        return cy.waitForCode("@templateLoad", 200);
    },

    templateSelect(templateOption) {
        return cy.get('[class="tox-selectfield"] [id^="form-field"]').select(templateOption);
    },

    insertTemplate(templateOption) {
        this.clickTinyMenuOption(TINY_TOOLBAR_OPTION.insertTemplate);
        this.interceptTemplate();
        this.templateSelect(templateOption.text);
        this.waitForTemplate();
        // todo get rid of the need for this
        cy.wait(500);
        this.clickToxSave();
    },

    attachFileToEditor(attachment) {
        this.clickTinyMenuOption(TINY_TOOLBAR_OPTION.uploadImage);
        this.clickUploadOption(TAB_LIST_OPTIONS.upload);
        uploadOnPageChain(this.tinyUploadChain, attachment, { type: DRAG });
        this.clickToxSave();
    },

    typeTextBox(question) {
        return cy.iframe('[class*="tox-edit-area__iframe"]').type(question);
    },

    clearTextBox() {
        return cy.iframe('[class*="tox-edit-area__iframe"]').clear();
    }
};
