#!/bin/bash
echo "====================================="
echo "  League of Bilkent - Setup Script"
echo "====================================="

# Check Java
if ! command -v javac &> /dev/null; then
    echo "[!] Java not found. Please install JDK 17+."
    exit 1
fi
echo "[OK] Java found: $(javac -version 2>&1)"

# Check MySQL
if ! command -v mysql &> /dev/null; then
    echo "[!] MySQL not found. Installing..."
    if [[ "$OSTYPE" == "darwin"* ]]; then
        brew install mysql && brew services start mysql
    else
        sudo apt-get install -y mysql-server && sudo systemctl start mysql
    fi
fi
echo "[OK] MySQL found"

# Get DB password
read -sp "Enter MySQL root password (press Enter if none): " DB_PASS
echo ""

# Create database
echo "Creating database..."
if [ -z "$DB_PASS" ]; then
    mysql -u root -e "CREATE DATABASE IF NOT EXISTS league_of_bilkent;"
else
    mysql -u root -p"$DB_PASS" -e "CREATE DATABASE IF NOT EXISTS league_of_bilkent;"
fi

if [ $? -ne 0 ]; then
    echo "[!] Database creation failed!"
    exit 1
fi
echo "[OK] Database ready"

# Update password in AppConstants
if [ ! -z "$DB_PASS" ]; then
    sed -i.bak "s|public static final String DB_PASS  = \"\"|public static final String DB_PASS  = \"$DB_PASS\"|" src/AppConstants.java
    echo "[OK] Password configured"
fi

# Download dependencies
echo "Downloading dependencies..."
if [ ! -f mysql-connector-j-8.3.0.jar ]; then
    curl -L -o mysql-connector-j-8.3.0.jar \
        "https://repo1.maven.org/maven2/com/mysql/mysql-connector-j/8.3.0/mysql-connector-j-8.3.0.jar"
fi
if [ ! -f javax.mail.jar ]; then
    curl -L -o javax.mail.jar \
        "https://repo1.maven.org/maven2/com/sun/mail/javax.mail/1.6.2/javax.mail-1.6.2.jar"
fi
echo "[OK] Dependencies ready"

# Compile
echo "Compiling..."
javac -cp ".:mysql-connector-j-8.3.0.jar:javax.mail.jar" src/*.java
if [ $? -ne 0 ]; then
    echo "[!] Compilation failed!"
    exit 1
fi
echo "[OK] Compiled successfully"

echo ""
echo "====================================="
echo "  Run with:"
echo "  java -cp \".:src:mysql-connector-j-8.3.0.jar:javax.mail.jar\" MainFile"
echo "====================================="
echo ""
echo "Demo login: damla / 1234"
