set classpath=.\lib\commons-io-1.3.1.jar
set classpath=%classpath%;.\lib\commons-lang-2.3.jar
set classpath=%classpath%;.\lib\commons-logging-1.0.4.jar
set classpath=%classpath%;.\lib\gdata-base-1.0.jar
set classpath=%classpath%;.\lib\gdata-client-1.0.jar
set classpath=%classpath%;.\lib\gdata-core-1.0.jar
set classpath=%classpath%;.\lib\gdata-blogger-2.0.jar
set classpath=%classpath%;.\lib\jsr305.jar
set classpath=%classpath%;.\lib\google-collect-1.0-rc1.jar
set classpath=%classpath%;.\lib\gdata-media-1.0.jar
set classpath=%classpath%;.\lib\iText-2.1.4.jar
set classpath=%classpath%;.\lib\log4j-1.2.8.jar
set classpath=%classpath%;.\lib\org.eclipse.swt.win32.win32.x86_3.3.0.v3346.jar
set classpath=%classpath%;.\lib\org.eclipse.swt_3.3.0.v3346.jar
set classpath=%classpath%;.\lib\swt-datepicker.jar
set classpath=%classpath%;.\bin

java -cp %classpath% org.thirdstreet.blogger.ui.BloggerUtilsMain
