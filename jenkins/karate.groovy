// Karate
import hudson.model.*
import jenkins.model.*
import hudson.tasks.test.AbstractTestResultAction
pipeline {
    agent { 
        label "spark" 
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
    }
    stages{
        stage('Run Tests') {
            steps{
                script {
                    lock("${env.TEST_ENV}")  {
                        dir('infrastructure/jenkins'){
                            karateRun = load 'src/karate_run.groovy'
                            postKarate = load 'src/post_karate_steps.groovy'
                            postAll = load 'src/post_all_steps.groovy'
                        }
                        stage('Run Karate') {
                            karateRun.karate()
                        }
                    }
                }
            }
            post{
                always{
                    script {
                        postKarate.always()
                        messgae = "Below is a link to the test results:\n" +
                            "${BUILD_URL}/Cucumber_5fReport/\n\n" 
                    }
                }
                success { 
                    script {
                        postAll.everySuccess(messgae)
                    }
                }
                unstable { 
                    script {
                        postAll.everyUnstable(messgae)
                    }
                }
                failure { 
                    script {
                        postAll.everyFail()
                    }
                }
            }
        }
    }
}
