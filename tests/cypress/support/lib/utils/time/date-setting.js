/// <reference types="cypress" />

import { MS_IN_DAY } from "../../constants/common-definitions";

export function setCypressDate(numDays = null) {
    if (numDays !== null) {
        setTime(numDays);
    }
    cy.get(".react-datepicker__day--today").click({ force: true });
}

export function setTime(numDays) {
    cy.log(`Set time to ${numDays} days in future`);
    cy.clock(Date.now() + numDays * MS_IN_DAY, ["Date"]);
}

export function resetTime() {
    cy.clock().then(clock => {
        clock.restore();
    });
    cy.log("Clock has been reset");
}
