// code for running post steps for cypress

def alwaysSpark(linkLast = true, folder = "integration"){
    publishHTML([
        reportDir: "tests/cypress/reports",
        reportFiles: "${folder}/index*.html",
        reportName: 'Cypress Report',
        reportTitles: '',
        allowMissing: true,
        alwaysLinkToLastBuild: linkLast,
        keepAll: linkLast])
    step([
        $class: 'CloverPublisher',
        cloverReportDir: 'tests/cypress-coverage',
        cloverReportFileName: 'clover.xml',
        healthyTarget: [methodCoverage: 70, conditionalCoverage: 80, statementCoverage: 80], // optional, default is: method=70, conditional=80, statement=80
        unhealthyTarget: [methodCoverage: 50, conditionalCoverage: 50, statementCoverage: 50], // optional, default is none
        failingTarget: [methodCoverage: 0, conditionalCoverage: 0, statementCoverage: 0]     // optional, default is none
    ])
}


return this