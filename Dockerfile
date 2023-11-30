#Base image containing Java runtime
FROM eclipse-temurin:21 as builder

#Add the application's jar to the container
COPY target/cloud-0.0.1-SNAPSHOT.jar spring_boot_app.jar

#Extract the layers
RUN java -Djarmode=layertools -jar spring_boot_app.jar extract

#Temurin 21 image as the base image
FROM eclipse-temurin:21

#Copy the extracted layers
COPY --from=builder dependencies/ ./
COPY --from=builder spring-boot-loader/ ./
COPY --from=builder snapshot-dependencies/ ./
COPY --from=builder application/ ./

# Expose port 8080
EXPOSE 8080

#Run the application
ENTRYPOINT ["java", "org.springframework.boot.loader.JarLauncher"]