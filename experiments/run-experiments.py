import json
import os
import shutil
import subprocess
import sys
import time
import uuid
from concurrent.futures import ThreadPoolExecutor, as_completed

temp_dir = "tmp"


def get_args(config, res_dir, experiment, rows):
    csv_name = config["input"]
    input = csv_name
    experiment_name = (config["name"] + "_" + experiment["name"] + "_rows_" + str(rows)).replace(" ", "-")
    outdir = f'{res_dir}/{config["outdir"]}'
    if rows > 0:
        # create temporary csv file with specified number of rows
        input = f"{temp_dir}/{experiment_name}.csv"
        if not os.path.exists(input.rsplit("/", 1)[0]):
            os.makedirs(input.rsplit("/", 1)[0], exist_ok=True)
        with open(input, "w") as f:
            with open(csv_name, "r") as original:
                for i in range(rows):
                    f.write(next(original))

    args = ["java", "-jar", "schema-entropy.jar", input]

    if experiment["find fds"]:
        args.append("--find-fds")
    args.append("--name")
    args.append(f"{outdir}/{experiment_name}.csv")
    if experiment["optimizations"]:
        args.append("-i")
        args.append("-s")
    if experiment["monte carlo"] > 0:
        args.append("-r")
        args.append(str(experiment["monte carlo"]))

    for fd in experiment["fds"]:
        args.append(fd)

    return args, experiment_name


def run_experiment(config, res_dir, timeout, experiment, rows):
    args, experiment_name = get_args(config, res_dir, experiment, rows)
    start = time.time()
    try:
        subprocess.run(args, timeout=timeout)
    except subprocess.TimeoutExpired:
        return [experiment_name, experiment["monte carlo"], rows, "timeout"]
    end = time.time()
    print(f"Finished {experiment_name} on {rows} rows in {(end - start)*1000} ms")
    runtime = round((end - start)*1000)
    return [experiment_name, experiment["monte carlo"], rows, runtime]


def run_experiments(conf_path, res_dir, timeout, workers):
    global temp_dir
    temp_dir += "_" + uuid.uuid4().hex

    with open(conf_path, "r") as f:
        config = json.load(f)

    if not os.path.exists(temp_dir):
        os.makedirs(temp_dir, exist_ok=True)
    if not os.path.exists(f'{res_dir}/{config["outdir"]}'):
        os.makedirs(f'{res_dir}/{config["outdir"]}', exist_ok=True)

    runtimes = [["experiment", "monte carlo", "rows", "runtime"]]
    with ThreadPoolExecutor(max_workers=workers) as executor:
        futures = [executor.submit(run_experiment, config, res_dir, timeout, experiment, rows) for rows in config["rows"] for experiment in config["experiments"]]

    for future in as_completed(futures):
        runtimes.append(future.result())

    with open(f'{res_dir}/{config["outdir"]}/{config["name"]}_runtimes.csv', "w") as f:
        for row in runtimes:
            f.write(",".join(map(str, row)) + "\n")

    if os.path.exists(temp_dir):
        shutil.rmtree(temp_dir)


if __name__ == "__main__":
    config_path = sys.argv[1]
    results_dir = sys.argv[2]
    timeout_seconds = int(sys.argv[3])
    threads = int(sys.argv[4])
    run_experiments(config_path, results_dir, timeout_seconds, threads)
