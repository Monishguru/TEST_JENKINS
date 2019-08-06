
def pipeLineJob = job('tik-Component-Segment') {
  wrappers {
    credentialsBinding {
      usernamePassword('ARTIFACTORY_USERNAME', 'ARTIFACTORY_PASSWORD', 'service-account')
    }
  }
   scm {
     git {
       remote {
         url('')
         credentials('')
       }
       branch("master")
     }
   }
   steps {
     gradle {
       useWrapper(true)
       makeExecutable(true)
       description('Run the Test and Clean ')
       tasks(':tik:clean --refresh-dependencies test')
       fromRootBuildScriptDir(true)
     }
     gradle {
       useWrapper(true)
       makeExecutable(true)
       description('Run Sonar')
       tasks('sonar --info')
       fromRootBuildScriptDir(true)
     }
     gradle {
       useWrapper(true)
       makeExecutable(true)
       description('Upload Artifact to Artifactory')
       switches('-Pversion=FC-${GIT_COMMIT} -PgitSha=${GIT_COMMIT}')
       tasks('uploadArchivesJenkins')
       fromRootBuildScriptDir(true)
     }
   }
   triggers {
     githubPush()
   }
   publishers {
     git {
       pushOnlyIfSuccess()
     }
     downstreamParameterized {
       trigger('SO-Segment') {
         condition('SUCCESS')
         triggerWithNoParameters(true)
         parameters {
           gitRevision()
         }
       }
     }
   }
   logRotator(-1, 10, -1, -1)
   label('gradle')
 }
