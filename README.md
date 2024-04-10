### Description: 

Handle data in a MySQL database using the Java Object Relational Mapping API, Hibernate

### Note on auto generated columns:
Relations are set to auto-generate their ID fields. They are set to **start incrementing
at value 1**. The instructions had the input data starting at 0 for some IDs, however this is prevented by the flag:
`NO_AUTO_VALUE_ON_ZERO`. It would also cause complications when checking for an empty value with id=0. Initial IDs may 
be 1 value higher than the provided base data. 


### Run Instructions

	`chmod +x ./run.sh`

	`./run.sh`

### Build Script Options

* `./run.sh build` -- Requires Maven (mvn command). Compile source code, the packed jar is located at ./hw4-packed-spring-boot.jar. 

* `./run.sh run` -- Run the jar file.

* `./run.sh clean` -- Remove the jar file. 
Not allowed to run without Maven as the jar will not be able to be reproduced. Use `rm hw4-packed-spring-boot.jar` if needed.

* `./run.sh` --Run the jar file. 

