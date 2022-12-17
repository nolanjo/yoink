// code for running php unit tests

def phpUnitRun(){
    withCredentials(
        [
            string(credentialsId: 'APP_KEY', variable: 'APP_KEY'),
            string(credentialsId: 'TESTING_AWS_KEY', variable: 'AWS_S3_KEY'),
            string(credentialsId: 'TESTING_AWS_KEY', variable: 'AWS_S3_BUCKET'),
            string(credentialsId: 'TESTING_AWS_KEY', variable: 'AWS_ET_KEY'),
            string(credentialsId: 'TESTING_AWS_KEY', variable: 'AWS_COMPREHEND_KEY'),
            string(credentialsId: 'TESTING_AWS_KEY', variable: 'AWS_TRANSLATE_KEY'),
            string(credentialsId: 'TESTING_AWS_SECRET', variable: 'AWS_S3_SECRET'),
            string(credentialsId: 'TESTING_AWS_SECRET', variable: 'AWS_ET_SECRET'),
            string(credentialsId: 'TESTING_AWS_SECRET', variable: 'AWS_COMPREHEND_SECRET'),
            string(credentialsId: 'TESTING_AWS_SECRET', variable: 'AWS_TRANSLATE_SECRET'),
        ]
    ) {
        dir ('server') {
            sh script: 'vendor/bin/paratest --log-junit results_74_$GITHUB_JOB.xml',
                returnStatus: true
        }
    }
}

// this is here for now but we don't actually use it at the minute...
def postAllways(){
    junit allowEmptyResults: true, 
    skipPublishingChecks: true,
    skipMarkingBuildUnstable: true,
    testResults: 'server/**/results_74_*.xml'
}

return this