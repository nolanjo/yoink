pipeline {
    agent { 
        label "master" 
    }
    tools {
        nodejs "Node16"
    }
    options {
        ansiColor('xterm')
        disableConcurrentBuilds()
        buildDiscarder(logRotator(daysToKeepStr: '30'))
    }
    stages{
        stage('Setup') {
            steps{
                git 'git@github.com:workvivo/workvivo.git'
                sh script: 'yarn install',
                   returnStatus: true
                dir('mobile'){
                     sh script: 'yarn install',
                        returnStatus: true
                }
                dir('tests'){
                     sh script: 'yarn install',
                        returnStatus: true
                }
                dir('storybook'){
                     sh script: 'yarn install',
                        returnStatus: true
                }
            }
        }
        stage('Generate Mobile Documentation'){
            steps{
                dir ('mobile') {
                    sh 'rm -rf docs/wv-framework'
                    sh 'yarn fw-docs-build'
                }
            }
        }
        stage('Upload Mobile to S3'){
            steps{
                dir ('mobile/docs'){
                    withAWS(region:'eu-west-1',credentials:'workvivo-dev') {
                        s3Upload bucket: 'workvivo-documentation/mobile', 
                            includePathPattern: '**/*'
                    }
                }
            }
        }
        stage('Generate Cypress Documentation'){
            steps{
                dir ('tests') {
                    sh 'yarn cypress:docs'
                }
            }
        }
        stage('Upload Cypress to S3'){
            steps{
                dir ('tests/docs/cypress'){
                    withAWS(region:'eu-west-1',credentials:'workvivo-dev') {
                        s3Upload bucket: 'workvivo-documentation/cypress', 
                            includePathPattern: '**/*'
                    }
                }
            }
        }
        stage('Generate Storybook'){
            steps{
                dir ('storybook') {
                    sh 'yarn build-storybook'
                }
            }
        }
        stage('Upload Storybook to S3'){
            steps{
                dir ('storybook/storybook-static'){
                    withAWS(region:'eu-west-1',credentials:'workvivo-dev') {
                        s3Upload bucket: 'workvivo-documentation/storybook', 
                            includePathPattern: '**/*'
                    }
                }
            }
        }
    }
}
