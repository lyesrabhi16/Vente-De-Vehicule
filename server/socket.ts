import { Socket } from "socket.io";
import { SocketClient as Client, Message, } from "./interfaces";
import { DBC } from "./DBConnection";




export const clients : Array<Client> = new Array<Client>();

export const connection = (socket : Socket) => {
    console.log(`socket connected : [${socket.id}]`);
    socket.emit("connected");
    socket.emit("request-init");
    console.log(`clients: [${clients}]`)
}

export const disconnection = (socket : Socket) => {
    console.log(`socket disconnected : [${socket.id}]`);
    clients.filter(client => client.socket == socket);
}

export const init = (id : number, socket : Socket) => {
    const client : Client = {
        socket : socket,
        userID     : id
    };
    clients.push(client);
    console.log(`client identified : [${socket.id}] : [${id}]`);
    console.log(`clients: [${clients}]`)
}

export const ClientMessage = (message : string , socket : Socket) => {
    console.log(`Message from [${socket.id}] :\n\t${message}`);
}

export const message = (message : Message , socket : Socket) => {
    console.log(`message from [${socket.id}] :\n\t${JSON.stringify(message)}`);
    let dbc : DBC = new DBC();
    message.etatMessage = "sent";
    console.log("saving message...")
    dbc.AddMessage(message)
        .then((result : any)=>{
            console.log("message saved");
            socket.emit("message-sent", message);
            console.log("clients : "+clients.map(client => client.userID));
            clients.forEach((client : Client) => {
                if (client.userID == (message.idClient_reciever)){
                    console.log("sending message...")
                    client.socket.emit("message", message, result.insertId);
                }
            });
        })
        .catch(err => console.log(err));
}

export const message_received = (id : number) =>{
    console.log("message ["+ id +"] reception confirmed.");
    
};