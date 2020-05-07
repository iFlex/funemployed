const process = require('process');
const fs = require('fs')
var spawn = require('child_process').spawn;
let config_path = './config.json'
let output_location = process.argv[2]

const config = require(config_path)

let workers = []
let nr_simulations = config.parallel_simulations
let run_id = 0
grouped_results = {}

//ToDo: remove closed processes
function spawn_new_game(){
    p = spawn('node',["funemployed-autogame.js",run_id,output_location])
    workers.push({"process:":p,"run_id":run_id})
    console.log("Spawned simulation nr:" + run_id)
    run_id++
    if(config.simulation_continuous){
        p.on('close', (code) => {
            spawn_new_game();
        });
    }
}

//start off simulations
for(let i = 0 ; i < nr_simulations; ++i){
    spawn_new_game()    
}