/// <reference types="cypress" />

import { MS_IN_DAY } from "../constants";

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

// returns date in format of yyyy-mm-dd
export function getDateFormatted(numDays = 0) {
    const tempDate = new Date();
    tempDate.setDate(tempDate.getDate() + numDays);
    return new Date(tempDate).toISOString().slice(0, 10);
}

// returns date in format of yyyy-mm-dd 00:00
export function dateWithTimeStamp(numDays = 1, time = "00:00") {
    return `${getDateFormatted(numDays)} ${time}`;
}
