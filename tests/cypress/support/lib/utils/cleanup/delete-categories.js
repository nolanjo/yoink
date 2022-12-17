/// <reference types="cypress" />

import { vivoApiCallVerified, vivoApiCallChain } from "../api/api-calls";

export function deleteAllCategories() {
    getCategories().then(res => {
        // if there are more than 10 categories, delete the ones made by automation
        if (res.body.length > 10) {
            for (const cat of res.body) {
                if (cat.title.match(/\d{13}/)) {
                    deleteCat(cat.id);
                }
            }
        }
    });
}

function getCategories() {
    return vivoApiCallVerified("api/external-link-categories");
}

function deleteCat(id) {
    return vivoApiCallChain(`api/external-link-categories/${id}`, "DELETE");
}
