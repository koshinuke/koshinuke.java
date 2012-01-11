# KoshiNuke server implementation by java

## Requirements
* java7 tested by 1.7.0_02
* gradle 1.0-milestone-7 (for development)
* eclipse 3.7.1 (for development)

## License
Apache License, Version 2.0

## setup for development
* clone this repository
* clone koshinuke repository
    * see. https://github.com/koshinuke/koshinuke
* make gradle.properties
    * add proxy entry if need.
    * koshinuke_path to local koshinuke repository root dir
    * closure_path to local clorsure-library root dir
* execute gradle  
  `gradle cloneweb sym`  
  `gradle eclipse`  
