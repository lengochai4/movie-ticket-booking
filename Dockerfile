# =====================================================================
# Stage 1: Build file WAR bằng Maven & JDK 21
# =====================================================================
FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /app

# Tải trước dependencies để tận dụng Docker layer cache
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code và đóng gói file WAR
COPY src ./src
RUN mvn clean package -DskipTests -B

# =====================================================================
# Stage 2: Chạy ứng dụng trên Apache Tomcat 10.1 (Jakarta EE 10 / JDK 21)
# =====================================================================
FROM tomcat:10.1-jdk21-temurin

# Thiết lập múi giờ Việt Nam (UTC+7) và UTF-8 cho lịch chiếu & thống kê doanh thu
ENV TZ=Asia/Ho_Chi_Minh
ENV CATALINA_OPTS="-Duser.timezone=Asia/Ho_Chi_Minh -Dfile.encoding=UTF-8"

# Xóa các ứng dụng mặc định của Tomcat
RUN rm -rf /usr/local/tomcat/webapps/*

# Copy PostgreSQL JDBC Driver vào thư mục lib của Tomcat để JNDI DataSource (META-INF/context.xml) nạp được driver
COPY --from=build /app/target/movie-booking-1.0-SNAPSHOT/WEB-INF/lib/postgresql-*.jar /usr/local/tomcat/lib/

# Copy file WAR vào ROOT.war để chạy trực tiếp tại đường dẫn gốc "/"
COPY --from=build /app/target/*.war /usr/local/tomcat/webapps/ROOT.war

EXPOSE 8080

# Render cấp cổng động qua biến môi trường $PORT, tự động cập nhật server.xml trước khi chạy Tomcat
CMD ["sh", "-c", "sed -i \"s/port=\\\"8080\\\"/port=\\\"${PORT:-8080}\\\"/g\" /usr/local/tomcat/conf/server.xml && catalina.sh run"]
