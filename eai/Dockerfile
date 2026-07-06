# For documentation see https://jboss-container-images.github.io/openjdk/
FROM registry.access.redhat.com/ubi10/openjdk-21-runtime:1.24-8@sha256:b83379c98109cf7fb402aa6547be282e4ad540c556915f23240e704900377b21

# Copy runnable jar to deployments
COPY target/*.jar /deployments/application.jar
