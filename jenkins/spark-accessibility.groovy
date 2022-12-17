// Accessibility
import hudson.model.*
import jenkins.model.*
import hudson.tasks.test.AbstractTestResultAction
pipeline {
    agent { 
        label "spark" 
    }
    tools {
        nodejs "Node"
    }
    environment {
        utiility = load 'infrastructure/jenkins/src/utils.groovy'
        TEST_ENV = utiility.findFreeEnvSpark()
        // TEST_ENV = "qa-john-nolan"
    }
    options {
        ansiColor('xterm')
        disableConcurrentBuilds()
        buildDiscarder(logRotator(daysToKeepStr: '30'))
        timeout(time: 1, unit: 'HOURS') 
    }
    stages{
        stage("Acquire Lock"){
            steps {
                script {
                    dir('infrastructure/jenkins'){
                        cypressRun = load 'src/cypress_run.groovy'
                        deployCode = load 'src/deploy_env.groovy'
                        yarnCode = load 'src/yarn_install.groovy'
                        postAll = load 'src/post_all_steps.groovy'
                    }
                    lock("${env.TEST_ENV}")  {
                        stage('Setup global') { 
                            yarnCode.yarnVivo()
                        }
                        stage('Setup Spark') {
                            yarnCode.yarnSpark()
                        }
                        stage('Run Tests'){
                            cypressRun.accessibilitySpark()
                        }        
                    }
                }
            }
        }
    }
    post {
        always {
            junit allowEmptyResults: true, skipPublishingChecks: true, testResults: '**/reports/**/*.xml'
            dir ('tests/cypress/axe_reports'){
                withAWS(region:'eu-west-1',credentials:'workvivo-dev') {
                    s3Upload bucket: 'workvivo-test-results/cypress/accessibility', 
                        includePathPattern: '**/*.html'
                }
            }
            step([
                $class: 'CloverPublisher',
                cloverReportDir: 'tests/cypress-coverage',
                cloverReportFileName: 'clover.xml',
                healthyTarget: [methodCoverage: 70, conditionalCoverage: 80, statementCoverage: 80], // optional, default is: method=70, conditional=80, statement=80
                unhealthyTarget: [methodCoverage: 50, conditionalCoverage: 50, statementCoverage: 50], // optional, default is none
                failingTarget: [methodCoverage: 0, conditionalCoverage: 0, statementCoverage: 0]     // optional, default is none
            ])
            script {
                message = "Below is a link to the test results:\n" +
                    "${BUILD_URL}/testReport/\n\n" 
            }
        }
        success { 
            script {
                postAll.everySuccess(message)
            }
        }
        unstable { 
            script {
                postAll.everyUnstable(message)
            }
        }
        failure { 
            script {
                postAll.everyFail()
            }
        }
    }
}

