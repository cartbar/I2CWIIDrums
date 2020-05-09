
java -Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=drumspi.home:1044 -Dlog4j.configurationFile=../conf/log4j.xml -DserverPort=10002 -jar ../build/I2CDrumKit_0_1.jar
