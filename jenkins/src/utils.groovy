// utitlity files for testing
import org.jenkins.plugins.lockableresources.LockableResourcesManager as LRM

def findFreeEnvSpark(){
    // findFreeEnv(["pre-production3", "pre-production4", "pre-production2"])
    findFreeEnv(["pre-production3", "pre-production4"])
}

def findFreeEnvMobile(){
    findFreeEnv(["pre-production"])
}

def findFreeEnv(resourceNames){
    def sleepyTime = 30
    def myResources = LRM.get().resources
    // need to make sure that the resource exists if it does not already...
    resourceNames.each { val -> if(!myResources.contains(val)){ LRM.get().createResource(val) }}
    myResources = LRM.get().resources
    echo "All available test Envs: ${myResources}"
    def notLocked = null
    while(true) {
        notLocked = myResources.find { r -> !r.isLocked() && !r.isQueued() && r.toString() in resourceNames }
        if(notLocked){
            echo "Found Test Env: ${notLocked.toString()}"
            break
        }
        else{
            echo "There is no resource available, sleeping for ${sleepyTime} seconds"
            sleep sleepyTime.toInteger()
        }
    }
    return notLocked.toString()
}

def boolean isBranchIndexingCauseCheck() {
    def isBranchIndexing = false
    if (!currentBuild.rawBuild) {
        return true
    }

    currentBuild.rawBuild.getCauses().each { cause ->
        if (cause instanceof jenkins.branch.BranchIndexingCause) {
        isBranchIndexing = true
        }
    }
    return isBranchIndexing && env.BUILD_NUMBER != '1'
}

// only pull request can have labels hence uses pull request
def checkLabel(expectedLabel){
    return pullRequest.labels.contains(expectedLabel)
}

def cleanReportsDir(){
    dir('tests'){
        sh(script: 'rm -r -f cypress/reports/*', returnStatus: true)
    }
    dir ('tests/postman'){
        sh(script: 'rm -rf newman', returnStatus: true)
    }
}

def deleteExistingComment(){
    for (comment in pullRequest.comments) {
        /* Where "automation-user" is the scm account. */
        if (comment.user == "workvivo-jenkins[bot]") {
            pullRequest.deleteComment(comment.id)
        }
    }
}

def gitComment(commentText){
    deleteExistingComment()
    def date = sh(returnStdout: true, script: "date -u").trim()
    pullRequest.comment("Build ${env.BUILD_ID} ran at ${date} \n ${commentText}")
}

def echoJenkinsVariables(){
    // debug funciton for comparison
    def list = [
        "BRANCH_NAME": env.BRANCH_NAME, 
        "BRANCH_IS_PRIMARY": env.BRANCH_IS_PRIMARY,
        "CHANGE_ID": env.CHANGE_ID,
        "CHANGE_URL": env.CHANGE_URL,
        "CHANGE_TITLE": env.CHANGE_TITLE,
        "CHANGE_AUTHOR": env.CHANGE_AUTHOR,
        "CHANGE_AUTHOR_DISPLAY_NAME": env.CHANGE_AUTHOR_DISPLAY_NAME,
        "CHANGE_AUTHOR_EMAIL": env.CHANGE_AUTHOR_EMAIL,
        "CHANGE_TARGET": env.CHANGE_TARGET,
        "CHANGE_BRANCH": env.CHANGE_BRANCH,
        "CHANGE_FORK": env.CHANGE_FORK,
        "TAG_NAME": env.TAG_NAME,
        "TAG_TIMESTAMP": env.TAG_TIMESTAMP,
        "TAG_UNIXTIME": env.TAG_UNIXTIME,
        "TAG_DATE": env.TAG_DATE,
        "JOB_DISPLAY_URL": env.JOB_DISPLAY_URL,
        "RUN_DISPLAY_URL": env.RUN_DISPLAY_URL,
        "RUN_ARTIFACTS_DISPLAY_URL": env.RUN_ARTIFACTS_DISPLAY_URL,
        "RUN_CHANGES_DISPLAY_URL":env.RUN_CHANGES_DISPLAY_URL,
        "RUN_TESTS_DISPLAY_URL":env.RUN_TESTS_DISPLAY_URL,
        "CI":env.CI,
        "BUILD_NUMBER": env.BUILD_NUMBER,
        "BUILD_ID":env.BUILD_ID,
        "BUILD_DISPLAY_NAME":env.BUILD_DISPLAY_NAME,
        "JOB_NAME":env.JOB_NAME,
        "JOB_BASE_NAME":env.JOB_BASE_NAME,
        "BUILD_TAG":env.BUILD_TAG,
        "EXECUTOR_NUMBER":env.EXECUTOR_NUMBER,
        "NODE_NAME":env.NODE_NAME,
        "NODE_LABELS":env.NODE_LABELS,
        "WORKSPACE": env.WORKSPACE,
        "WORKSPACE_TMP":env.WORKSPACE_TMP,
        "JENKINS_HOME":env.JENKINS_HOME,
        "JENKINS_URL":env.JENKINS_URL,
        "BUILD_URL":env.BUILD_URL,
        "JOB_URL": env.JOB_URL,
        "GIT_COMMIT": env.GIT_COMMIT,
        "GIT_PREVIOUS_COMMIT":env.GIT_PREVIOUS_COMMIT,
        "GIT_PREVIOUS_SUCCESSFUL_COMMIT":env.GIT_PREVIOUS_SUCCESSFUL_COMMIT,
        "GIT_BRANCH": env.GIT_BRANCH,
        "GIT_LOCAL_BRANCH":env.GIT_LOCAL_BRANCH,
        "GIT_CHECKOUT_DIR":env.GIT_CHECKOUT_DIR,
        "GIT_URL": env.GIT_URL,
        "GIT_COMMITTER_NAME":env.GIT_COMMITTER_NAME,
        "GIT_AUTHOR_NAME":env.GIT_AUTHOR_NAME,
        "GIT_COMMITTER_EMAIL":env.GIT_COMMITTER_EMAIL,
        "GIT_AUTHOR_EMAIL":env.GIT_AUTHOR_EMAIL
    ]

    list.each { item -> 
        echo "${item.key}: ${item.value} \n"
    }

    echo "${currentBuild.durationString}"

    echo "${currentBuild}"
}

return this
