# Spring Boot Using Java Modules

Java is one of the most mature and persistent development languages that exist. Recently it entered into a 6-month release schedule which enabled to deliver more frequent updates to the language.
One of those changes was the modular system that is available since Java 9.

The Modular system came to address a recurring concern on Java apps: 'how to hide classes from libraries that are not meant to be used outside or just by specific applications?'
We know that we have the visibility modifiers: public, private, protected and default, but those are not enough to provide external visibility. It is common for a class to live inside a package and be used throughout the library, but it may be a class not meant for external use. Therefore, it has public visibility but on the other side, it shouldn't be available for applications depending on that library. This is the situation in which the Modular System can help.


## Introduction 

Starting on Java 9 the JDK went under a major refactoring to modularize its content. Various modules were created to organize the contents, some examples are `java.base`, `java.sql`, `java.xml` and others. To have an idea there are a total of 60 modules in Java 14 JDK.

`java.base` has the fundamental classes like `Object`, `String`, `Integer`, `Double`, etc

`java.sql` has classes related to accessing the JDBC API like `ResultSet` , `Connection` and others

`java.xml` has classes related to XML manipulation like `XMLStreamReader`, `XMLStreamWriter` and others 

The modularization also enabled the possibility of reducing the Java runtime to include, let's say, just the `java.base` if your application depends just on this module. By using the `jlink` tool, that is bundled with the JDK, you can create a micro runtime with just the JDK modules you need. I'll not cover how to use `jlink` as it is not the focus but you can see an example on this [Baeldung article](https://www.baeldung.com/jlink)

To go through this article you should have at least some basic understanding of Spring Boot, Maven, REST web services principles and Docker installed.

## Install a Java 9+ JDK

First, you'll need a Java 9+ JDK to use modules. If you have been using Java 8 then you'll probably have to download a separate JDK with a version of 9 or later to be used on this tutorial. The project is set up to use JDK 11 in this tutorial. You can download the JDKs from [AdoptOpenJDK](https://adoptopenjdk.net/). Just make sure your JAVA_HOME environment variable is pointing to that JDK.

## Project Structure

In this article, we'll be covering how to develop a simple application with two modules: the `application` module that contains the web-facing classes and the `persistence` module that contains the data access layer. We'll also be using a couple of dependencies to illustrate how to use those on a modular application: `spring-boot-starter-data-mongodb` and `okta-spring-boot-starter`

The project source code can be found at [GitHub](https://github.com/brunocleite/spring-boot-using-java-modules)

### How to Structure a Modular Project with Maven?

Let's create this project folder structure manually to better understand how it should be structured. Each module will live inside a separate directory and have it's own `pom.xml` file. There will also be a `pom.xml` on the project root that will serve as the parent pom for the modules.  You can create the following folder structure:

```
.
├── application
│   ├── pom.xml
│   └── src
│       └── main
│           ├── java
│           └── resources
├── persistence
│   └── src
│       └── main
│           └── java
└── pom.xml
```

### How to Tie Up the Three pom.xml Files with Maven?

First, let's define the root `pom.xml`. It will contain the common `<parent>` indication to `spring-boot-started-parent` and two entries on `<module>` section, that are the name of the directories for the modules we are developing. Please note that those are specific to Maven and specify sub-projects and have nothing to do with the Java modules that we'll be working on later.

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>2.3.1.RELEASE</version>
        <relativePath/> <!-- lookup parent from repository -->
    </parent>
    <groupId>com.okta.developer</groupId>
    <artifactId>spring-boot-with-modules</artifactId>
    <version>0.0.1-SNAPSHOT</version>
    <packaging>pom</packaging>

    <properties>
    	<java.version>11</java.version>
    </properties>

    <modules>
        <module>application</module>
        <module>persistence</module>
    </modules>

</project>
```

The `application` module will have a pom.xml like below, pointing to the parent pom.xml that was described above. It will also have a dependency on `spring-boot-starter-web` because we'll be creating some REST endpoints on it and also a dependency on our 'persistence' module that will be described next.

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>com.okta.developer</groupId>
        <artifactId>spring-boot-with-modules</artifactId>
        <version>0.0.1-SNAPSHOT</version>
    </parent>
    <artifactId>spring-boot-with-modules-app</artifactId>

    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>com.okta.developer</groupId>
            <artifactId>spring-boot-with-modules-persistence</artifactId>
            <version>${project.version}</version>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>

</project>
```

At last, we have the `persistence` module which will have a `pom.xml` like the one below, also pointing to the parent pom.xml that was first defined. This one will have a dependency on `spring-data-mongo` as we'll be saving our data to a Mongo DB.

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>com.okta.developer</groupId>
        <artifactId>spring-boot-with-modules</artifactId>
        <version>0.0.1-SNAPSHOT</version>
    </parent>
    <artifactId>spring-boot-with-modules-persistence</artifactId>

    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-mongodb</artifactId>
        </dependency>
    </dependencies>

</project>
```

This project structure is enough for compiling if you go to the project root and run `mvn compile`. 

**NOTE:** Please don't confuse Maven modules with Java Modules. 

**Maven modules** are used to separate a project into multiple sub-projects. The main project will have a `pom.xml` referencing sub-projects on `<modules>` section. Each sub-project will have its own `pom.xml`. When building the main project it will automatically build the sub-projects too.

**Java modules** is another name for JPMS (Java Platform Module System), added on JDK 9 under the name Project Jigsaw. It allows applications (packaged as JAR or WAR) to define a `module-info.java` file on its root with directives that control which classes the application will allow others to access and which other modules it needs on compile or runtime.

## Writing Initial Application Without Java Modules

Let's write some classes without using the Java modules so we can afterward include the module definitions and see the differences. 
What defines that an application is using Java modules is the presence of `module-info.java` on its source root. We'll be creating this later on.

### Persistence Module

Create a class `Bird` on the Persistence module for representing the entity that we'll be saving to DB. 
This class will be stored on `persistence/src/main/java/com/okta/developer/animals/bird/Bird.java`

```java
package com.okta.developer.animals.bird;

import org.springframework.data.annotation.Id;

public class Bird {

    @Id
    private String id;

    private String specie;
    private String size;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSpecie() {
        return specie;
    }

    public void setSpecie(String specie) {
        this.specie = specie;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }
}

```

Now we need to create a repository to save this entity to DB. `Spring Data MongoDB` does that for us automatically creating the CRUD operations so we just have to create an interface extending MongoRepository.
This class will be stored on `persistence/src/main/java/com/okta/developer/animals/bird/BirdRepository.java`
```java
package com.okta.developer.animals.bird;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface BirdRepository extends MongoRepository<Bird, String> {
}
```

At last, for the persistence module, we'll be creating a service class to expose the persistence operations.
This class will be stored on `persistence/src/main/java/com/okta/developer/animals/bird/BirdPersistence.java`

```java
package com.okta.developer.animals.bird;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;

@Component
public class BirdPersistence {

    private BirdRepository birdRepository;

    @Autowired
    public BirdPersistence(BirdRepository birdRepository) {
        this.birdRepository = birdRepository;
    }

    @PostConstruct
    void postConstruct(){
        Bird sampleBird = new Bird();
        sampleBird.setSpecie("Hummingbird");
        sampleBird.setSize("small");
        save(sampleBird);
    }

    public void save(Bird bird) {
        birdRepository.save(bird);
    }

    public List<Bird> get() {
        return birdRepository.findAll();
    }
}

```


### Application Module

Now on the application module create the main application class annotated with `@SpringBootApplication`.
This class will be stored on `application/src/main/java/com/okta/developer/SpringBootModulesApplication.java`

```java
package com.okta.developer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SpringBootModulesApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringBootModulesApplication.class, args);
    }
}

```

And now a controller to expose REST operations on the Bird classes
This class will be stored on `application/src/main/java/com/okta/developer/BirdController.java`

```java
package com.okta.developer;

import com.okta.developer.animals.bird.Bird;
import com.okta.developer.animals.bird.BirdPersistence;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class BirdController {

    private BirdPersistence birdPersistence;

    public BirdController(BirdPersistence birdPersistence) {
        this.birdPersistence = birdPersistence;
    }

    @GetMapping("bird")
    public List<Bird> getBird() {
        return birdPersistence.get();
    }

    @PostMapping("bird")
    public void saveBird(@RequestBody Bird bird) {
        birdPersistence.save(bird);
    }

}

```

At this point, the application is functional and can be run. First, start a MongoDB instance using the following docker command:
```bash
docker run -p 27017:27017 mongo:3.6-xenial
```
Then go to the project root and run:
```bash
mvn install && mvn spring-boot:run -pl application
```

If everything went correctly you'll be able to navigate to `http://localhost:8080/bird` and see a JSON output like the following:
```json
[{"id":"5f03ff7277a08a55ae73c8b9","specie":"Hummingbird","size":"small"}]
```

## Making the Application Secure

Let's tune our app and make it secure so we start depending on another external library before moving on to adding Java modules.

Add the following dependency to your `application/pom.xml` file:
```xml
<dependency>
    <groupId>com.okta.spring</groupId>
    <artifactId>okta-spring-boot-starter</artifactId>
    <version>1.3.0</version>
</dependency>
```

### Register an Application on Okta

To begin, sign up for a [forever-free Okta developer account](https://developer.okta.com/signup/).

Once you're signed in to Okta, register your client application.

* In the top menu, click on **Applications**
* Click on **Add Application**
* Select **Web** and click **Next**
* Enter `Spring Boot with Java Modules` for the **Name** (this value doesn't matter, so feel free to change it)
* Change the Login redirect URI to be `http://localhost:8080/login/oauth2/code/okta`
* Click **Done**

### Configure the App with Okta Information

Create a file `application/src/main/resources/application.properties` with the following content:

```
okta.oauth2.issuer=https://{yourOktaDomain}/oauth2/default
okta.oauth2.clientId={clientId}
okta.oauth2.clientSecret={clientSecret} 
```

You can find {clientId} and {clientSecret} in your application setup.

The {yourOktaDomain} you can find on Okta dashboard.

Now if you restart the app and navigate to `http://localhost:8080/bird` you'll get a login page appearing.

## Using Java Modules

Now it is time to modularize the app. This is achieved by placing a file `module-info.java` on source root for each module. We'll be doing this for our two modules `application` and `persistence`. There are two ways to modularize a Java app: top-down and bottom-up. In this tutorial we'll be showing the bottom-up approach, that is modularizing the libraries before the app. This approach is preferable as we'll have `persistence` already modularized when writing the `application` `module-info.java`. If `application` was modularized first then `persistence` would be treated as an automatic module, and it would have to be referenced as the JAR file name.

### Modularize `persistence` Library

Create a module declaration file `persistence/src/main/java/module-info.java` with the following content:

```java
module com.okta.developer.modules.persistence {

    requires java.annotation;
    requires spring.beans;
    requires spring.context;
    requires spring.data.commons;
    requires spring.data.mongodb;

    exports com.okta.developer.animals.bird;
}
```

Each `requires` keyword signalize that this module will be depending on some other module.
Spring, on version 5, is not modularized yet so its JAR files don't have the `module-info.java`. 
When you have a dependency on the `modulepath` (former classpath for non-modular applications) like this they will be available as `automatic modules`.

An `automatic module` gets its name from the property `Automatic-Module-Name` inside `MANIFEST.MF` or from the `JAR` filename itself if that is absent.

On Spring 5 the team was nice enough to put the `Automatic-Module-Name` for all the libraries. So those are the names used on our persistence app dependencies: `spring.beans`, `spring.context`, `spring.data.commons`, `spring.data.mongodb`.

The `exports` keyword exports all classes in the package. When another module uses a `requires` clause referencing this one it will have access to those classes. 

In this example, the module is exporting all classes under `com.okta.developer.animals.bird` package.

### Modularize `application` App

Create a module declaration file `application/src/main/java/module-info.java` with the following content:

```java
module com.okta.developer.modules.app {

    requires com.okta.developer.modules.persistence;

    requires spring.web;
    requires spring.boot;
    requires spring.boot.autoconfigure;
}
```

This one is similar to the first one but besides the Spring dependencies we also have the `com.okta.developer.modules.persistence` dependency. That is the other module we have developed.

By adding the `requires com.okta.developer.modules.persistence` this module will have access to the package that was exported `com.okta.developer.animals.bird`.


## Running the App

Go to the project root and run 
```bash
mvn install && mvn spring-boot:run -pl application
```

Again, if everything went correctly you'll be able, after logging in, to navigate to `http://localhost:8080/bird` and see a JSON output.

## Learning More About Java Modular System 

The Java Modular System is an excellent addition to the Java ecosystem, it helps organize and isolate classes that were otherwise exposed without need. By looking at the application `module-info.java` it is possible to have a blueprint of its dependencies. 

The topic is broad and if you want to learn more this [talk by Alex Buckley](https://www.youtube.com/watch?v=22OW5t_Mbnk) is an excellent start. 

In case you have an existing Spring Boot application and want to make it use the modular system then this [talk by Jaap Coomans](https://www.youtube.com/watch?v=hxsCYxZ1gXU) covers it nicely.

If you have any questions about this post, please add a comment below. For more awesome content, follow @oktadev on Twitter, like us on Facebook, or subscribe to our YouTube channel.