
java -Dlog4j.configurationFile=../conf/log4j.xml -DserverPort=10002 -Dcom.sun.management.jmxremote=true -Dcom.sun.management.jmxremote.port=10003 -Dcom.sun.management.jmxremote.authenticate=false -Dcom.sun.management.jmxremote.ssl=false -Djava.rmi.server.hostname=drumspi.home  -XX:+UnlockCommercialFeatures -XX:+FlightRecorder  -jar ../build/I2CDrumKit_0_1.jar 
