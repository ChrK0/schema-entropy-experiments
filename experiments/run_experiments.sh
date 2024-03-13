#!/bin/bash

results_dir="results_new"
results_dir_new="results_new"
timeout_seconds=86400
threads=6
skip_ent_calc=false

i=1
while [ $i -lt $# ]
do
  if [ "${!i}" == "--timeout" ]
  then i=$((i+1)); timeout_seconds=${!i}
  elif [ "${!i}" == "--threads" ]
  then i=$((i+1)); threads=${!i}
  elif [ "${!i}" == "--skip-entropy-calc" ]
  then i=$((i+1)); skip_ent_calc=${!i}
  else echo "\"${!i}\" is not a valid command."; exit 0
  fi
  i=$((i+1))
done

rm -rf $results_dir
mkdir -p $results_dir

if [ "$skip_ent_calc" == true ]
then results_dir="results"
elif [ "$skip_ent_calc" == false ]
then
  python3 run-experiments.py tables/satellites/config.json $results_dir $timeout_seconds $threads & if [[ $(jobs -r -p | wc -l) -ge $threads ]]; then wait -n; fi
  python3 run-experiments.py tables/adult/config.json $results_dir $timeout_seconds $threads & if [[ $(jobs -r -p | wc -l) -ge $threads ]]; then wait -n; fi
  python3 run-experiments.py tables/echocardiogram/config.json $results_dir $timeout_seconds $threads & if [[ $(jobs -r -p | wc -l) -ge $threads ]]; then wait -n; fi
  python3 run-experiments.py tables/iris/config.json $results_dir $timeout_seconds $threads & if [[ $(jobs -r -p | wc -l) -ge $threads ]]; then wait -n; fi
  python3 run-experiments.py tables/ncvoter/config.json $results_dir $timeout_seconds $threads & if [[ $(jobs -r -p | wc -l) -ge $threads ]]; then wait -n; fi
  python3 run-experiments.py tables/satellites/config_opt-off.json $results_dir $timeout_seconds $threads & if [[ $(jobs -r -p | wc -l) -ge $threads ]]; then wait -n; fi
  python3 run-experiments.py tables/satellites/config_opt-on.json $results_dir $timeout_seconds $threads & if [[ $(jobs -r -p | wc -l) -ge $threads ]]; then wait -n; fi
else echo "skip_entropy_calc not set correctly."; exit 0
fi
wait

python3 heatmap_runtimes.py satellites ${results_dir}/satellites/satellites_runtimes.csv $results_dir_new

mc_sat=${results_dir}/satellites/satellites_monte-carlo-100-000_rows_150.csv
mc_adult=${results_dir}/adult/adult_monte-carlo-100-000_rows_150.csv
mc_echo=${results_dir}/echocardiogram/echocardiogram_monte-carlo-100-000_rows_132.csv
mc_iris=${results_dir}/iris/iris_monte-carlo-100-000_rows_150.csv
mc_ncvoter=${results_dir}/ncvoter/ncvoter_monte-carlo-100-000_rows_150.csv

if test -f "$mc_sat"
then python3 histogram.py satellites "$mc_sat" $results_dir_new
fi

if test -f "$mc_sat"
then python3 heatmap_entropies.py satellites "$mc_sat" $results_dir_new bar zoom results/satellites/satellites_monte-carlo-100-000_rows_150_r110-130.csv tables/satellites/satellites_zoom.csv
fi

if test -f "$mc_adult"
then python3 heatmap_entropies.py adult "$mc_adult" $results_dir_new
fi

if test -f "$mc_echo"
then python3 heatmap_entropies.py echocardiogram "$mc_echo" $results_dir_new
fi

if test -f "$mc_iris"
then python3 heatmap_entropies.py iris "$mc_iris" $results_dir_new
fi

if test -f "$mc_ncvoter"
then python3 heatmap_entropies.py ncvoter "$mc_ncvoter" $results_dir_new
fi

python3 accuracy.py $results_dir_new
