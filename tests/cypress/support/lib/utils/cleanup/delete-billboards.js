/// <reference types="cypress" />

import { vivoApiCallVerified, vivoApiCallChain } from "../api/api-calls";

export function deleteBillboards() {
    getBillboards().then(res => {
        // if there are more than 6 billboards, delete the ones made by automation
        if (res.body.length >= 6) {
            for (const bill of res.body) {
                if (bill.title.match(/\d{13}/)) {
                    deleteBillboard(bill.id);
                }
            }
        }
    });
}

function getBillboards() {
    return vivoApiCallVerified("api/billboards/admin?type=all");
}

function deleteBillboard(id) {
    return vivoApiCallChain(`api/billboards/${id}`, "DELETE");
}
