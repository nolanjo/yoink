/// <reference types="cypress" />

import { loggable } from "cypress-pipe";

const POP_UP_CLOSE = '[class="Toastify__toast-body"]';

export function clickConfirmPopUp() {
    const getPopUp = loggable(body => {
        const found = body.find(POP_UP_CLOSE);
        if (found.length > 0 && found.length < 2) {
            cy.wrap(found).click({ force: true });
        } else if (found.length > 1) {
            // if there is more than one, deal with first and call self
            cy.wrap(found[0]).click({ force: true });
            clickConfirmPopUp();
        }
    });
    return cy.get("body").pipe(getPopUp);
}
