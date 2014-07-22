What its about.
===

Github-reporter is a commandline reporting tool for teams.

Github-reporter reports a teams progress in handling github issues and pull requests.

It is currently set to send out a weekly report but can be configured to do monthly reports as well.

Github-reporter is part of [NeverwinterDP the Data Pipeline for Hadoop]


Usage
===
In its simplest format the report will scan through github repositories of the team and provide a summary of:    
1. A count of tickets and pull requests handled by each team member.    
2. A count of tickets and pull requests handled in each repository.    
3. Details of all new tickets    
4. Details of all closed tickets    
5. Details of all new pull requests    
6. Details of all closed pull requests    

Requirements    
===
1. [jdk 1.7]
2. [gradle]


Installation instructions
===
```
1. git clone https://github.com/DemandCube/github-reporter.git    
2. cd github-reporter    
3. vi viper.properties    
    edit the sender's email address.    
    if the report is to be sent to a single address (as during testing)  provide the 'to' property.    
4. gradle run -DgmailUsername=xxxx -DgmailPassword=xxxx -DgithubUser=xxxx  -DgithubPassword=xxxx
```

The report will be sent in .xls format to all members of the team or to the individual address provided in step 3.

Dependencies.
===
Git hub reporter is a java application that depends on the following libraries    
1. [guava 17.0]    
2. [apache poi 3.7]    
3. [github-api 1.50]    
4. [log4j 1.2.17]    
5. [poi-ooxml 3.10-FINAL]    
6. [spark-core 1.0]    
7. [gson 2.2.4]    
8. [jakarta commons-email 1.3.2]    

[NeverwinterDP the Data Pipeline for Hadoop]:https://github.com/DemandCube/NeverwinterDP
[jdk 1.7]:http://www.oracle.com/technetwork/java/javase/downloads/jdk7-downloads-1880260.html
[gradle]:http://www.gradle.org/
[guava 17.0]:https://code.google.com/p/guava-libraries/
[apache poi 3.7]:http://poi.apache.org/
[github-api 1.50]:https://github.com/kohsuke/github-api
[log4j 1.2.17]:http://logging.apache.org/log4j/1.2/
[poi-ooxml 3.10-FINAL]:http://poi.apache.org/
[spark-core 1.0]:http://www.sparkjava.com/
[gson 2.2.4]:https://code.google.com/p/google-gson/
[jakarta commons-email 1.3.2]:http://commons.apache.org/proper/commons-email/



