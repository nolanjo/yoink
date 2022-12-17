// All Tests
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
    options {
        ansiColor('xterm')
        disableConcurrentBuilds()
        buildDiscarder(logRotator(daysToKeepStr: '30'))
    }
    environment {
        utiility = load 'infrastructure/jenkins/src/utils.groovy'
        TEST_ENV = utiility.findFreeEnvSpark()
        // TEST_ENV = "qa-john-nolan"
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
        USE_ISTANBUL = true
        GITHUB_URL = 'git@github.com:workvivo/workvivo.git'
        GITHUB_ACTOR = "Jenkins ${JOB_BASE_NAME}"
    }
    stages{
        stage("Acquire Lock"){
            steps {
                script {
                    lock("${env.TEST_ENV}")  {
                        dir('infrastructure/jenkins') {
                            cypressRun = load 'src/cypress_run.groovy'
                            deployCode = load 'src/deploy_env.groovy'
                            yarnCode = load 'src/yarn_install.groovy'
                            jestRun = load 'src/jest_run.groovy'
                            karateRun = load 'src/karate_run.groovy'
                            postmanRun = load 'src/postman_run.groovy'
                            postKarate = load 'src/post_karate_steps.groovy'
                            postCypress = load 'src/post_cypress_steps.groovy'
                            postPostman = load 'src/post_postman_steps.groovy'
                            postJest = load 'src/post_jest_steps.groovy'
                            postAll = load 'src/post_all_steps.groovy'
                            util = load 'src/utils.groovy'
                            gitRun = load 'src/git_actions.groovy'
                            labels = load 'src/labels.groovy'
                        }
                        // if(util.checkLabel(labels.TEST_AUTOMATION())){
                            stage('Setup PHP') {
                                // sh script: "sudo apt-get update"
                                postmanRun.postman()
                                // gitRun.gitMergeReady()
                                // yarnCode.composerInstall()
                            }
                        // }
                        // if(util.checkLabel(labels.TEST_AUTOMATION())){
                        //     stage('Setup global') {
                        //         yarnCode.yarnVivo()
                        //     }
                        // }
                        // stage('Setup Spark') {
                        //     yarnCode.yarnSpark()
                        // }
                        // stage('Deploy PreProd') {
                        //     echo "test"
                        //     // deployCode.deploySpark()
                        // }
                        // stage('Run E2E Integration') {
                        //     echo "test"
                        //     // cypressRun.cypressSpark()
                        // }
                        // stage('Run Karate') {
                        //     karateRun.karate()
                        // }
                        // if(util.checkLabel(labels.TEST_AUTOMATION())){
                        //     stage('Run Postman') {
                        //         echo "test"
                        //         // 
                        //     }
                        // }
                        // if(util.checkLabel(labels.TEST_AUTOMATION())){
                        //     stage('Run Jest') {
                        //         echo "TODO... THIS "
                        //         // jestRun.runSpark()
                        //     }
                        // }
                    }
                }
            }
        }
    }
    post {
        always {
            script {
                // postCypress.alwaysSpark()
                postPostman.always()
                // postKarate.always()
                // postJest.alwaysSpark()
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
