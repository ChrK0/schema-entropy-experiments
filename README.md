# A Plaque Test for Redundancies in Relational Data - Reproduction Package

This is a reproduction package for the paper "A Plaque Test for Redundancies in Relational Data" by Christoph Köhnen, Stefan Klessinger, Jens Zumbrägel and Stefanie Scherzinger, published in QDB at VLDB, 2023.
It contains a program code as a submodule and experiments on real-world datasets, executed with this program.

This reproduction package provides a Docker container.

The datasets are located in the directory ``experiments/tables``, the original results in the directory ``experiments/results``.

## Setting up the reproduction package
Clone the reproduction package and the program code using the parameter ``--recurse-submodules``.
You can set up the package on the host system or on a docker container.

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
* ``--threads <number>``: The entropy computations are parallelized in ``<number>`` threads. The default value is 1.
* ``--skip-entropy-calc <boolean>``: Only produces charts using the results in the folder ``results`` if ``<boolean>`` is true. The default value is false.
The results of the experiments are saved in the folder ``results_new``.

## Folder structure
The repository consists of the folders ``schema-entropy`` and ``experiments``.

### The folder ``schema-entropy``
This folder contains the [schema entropy program code](https://github.com/sdbs-uni-p/schema_entropy) as a submodule.

To run the experiments on a specified version, enter this folder and checkout this version.

### The folder ``experiments``
This folder consists of the following components:
* Python scripts to run the schema entropy program for experiments and to create charts.
* The script ``run_experiments.sh`` to run all the experiments and create charts.
* The folder ``tables`` containing the data and configuration files.
* The folder ``results`` with (expected) experiment results.

## About
To reference this work, please use the following BibTeX entry.
```bibtex
@inproceedings{plaque_test,
  author       = {Christoph K{\"{o}}hnen and
                  Stefan Klessinger and
                  Jens Zumbr{\"{a}}gel and
                  Stefanie Scherzinger},
  title        = {A Plaque Test for Redundancies in Relational Data},
  booktitle    = {QDB at VLDB},
  series       = {{CEUR} Workshop Proceedings},
  volume       = {3462},
  year         = {2023}
}
```

All artifacts, including source code, data, and scripts, are available on Zenodo, DOI: [10.5281/zenodo.8220684](https://doi.org/10.5281/zenodo.8220684).

The reproduction package was created by Christoph Köhnen. The experimental scripts were created by Stefan Klessinger and Christoph Köhnen.

This work was partly funded by Deutsche Forschungsgemeinschaft (DFG, German Research Foundation) grant #385808805.
