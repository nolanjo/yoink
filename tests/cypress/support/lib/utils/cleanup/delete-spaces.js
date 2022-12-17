/// <reference types="cypress" />

import { vivoApiCallVerified, vivoApiCallChain } from "../api/api-calls";

export function deleteSpaces() {
    getAllSpaces().then(res => {
        // delete the space made by automation if there are more than 18
        if (res.body.data.length >= 18) {
            for (const space of res.body.data) {
                if (space.name.match(/\d{13}/)) {
                    deleteSpace(space.id);
                }
            }
        }
    });
}

function getAllSpaces() {
    return vivoApiCallVerified("api/spaces?type=allModal");
}

function deleteSpace(spaceId) {
    vivoApiCallChain(`api/spaces/${spaceId}`, "DELETE");
    vivoApiCallChain(`api/spaces/${spaceId}/permadelete`, "DELETE");
}
