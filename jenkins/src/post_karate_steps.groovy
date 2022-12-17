// code for running post steps for postman

def always(linkLast = true){
    junit allowEmptyResults: true, 
        skipPublishingChecks: true,
        skipMarkingBuildUnstable: true,
        testResults: 'tests/api-automation/target/**/*.xml'
    publishHTML (target : [
        allowMissing: true,
        alwaysLinkToLastBuild: linkLast,
        keepAll: linkLast,
        reportDir: 'tests/api-automation/target/cucumber-html-reports',
        reportFiles: 'overview-features.html',
        reportName: 'CucumberReport',
        reportTitles: 'CucumberReport'])
}

return this