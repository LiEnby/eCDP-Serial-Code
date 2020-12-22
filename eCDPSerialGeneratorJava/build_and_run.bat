@echo off
set ECDP_VERSION=1.0
set MAVEN_HOME=D:\apache-maven-3.6.3
set JAVA_HOME=D:\Java\adopt-openjdk-11.0.9.1
set PATH=%MAVEN_HOME%\bin;%JAVA_HOME%\bin;%SystemRoot%\system32

echo MAVEN_HOME set to %MAVEN_HOME%
echo JAVA_HOME set to %JAVA_HOME%

IF EXIST %MAVEN_HOME% (	
	IF EXIST %JAVA_HOME% (
		mvn clean package
		if not errorlevel 1 (
			java -jar .\target\eCDPPasswordGeneratorApp_%ECDP_VERSION%.jar
		)
	) ELSE (
		echo ERROR - %JAVA_HOME% does not exist!
		pause
	)
) ELSE (
	echo ERROR - %MAVEN_HOME% does not exist!
	pause
)
