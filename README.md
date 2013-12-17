Embezzler
==============

**Embezzler** is a simple script to automatically assign [SonarQube](http://www.sonarqube.org/) issues to authors according to [SCM](http://en.wikipedia.org/wiki/Software_configuration_management).

**Embezzler** is a self-contained script that could be easily distributed, it is written in [Groovy](http://groovy.codehaus.org/), uses [Grape](http://groovy.codehaus.org/Grape) for [SonarQube](http://www.sonarqube.org/) [API dependency](http://docs.codehaus.org/display/SONAR/Using+the+Web+Service+Java+client).

Run
--------

```bash
# get last unassigned issue and assign it to appropriate user
groovy Embezzler.groovy -s http://localhost:9000 -u login -p password -c 'project1,project2' -n 1 --dry-run false
# get last 5 unassigned issues started from yesterday and assign it to appropriate users
groovy Embezzler.groovy -s http://localhost:9000 -u login -p password -c 'project1,project2' -n 5 -db 1 --dry-run false
# get all unassigned issues started from yesterday and assign it to appropriate users
groovy Embezzler.groovy -s http://localhost:9000 -u login -p password -c 'project1,project2' -db 1 --dry-run false
```

Usage
-----

```bash

groovy main/groovy/Embezzler.groovy --help

usage: groovy Embezzler.groovy -s http://localhost:9000 -u user -p
              password
 -c,--components <arg>   specify list of components (separated by comma)
 -d,--dry-run <arg>      specify to not to perform any action, by default
                         is dry-run
 -h,--help               print this message
 -n,--number <arg>       specify number of issues to retreive, if not 
                         specified will be queried all issues for whole day
 -db,--days-before <arg>  specify day for which will be queried issues
                          (1 - yesterday, 2 - two days ago and so on), if not 
                          specified issues will be queried without date 
                          relation
 -p,--password <arg>     specify user password
 -s,--host <arg>         specify SonarQube URL
 -u,--user <arg>         specify user login

```

Configuration
----------

In order to hide password from console history or script is used with CI server, for example Jenkins, you can specify environment variable **SNR_PASSWORD** instead of passing password with **-p**.

```bash

export SNR_PASSWORD=mypassword

```
