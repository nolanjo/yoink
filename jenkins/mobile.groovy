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
        TEST_ENV = utiility.findFreeEnvMobile()
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
        stage("Acquire Lock") {
            steps {
                script {
                    lock("${env.TEST_ENV}")  {
                        dir('infrastructure/jenkins') {
                            yarnCode = load 'src/yarn_install.groovy'
                            deployCode = load 'src/deploy_env.groovy'
                            postAll = load 'src/post_all_steps.groovy'
                            util = load 'src/utils.groovy'
                            gitRun = load 'src/git_actions.groovy'
                        }
                        stage('Setup Mobile') {
                            yarnCode.yarnVivo()
                        }
                        stage('Setup Spark') {
                            yarnCode.yarnSpark()
                            util.cleanReportsDir()
                        }
                        stage('Deploy PreProd') {
                            deployCode.deploySpark()
                        }
                        stage('Run Mobile') {
                            echo "TODO... THIS "
                            // todo, run mobile 
                        }
                    }
                }
            }
        }
    }
    post{
        always{
            script{
                message = postAll.buildMessage(
                    true
                ).message
                postCypress.alwaysSpark(true, "*")
                postPostman.always()
                postKarate.always()
                message += "\nBelow is a link to the console:\n" +
                    "${BUILD_URL}/console"
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
