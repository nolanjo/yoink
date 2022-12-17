// code for running karate testing jobs

def karate(){
    withCredentials(
        [
            string(credentialsId: "${env.TEST_ENV}", variable: 'testUrl'),
            string(credentialsId: 'adminEmail', variable: 'adminEmail'),
            string(credentialsId: 'adminPassword', variable: 'adminPassword'), 
            string(credentialsId: 'general_employee_access_userEmail', variable: 'general_employee_access_userEmail'), 
            string(credentialsId: 'general_employee_access_userPassword', variable: 'general_employee_access_userPassword'), 
            string(credentialsId: 'general_employee_access_user_2Email', variable: 'general_employee_access_user_2Email'), 
            string(credentialsId: 'general_employee_access_user_2Password', variable: 'general_employee_access_user_2Password'), 
            string(credentialsId: 'general_employee_access_user_3Email', variable: 'general_employee_access_user_3Email'), 
            string(credentialsId: 'general_employee_access_user_3Password', variable: 'general_employee_access_user_3Password'),
            string(credentialsId: 'external_user_Email', variable: 'external_user_Email'), 
            string(credentialsId: 'external_user_password', variable: 'external_user_password'),
            string(credentialsId: "${env.TEST_ENV}-DBHost", variable: 'DBHost'),
            string(credentialsId: "${env.TEST_ENV}-DBPassword", variable: 'DBPassword'),
            string(credentialsId: 'DBUsername', variable: 'DBUsername'),
            string(credentialsId: 'OrganisationId', variable: 'OrganisationId')
        ]
    ) {
        dir ('tests/api-automation') {
            sh script: 'mvn clean test -Dkarate.options="--tags @Regression" -DadminEmail="$adminEmail" -DadminPassword="$adminPassword" -Dgeneral_employee_access_userEmail="$general_employee_access_userEmail" -Dgeneral_employee_access_userPassword="$general_employee_access_userPassword" -Dgeneral_employee_access_user_2Email="$general_employee_access_user_2Email" -Dgeneral_employee_access_user_2Password="$general_employee_access_user_2Password" -Dgeneral_employee_access_user_3Email="$general_employee_access_user_3Email" -Dgeneral_employee_access_user_3Password="$general_employee_access_user_3Password" -Dexternal_user_Email="$external_user_Email" -Dexternal_user_password="$external_user_password" -DtestUrl="$testUrl" -DDBHost="$DBHost" -DDBPassword="$DBPassword" -DDBUsername="$DBUsername" -DOrganisationId="$OrganisationId"',
                returnStatus: true
        }
    }
}

return this