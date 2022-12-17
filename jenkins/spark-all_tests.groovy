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
        timeout(time: 2, unit: 'HOURS') 
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
                            dataGatehring = load 'src/send_data.groovy'
                            dataGatehring.installScripts()
                        }
                        stage('Setup PHP') {
                            yarnCode.composerInstall()
                        }
                        stage('Setup global') {
                            yarnCode.yarnVivo()
                        }
                        stage('Setup Spark') {
                            yarnCode.yarnSpark()
                            util.cleanReportsDir()
                        }
                        stage('Deploy PreProd') {
                            deployCode.deploySpark()
                        }
                        stage('Run Critical') {
                            cypressRun.cypressSparkCriticalPath()
                        }
                        stage('Run Feature') {
                            cypressRun.cypressSpark()
                        }
                        stage('Run Regression') {
                            cypressRun.cypressSparkRegression()
                        }
                        stage('Run Logins') {
                            cypressRun.cypressLogins()
                        }
                        stage('Run Karate') {
                            karateRun.karate()
                        }
                        stage('Run Postman') {
                            postmanRun.postman()
                        }
                        stage('Run Postman Access Roles and Permissions') {
                            postmanRun.postmanRolesAndPermissions()
                        }
                        stage('Run Postman Notifications') {
                            postmanRun.postmanNotifications()
                        }
                        stage('Run Jest') {
                            echo "TODO... THIS "
                            // jestRun.runSpark()
                        }
                    }
                }
            }
        }
    }
    post {
        always {
            script {
                dataGatehring.sendMetrics(true, true, true, true)
                message = postAll.buildMessage(
                    true, 
                    true, 
                    true, 
                    true, 
                    true, 
                    true, 
                    true
                ).message
                postCypress.alwaysSpark(true, "*")
                postPostman.always()
                postKarate.always()
                // postJest.alwaysSpark()
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
