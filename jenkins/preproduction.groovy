// Pre-Production
import hudson.model.*
import jenkins.model.*
import hudson.tasks.test.AbstractTestResultAction
pipeline {
    agent { 
        label "" 
    }
    tools {
        nodejs "Node"
    }
    options {
        ansiColor('xterm')
        disableConcurrentBuilds()
        buildDiscarder(logRotator(daysToKeepStr: '30'))
        lock('pre-production')
    }
    environment {
        FILESYSTEM_DRIVER='local'
        APP_BASE_DOMAIN='server.test'
        APP_URL='http://server.test'
        AWS_S3_REGION='eu-west-1'
        AWS_CLOUDFRONT_DOMAIN=''
        AWS_ET_REGION='eu-west-1'
        AWS_ET_PIPELINE_ID=''
        AWS_ET_PRESET_ID=''
        AWS_ET_VERSION='latest'
        AWS_COMPREHEND_REGION='eu-west-1'
        AWS_TRANSLATE_REGION='eu-west-1'
        SCOUT_DRIVER='null'
        ELASTICSEARCH_HOST='localhost'
    }
    stages{
        stage('Setup') {
            steps{
                withCredentials([
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
                ]){
                    sh script: 'yarn install'
                    dir('server'){
                        sh script: 'composer install',
                           returnStatus: true
                    }
                }
            }
        }
        stage('Deploy PreProd'){
            environment{
                USE_ISTANBUL = true
                GITHUB_ACTOR = "Jenkins ${JOB_BASE_NAME}"
            }
            steps{
                writeFile file: 'cli/.cache/data.json', 
                          text: '''{
                                "user": {
                                    "name": "Deploy Bot"
                                }
                            }
                        '''
                withEnv(['PATH+EXTRA=$PATH:/usr/local/bin']) {
                    sh script: '''
                        alias vivo="${WORKSPACE}/cli/src/cli/cli.ts"
                        vivo deploy web pre-production --deployStrategy=AllAtOnce --buildFrescoThemes=true --buildSparkThemes=true --buildFrescoAssets=true --buildSparkAssets=false --buildAdminAssets=true --buildDashboardAssets=false --reindexSearch=false --updateTranslations=false
                      ''',
                      returnStatus: true
                }            
            }
        }
        stage('Parallel Tests'){
            parallel {
                stage('Cypress'){
                    steps{
                        withCredentials([string(credentialsId: 'testUrl', variable: 'CYPRESS_testUrl'), 
                            string(credentialsId: 'adminEmail', variable: 'CYPRESS_adminEmail'), 
                            string(credentialsId: 'adminPassword', variable: 'CYPRESS_adminPassword'), 
                            string(credentialsId: 'general_employee_access_userEmail', variable: 'CYPRESS_general_employee_access_userEmail'), 
                            string(credentialsId: 'general_employee_access_userPassword', variable: 'CYPRESS_general_employee_access_userPassword'), 
                            string(credentialsId: 'general_employee_access_user_2Email', variable: 'CYPRESS_general_employee_access_user_2Email'), 
                            string(credentialsId: 'general_employee_access_user_2Password', variable: 'CYPRESS_general_employee_access_user_2Password'), 
                            string(credentialsId: 'general_employee_access_user_3Email', variable: 'CYPRESS_general_employee_access_user_3Email'), 
                            string(credentialsId: 'general_employee_access_user_3Password', variable: 'CYPRESS_general_employee_access_user_3Password'),
                            string(credentialsId: 'external_user_Email', variable: 'CYPRESS_external_user_Email'), 
                            string(credentialsId: 'external_user_password', variable: 'CYPRESS_external_user_password'),
                            string(credentialsId: 'mailHogUrl', variable: 'CYPRESS_mailHogUrl')]) {
                            dir ('fresco') {
                                sh script: 'npx cypress run --env configFile=pre-production --spec "cypress/integration/**/*.js" --browser chrome',
                                   returnStatus: true
                            }
                        }
                    }
                    post{
                        always{
                            junit testResults: 'fresco/cypress/reports/junit/*.xml',
                                  allowEmptyResults: true
                            publishHTML([
                                reportDir: 'fresco/cypress/reports',
                                reportFiles: 'index.html',
                                reportName: 'Cypress Report',
                                reportTitles: '',
                                allowMissing: false,
                                alwaysLinkToLastBuild: false,
                                keepAll: false])
                        }
                    }
                }
                stage('Jest'){
                    steps{
                        dir ('fresco') {
                            sh script: 'yarn jest --ci --reporters=jest-junit --reporters=default',
                               returnStatus: true
                        }
                    }
                    post{
                        always{            
                            junit testResults: 'fresco/junit.xml',
                                  allowEmptyResults: true
                        }
                    }
                }
                stage('PHP Unit'){
                    steps{
                        withCredentials([
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
                        ]){
                            dir ('server'){
                                sh script: 'vendor/bin/paratest --log-junit results_74_$GITHUB_JOB.xml',
                                   returnStatus: true
                            }
                        }
                    }
                    post{
                       always{
                            junit testResults: 'server/**/results_74_*.xml',
                                  allowEmptyResults: true
                        }
                    }
                }
            }
        }
        stage('Karate'){
            steps{
                withCredentials([string(credentialsId: 'testUrl', variable: 'testUrl'),
                    string(credentialsId: 'adminEmail', variable: 'adminEmail'),
                    string(credentialsId: 'adminPassword', variable: 'adminPassword'), 
                    string(credentialsId: 'general_employee_access_userEmail', variable: 'general_employee_access_userEmail'), 
                    string(credentialsId: 'general_employee_access_userPassword', variable: 'general_employee_access_userPassword'), 
                    string(credentialsId: 'general_employee_access_user_2Email', variable: 'general_employee_access_user_2Email'), 
                    string(credentialsId: 'general_employee_access_user_2Password', variable: 'general_employee_access_user_2Password'), 
                    string(credentialsId: 'general_employee_access_user_3Email', variable: 'general_employee_access_user_3Email'), 
                    string(credentialsId: 'general_employee_access_user_3Password', variable: 'general_employee_access_user_3Password'),
                    string(credentialsId: 'external_user_Email', variable: 'external_user_Email'), 
                    string(credentialsId: 'external_user_password', variable: 'external_user_password'),
                    string(credentialsId: 'DBHost', variable: 'DBHost'),
                    string(credentialsId: 'DBPassword', variable: 'DBPassword'),
                    string(credentialsId: 'DBUsername', variable: 'DBUsername'),
                    string(credentialsId: 'OrganisationId', variable: 'OrganisationId')]) {
                        dir ('tests/api-automation') {
                            sh script: 'mvn clean test -Dkarate.options="--tags @Regression" -DadminEmail="$adminEmail" -DadminPassword="$adminPassword" -Dgeneral_employee_access_userEmail="$general_employee_access_userEmail" -Dgeneral_employee_access_userPassword="$general_employee_access_userPassword" -Dgeneral_employee_access_user_2Email="$general_employee_access_user_2Email" -Dgeneral_employee_access_user_2Password="$general_employee_access_user_2Password" -Dgeneral_employee_access_user_3Email="$general_employee_access_user_3Email" -Dgeneral_employee_access_user_3Password="$general_employee_access_user_3Password" -Dexternal_user_Email="$external_user_Email" -Dexternal_user_password="$external_user_password" -DtestUrl="$testUrl" -DDBHost="$DBHost" -DDBPassword="$DBPassword" -DDBUsername="$DBUsername" -DOrganisationId="$OrganisationId"',
                               returnStatus: true
                        }
                    }
            }
            post{
               always{
                    junit testResults: 'tests/api-automation/target/**/*.xml',
                          allowEmptyResults: true
                    publishHTML (
                        target : [
                            allowMissing: false,
                            alwaysLinkToLastBuild: true,
                            keepAll: true,
                            reportDir: 'tests/api-automation/target/cucumber-html-reports',
                            reportFiles: 'overview-features.html',
                            reportName: 'CucumberReport',
                            reportTitles: 'CucumberReport'
                        ]
                    )
                }
            }
        }
    }
    post {
        always{
            script{
                dir ('fresco') {
                    sh script: 'npx merge-cypress-jest-coverage',
                        returnStatus: true
                }
            }
            step([
                $class: 'CloverPublisher',
                cloverReportDir: 'fresco/coverage',
                cloverReportFileName: 'clover.xml',
                healthyTarget: [methodCoverage: 70, conditionalCoverage: 80, statementCoverage: 80], // optional, default is: method=70, conditional=80, statement=80
                unhealthyTarget: [methodCoverage: 50, conditionalCoverage: 50, statementCoverage: 50], // optional, default is none
                failingTarget: [methodCoverage: 0, conditionalCoverage: 0, statementCoverage: 0]     // optional, default is none
            ])
        }

        success { 
            script {
                AbstractTestResultAction testResult =  currentBuild.rawBuild.getAction(AbstractTestResultAction.class)
                currentBuild.description = "Tests: ${testResult.totalCount}"
                slackSend teamDomain: 'workvivo', 
                          tokenCredentialId: 'slack-token', 
                          message: "${JOB_BASE_NAME} test run ${BUILD_NUMBER} has finished and all tests have passed.\n\n" +
                                   "Below is a link to the test results:\n" +
                                   "${BUILD_URL}/testReport/\n\n"
            }
        }
        unstable { 
            script {
                AbstractTestResultAction testResult =  currentBuild.rawBuild.getAction(AbstractTestResultAction.class)
                if (testResult != null && testResult.failCount > 0) {
                    currentBuild.description = "Tests: ${testResult.totalCount}, Failures: ${testResult.failCount}"
                    slackSend teamDomain: 'workvivo', 
                              tokenCredentialId: 'slack-token', 
                              message: "${JOB_BASE_NAME} test run ${BUILD_NUMBER}  has finished with the below results.\n\n" +
                                       "${testResult.failCount} out of ${testResult.totalCount} tests have failed\n" +
                                       "Below is a link to the test results:\n" +
                                       "${BUILD_URL}/testReport/\n\n" 
                }
            }
        }
        failure { 
            slackSend teamDomain: 'workvivo', 
                      tokenCredentialId: 'slack-token', 
                      message: "${JOB_BASE_NAME} test run ${BUILD_NUMBER}  has failed.\n\n" +
                               "Below is a link to the console:\n" +
                               "${BUILD_URL}/console"
            emailext to: 'jenkins+fails@workvivo.com',
                     subject: "${JOB_BASE_NAME} ${BUILD_NUMBER} failed",
                     body: "Hi,\n\n" +
                           "The build for ${JOB_BASE_NAME} has failed.\n\n" +
                           "Below is a link to the console:\n" +
                           "${BUILD_URL}/console"
        }
    }
}
