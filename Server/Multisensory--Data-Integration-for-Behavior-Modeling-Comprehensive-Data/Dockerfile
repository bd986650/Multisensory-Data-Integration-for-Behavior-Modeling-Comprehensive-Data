# Указываем базовый образ с Java
FROM openjdk:17-jdk

# Указываем рабочую директорию внутри контейнера
WORKDIR /app

# Копируем файл сборки (jar-файл) в контейнер
COPY target/Multisensory-Data-Integration-for-Behavior-Modeling-Comprehensive-Data-0.0.1-SNAPSHOT.jar /app/app.jar

# Указываем команду для запуска Java-приложения
ENTRYPOINT ["java", "-jar", "app.jar"]

# Пример настройки порта (если требуется)
EXPOSE 8080
