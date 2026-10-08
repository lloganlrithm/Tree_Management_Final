# ---------- ขั้นที่ 1: build ไฟล์ jar ----------
# ใช้ image ที่มี Maven ในตัว ไม่ใช้ mvnw เพราะไฟล์ mvnw ในเครื่อง Windows มีขึ้นบรรทัดแบบ CRLF รันบน Linux ไม่ได้
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# copy pom ก่อนแล้วโหลด dependency: ถ้าแก้แค่โค้ด Docker ใช้ cache ชั้นนี้ ไม่ต้องโหลดใหม่ทุกครั้ง
COPY pom.xml .
RUN mvn -q -B dependency:go-offline

COPY src ./src
# ข้าม test ตอน build image (test มี @SpringBootTest ที่ต้องต่อ DB จริง) test รันแยกใน CI
RUN mvn -q -B package -DskipTests

# ---------- ขั้นที่ 2: image สำหรับรันจริง (มีแค่ JRE + jar เล็กกว่ามาก) ----------
FROM eclipse-temurin:21-jre
WORKDIR /app

# ไม่รันแอปด้วย root
RUN groupadd --system app && useradd --system --gid app app
COPY --from=build /app/target/*.jar app.jar
USER app

# Render / Railway ส่งพอร์ตมาทาง PORT ถ้าไม่มีใช้ 8080
# MaxRAMPercentage: ให้ JVM ใช้ RAM ตามที่เครื่องให้มา (free tier มี RAM น้อย)
ENV PORT=8080 \
    JAVA_OPTS="-XX:MaxRAMPercentage=75"
EXPOSE 8080

# ค่า DB / Cloudinary ส่งเข้ามาเป็น environment variable ตอนรัน ไม่ฝังใน image
#   SPRING_DATASOURCE_URL, SPRING_DATASOURCE_USERNAME, SPRING_DATASOURCE_PASSWORD, CLOUDINARY_URL
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -Dserver.port=$PORT -jar app.jar"]
