pipeline {
   agent any


   environment {
      DOCKER_IMAGE_NAME = "somoa_back"
   }


   stages {
     // application.yml 복사
     stage('application.yml copy') {
         when { expression { env.GIT_BRANCH == 'origin/master' || env.GIT_BRANCH == 'origin/develop'} }
         steps {
            updateGitlabCommitStatus name: 'build', state: 'pending'
            echo "현재 브랜치 : ${env.GIT_BRANCH}"
            dir ('.') {
               sh """
               pwd
               if [ ! -d ./src/main/resources ]
               then
                  mkdir ./src/main/resources
                  cp ../application-somoa.yml ./src/main/resources/application.yml
               else
                  if [ -f ./src/main/resources/application.properties ]
                  then
                     rm -rf ./src/main/resources/application.properties
                  fi
                  if [ -f ./src/main/resources/application.yml ]
                  then
                     rm -rf ./src/main/resources/application.yml
                  fi
                  cp ../application-somoa.yml ./src/main/resources/application.yml
               fi
               """
            }
         }
         post {
            success {
               echo 'application.yml copy success :D'
            }
            failure {
               updateGitlabCommitStatus name: 'build', state: 'failed'
               echo 'application.yml copy failed... :('
            }
         }
      }


      // Gradle build, docker image build
      stage('Gradle Build & Docker image Build') {
         when { expression { env.GIT_BRANCH == 'origin/master' || env.GIT_BRANCH == 'origin/develop'} }
         steps {
            dir ('.'){
            sh """
            image=\$(docker images --filter=reference=${env.DOCKER_IMAGE_NAME} -q)
            if [ -n "\$image" ]; then
               docker image tag ${env.DOCKER_IMAGE_NAME}:latest ${env.DOCKER_IMAGE_NAME}:old
               docker rmi ${env.DOCKER_IMAGE_NAME}:latest
            fi
            docker build --tag ${env.DOCKER_IMAGE_NAME} .
            """
            }
         }
         post {
            success {
               echo 'Docker image build success :D'
            }
            failure {
               sh """
               image=\$(docker images --filter=reference=${env.DOCKER_IMAGE_NAME}:old -q)
               if [ -n "\$image" ]; then
                  docker image tag ${env.DOCKER_IMAGE_NAME}:old ${env.DOCKER_IMAGE_NAME}:latest
                  docker rmi ${env.DOCKER_IMAGE_NAME}:old
               fi
               """
               updateGitlabCommitStatus name: 'build', state: 'failed'
               error 'Docker image build faild... :('
            }
         }
      }


      // docker test run
      stage('Docker Test Run') {
         when { expression { env.GIT_BRANCH == 'origin/master' || env.GIT_BRANCH == 'origin/develop'} }
         steps {
            sh """
            docker run -d -p 8083:8080 --rm --name ${env.DOCKER_IMAGE_NAME}-test ${env.DOCKER_IMAGE_NAME}:latest
            sleep 15
            curl -s -m 3 GET http://j10s001.p.ssafy.io:8083/api/test
            """
         }
         post {
            always {
               sh """
               container=\$(docker ps -q --filter name=${env.DOCKER_IMAGE_NAME}-test)
               if [ -n "\$container" ]; then
                  docker stop \$container
               fi
               """
            }
            success {
               echo 'Docker Test run success :D'
            }
            failure {
               sh """
               docker rmi ${env.DOCKER_IMAGE_NAME}:latest
               res=\$(docker images --filter=reference=${env.DOCKER_IMAGE_NAME}:old -q)
               if [ -n "\$res" ]; then
                  docker image tag ${env.DOCKER_IMAGE_NAME}:old ${env.DOCKER_IMAGE_NAME}:latest
                  docker rmi ${env.DOCKER_IMAGE_NAME}:old
               fi
               """
               updateGitlabCommitStatus name: 'build', state: 'failed'
               error 'Docker Test run failed :('
            }
         }
      }


      // docker run
      stage('Docker Run') {
         when { expression { env.GIT_BRANCH == 'origin/master' || env.GIT_BRANCH == 'origin/develop'} }
         steps {
            sh """
            container=\$(docker ps -q --filter name="${env.DOCKER_IMAGE_NAME}")
            if [ -n "\$container" ]; then
               docker stop \$container
            fi
            image=\$(docker images --filter=reference=${env.DOCKER_IMAGE_NAME}:old -q)
            if [ -n "\$image" ]; then
               docker rmi ${env.DOCKER_IMAGE_NAME}:old
            fi
            docker run -d -p 8080:8080 --rm --name "${env.DOCKER_IMAGE_NAME}" ${env.DOCKER_IMAGE_NAME}:latest
            """
         }
         post {
            success {
               updateGitlabCommitStatus name: 'build', state: 'success'
               echo 'Docker run success :D'
            }
            failure {
               updateGitlabCommitStatus name: 'build', state: 'failed'
               error 'Docker run failed :('
            }
         }
      }


      // Mattermost에 알림 보내기
      stage('Mattermost') {
         when { expression { env.GIT_BRANCH == 'origin/master' || env.GIT_BRANCH == 'origin/develop'} }
         steps {
            mattermostSend(
              channel: "S001-Test",
              message: "Somoa Backend의 ${env.BUILD_NUMBER}번째 빌드가 성공했습니다."
           )
         }
         post {
            success {
               echo 'Mattermost notify success :D'
            }
            failure {
               error 'Mattermost notify failed... :('
            }
         }
      }

   }
}
