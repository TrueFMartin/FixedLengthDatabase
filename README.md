### Description: 

Handle data in a MySQL database using the Java Object Relational Mapping API, Hibernate

### Note on auto generated columns 'item_no' and 'order_no':
Relations 'food_order' and 'menu_item' are set to autoincrement. They are set to **start incrementing
at value 1**. The instructions had the input data starting at 0, however this is prevented by the flag:
`NO_AUTO_VALUE_ON_ZERO`. It would also cause complications when checking for an empty value with id=0. 

### Run Instructions

	`chmod +x ./run.sh`

	`./run.sh`

### Build Script Options

* `./run.sh build` -- Compile source code, class files created in ./out/ 

* `./run.sh run` -- Run class files in ./out/

* `./run.sh clean` -- Remove class files

* `./run.sh` -- Compile source code, then run program

### Log Args: 

* If you are not using the run script, add the following flag to your run command:

	`-Dlog4j.configurationFile=./log4j2.xml`

