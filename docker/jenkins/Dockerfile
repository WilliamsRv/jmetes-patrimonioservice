FROM jenkins/jenkins:lts-jdk17

USER root

RUN apt-get update && apt-get install -y --no-install-recommends \
    wget curl unzip ca-certificates git maven \
    && rm -rf /var/lib/apt/lists/*

RUN ln -s /usr/share/maven/bin/mvn /usr/local/bin/mvn

ARG JMETER_VERSION=5.6.3
RUN curl -fsSL https://archive.apache.org/dist/jmeter/binaries/apache-jmeter-${JMETER_VERSION}.tgz \
    -o /tmp/jmeter.tgz \
    && tar -xzf /tmp/jmeter.tgz -C /opt/ \
    && ln -s /opt/apache-jmeter-${JMETER_VERSION}/bin/jmeter /usr/local/bin/jmeter \
    && rm /tmp/jmeter.tgz

ARG DOCKER_VERSION=24.0.7
RUN curl -fsSL https://download.docker.com/linux/static/stable/x86_64/docker-${DOCKER_VERSION}.tgz \
    -o /tmp/docker.tgz \
    && tar -xzf /tmp/docker.tgz -C /tmp/ \
    && mv /tmp/docker/docker /usr/local/bin/docker \
    && rm -rf /tmp/docker.tgz /tmp/docker

RUN echo 'jenkins ALL=(ALL) NOPASSWD: ALL' >> /etc/sudoers

COPY plugins.txt /usr/share/jenkins/ref/plugins.txt
RUN jenkins-plugin-cli --plugin-file /usr/share/jenkins/ref/plugins.txt

USER jenkins
