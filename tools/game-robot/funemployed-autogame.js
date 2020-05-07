//const https = require('https')
const http = require('http')
const process = require('process');
const { PerformanceObserver, performance } = require('perf_hooks');
const fs = require('fs')

let run_id = process.argv[2]
let output_path = process.argv[3]
let config_path = './config.json'
//ToDo: add more flexible configuration via args

const config = require(config_path)
const url_prefix = config["protocol"] + "://" + config["hostname"] + ":" + config["port"] + config["api_location"]
console.log("Using API Prefix:" + url_prefix)
const stats = {}

//generate players
for(let i = 0 ; i < config.generate_players; ++i){
    config.players.push({"id":"player_"+i})
    console.log("Generated player " + i)
}


function positional_parameterised_url(params){
    const regex = "%"
    url = params[0];
    for(let i = 1; i < params.length; ++i){
        url = url.replace(regex, params[i])
    }

    return url_prefix + url
}

function replaceAll(string, search, replace) {
    return string.split(search).join(replace);
}

function compute_perf_report(){
    result = {}
    replace = ["/","%"]
    for(let k in stats){
        stats[k].average /= stats[k].count
        key = k
        for(let i in replace){
            key = replaceAll(key,replace[i],"")
        }
        result[key] = stats[k]
    }

    return JSON.stringify(result)
}

function record_new_call(url, time){
    if(!stats[url]){
        stats[url] = {
            "max":0,
            "min":-1,
            "average":0,
            "count":0
        }
    }

    stats[url].average += time
    stats[url].count += 1
    if(time > stats[url].max){
        stats[url].max = time
    }
    if(stats[url].min == -1 || stats[url].min > time){
        stats[url].min = time
    }
}

async function api_call(params) {
    return new Promise((resolve, reject) => {
        let url = positional_parameterised_url(params)
        var t0 = performance.now()
        
        const req = http.get(url, (response) => {
            let chunks_of_data = [];
            response.on('data', (fragments) => {
                chunks_of_data.push(fragments);
            });
            
            response.on('end', () => {
                let response_body = Buffer.concat(chunks_of_data);
                // promise resolved on success
                var t1 = performance.now()
                time = (t1 - t0)
                console.log(url + "," + time +" ms")
                record_new_call(params[0],time)

                if(response.statusCode == 200){
                    resolve(JSON.parse(response_body.toString()));
                } else {
                    reject("RESPONSE_CODE:"+response.statusCode+"\n"+response_body);
                }
            });
    
            response.on('error', (error) => {
                // promise rejected on error
                var t1 = performance.now()
                console.log(url + ", " + (t1 - t0)+" ms")
                reject(error);
            });
        });
    });
}

async function new_game() {
    game_data = await api_call([config.api.game_new])
    config.game_id = game_data.id;
    for(let i in config.players){
        player = config.players[i]
        await api_call([config.api.player_add, config.game_id, player.id])
    }
}

async function ready_players(){
    for(let i in config.players){
        let player = config.players[i]
        if(player.id != config.employer.id){
            await api_call([config.api.player_ready, config.game_id, player.id, player.traits[0].id, player.traits[1].id, player.traits[2].id])
        }
    }
}

async function interview_player(player){
    await api_call([config.api.interview_start, config.game_id, player.id])
    for(let i = 0 ; i < 3; ++i){
        await api_call([config.api.interview_reveal, config.game_id, player.id, player.traits[i].id]);
    }
    //interview ends automatically after all cards were revealed
}

async function interview_players(){
    for(let i in config.players){
        let player = config.players[i]
        if(player.id != config.employer.id){
            await interview_player(player)
        }
    }
}

last_winner = -1
async function pick_winner(mode){
    if(mode == "random"){
        winner_index = Math.floor(Math.random() * 100) % config.players.length
    } else if(mode == "in_order"){
        winner_index = (last_winner + 1) % config.players.length;
    } else {
        winner_index = 0    
    } 
    
    if(config.employer.id == config.players[winner_index].id) {
        winner_index = (winner_index + 1) % config.players.length;
    }

    last_winner = winner_index
    winner = config.players[winner_index]
    await api_call([config.api.turn_end, config.game_id, winner.id])
}

async function play_turn() {
    turn_data = await api_call([config.api.turn_start, config.game_id])
    config.players = turn_data.players
    config.employer = turn_data.currentEmployer
    config.turnsLeft = turn_data.turnsLeft
    config.last_turn_data = turn_data

    await ready_players(config.employer)
    await interview_players()
    await pick_winner(config.winner_pick_mode)
}

console.log("SIMULATION START::");

(async function () {
    // wait to http request to finish
    await new_game();
    if(config.turns_to_play > -1) {
        //play a preconfigured number of turns or forever (0)
        for(let i = 0; config.turns_to_play == 0 || i < config.turns_to_play; i++){
            try{
                await play_turn();
            } catch(e){
                console.log("SIMULATION_END via ERROR")
                console.log(e)
                break;
            }
        }    
    } else {
        //play as many turns as the game indicates are left
        do {
            try{
                await play_turn();
            } catch(e){
                console.log("SIMULATION_END ended via ERROR")
                console.log(e)
                break;
            }
        } while(config.turnsLeft > 1);
    }
    console.log("SIMULATION_END")
    try {
        fs.writeFileSync(output_path+"/"+run_id+".json", compute_perf_report())
      } catch (err) {
        console.error(err)
      }
})();