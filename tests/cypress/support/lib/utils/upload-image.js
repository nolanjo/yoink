/// <reference types="cypress" />

// all the files relate to fixtures dir, new version requires this prefix
const PREFIX = "cypress/fixtures/";

// the options for upload
export const DRAG = "drag-drop";
export const SELECT = "select";

function uploadImage(el, variableName, imageName = "banner.jpg", imageType = "image/jpg") {
    const blob = Cypress.Blob.binaryStringToBlob(variableName, imageType);

    const file = new File([blob], imageName, { type: imageType });
    const list = new DataTransfer();

    list.items.add(file);
    const myFileList = list.files;

    el[0].files = myFileList;
    el[0].dispatchEvent(new Event("change", { bubbles: true }));
}

export function uploadOnPageChain(cypressChain, testImage, options = {}) {
    options.type = options.type ? options.type : SELECT;
    if (options.check) {
        checkedUploadOnPage(cypressChain, testImage, options);
    } else {
        cy.log("Upload without hard checks");
        addAttachmentChain(cypressChain, testImage, options.type);
    }
}

function checkedUploadOnPage(cypressChain, testImage, options) {
    interceptSign();
    addAttachmentChain(cypressChain, testImage, options.type);
    waitForSign();
    checkUpload(testImage);
}

function interceptSign() {
    return cy
        .intercept({
            method: "POST",
            path: /api\/s3\/signature\/generate?.+/,
            times: 1
        })
        .as("signUpload");
}

function waitForSign() {
    return cy
        .wait("@signUpload")
        .its("response.statusCode")
        .should("eq", 200);
}

function checkUpload(attachmentFile) {
    const fileName = attachmentFile
        .split("/")
        .slice(-1)
        .toString();
    cy.log(`Checking length of ${fileName}`);
    return cy.contains("td", fileName).should("have.length.gte", 1);
}

function addAttachmentChain(cypressChain, attachmentFile, type = DRAG, params = {}) {
    return cypressChain(params).selectFile(`${PREFIX}${attachmentFile}`, {
        action: type,
        force: true
    });
}
