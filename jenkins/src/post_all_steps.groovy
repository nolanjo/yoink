// code for running post steps for all
import hudson.tasks.test.AbstractTestResultAction
import org.jenkins.plugins.lockableresources.LockableResourcesManager as LRM

def everySuccess(additonalMessage=""){
    echo "Tests Success"
    parseresults(additonalMessage)
}

def everyUnstable(additonalMessage=""){
    echo "Tests Unstable"
    parseresults(additonalMessage)
}

// special case
def everyFail(additonalMessage=""){
    slackSend teamDomain: 'workvivo', 
        tokenCredentialId: 'slack-token', 
        channel: "#jenkins-fails",
        message: "${JOB_BASE_NAME} test run ${BUILD_NUMBER}  has failed.\n\n" +
                "Below is a link to the console:\n" +
                "${BUILD_URL}/console\n" +
                "${additonalMessage}\n\n" 
    emailext to: 'jenkins+fails@workvivo.com',
        subject: "${JOB_BASE_NAME} ${BUILD_NUMBER} failed",
        body: "Hi,\n\n" +
            "The build for ${JOB_BASE_NAME} has failed.\n\n" +
            "Below is a link to the console:\n" +
            "${BUILD_URL}/console"
}

def parseresults(additonalMessage=""){
    def slackChannel = "#jenkins"
    AbstractTestResultAction testResult =  currentBuild.rawBuild.getAction(AbstractTestResultAction.class)
    if (testResult != null && testResult.failCount > 0) {
        slackChannel = "#jenkins-fails"
        currentBuild.description = "Tests: ${testResult.totalCount}, Failures: ${testResult.failCount}"
        slackMessage = "${JOB_BASE_NAME} test run ${BUILD_NUMBER}  has finished and ${testResult.failCount} out of ${testResult.totalCount} tests have failed :x:\n\n" +
                       "${additonalMessage}\n\n" 
    }
    else if (testResult != null && testResult.failCount == 0){
        currentBuild.description = "Tests: ${testResult.totalCount}"
        slackMessage = "${JOB_BASE_NAME} test run ${BUILD_NUMBER} has finished and all ${testResult.totalCount} tests have passed :white_check_mark:\n\n" +
                       "${additonalMessage}\n\n" 
    }
    slackSend teamDomain: 'workvivo',
        tokenCredentialId: 'slack-token', 
        channel: slackChannel,
        message: "${slackMessage}"
}

def everyReadyToMerge(additionalMessage, deployError){
    AbstractTestResultAction testResult =  currentBuild.rawBuild.getAction(AbstractTestResultAction.class)
    def message = ""
    if (testResult != null && testResult.failCount > 0) {
        currentBuild.description = "Tests: ${testResult.totalCount}, Failures: ${testResult.failCount}"
        message += "${testResult.failCount} out of ${testResult.totalCount} tests have failed\n"
    }
    else if (testResult != null && testResult.failCount == 0) {
        currentBuild.description = "Tests: ${testResult.totalCount}, Failures: ${testResult.failCount}"
        message += "All ${testResult.totalCount} tests have passed\n"
    }

    message += additionalMessage

    if (deployError) {
        message += "\n*Fail in path that requires clarification* :x:"
    }else {
        message += "\n*No Fail in relevant path, good to deploy* :white_check_mark:"
    }

    slackSend teamDomain: 'workvivo',
        tokenCredentialId: 'slack-token', 
        channel: "#jenkins-deploy-check",
        message: "${JOB_BASE_NAME} test run ${BUILD_NUMBER}  has finished with the below results.\n\n" +
                "${message}\n\n" 
}

def String buildMessage(feCritical=false, feFeature=false, beCritical=false, feLogin=false, feRegression=false, bePermissions=false, beNotifictaions=false){
    def returnState = [message:"", deployerror:false]
    if (feCritical) {
        def e2eCriticalSummary = junit testResults: "tests/cypress/reports/critical/junit/*.xml",
            allowEmptyResults: true,
            skipPublishingChecks: true,
            skipMarkingBuildUnstable: false;
        returnState.message += "\n <${BUILD_URL}/Cypress_20Report|*E2E Critical Tests*> - ${e2eCriticalSummary.totalCount}, Failures: ${e2eCriticalSummary.failCount}, Skipped: ${e2eCriticalSummary.skipCount}, Passed: ${e2eCriticalSummary.passCount}"
        returnState.deployerror = updateDeployCheck(returnState, e2eCriticalSummary.failCount)
    }
    if (feFeature) {
        def e2eFeatureSummary = junit testResults: "tests/cypress/reports/integration/junit/*.xml",
            allowEmptyResults: true,
            skipPublishingChecks: true,
            skipMarkingBuildUnstable: false;
        returnState.message += "\n <${BUILD_URL}/Cypress_20Report|*E2E Feature Tests*> - ${e2eFeatureSummary.totalCount}, Failures: ${e2eFeatureSummary.failCount}, Skipped: ${e2eFeatureSummary.skipCount}, Passed: ${e2eFeatureSummary.passCount}"
        returnState.deployerror = updateDeployCheck(returnState, e2eFeatureSummary.failCount)
    }
    if (beCritical) {
        def serverCriticalSummary = junit testResults: "tests/postman/newman/postman-critical.xml",
            allowEmptyResults: true,
            skipPublishingChecks: true,
            skipMarkingBuildUnstable: false;
        returnState.message += "\n <${BUILD_URL}/Newman_20Report|*Server Critical Tests*> - ${serverCriticalSummary.totalCount}, Failures: ${serverCriticalSummary.failCount}, Skipped: ${serverCriticalSummary.skipCount}, Passed: ${serverCriticalSummary.passCount}"
        returnState.deployerror = updateDeployCheck(returnState, serverCriticalSummary.failCount)
    }
    if (feLogin) {
        def e2eLogin = junit testResults: "tests/cypress/reports/logins/junit/*.xml",
            allowEmptyResults: true,
            skipPublishingChecks: true,
            skipMarkingBuildUnstable: false;
        returnState.message += "\n <${BUILD_URL}/Cypress_20Report|*E2E Login Tests*> - ${e2eLogin.totalCount}, Failures: ${e2eLogin.failCount}, Skipped: ${e2eLogin.skipCount}, Passed: ${e2eLogin.passCount}"
        returnState.deployerror = updateDeployCheck(returnState, e2eLogin.failCount)
    }
    if (feRegression) {
        def e2eRegression = junit testResults: "tests/cypress/reports/regression/junit/*.xml",
            allowEmptyResults: true,
            skipPublishingChecks: true,
            skipMarkingBuildUnstable: false;
        returnState.message += "\n <${BUILD_URL}/Cypress_20Report|*E2E Regression Tests*> - ${e2eRegression.totalCount}, Failures: ${e2eRegression.failCount}, Skipped: ${e2eRegression.skipCount}, Passed: ${e2eRegression.passCount}"
        // returnState.deployerror = updateDeployCheck(returnState, e2eRegression.failCount)
    }
    if (bePermissions) {
        def serverPermission = junit testResults: "tests/postman/newman/postman-roles.xml",
            allowEmptyResults: true,
            skipPublishingChecks: true,
            skipMarkingBuildUnstable: false;
        returnState.message += "\n <${BUILD_URL}/Newman_20Report|*Server Role + Permission Tests*> - ${serverPermission.totalCount}, Failures: ${serverPermission.failCount}, Skipped: ${serverPermission.skipCount}, Passed: ${serverPermission.passCount}"
        // returnState.deployerror = updateDeployCheck(returnState, serverPermission.failCount)
    }
    if (beNotifictaions) {
        def serverNotifications = junit testResults: "tests/postman/newman/postman-notifications.xml",
            allowEmptyResults: true,
            skipPublishingChecks: true,
            skipMarkingBuildUnstable: false;
        returnState.message += "\n <${BUILD_URL}/Newman_20Report|*Server Notification Tests*> - ${serverNotifications.totalCount}, Failures: ${serverNotifications.failCount}, Skipped: ${serverNotifications.skipCount}, Passed: ${serverNotifications.passCount}"
        // returnState.deployerror = updateDeployCheck(returnState, serverNotifications.failCount)
    }

    return returnState
}

def updateDeployCheck(returnState, errorCount){
    return returnState.deployerror || errorCount
}

return this