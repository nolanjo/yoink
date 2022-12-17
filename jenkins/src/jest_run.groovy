// code for running jest testing jobs

def runSpark(){
    dir ('spark') {
        sh script: 'yarn jest --ci --reporters=jest-junit --reporters=default',
            returnStatus: true
    }
}

return this