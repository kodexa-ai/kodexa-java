# Kodexa Java client

This project is the Java client for the Kodexa Content Model can be used to allow you to work with the Kodexa platform.

## Using Maven

If you want to use this project first include our Maven repository

    <repositories>
        <repository>
            <id>Kodexa</id>
            <url>https://java.kodexa.com/repo/</url>
        </repository>
    </repositories>
    
And then you can add a dependency

    <dependencies>
        <dependency>
            <groupId>com.kodexa.client</groupId>
            <artifactId>kodexa-java</artifactId>
            <version>{{version}}</version>
        </dependency>
        ...
    </dependencies>