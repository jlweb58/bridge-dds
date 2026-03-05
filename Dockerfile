FROM eclipse-temurin:21-jre-noble
RUN mkdir /opt/app
ENV SPRING_PROFILE=production
ARG VERSION
ENV APP_VERSION=${VERSION}

WORKDIR /opt/app

# Speed up APT: force IPv4, avoid translations, avoid incremental diffs
RUN printf '%s\n' \
  'Acquire::ForceIPv4 "true";' \
  'Acquire::Languages "none";' \
  'Acquire::PDiffs "false";' \
  > /etc/apt/apt.conf.d/99fast-apt
# Install runtime deps commonly needed by C++/OpenMP/native libs
RUN apt-get update \
 && apt-get install -y --no-install-recommends binutils libstdc++6 libgomp1 \
 && rm -rf /var/lib/apt/lists/*

COPY build/libs/bridge-dds-*.jar /opt/app/bridge-dds.jar
COPY src/main/resources/libdds.so /usr/local/lib/
COPY src/main/resources/libboost_thread.so.1.83.0 /usr/local/lib

ENV LD_LIBRARY_PATH=/usr/local/lib:${LD_LIBRARY_PATH}

ENTRYPOINT ["java", "-Dspring.profiles.active=${SPRING_PROFILE}", "-jar", "/opt/app/bridge-dds.jar"]
