/// <reference types="cypress" />

export default {
    day(date) {
        return cy.contains('[aria-label^="day-"]', new RegExp("\\b" + date + "\\b"));
    },

    time(time) {
        return cy.contains('[class="react-datepicker__time-list-item"]', time);
    }
};
