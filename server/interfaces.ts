import { Socket } from "socket.io";

export interface Message{
    idClient_sender:number,
    idClient_reciever:number,
    contenuMessage:string,
    etatMessage:string,
    date:string
}
export interface SocketClient {
    socket : Socket,
    userID : number
}
export interface User {
    nomClient : string, 
    prenomClient: string,
    ageClient : string,
    email : string,
    numTel : string,
    password : string    
}