const process = require("process");

module.exports = (locale = "testing") => {
    return {
        reporterEnabled:
            "cypress-parallel/json-stream.reporter.js, cypress-mochawesome-reporter, mocha-junit-reporter",
        cypressMochawesomeReporterReporterOptions: {
            reportDir: `cypress/reports/${locale}`,
            charts: true,
            overwrite: true,
            reportPageTitle: "Cypress Testing",
            embeddedScreenshots: true,
            inlineAssets: true,
            html: false,
            json: true
        },
        mochaJunitReporterReporterOptions: {
            mochaFile: `cypress/reports/${locale}/junit/results-[hash].xml`,
            suiteTitleSeparatedBy: ".",
            testCaseSwitchClassnameAndName: true,
            testsuitesTitle: true,
            jenkinsMode: true
        },
        reporterOptions: {
            reportDir: `cypress/reports/${locale}`
        }
    };
};
