pipeline {
    agent { 
        label "" 
    }
    environment {
        utiility = load 'infrastructure/jenkins/src/utils.groovy'
        // TEST_ENV = utiility.findFreeEnvFresco()
        TEST_ENV = "qa-john-nolan"
    }
    options {
        ansiColor('xterm')
        disableConcurrentBuilds()
        buildDiscarder(logRotator(daysToKeepStr: '30'))
    }
    stages{
        stage("Acquire Lock"){
            steps {
                script {
                    lock("${env.TEST_ENV}") {
                        dir('infrastructure/jenkins') {
                            postmanRun = load 'src/postman_run.groovy'
                            postPostman = load 'src/post_postman_steps.groovy'
                        }
                        stage('Run Postman') {
                            postmanRun.postman()
                        }
                    }
                }
            }
        }
    }
    post {
        always {
            script {
                postPostman.always()
            }
        }
    }
}
