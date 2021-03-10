buildMvn {
  publishModDescriptor = 'yes'
  mvnDeploy = 'yes'
  doKubeDeploy = true
  buildNode = 'jenkins-agent-java11'

  doApiLint = true
  apiTypes = 'RAML OAS'
  apiDirectories = 'ramls src/main/resources/swagger.api'

  doDocker = {
    buildDocker {
      publishMaster = 'yes'
      healthChk = 'yes'
      healthChkCmd = 'curl --fail http://localhost:8081/admin/health/ || exit 1'
    }
  }
}

