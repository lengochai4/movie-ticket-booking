# ==============================================================================
# Stage 1: Build the WAR file using Maven and Eclipse Temurin JDK 21
# ==============================================================================
FROM maven:3.9.6-eclipse-temurin-21 AS builder

WORKDIR /app

# Cache dependencies layer
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code and package the WAR file
COPY src ./src
RUN mvn clean package -DskipTests

# ==============================================================================
# Stage 2: Run application with Apache Tomcat 10.1 (Jakarta EE 10 / Servlet 6.0)
# ==============================================================================
FROM tomcat:10.1-jdk21-temurin

WORKDIR /usr/local/tomcat

# Remove default sample webapps to keep image clean and secure
RUN rm -rf /usr/local/tomcat/webapps/*

# Copy WAR file from builder stage to Tomcat webapps as ROOT.war
# Deploying as ROOT.war allows accessing directly via http://localhost:8080/
COPY --from=builder /app/target/*.war /usr/local/tomcat/webapps/ROOT.war

# Set environment variables (UTF-8 encoding and Vietnam timezone)
ENV JAVA_OPTS="-Dfile.encoding=UTF-8 -Duser.timezone=Asia/Ho_Chi_Minh -Xms512m -Xmx1024m"

# Expose Tomcat default port
EXPOSE 8080

# Start Tomcat server
CMD ["catalina.sh", "run"]
