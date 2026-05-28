pipeline {
    agent any

    environment {
        SONAR_HOST_URL = 'http://5.189.146.142:9000'
        SONAR_TOKEN    = credentials('sonarqube-token')
    }

    stages {
        stage('Build') {
            steps {
                sh 'mvn compile -DskipTests'
            }
        }

        stage('Test') {
            steps {
                sh '''
                    mvn test -Dtest="AssetSimpleTest" 2>&1 | tee test-output.log
                    grep -oE "Tests run: [0-9]+, Failures: [0-9]+, Errors: [0-9]+" test-output.log | tail -1 > test-summary.txt || echo "Pruebas: no ejecutadas" > test-summary.txt
                '''
            }
            post {
                always {
                    junit allowEmptyResults: true, testResults: 'target/surefire-reports/TEST-*.xml'
                    jacoco execPattern: 'target/jacoco.exec',
                           classPattern: 'target/classes',
                           sourcePattern: 'src/main/java'
                }
            }
        }

        stage('Package') {
            steps {
                sh 'mvn package -DskipTests -Djacoco.skip=true'
            }
        }

        stage('Code Quality') {
            steps {
                sh '''
                    mvn test jacoco:report sonar:sonar \
                        -Dsonar.qualitygate.wait=true \
                        -Dsonar.qualitygate.timeout=300 \
                        -Dmaven.test.failure.ignore=true \
                        -Dsonar.projectKey=vg-ms-patrimonioservice \
                        -Dsonar.host.url=${SONAR_HOST_URL} \
                        -Dsonar.login=${SONAR_TOKEN} \
                        -Dsonar.coverage.jacoco.xmlReportPaths=target/site/jacoco/jacoco.xml 2>&1 | tee sonar-output.log
                    grep -E "Quality gate" sonar-output.log | tail -1 > sonar-summary.txt || echo "Calidad: no disponible" > sonar-summary.txt
                '''
            }
        }

        stage('Load Test') {
            steps {
                sh '''
                    ./load-test.sh 2>&1 | tee load-output.log
                    grep -E "summary = " load-output.log | tail -1 > load-summary.txt || echo "Carga: no completada" > load-summary.txt
                '''
            }
            post {
                always {
                    archiveArtifacts artifacts: 'jmeter/results/resultados.jtl', allowEmptyArchive: true
                    archiveArtifacts artifacts: 'jmeter/results/report/**', allowEmptyArchive: true
                }
            }
        }
    }

    post {
        always {
            script {
                def result = currentBuild.result ?: 'SUCCESS'
                def color = result == 'SUCCESS' ? '#36A64F' : (result == 'UNSTABLE' ? '#FFA500' : '#FF0000')
                def icon  = result == 'SUCCESS' ? ':white_check_mark:' : (result == 'UNSTABLE' ? ':warning:' : ':red_circle:')
                def status = result == 'SUCCESS' ? 'exitoso' : (result == 'UNSTABLE' ? 'inestable' : 'FALLO')
                def ts = fileExists('test-summary.txt') ? readFile('test-summary.txt').trim() : '⏳ Saltado'
                def ss = fileExists('sonar-summary.txt') ? readFile('sonar-summary.txt').trim() : '⏳ Saltado'
                def ls = fileExists('load-summary.txt') ? readFile('load-summary.txt').trim() : '⏳ Saltado'
                slackSend channel: '#vg-ms-patrimonioservice',
                    color: color,
                    tokenCredentialId: 'slack-token',
                    message: "${icon} Pipeline ${status} - ${env.JOB_NAME} #${env.BUILD_NUMBER}\\n📋 Pruebas: ${ts}\\n📊 Calidad: ${ss}\\n⚡ Carga: ${ls}\\n🔗 ${env.BUILD_URL}"
            }
            sh 'kill $(cat service.pid 2>/dev/null) 2>/dev/null || true'
            cleanWs cleanWhenAborted: true, cleanWhenFailure: true,
                    cleanWhenNotBuilt: true, cleanWhenSuccess: true,
                    cleanWhenUnstable: true
        }
    }
}
