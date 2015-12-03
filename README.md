# Maven Camel SQL Loader Archetype
8/7/14

Archetype for loading data off the bus into SQL

Thanks to:

* http://www.luckyryan.com/2013/02/15/create-maven-archetype-from-existing-project/
* http://marosmars.weebly.com/blog/maven-archetype-tutorial

# creating & installing the archetype project

    mvn clean archetype:create-from-project
    cd target/generated-sources/archetype
    mvn install

# testing the archetype project

    #cd to the directory above (target/generated-sources/archetype)
    # add 'clean test' to goal.txt
    mvn clean install archetype:integration-test

# deploying the archetype

# creating a new project from the archetype

    #in a new folder
    mvn archetype:generate -X -e \
    -DarchetypeGroupId=la.isg \
    -DarchetypeArtifactId=maven-camel-sql-loader-archetype \
    -DarchetypeVersion=0.2.0-SNAPSHOT \
    -DgroupId=com.fandango \
    -DartifactId=data-warehouse-loader \
    -DinteractiveMode=true

Test it with

    mvn clean test
        
If all goes well, all tests should pass.

### Running in jetty

>mvn jetty:run-forked -DskipTests -Ptest

The default profile is development.  Other profiles are test, production.  You need to set the appropriate environment variables in your ~/.m2/settings.xml file.
You need to use run-forked for maven to map properties into environment variables when launching jetty. 

### Injecting credentials and other environment specific configuration

Injection of credentials needs to meet 2 specific requirements:
* build once, run anywhere - this means the docker container or WAR once built is immutable.  It can be promoted throughout the Continuous Deployment lifecycle without modification
* production security credentials are only kept plain-text in the context of the running service account.  For example, chef builds the service account and pulls those credentials from the vault.

For Maven/Jetty/Camel, this means:
* Profile dependent properties must be limited to that which can be injected into ENV variables
* test credentials can be stored in externalized .properties files and checked in to Git, but real credentials should be kept in personal .m2/settings
* a sample settings file can be provided in Git
* Unit/Integration tests running under Surefire, must set the appropriate environment variables
* Running under the Jetty plugin, must set the appropriate environment variables
* Running under the Camel plugin, must set the appropriate environment variables

### Unit and Integration testing

Tests need to
* migrate the input and output database schema
* prime the input database with test data,
* or mock connectors

## How to allow a local repo commit of a public open source project without changing any of the files

In settings.xml:
        <profile>
            <id>repo</id>
            <properties>
                <distMgmtSnapshotsUrl>http://change.me
                </distMgmtSnapshotsUrl>
            </properties>
        </profile>

When deploying:
>mvn deploy -Poddz-repo

## Initial Redshift DB Creation

issue a personal IAM account
access the AWS console using the personal IAM account
create Redshift cluster in correct vpc (prod, staging, dev)
set redshift firewall to allow access on 5439 from the rest of the subnet|vpc|...

access the VPN
connect to the database as the sysop user

create the owner users (see below)
          
create user <user1> password '<pw>'  createuser;
create user <user2> password '<pw>'  createuser;

create group warehouse_owner_role;

alter group warehouse_owner_role add user <user1>, <user2>;       
   
## Setup credentials in 
  .env.test
  .env.production
  ~/.m2/settings.xml

## Running the Flyway plugin

Add this to your settings.xml:
...
    <pluginGroups>
       <pluginGroup>org.flywaydb</pluginGroup>
    </pluginGroups>
...

Run mvn compile flyway:migrate

Run mvn compile org.flywaydb:flyway-maven-plugin:migrate