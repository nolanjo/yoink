// data gathering files for testing
import groovy.json.JsonOutput

def gatherData(critical, integration=false, regression=false, server=false){
    dir('infrastructure/jenkins/pythonScript'){
        sh(script: "python3 ./metricGathering.py ${critical? '-c t': ''} ${integration? '-d t': ''} ${regression? '-r t': ''} ${server? '-b t': ''}", returnStatus: true)
    }
}

def sendData(json){
    dir('infrastructure/jenkins/pythonScript'){
        // echo "${json}"
        sh(script: "python3 ./metricSending.py -j '${json}'", returnStatus: true)
    }
}

def installScripts(){
    dir('infrastructure/jenkins/pythonScript'){
        sh(script: "pip3 install -r requirements.txt", returnStatus: true)
    }
}

def sendMetrics(critical, integration=false, regression=false, server=false){
    def lovelyMetrics = readJSON text: '{}'
    lovelyMetrics.name = env.JOB_NAME.split("/")[0]
    lovelyMetrics.baseName = env.JOB_BASE_NAME
    lovelyMetrics.runTime = currentBuild.durationString.replace(' and counting', '')
    lovelyMetrics.buildNumber = env.BUILD_NUMBER
    lovelyMetrics.resource = env.TEST_ENV
    gatherData(critical, integration, regression, server)
    try {
        dir('tests'){
            lovelyMetrics.results = readJSON(text: readFile("${jsonLocation()}").trim())
        }
        // def test = JsonOutput.toJson(lovelyMetrics)
        sendData(JsonOutput.toJson(lovelyMetrics))
    } catch(Exception e) {
        echo "---- Failed To Send Data -----"
        echo "Exception: ${e}"
    }
}

def jsonLocation(){
    return "parsed.json"
}

return this