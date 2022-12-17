/// <reference types="cypress" />

import reactDatePicker from "./react-date-picker";

export default {
    clickDay(date) {
        reactDatePicker.day(date).click();
    },

    clickHour(hour) {
        reactDatePicker.time(hour).click();
    },

    clickTime(time) {
        reactDatePicker.time(time).click();
    }
};
