# KoshiNuke server implementation by java

## Requirements
* java7 or more tested by 1.7.0_02
* gradle 1.0-milestone-7 (for development and build)
* eclipse 3.7.2 (for development)
    * [Groovy-Eclipse 2.6.1](http://groovy.codehaus.org/Eclipse+Plugin)

## License
Apache License, Version 2.0

## Install and Execute
* download latest release from [here](https://github.com/koshinuke/koshinuke.java/downloads)
* execute  
  `java -jar koshinuke.jar`
* access to [localhost](http://localhost)
* default username and password is taichi/taichipass
* if you want to add user, gradle script contains password utility  
    * modify passwd task  
    * execute passwd task  
       `gradle passwd`  
    * copy output to etc/login.properties

## Setup for development
* clone this repository  
  `git clone git@github.com:koshinuke/koshinuke.java.git`  
* clone koshinuke repository  
  `git clone git@github.com:koshinuke/koshinuke.git`  
* checkout closure-library  
  `svn co http://closure-library.googlecode.com/svn/trunk/ closure-library`  
* make gradle.properties
    * add proxy entry if you need.
    * koshinuke\_path to local koshinuke repository root dir
    * closure\_path to local clorsure-library root dir  
* copy static contents from koshinuke  
  `gradle cloneweb sym`  
* convert to eclipse project  
  `gradle eclipse`  
* build executable jar  
  `gradle war`  
* execute src/groovy/Debug.groovy for debug
