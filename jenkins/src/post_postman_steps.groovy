// code for running post steps for postman

def always(linkLast = true){
    publishHTML([
        reportDir: 'tests/postman/newman',
        reportFiles: '*.html',
        reportName: 'Newman Report',
        reportTitles: '',
        allowMissing: true,
        alwaysLinkToLastBuild: linkLast,
        keepAll: linkLast])
}

return this