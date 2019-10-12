# jmc 远程监控查看jvm

* 如果是可运行的jar包或者是单个可运行的class文件，可以在命令行执行类似命令： 
  java -Dcom.sun.management.jmxremote.port=1090 -Dcom.sun.management.jmxremote.ssl=false -Dcom.sun.management.jmxremote.authenticate=false

* 如果是tomcat服务器，执行的是%CATALINA_HOME%/bin/startup.sh，打开catalina.sh文件，加入以下语句： 
  JAVA_OPTS="$JAVA_OPTS -Djava.rmi.server.hostname=192.168.0.9 -Dcom.sun.management.jmxremote.port=1090 -Dcom.sun.management.jmxremote.ssl=false -Dcom.sun.management.jmxremote.authenticate=false" 

* 其他web服务器的情况类似，就是把上述jmxremote参数加入到相应的启动文件中即可。 

*   被监控端的hostname（主机名）要设置成自己的ip地址。 

  Dcom.sun.management.jmxremote.port=1090  jmx连接端口 
  Dcom.sun.management.jmxremote.ssl=false  不需要ssl连接 
  Dcom.sun.management.jmxremote.authenticate=false   不需要权限验证   

* 以上3个参数，总结一句话就是jmx通过ip地址和端口就可以直接进行连接。 
  注意：对于局域网或者互联网中有固定ip地址的服务器，可以设置成功。对于需要端口映射或转接的服务器，基本上是无法连接的。 





## 二、 使用JProfiler 进行内存信息查看