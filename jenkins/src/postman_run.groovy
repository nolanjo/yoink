// code for running postman testing

def postman(){
    genericPostman("https://api.postman.com/collections/19163082-8ef5731f-4d02-46e8-941f-86c5d48d2903?access_key=PMAT-01GK1WG5JP0ZD4EBS2J0BTK710" , "newman/postman-critical.xml")
}

def postmanRolesAndPermissions(){
    genericPostman("https://api.postman.com/collections/19163082-821591af-4ad1-45d8-ab4d-bbb1d599fc88?access_key=PMAT-01GK1M4WPMC9WFVTKTR0TTWVQT" , "newman/postman-roles.xml")
}

def postmanNotifications(){
    genericPostman("https://api.postman.com/collections/19163082-2bdd6c88-0253-48a1-9a96-40bc8cc81665?access_key=PMAT-01GK1KKXG7C2DJ4RK8A39TQYCC" , "newman/postman-notifications.xml")
}

def genericPostman(collection, output){
    withCredentials([
        string(credentialsId: 'PostmanAPIKey', variable: 'PostmanAPIKey'),
        string(credentialsId: "${env.TEST_ENV}-PostmanAPIKey", variable: 'envAPI')
        ]) {
        dir ('tests/postman') {
            sh(script: """
            npx newman run ${collection} -e https://api.getpostman.com/environments/${envAPI}?apikey=${PostmanAPIKey} -k --reporters cli,junit,htmlextra --reporter-htmlextra-showOnlyFails --reporter-junit-export "${output}"
            """, returnStatus: true)
        }
    }
}

return this