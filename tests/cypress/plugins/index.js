/// <reference types="cypress" />

const fs = require("fs-extra");
const path = require("path");
const { lighthouse, pa11y, prepareAudit } = require("cypress-audit");
const ReportGenerator = require("lighthouse/report/generator/report-generator");
const lad = require("lodash");
const { AssertionError } = require("assert");
const { beforeRunHook } = require("cypress-mochawesome-reporter/lib");
const { install, ensureBrowserFlags } = require("@neuralegion/cypress-har-generator");

// tries to read env variables and if it cannot, will look for local variables in config-protected
function getProtectedData(fileName, runData) {
    let data = {};
    if (typeof runData.testUrl !== "undefined") {
        data = {
            url: runData.testUrl,
            mailHogUrl: runData.mailHogUrl,
            saml_url: runData.samlUrl,
            okta_url: runData.oktaUrl,

            adminUser: {
                email: runData.adminEmail,
                password: runData.adminPassword
            },
            general_employee_access_user: {
                email: runData.general_employee_access_userEmail,
                password: runData.general_employee_access_userPassword
            },
            general_employee_access_user_2: {
                email: runData.general_employee_access_user_2Email,
                password: runData.general_employee_access_user_2Password
            },
            general_employee_access_user_3: {
                email: runData.general_employee_access_user_3Email,
                password: runData.general_employee_access_user_3Password
            },
            external_user: {
                email: runData.external_user_Email,
                password: runData.external_user_password
            },
            content_writer: {
                email: runData.content_writer_email,
                password: runData.content_writer_password
            },
            editor: {
                email: runData.editor_email,
                password: runData.editor_password
            },
            sso_admin: {
                email: runData.sso_admin_email,
                password: runData.sso_admin_password
            },
            pass_admin: {
                email: runData.pass_admin_email,
                password: runData.pass_admin_password
            },
            sso_general: {
                email: runData.sso_general_email,
                password: runData.sso_general_password
            },
            pass_general: {
                email: runData.pass_general_email,
                password: runData.pass_general_password
            },
            sso_external: {
                email: runData.sso_external_email,
                password: runData.sso_external_password
            },
            pass_external: {
                email: runData.pass_external_email,
                password: runData.pass_external_password
            },
            sso_external_saml: {
                email: runData.sso_external_saml_email,
                password: runData.sso_external_saml_password
            }
        };
    } else {
        const pathToConfigFile = path.resolve(
            "cypress",
            "config",
            "hidden",
            `${path.basename(fileName)}`
        );
        data = require(pathToConfigFile);
    }

    return data;
}

/**
 * @type {Cypress.PluginConfig}
 */

module.exports = (on, config) => {
    install(on, config);

    // import for code coverage
    // this is setting the code coverage task to true
    require("@cypress/code-coverage/task")(on, config);
    // import for moacha awesome
    require("cypress-mochawesome-reporter/plugin")(on);

    const file = config.configFile;
    let runData = config;
    runData.env = lad.merge(runData.env, getProtectedData(file, config.env));

    // // require to allow unit test use code coverage
    // on(
    //     "file:preprocessor",
    //     require("@cypress/code-coverage/use-browserify-istanbul")
    // );

    // deletes the nyc folder to fix coverage issue, need to run before run hook for mochawesome manually
    on("before:run", async details => {
        await beforeRunHook(details);
        if (runData.env.codeCoverageTasksRegistered === true) {
            const pathToDir = path.resolve("..", "spark", ".nyc_output");
            fs.rmdir(pathToDir, { recursive: true }, err => {
                if (err) {
                    console.log(err);
                }
            });
        }
    });

    on("before:browser:launch", (browser = {}, launchOptions) => {
        prepareAudit(launchOptions);
        if (browser.name === "chrome") {
            launchOptions.args.push("--use-fake-ui-for-media-stream");
            launchOptions.args.push("--use-fake-device-for-media-stream");
        }
        ensureBrowserFlags(browser, launchOptions);
        return launchOptions;
    });

    on("task", {
        log(message) {
            console.log(message);
            return null;
        },
        lighthouse: lighthouse(lighthouseReport => {
            // try remove the http part, if its not defined then just take full url
            let fileName = lighthouseReport.lhr.requestedUrl.split("//")[1];
            if (typeof fileName === "undefined") {
                fileName = lighthouseReport.lhr.requestedUrl;
            }
            fileName = fileName.replace(/\//g, "-");
            fileName = fileName.replace(/\./g, "-");
            fileName = fileName.replace(/-+$/, "");

            const pathToFile = path.resolve(
                "..",
                "tests",
                "cypress",
                "axe_reports",
                `${fileName}.html`
            );
            const htmlReport = ReportGenerator.generateReport(lighthouseReport.lhr, "html");
            fs.writeFileSync(pathToFile, htmlReport);
        }),
        pa11y: pa11y(pa11yReport => {
            console.log(pa11yReport); // raw pa11y reports
        })
    });

    return runData;
};
