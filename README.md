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
    _example of gradle.properties_  
    ```
    systemProp.http.proxyHost=proxy.example.org  
    systemProp.http.proxyPort=8080  
    koshinuke_path=~/repos/koshinuke  
    closure_path=~/repos/closure-library
    ```
* copy static contents from koshinuke  
  `gradle cloneweb sym`  
* convert to eclipse project  
  `gradle eclipse`  
* build executable jar  
  `gradle war`  
* execute src/groovy/Debug.groovy for debug
