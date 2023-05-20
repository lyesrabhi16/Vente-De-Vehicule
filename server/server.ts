import { DBC } from "./DBConnection";
import express from 'express';
import cors from 'cors';
import { sign } from "crypto";
import { ClientMessage, clients, connection, disconnection, init, message, message_received } from "./socket";
import { Server, Socket } from "socket.io";
import http from "http";
import { io } from "socket.io-client";
import socket from 'socket.io';
import { Annonce, Message } from "./interfaces";
/* DB connection */

const dbc : DBC  = new DBC();


/* Express Server */
const APP : express.Application = express();
const PORT : number = 5000;


// parse incoming request body and append data to `req.body`
APP.use(express.json());
APP.use(express.urlencoded({ extended: true }));

// enable all CORS request 
APP.use(cors());


APP.get("/",(_req : express.Request, _res : express.Response) => {
    console.log("GET /");
    _res.send("hello from Server");
});

APP.post("/user",
    async (_req : express.Request, _res : express.Response)=>{
        console.log("POST /user");
        let id : string = _req.body.idClient;
        if(!id) {
            _res.json({error:"missing required fields : [idClient]"});
            return;
        }
        dbc.getUser(id)
            .then(user => _res.json({user:user}))
            .catch(err => _res.json({error:err}))

})

APP.post("/signup", (_req : express.Request, _res : express.Response) => {
    console.log("POST /signup");
    let 
        missingFields : string[] = [],
        user : {
            nomClient : string,
            prenomClient : string,
            ageClient : string,
            email : string,
            numTel : string,
            password : string
        };


        user = {
                    nomClient : _req.body["nomClient"],
                    prenomClient : _req.body["prenomClient"],
                    ageClient : _req.body["ageClient"],
                    email :  _req.body["email"],
                    numTel : _req.body["numTel"],
                    password : _req.body["password"]
                }

        if(!user.nomClient){
            missingFields.push("nomClient")
        }
        
        if(!user.prenomClient){
            missingFields.push("prenomClient")
        }
        
        if(!user.ageClient){
            missingFields.push("ageClient")
        }
        if(!user.email){
            missingFields.push("email")
        }
        
        if(!user.numTel){
            missingFields.push("numTel")
        }
        
        if(!user.password){
            missingFields.push("password")
        }
    
        if(missingFields.length > 0){
            _res.json({
                error : `missing required fields : [${missingFields.join(", ")}]`
            })
            return;
        }
        dbc.signup(user)
                .then( (result:any) =>{
                    dbc.getUser(result.insertId)
                            .then((result:any)=> {
                                let response = {
                                    idClient : result[0].idClient,
                                    nomClient : result[0].nomClient,
                                    prenomClient : result[0].prenomClient,
                                    ageClient : result[0].ageClient,
                                    email : result[0].email,
                                    numTel: result[0].numTel
                                }
                                _res.json(response);
                            })
                            .catch(err => _res.json({error: err}))
                } )
                .catch(err => _res.json({ error: err}))

})

APP.post("/signin", (_req : express.Request, _res : express.Response) => {
    console.log("POST /signin");
    let 
        missingFields : string[] = [],
        email : string = _req.body["email"],
        numTel : string = _req.body["numTel"],
        password : string = _req.body["password"];

        if(!email && !numTel){
            missingFields.push("email/numTel")
        }
        
        if(!password){
            missingFields.push("password")
        }
    
        if(missingFields.length > 0){

            _res.json({
                error : `missing required fields : [${missingFields.join(", ")}]`
            });
            return;

        }

        dbc.signin(email, numTel, password)
            .then(
                (result:any)=>{
                    let response = {
                        idClient : result[0].idClient,
                        nomClient : result[0].nomClient,
                        prenomClient : result[0].prenomClient,
                        ageClient : result[0].ageClient,
                        email : result[0].email,
                        numTel: result[0].numTel
                    }
                    _res.json(response);
                }
            )
            .catch(error => _res.json({error : error}))

});

APP.post("/search", (_req : express.Request , _res : express.Response) => {
    console.log("POST /search");

    let 
        missingFields : string[] = [],
        term : string = _req.body["term"],
        table : string = _req.body["table"],
        cols : string = _req.body["cols"];
        


    if(!term){
        missingFields.push("term")
    }
    
    if(!table){
        missingFields.push("table")
    }
    
    if(!cols){
        missingFields.push("cols")
    }

    if(missingFields.length > 0){
        _res.json({
            error : `missing required fields : [${missingFields.join(", ")}]`
        })
        return;
    }

    dbc.search(table, cols.split(","), term.split(' '))
        .then(
            (result) => _res.json({result : result})
        )
        .catch(
            (error) => _res.json({error : error})
        )
        

    
});
APP.post("/chats", (_req : express.Request, _res : express.Response) => {
    console.log("POST /chats");
    let 
        missingFields : string[] = [],
        idClient : number = Number.parseInt(_req.body["idClient"]);
        

    if(!idClient){
        missingFields.push("idClient");
    }
    
    if(missingFields.length > 0){
        _res.json({
            error : `missing required fields : [${missingFields.join(", ")}]`
        })
        return;
    }

    dbc.chats(idClient)
        .then(result=> _res.json({result:result}))
        .catch(error => _res.json({error:error}));
});

APP.post("/messages", (_req : express.Request, _res : express.Response) => {
    console.log("POST /messages");
    let 
        missingFields : string[] = [],
        idClient1 : number = Number.parseInt(_req.body["idClient1"]),
        idClient2 : number = Number.parseInt(_req.body["idClient2"]);
        

    if(!idClient1){
        missingFields.push("idClient1");
    }
    
    if(!idClient1){
        missingFields.push("idClient2");
    }
    
    if(missingFields.length > 0){
        _res.json({
            error : `missing required fields : [${missingFields.join(", ")}]`
        })
        return;
    }

    dbc.messages(idClient1, idClient2)
        .then(result=> _res.json({result:result}))
        .catch(error => _res.json({error:error}));
});

APP.post("/messages/add", (_req : express.Request, _res : express.Response) => {
    console.log("POST /messages/add");
    let 
        missingFields : string[] = [],
        idClient_sender : number = Number.parseInt(_req.body["idClient_sender"]),
        idClient_reciever : number = Number.parseInt(_req.body["idClient_reciever"]),
        contenuMessage : string = _req.body["contenuMessage"],
        etatMessage : string = _req.body["etatMessage"],
        date : string = _req.body["date"];

        

    if(!idClient_sender){
        missingFields.push("idClient_sender");
    }
    if(!idClient_reciever){
        missingFields.push("idClient_reciever");
    }
    if(!contenuMessage){
        missingFields.push("contenuMessage");
    }
    if(!etatMessage){
        missingFields.push("etatMessage");
    }
    if(!date){
        missingFields.push("date");
    }
    
    if(missingFields.length > 0){
        _res.json({
            error : `missing required fields : [${missingFields.join(", ")}]`
        })
        return;
    }

    let msg : Message = {
        idClient_sender,
        idClient_reciever,
        contenuMessage,
        etatMessage : etatMessage,
        date
    } 

    dbc.AddMessage(msg)
        .then((result:any)=> _res.json({result:result.insertId}))
        .catch(error => _res.json({error:error}));
});

APP.post("/annonce", (_req : express.Request, _res : express.Response) => {
    console.log("POST /annonce");
    let 
    missingFields : string[] = [],
    idAnnonce : number = Number.parseInt(_req.body["idAnnonce"]);
    

    if(!idAnnonce){
        missingFields.push("idAnnonce");
    }

    if(missingFields.length > 0){
        _res.json({
            error : `missing required fields : [${missingFields.join(", ")}]`
        })
        return;
    }

    dbc.Annonce("idAnnonce",idAnnonce)
        .then(result=> _res.json({result:result}))
        .catch(error => _res.json({error:error})); 
});

APP.post("/annonces", (_req : express.Request, _res : express.Response) => {
    console.log("POST /annonces");
    /*let 
    missingFields : string[] = [],
    idClient : number = Number.parseInt(_req.body["idClient"]);
    

    if(!idClient){
        missingFields.push("idClient");
    }

    if(missingFields.length > 0){
        _res.json({
            error : `missing required fields : [${missingFields.join(", ")}]`
        })
        return;
    }*/

    dbc.Annonce("1",1)
        .then(result=> _res.json({result:result}))
        .catch(error => _res.json({error:error})); 
});

APP.post("/annonces/user", (_req : express.Request, _res : express.Response) => {
    console.log("POST /annonces/user");
    let 
    missingFields : string[] = [],
    idClient : number = Number.parseInt(_req.body["idClient"]);
    

    if(!idClient){
        missingFields.push("idClient");
    }

    if(missingFields.length > 0){
        _res.json({
            error : `missing required fields : [${missingFields.join(", ")}]`
        })
        return;
    }

    dbc.Annonce("idClient",idClient)
        .then(result=> _res.json({result:result}))
        .catch(error => _res.json({error:error})); 
});



APP.post("/annonce/add", (_req : express.Request, _res : express.Response) => {
    console.log("POST /annonce/add");
    let 
        missingFields : string[] = [],
        annonce : Annonce = {
        idAnnonce: null,
        idClient:Number.parseInt(_req.body["idClient"]),
        titre:_req.body["titre"],
        description:_req.body["description"],
        typeVehicule:_req.body["typeVehicule"],
        marqueVehicule:_req.body["marqueVehicule"],
        modeleVehicule:_req.body["modeleVehicule"],
        couleurVehicule:_req.body["couleurVehicule"],
        transmissionVehicule:_req.body["transmissionVehicule"],
        kilometrageVehicule:_req.body["kilometrageVehicule"],
        anneeVehicule:Number.parseInt(_req.body["anneeVehicule"]),
        moteurVehicule:_req.body["moteurVehicule"],
        energieVehicule:_req.body["energieVehicule"],
        prixVehicule:_req.body["prixVehicule"]
    };

    

    if(!annonce.idClient){
        missingFields.push("idClient");
    }
    if(!annonce.titre){
        missingFields.push("titre");
    }
    if(!annonce.description){
        missingFields.push("description");
    }
    if(!annonce.typeVehicule){
        missingFields.push("typeVehicule");
    }
    if(!annonce.marqueVehicule){
        missingFields.push("marqueVehicule");
    }
    if(!annonce.modeleVehicule){
        missingFields.push("modeleVehicule");
    }    
    if(!annonce.couleurVehicule){
        missingFields.push("couleurVehicule");
    }
    if(!annonce.transmissionVehicule){
        missingFields.push("transmissionVehicule");
    }

    if(!annonce.kilometrageVehicule){
        missingFields.push("kilometrageVehicule");
    }
    if(!annonce.anneeVehicule){
        missingFields.push("anneeVehicule");
    }
    if(!annonce.moteurVehicule){
        missingFields.push("moteurVehicule");
    }    
    if(!annonce.energieVehicule){
        missingFields.push("energieVehicule");
    }
    if(!annonce.prixVehicule){
        missingFields.push("prixVehicule");
    }


    if(missingFields.length > 0){
        _res.json({
            error : `missing required fields : [${missingFields.join(", ")}]`
        })
        return;
    }

    dbc.AddAnnonce(annonce)
        .then((result:any)=> _res.json({result:result.insertId}))
        .catch(error => _res.json({error:error})); 
});

APP.post("/annonce/remove", (_req : express.Request, _res : express.Response) => {
    console.log("POST /annonce/remove");
    let 
    missingFields : string[] = [],
    idAnnonce : number = Number.parseInt(_req.body["idAnnonce"]);
    

    if(!idAnnonce){
        missingFields.push("idAnnonce");
    }

    if(missingFields.length > 0){
        _res.json({
            error : `missing required fields : [${missingFields.join(", ")}]`
        })
        return;
    }

    dbc.DelAnnonce(idAnnonce)
        .then(result=> _res.json({result:result}))
        .catch(error => _res.json({error:error})); 
});


const server = http.createServer(APP);

server.listen(PORT, ()=>{
    console.info("Server listening on Port : 5000");
})

const socketServer = new Server(server);

socketServer.on("connect", (socket : Socket ) =>{
    connection(socket)
    socket.on("init", (id : number) => init(id, socket) );
    socket.on("client-message", (msg : string) => ClientMessage(msg, socket));
    socket.on("message", (msg : Message) => message(msg, socket));  
    socket.on("message-received", (id : number) => message_received(id));
    socket.on("disconnect", () => disconnection(socket) );
});

/*  upload/download image javascript
app.post("/upload/image", (req, res) => {

    if(req.body["userID"] === undefined){
        let message = " missing parameter : userID"
        console.error(message);
        res.json(
            {error : message}
        );
        return;
    }
    if(req.body["imgB64"] === undefined){
        let message = "missing parameter : imgB64"
        console.error(message);
        res.json(
            {error : message}
        );
        return;
    }
    if(req.body["format"] === undefined){
        let message = "missing parameter : format"
        console.error(message);
        res.json(
            {error : message}
        );
        return;
    }
    console.log("image is being uploaded...");

    let 
        imgB64 = req.body["imgB64"],
        userID = req.body["userID"],
        format = req.body["format"];

    const buffer = Buffer.from(imgB64, 'base64');

    fs.writeFile(`images/Avatar-[${userID}].${format}`, buffer, (err) => {
        if (err) {
            console.error(err);
            res.send('Error saving image');
        } else {
            console.log("image uploaded.");
            res.send('Image saved successfully');
        }
    });
})


app.post("/download/image", (req, res) => {

    if(req.body["imgName"] === undefined){
        let message = "missing parameter : imgName"
        console.error(message);
        res.json(
            {error : message}
        );
        return;
    }
    console.log("sending image...");

    let 
        name = req.body["imgName"];

    const image = fs.readFile(`images/${name}`,(err, data) => {
        if(err) {
            console.log("image not found.");
            res.json({error: "image not found."});
        }
        else{
            const buffer = Buffer.from(data).toString("base64");
            res.json({imgB64 : buffer});
            console.log("image sent.");
        }
    });
}) 
*/