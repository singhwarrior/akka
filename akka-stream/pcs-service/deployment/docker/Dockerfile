FROM centos:7
RUN mkdir software;cd software;mkdir java;mkdir pcs
ADD jdk* /software/java/
ADD pcs-service-0.0.1-SNAPSHOT.jar /software/pcs/ 
RUN rpm -ivh /software/java/jdk-7u80-linux-x64.rpm
ENV JAVA_HOME /usr/java/jdk1.7.0_80/
ENV PATH $PATH:$JAVA_HOME/bin
WORKDIR /software/pcs/
ENTRYPOINT ["java","-jar","pcs-service-0.0.1-SNAPSHOT.jar"]
CMD ["grunt"]
