const plugins = require("./cypress/plugins/index");
const lad = require("lodash");
const process = require("process");
const reporterConfig = require("./cypress/config/reporter-config");

const baseConfig = {
    e2e: {
        supportFile: "cypress/support/e2e.js",
        specPattern: "cypress/integration/**/*.js",
        env: {
            "cypress-react-selector": {
                root: "#app"
            },

            codeCoverageTasksRegistered: false,

            adminUser: {
                fullName: "James Ashton",
                name: "James",
                space: "Podcast Club",
                department: "Sales",
                location: "Paris"
            },

            general_employee_access_user: {
                fullName: "Claire Hayes",
                name: "Claire",
                space: "New Hires",
                department: "Finance",
                location: "Germany"
            },

            general_employee_access_user_2: {
                fullName: "Kory Andrews",
                name: "Kory",
                space: "Diversity & Inclusion",
                department: "Engineering",
                location: "London"
            },

            general_employee_access_user_3: {
                fullName: "Jen Busch",
                name: "Jen",
                space: "Health",
                department: "Sales",
                location: "San Francisco"
            },

            content_writer: {
                fullName: "Carol Robertson",
                name: "Carol",
                space: "New Hires",
                department: "Human Resources",
                location: "Ireland"
            },

            editor: {
                fullName: "Kate Banks",
                name: "Kate",
                space: "San Francisco Running Club",
                department: "Communications",
                location: "San Francisco"
            },

            peggyConfig: {
                acknowledgedPost: true,
                tinyEditor: true,
                limitLocalPosts: true,
                limitGlobalPosts: true,
                loginVersion: {
                    v1: true,
                    v2: false
                },
                orgId: 1
            },
            goal: "People",
            shoutOutTeam: "Finance",
            space: "Podcast",
            cleanUp: false,
            harRecord: false,
            harOutputDir: "cypress/har",
            searchRetries: 2
        },
        retries: {
            runMode: 0,
            openMode: 0
        },
        viewportWidth: 1280,
        viewportHeight: 840,

        defaultCommandTimeout: 15000,

        chromeWebSecurity: true,
        video: false,
        scrollBehavior: "center",

        reporter: "../node_modules/cypress-multi-reporters",
        reporterOptions: {
            ...reporterConfig("integration")
        }
    }
};

exports.baseConfig = baseConfig;

exports.setUpBase = config => {
    process.chdir(__dirname);
    config = lad.merge(baseConfig, config);
    config.e2e = {
        ...baseConfig.e2e,
        setupNodeEvents(on, config) {
            return plugins(on, config);
        }
    };

    return config;
};
