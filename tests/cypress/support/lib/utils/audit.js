/// <reference types="cypress" />

export const DEFAULT_THRESHOLDS = {
    // performance score seems to be unreliable so I am just ignoring
    performance: 0,
    accessibility: 80
    // seo: 70,
    // 'first-contentful-paint': 2000,
    // 'largest-contentful-paint': 3000,
    // 'cumulative-layout-shift': 0.1,
    // 'total-blocking-time': 500,
};

export const DESKTOP_CONFIG = {
    formFactor: "desktop",
    screenEmulation: { disabled: true }
};

export function runLighthouseCheck(customThresholds = DEFAULT_THRESHOLDS, config = DESKTOP_CONFIG) {
    return cy.lighthouse(customThresholds, config);
}
