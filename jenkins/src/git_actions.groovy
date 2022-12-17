// all git related functions 

labelConstants = load 'src/labels.groovy'

def gitLabel(label=labelConstants.READY_TO_MERGE()){
    def labels = URLEncoder.encode(label).replaceAll('\\+', '%20')
    withCredentials([
        string(credentialsId: 'gitKey', variable: 'GITHUBKEY')])
    {
        def json = sh(
            script: "curl -H 'Accept: application/vnd.github+json' -H 'Authorization: Bearer ${GITHUBKEY}'  https://api.github.com/search/issues?q=is:pr%20label:%22${labels}%22%20repo:workvivo/workvivo%20state:open"
        , returnStdout: true).trim()
        return readJSON(text: json)
    }
}

def gitAddLabel(prNum, label=labelConstants.MERGE_CONFLICT()){
    // def labels = URLEncoder.encode(label).replaceAll('\\+', '%20')
    withCredentials([
        string(credentialsId: 'gitKey', variable: 'GITHUBKEY')])
    {
        def json = sh(script: "curl --location --request POST 'https://api.github.com/repos/workvivo/workvivo/issues/${prNum}/labels' \
            -H 'Accept: application/vnd.github.v3+json' \
            -H 'Authorization: Bearer ${GITHUBKEY}' \
            -H 'Content-Type: application/json' \
            --data-raw '{\"labels\": [\"${label}\"]}'" 
            , returnStdout: true).trim()
        echo "${readJSON(text: json)}"
        return readJSON(text: json)
    }
}

// returns full
def gitPrInfo(prNum){
    withCredentials([
        string(credentialsId: 'gitKey', variable: 'GITHUBKEY')])
    {
        def json = sh(
            script: "curl -H 'Accept: application/vnd.github+json' -H 'Authorization: Bearer ${GITHUBKEY}'  https://api.github.com/repos/workvivo/workvivo/pulls/${prNum}"
        , returnStdout: true).trim()
        return readJSON(text: json)
    }
}

def gitFetch(){
    return sh(script: "git fetch --all", returnStatus: true)
}

def gitCheckout(branchName){
    return sh(script: "git checkout origin/${branchName}", returnStatus: true)
}

def gitMergeAbort(){
    return sh(script: "git merge --abort", returnStatus: true)
}

def gitMerge(prName){
    sh(script: "git merge origin/${prName} -q", returnStdout: true).trim()
}

def gitCheckConflicts(prName){
    //git merge dev --no-ff --no-commit
    return sh(script: "git merge origin/${prName} --no-ff --no-commit", returnStatus: true)
}

def parsePrNums(labeledPrs){
    def filteredData = []
    labeledPrs.each{ pr ->
        filteredData.add(pr.number)
    }
    // echo "------------------"
    // echo "${filteredData}"
    return filteredData
}

def parsePrHeadNum(prInfo){
    def filteredData = []
    // need to return number here as well
    prInfo.each{ pr ->
        filteredData.add(["head": pr.head.ref, "number": pr.number])
    }
    // echo "------------------"
    // echo "${filteredData}"
    return filteredData
}

def gitMergeReady(){
    def prInfo = []
    // using ids, make a call to each pr to get the head ref
    findPrNumbers().each { prNum ->
        prInfo.add(gitPrInfo(prNum))
    }
    def prRefs = parsePrHeadNum(prInfo)
    // checkout using head ref 
    gitFetch()
    prRefs.each { prRef -> 
        // gitCheckout(prRef)
        if (gitCheckConflicts(prRef.head) == 0) {
            gitMergeAbort()
            gitMerge(prRef.head)
        } else {
            echo "----------CONFLICT FOUND----------"
            gitMergeAbort()
            gitAddLabel(prRef.number)
        }
    }
}

def findPrNumbers(){
    // parses pr nums from return 
    return parsePrNums(gitLabel().items)
}

def prPrintData(){
    def returnMessage = "\n"
    def prInfo = []

    findPrNumbers().each { prNum ->
        prInfo.add(gitPrInfo(prNum))
    }

    def filteredData = []
    prInfo.each{ pr ->
        filteredData.add(["title": pr.title, "number": pr.number, "author": pr.user.login, "link": pr._links.html.href])
    }

    filteredData.each { pr -> 
        returnMessage += "* Num: <${pr.link}|*${pr.number}*> Title : ${pr.title} Author: ${pr.author} * \n"
    }

    return returnMessage
}

return this