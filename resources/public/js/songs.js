
// Websocket
const uri = ((window.location.protocol === "https:") ? "wss://" : "ws://")
            + window.location.host;
let ws = null;

function connect(){
    ws = new WebSocket(uri);
    ws.onopen=function(){console.log("Connected.");}
    ws.onmessage=function(m){
        console.log(m);
        data.unshift(JSON.parse(m.data));
    };
    ws.onclose=function(){
        console.log("Disconnected.")
        connection_check();
    };
};

function connection_check(){
    if(!ws || ws.readyState == WebSocket.CLOSED) connect();
    ws.send("ping"); // heroku..
}

connect();
setInterval(connection_check, 5000);

// View model
window.addEventListener('load', function () {
    const vm = new Vue({
        data: {songs: data},
        el: '#songs'
    });
});

