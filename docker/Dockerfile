FROM ubuntu:24.04

# Avoid prompts during installation
ENV DEBIAN_FRONTEND=noninteractive

# Install dependencies: JDK 21, Nginx, OpenSSH Server, Git, MariaDB Client (for mysqladmin check)
RUN apt-get update && apt-get install -y \
    openjdk-21-jdk \
    nginx \
    openssh-server \
    git \
    curl \
    mariadb-client \
    php-cli \
    && rm -rf /var/lib/apt/lists/*

# Configure SSH
RUN mkdir -p /var/run/sshd
RUN echo 'root:Hello@123' | chpasswd
RUN sed -i 's/#PermitRootLogin prohibit-password/PermitRootLogin yes/' /etc/ssh/sshd_config
RUN sed -i 's/PermitRootLogin prohibit-password/PermitRootLogin yes/' /etc/ssh/sshd_config
RUN sed -i 's@session\s*required\s*pam_loginuid.so@session optional pam_loginuid.so@g' /etc/pam.d/sshd

# Setup Nginx configuration
RUN rm /etc/nginx/sites-enabled/default
COPY nginx.conf /etc/nginx/sites-enabled/default

# Clone project and set directory
WORKDIR /app
RUN git clone https://github.com/Sothiphak/DevOps_Final.git /app

# Copy entrypoint script
COPY entrypoint.sh /entrypoint.sh
RUN chmod +x /entrypoint.sh

# Expose ports: 8080 (for Nginx proxy) and 22 (for SSH)
EXPOSE 8080 22

ENTRYPOINT ["/entrypoint.sh"]
