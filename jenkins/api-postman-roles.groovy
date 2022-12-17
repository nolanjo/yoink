// Cypress
import hudson.model.*
import jenkins.model.*
import hudson.tasks.test.AbstractTestResultAction
import org.jenkins.plugins.lockableresources.LockableResourcesManager as LRM

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
        // TEST_ENV = "pre-production3"
        USE_ISTANBUL = true
        GITHUB_ACTOR = 'Jenkins'
    }
    options {
        ansiColor('xterm')
        disableConcurrentBuilds()
        buildDiscarder(logRotator(daysToKeepStr: '30'))
        timeout(time: 1, unit: 'HOURS') 
    }

    stages{
        stage("Acquire Lock") {
            steps {
                script {
                    lock("${env.TEST_ENV}")  {
                        dir('infrastructure/jenkins') {
                            yarnCode = load 'src/yarn_install.groovy'
                            deployCode = load 'src/deploy_env.groovy'
                            cypressRun = load 'src/cypress_run.groovy'
                            postmanRun = load 'src/postman_run.groovy'
                            karateRun = load 'src/karate_run.groovy'
                            postCypress = load 'src/post_cypress_steps.groovy'
                            postPostman = load 'src/post_postman_steps.groovy'
                            postKarate = load 'src/post_karate_steps.groovy'
                            postAll = load 'src/post_all_steps.groovy'
                            util = load 'src/utils.groovy'
                            gitRun = load 'src/git_actions.groovy'
                            dataGatehring = load 'src/send_data.groovy'
                            dataGatehring.installScripts()
                        }
                        // stage('GitHub Merge') {
                        //     gitRun.gitMergeReady()
                        // }
                        stage('Setup global') {
                            yarnCode.yarnVivo()
                        }
                        stage('Setup Spark') {
                            yarnCode.yarnSpark()
                            util.cleanReportsDir()
                        }
                        // stage('Deploy PreProd') {
                        //     deployCode.deploySpark()
                        // }
                        stage('Run Postman') {
                            postmanRun.postman()
                        }
                        stage('Access Roles and Permissions') {
                            postmanRun.postmanRolesAndPermissions()
                        }
                        // stage('Run Critical') {
                        //     cypressRun.cypressSparkCriticalPath()
                        // }
                        // stage('Run Feature') {
                        //     cypressRun.cypressSpark()
                        // }
                        // stage('Run Regression') {
                        //     cypressRun.cypressSparkRegression()
                        // }
                    }
                }
            }
        }
    }
    post{
        always{
            script{
                // dataGatehring.sendMetrics(true, true, true, true)
                // postCypress.alwaysSpark(true, "*")
                postPostman.always()
                // postKarate.always()
                message = "Below is a link to the console:\n" +
                    "${BUILD_URL}/console"
            }
        }
        // success { 
        //     script {
        //         postAll.everySuccess(message)
        //     }
        // }
        // unstable { 
        //     script {
        //         postAll.everyUnstable(message)
        //     }
        // }
        // failure { 
        //     script {
        //         postAll.everyFail()
        //     }
        // }
    }
}