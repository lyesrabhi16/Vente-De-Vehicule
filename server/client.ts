import { Socket, io } from 'socket.io-client';
import { Server_Url } from './configs';


const client : Socket = io(Server_Url);

console.log("connecting...")

client.on("connected", () => {
    console.log("server connected.");
})

client.on("request-init", ()=>{
    client.emit("init", 1);
})

client.emit("client-message", "Hello from client");
