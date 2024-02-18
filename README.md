### Convert a CSV file into a fixed length record database.

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

