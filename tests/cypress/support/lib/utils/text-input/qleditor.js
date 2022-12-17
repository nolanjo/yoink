/// <reference types="cypress" />

import { DRAG, uploadOnPageChain } from "../";

function formBody() {
    return cy.get("[class^='ql-editor']");
}

export default {
    attachFileToEditor(attachment) {
        return uploadOnPageChain(formBody, attachment, { type: DRAG });
    },

    typeTextBox(testText) {
        return formBody().type(testText);
    },
    clearTextBox() {
        return formBody().clear();
    }
};
