pipeline {
    agent any

    triggers {
        // Periodically check for updates from Git (Poll SCM every 5 minutes)
        pollSCM('*/5 * * * *')
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build & Test') {
            steps {
                // Build using Maven wrapper and automatically run tests
                sh './mvnw clean package'
            }
        }

        stage('Deploy') {
            steps {
                // Deploy using the Ansible Playbook on success
                sh 'ansible-playbook -i hosts.yaml playbook.yml'
            }
        }
    }

    post {
        failure {
            // Send email to srengty@gmail.com and the developer who broke the build
            emailext (
                to: 'srengty@gmail.com',
                subject: "Build Failed: ${currentBuild.fullDisplayName}",
                body: "Build failed. Check Jenkins console logs here: ${env.BUILD_URL}",
                recipientProviders: [
                    [$class: 'CulpritsRecipientProvider'],
                    [$class: 'DevelopersRecipientProvider']
                ]
            )
        }
    }
}
