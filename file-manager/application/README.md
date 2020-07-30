# file-manager-app

## building
```
mvn clean package
```

## running
```
mvn spring-boot:run

# or to run on an alternative port:
mvn spring-boot:run -Dspring-boot.run.arguments=--server.port=8085

# or:
PORT=8888 mvn spring-boot:run
```
If you have successfully started the application using e.g. `mvn spring-boot:run`, you can open  
http://localhost:8080/  
in your browser, to use the application with access to your local system.

## deployment 

### heroku
```
heroku deploy:jar target/*.war --app web-modules-file-manager
heroku ps:scale web=1 -a web-modules-file-manager
```


