// Cypress Logins
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
        // TEST_ENV = "pre-production3"
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
                            postCypress = load 'src/post_cypress_steps.groovy'
                            postAll = load 'src/post_all_steps.groovy'
                            util = load 'src/utils.groovy'
                        }
                        stage('Setup global') {
                            yarnCode.yarnVivo()
                        }
                        stage('Run Login tests') {
                            cypressRun.cypressLogins()
                        }

                    }
                }
            }
        }
    }
    post {
        always {
            script {
                postCypress.alwaysSpark(true, "*")
                message = "Below is a link to the console:\n" +
                    "${BUILD_URL}/testReport/\n\n" 
            }
        }
    }
}