# Schema Entropy Experiments

## Setting up the reproduction package
Clone and enter the repository:
````shell
git clone https://github.com/ChrK0/schema-entropy-experiments.git
cd schema-entropy-experiments
````
Then set up the package on the host system or on a docker container.
### Setup on host system
Enter the schema entropy folder, set up the program and copy the jar file into the experiments folder:
```shell
cd schema-entropy
./setup.sh
cd ..
cp schema-entropy/schema-entropy.jar experiments
```
### Setting up a docker container
````shell
docker build -t experiments .
docker run -it experiments
````

## Running the experiments
On the host system or in the docker container, enter the experiments folder and run the experiments:
````shell
cd experiments
./run_experiments.sh
````
The experiments can take several hours. To reduce the runtime, use the provided options:
* ``--timeout <seconds>``: Each entropy computation aborts after ``<seconds>`` seconds. The default value is 86400 seconds, i.e., 24 hours.
* ``--treads <number>``: The entropy computations are parallelized in ``<number>`` threads. The default value is 6.
* ``--skip-entropy-calc``: Only produces charts using the results in the folder ``results``.
The results of the experiments are saved in the folder ``results_new``.

## Folder structure
The repository consists of the folders ``schema-entropy`` and ``experiments``.
The former one consists of the schema entropy program code and a script to build the program.
The latter one contains the following:
* Python scripts to run the schema entropy program for experiments and to create charts.
* The script ``run_experiments.sh`` to run all the experiments and create charts.
* The folder ``tables`` containing the data and configuration files.
* The folder ``results`` with (expected) experiment results.
