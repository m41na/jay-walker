pipeline {
    agent any

    tools {
        gradle "gradle-8.4"
        maven "maven-3.9.5"
    }

    stages {
        stage('Hello') {
            steps {
                echo 'Hello World'
                sh "java -version"
                sh "gradle -version"
                sh "mvn -version"
            }
        }
    }
}