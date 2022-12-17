// Cypress
import hudson.model.*
import jenkins.model.*
import hudson.tasks.test.AbstractTestResultAction
import org.jenkins.plugins.lockableresources.LockableResourcesManager as LRM

pipeline {
    agent {
        node {
            label "spark"
            customWorkspace 'workspace/spark-pr'
        }
    }
    tools {
        nodejs "Node"
    }
    environment {
        utiility = load 'infrastructure/jenkins/src/utils.groovy'
        TEST_ENV = utiility.findFreeEnvSpark()
        // TEST_ENV = "qa-john-nolan"
        USE_ISTANBUL = true
        GITHUB_ACTOR = 'Jenkins'
    }
    options {
        ansiColor('xterm')
        disableConcurrentBuilds()
        buildDiscarder(logRotator(daysToKeepStr: '30'))
        timeout(time: 2, unit: 'HOURS') 
    }

    stages{
        stage("Acquire Lock"){
            steps {
                script {
                    dir('infrastructure/jenkins'){
                        cypressRun = load 'src/cypress_run.groovy'
                        deployCode = load 'src/deploy_env.groovy'
                        yarnCode = load 'src/yarn_install.groovy'
                        postmanRun = load 'src/postman_run.groovy'
                        postCypress = load 'src/post_cypress_steps.groovy'
                        postPostman = load 'src/post_postman_steps.groovy'
                        postAll = load 'src/post_all_steps.groovy'
                        util = load 'src/utils.groovy'
                        labels = load 'src/labels.groovy'
                        dataGatehring = load 'src/send_data.groovy'
                        dataGatehring.installScripts()
                    }
                    lock("${env.TEST_ENV}")  {
                        stage('Setup global') {
                            yarnCode.yarnVivo()
                        }
                        stage('Setup Spark') {
                            util.cleanReportsDir()
                            yarnCode.yarnSpark()
                        }
                        stage('Deploy PreProd'){
                            deployCode.deploySpark()
                        }
                        if( util.checkLabel(labels.FE_CRITICAL_TESTING()) || util.checkLabel(labels.FE_FEATURE_TESTING()) ){
                            stage('Critical Path Testing'){
                                cypressRun.cypressSparkCriticalPath()
                            }
                        }
                        if( util.checkLabel(labels.FE_FEATURE_TESTING()) ){
                            stage('Feature Path Testing'){
                                cypressRun.cypressSpark()
                            }
                        }
                        if( util.checkLabel(labels.BE_CRITICAL_TESTING()) ){
                            stage('Critical Server Testing'){
                                postmanRun.postman()
                            }
                        }
                    }
                }
            }
        }
    }
    post{
        always{
            script{
                dataGatehring.sendMetrics(
                    util.checkLabel(labels.FE_CRITICAL_TESTING()) || util.checkLabel(labels.FE_FEATURE_TESTING()),  
                    util.checkLabel(labels.FE_FEATURE_TESTING()), 
                    false, 
                    util.checkLabel(labels.BE_CRITICAL_TESTING())
                )
                message = postAll.buildMessage(
                    util.checkLabel(labels.FE_CRITICAL_TESTING()) || util.checkLabel(labels.FE_FEATURE_TESTING()), 
                    util.checkLabel(labels.FE_FEATURE_TESTING()), 
                    util.checkLabel(labels.BE_CRITICAL_TESTING())
                ).message
                postCypress.alwaysSpark(false, "*")
                postPostman.always(false)
                util.gitComment("Results can be found here: ${BUILD_URL}/testReport/\n\n")
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
