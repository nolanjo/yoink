// code for running cypress testing jobs

def accessibilitySpark(){
    genericCypress('npx cypress run --config-file=./cypress/config/qa-john-accessibility.js --browser chrome')
}

def cypressSpark() {
    genericCypress('npx cypress-parallel -s cypress:parallel -c ./cypress/config/pre-production.js -t 5 -d "cypress/integration/**/*.js" -x "cypress/reports/integration"')
}

def cypressSparkCriticalPath() {
    genericCypress('npx cypress-parallel -s cypress:parallel -c ./cypress/config/pre-production-critical.js -t 5 -d "cypress/critical/**/*.js" -x "cypress/reports/critical"')
}

def cypressSparkRegression() {
    genericCypress('npx cypress-parallel -s cypress:parallel -c ./cypress/config/pre-production-regression.js -t 5 -d "cypress/regression/**/*.js" -x "cypress/reports/regression"')
}

def genericCypress(executionString){
    withCredentials([
        string(credentialsId: "${env.TEST_ENV}", variable: 'CYPRESS_testUrl'), 
        string(credentialsId: 'adminEmail', variable: 'CYPRESS_adminEmail'), 
        string(credentialsId: 'adminPassword', variable: 'CYPRESS_adminPassword'), 
        string(credentialsId: 'general_employee_access_userEmail', variable: 'CYPRESS_general_employee_access_userEmail'), 
        string(credentialsId: 'general_employee_access_userPassword', variable: 'CYPRESS_general_employee_access_userPassword'), 
        string(credentialsId: 'general_employee_access_user_2Email', variable: 'CYPRESS_general_employee_access_user_2Email'), 
        string(credentialsId: 'general_employee_access_user_2Password', variable: 'CYPRESS_general_employee_access_user_2Password'), 
        string(credentialsId: 'general_employee_access_user_3Email', variable: 'CYPRESS_general_employee_access_user_3Email'), 
        string(credentialsId: 'general_employee_access_user_3Password', variable: 'CYPRESS_general_employee_access_user_3Password'),,
        string(credentialsId: 'external_user_Email', variable: 'CYPRESS_external_user_Email'), 
        string(credentialsId: 'external_user_password', variable: 'CYPRESS_external_user_password'),
        string(credentialsId: 'content_writer_email', variable: 'CYPRESS_content_writer_email'), 
        string(credentialsId: 'content_writer_password', variable: 'CYPRESS_content_writer_password'),
        string(credentialsId: 'editor_email', variable: 'CYPRESS_editor_email'), 
        string(credentialsId: 'editor_password', variable: 'CYPRESS_editor_password'),
        string(credentialsId: 'mailHogUrl', variable: 'CYPRESS_mailHogUrl')]) {
        dir ('tests') {
            sh script: executionString,
            returnStatus: true
        }
    }
}

def cypressLogins(){
    withCredentials([
        string(credentialsId: "${env.TEST_ENV}-mixed", variable: 'CYPRESS_testUrl'),
        string(credentialsId: "${env.TEST_ENV}-saml", variable: 'CYPRESS_samlUrl'),
        string(credentialsId: "okta_url", variable: 'CYPRESS_oktaUrl'),
        string(credentialsId: 'sso_admin_email', variable: 'CYPRESS_sso_admin_email'),
        string(credentialsId: 'sso_admin_password', variable: 'CYPRESS_sso_admin_password'),
        string(credentialsId: 'pass_admin_email', variable: 'CYPRESS_pass_admin_email'),
        string(credentialsId: 'sso_admin_password', variable: 'CYPRESS_pass_admin_password'),
        string(credentialsId: 'sso_general_email', variable: 'CYPRESS_sso_general_email'),
        string(credentialsId: 'sso_admin_password', variable: 'CYPRESS_sso_general_password'),
        string(credentialsId: 'pass_general_email', variable: 'CYPRESS_pass_general_email'),
        string(credentialsId: 'sso_admin_password', variable: 'CYPRESS_pass_general_password'),
        string(credentialsId: 'sso_external_email', variable: 'CYPRESS_sso_external_email'),
        string(credentialsId: 'sso_admin_password', variable: 'CYPRESS_sso_external_password'),
        string(credentialsId: 'pass_external_email', variable: 'CYPRESS_pass_external_email'),
        string(credentialsId: 'sso_admin_password', variable: 'CYPRESS_pass_external_password'),
        string(credentialsId: 'sso_external_saml_email', variable: 'CYPRESS_sso_external_saml_email'),
        string(credentialsId: 'sso_admin_password', variable: 'CYPRESS_sso_external_saml_password')]) {
        dir ('tests') {
            sh script: 'npx cypress-parallel -s cypress:parallel -c ./cypress/config/logins-only.js -t 2 -d "cypress/login-tests/**/*.js" -x "cypress/reports/logins"',
            returnStatus: true
        }
    }
}
return this