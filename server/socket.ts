import { Socket } from "socket.io"


interface Client {
    socket : Socket,
    userID : number
}

export const clients : Set<Client> = new Set<Client>();

export const connection = (socket : Socket) => {
    console.log(`socket connected : [${socket.id}]`);
    socket.emit("connected");
    socket.emit("request-init");
}

export const disconnection = (socket : Socket) => {
    console.log(`socket disconnected : [${socket.id}]`);
    clients.forEach(client => {
        if( client.socket === socket ) clients.delete(client);
    });
}

export const init = (id : number, socket : Socket) => {
    let client : Client = {
        socket : socket,
        userID     : id
    };
    clients.add(client);
    console.log(`client identified : [${socket.id}] : [${id}]`);
}

export const ClientMessage = (message : string , socket : Socket) => {
    console.log(`Message from [${socket.id}] :\n\t${message}`);
}
