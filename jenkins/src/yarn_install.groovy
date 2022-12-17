// code for running yarn install jobs

def yarnSpark() {
    dir('spark'){
        sh script: 'yarn install',
        returnStatus: true
    }
}

def yarnVivo() {
    sh(script: 'yarn install && cd tests && yarn postinstall', returnStatus: true)
}

def composerInstall() {
    withCredentials (
        [
            string(credentialsId: 'APP_KEY', variable: 'APP_KEY'),
            string(credentialsId: 'TESTING_AWS_KEY', variable: 'AWS_S3_KEY'),
            string(credentialsId: 'TESTING_AWS_KEY', variable: 'AWS_S3_BUCKET'),
            string(credentialsId: 'TESTING_AWS_KEY', variable: 'AWS_ET_KEY'),
            string(credentialsId: 'TESTING_AWS_KEY', variable: 'AWS_COMPREHEND_KEY'),
            string(credentialsId: 'TESTING_AWS_KEY', variable: 'AWS_TRANSLATE_KEY'),
            string(credentialsId: 'TESTING_AWS_SECRET', variable: 'AWS_S3_SECRET'),
            string(credentialsId: 'TESTING_AWS_SECRET', variable: 'AWS_ET_SECRET'),
            string(credentialsId: 'TESTING_AWS_SECRET', variable: 'AWS_COMPREHEND_SECRET'),
            string(credentialsId: 'TESTING_AWS_SECRET', variable: 'AWS_TRANSLATE_SECRET'),
        ]
    ) {
        dir('server') {
            sh script: 'composer install',
                returnStatus: true
        }
    }
}

return this