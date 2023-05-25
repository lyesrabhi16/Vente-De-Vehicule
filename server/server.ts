import { DBC } from "./DBConnection";
import express from 'express';
import fs from "fs";
import cors from 'cors';
import { ClientMessage, clients, connection, disconnection, init, message, message_received } from "./socket";
import { Server, Socket } from "socket.io";
import http from "http";
import { Annonce, Message, RendezVous, Reservation } from "./interfaces";
import path from "path";
/* DB connection */

const dbc : DBC  = new DBC();


/* Express Server */
const APP : express.Application = express();
const PORT : number = 5000;


// parse incoming request body and append data to `req.body`
APP.use(express.json({limit:"200mb"}));
APP.use(express.urlencoded({ extended: true, limit:"200mb" }));

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
    let missingFields : string[] = [],
        filterObj : Object;
    try {
        filterObj = JSON.parse(_req.body["filterObj"]);
    } catch (error) {
        filterObj = "1 = 1";   
    }


    dbc.Annonces(filterObj)
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

APP.post("/reservations", (_req : express.Request, _res : express.Response) => {
    console.log("POST /reservations");
    let 
    missingFields : string[] = [],
    filter : Object;
    try {
        filter = JSON.parse(_req.body["filterObj"]);
    } catch (error) {
        filter = "1 = 1";   
    }
    
    if(JSON.stringify(filter) === JSON.stringify({})){
        filter = { "1" : 1};
    }

    if(!filter){
        missingFields.push("FilterObj");
    }

    if(missingFields.length > 0){
        _res.json({
            error : `missing required fields : [${missingFields.join(", ")}]`
        })
        return;
    }

    dbc.Reservations(filter)
        .then(result=> _res.json({result:result}))
        .catch(error => _res.json({error:error})); 
});
APP.post("/reservation", (_req : express.Request, _res : express.Response) => {
    console.log("POST /reservation");
    let 
    missingFields : string[] = [],
    idAnnonce : number = Number.parseInt(_req.body["idAnnonce"]),
    idClient  : number = Number.parseInt(_req.body["idClient"]);
    

    if(!idAnnonce){
        missingFields.push("idAnnonce");
    }

    if(!idClient){
        missingFields.push("idClient");
    }
    if(missingFields.length > 0){
        _res.json({
            error : `missing required fields : [${missingFields.join(", ")}]`
        })
        return;
    }

    dbc.Reservation(idClient, idAnnonce)
        .then(result=> _res.json({result:result}))
        .catch(error => _res.json({error:error})); 
});

APP.post("/reservation/add", (_req : express.Request, _res : express.Response) => {
    console.log("POST /reservation/add");
    let 
    missingFields : string[] = [],
    idAnnonce : number = Number.parseInt(_req.body["idAnnonce"]),
    idClient  : number = Number.parseInt(_req.body["idClient"]),
    dateDebut : string = _req.body["dateDebut"],
    dateFin : string = _req.body["dateFin"],
    lieuReservation : string = _req.body["lieuReservation"],
    etatReservation : string = _req.body["etatReservation"];

    if(!idAnnonce){
        missingFields.push("idAnnonce");
    }
    if(!idClient){
        missingFields.push("idClient");
    }
    if(!dateDebut){
        missingFields.push("dateDebut");
    }
    if(!dateFin){
        missingFields.push("dateFin");
    }
    if(!lieuReservation){
        missingFields.push("lieuReservation");
    }
    if(!etatReservation){
        etatReservation = "Pending";
    }

    if(missingFields.length > 0){
        _res.json({
            error : `missing required fields : [${missingFields.join(", ")}]`
        })
        return;
    }

    let reservation : Reservation = {
        idClient: idClient,
        idAnnonce: idAnnonce,
        dateDebut: dateDebut,
        dateFin: dateFin,
        lieuReservation: lieuReservation,
        etatReservation: etatReservation
    }

    dbc.AddReservation(reservation)
        .then(result=> _res.json({result:result}))
        .catch(error => _res.json({error:error})); 
});
APP.post("/reservation/remove", (_req : express.Request, _res : express.Response) => {
    console.log("POST /reservation/remove");
    let 
    missingFields : string[] = [],
    idAnnonce : number = Number.parseInt(_req.body["idAnnonce"]),
    idClient  : number = Number.parseInt(_req.body["idClient"]);
    

    if(!idAnnonce){
        missingFields.push("idAnnonce");
    }

    if(!idClient){
        missingFields.push("idClient");
    }
    if(missingFields.length > 0){
        _res.json({
            error : `missing required fields : [${missingFields.join(", ")}]`
        })
        return;
    }

    dbc.DelReservation({idAnnonce : idAnnonce, idClient : idClient})
        .then(result=> _res.json({result:result}))
        .catch(error => _res.json({error:error})); 
});


APP.post("/rendezvous/all", (_req : express.Request, _res : express.Response) => {
    console.log("POST /rendezvous/all");
    let 
    missingFields : string[] = [],
    filter : Object;
    try {
        filter = JSON.parse(_req.body["filterObj"]);
    } catch (error) {
        filter = { "1" : 1 };   
    }
    if(JSON.stringify(filter) === JSON.stringify({})){
        filter = { "1" : 1};
    }
    
    

    if(!filter){
        missingFields.push("FilterObj");
    }

    if(missingFields.length > 0){
        _res.json({
            error : `missing required fields : [${missingFields.join(", ")}]`
        })
        return;
    }

    dbc.AllRendezVous(filter)
        .then(result=> {
            console.log("result: "+ result);
            _res.json({result:result})})
        .catch(error => {
            console.log("error: "+ error);
            _res.json({error:error})}); 
});
APP.post("/rendezvous", (_req : express.Request, _res : express.Response) => {
    console.log("POST /rendezvous");
    let 
    missingFields : string[] = [],
    idAnnonce : number = Number.parseInt(_req.body["idAnnonce"]),
    idClient  : number = Number.parseInt(_req.body["idClient"]);
    

    if(!idAnnonce){
        missingFields.push("idAnnonce");
    }

    if(!idClient){
        missingFields.push("idClient");
    }
    if(missingFields.length > 0){
        _res.json({
            error : `missing required fields : [${missingFields.join(", ")}]`
        })
        return;
    }

    dbc.RendezVous(idClient,idAnnonce)
        .then(result=> _res.json({result:result}))
        .catch(error => _res.json({error:error})); 
});

APP.post("/rendezvous/add", (_req : express.Request, _res : express.Response) => {
    console.log("POST /rendezvous/add");
    let 
    missingFields : string[] = [],
    idAnnonce : number = Number.parseInt(_req.body["idAnnonce"]),
    idClient  : number = Number.parseInt(_req.body["idClient"]),
    dateRendezVous : string = _req.body["dateRendezVous"],
    lieuRendezVous : string = _req.body["lieuRendezVous"],
    etatRendezVous : string = _req.body["etatRendezVous"];

    if(!idAnnonce){
        missingFields.push("idAnnonce");
    }
    if(!idClient){
        missingFields.push("idClient");
    }

    if(!dateRendezVous){
        missingFields.push("dateRendezVous");
    }
    if(!lieuRendezVous){
        missingFields.push("lieuRendezVous");
    }
    if(!etatRendezVous){
        etatRendezVous = "Pending";
    }

    if(missingFields.length > 0){
        _res.json({
            error : `missing required fields : [${missingFields.join(", ")}]`
        })
        return;
    }

    let rendezvous : RendezVous = {
        idClient: idClient,
        idAnnonce: idAnnonce,
        dateRendezVous: dateRendezVous,
        lieuRendezVous: lieuRendezVous,
        etatRendezVous: etatRendezVous
    }

    dbc.AddRendezVous(rendezvous)
        .then(result=> _res.json({result:result}))
        .catch(error => _res.json({error:error})); 
});
APP.post("/rendezvous/remove", (_req : express.Request, _res : express.Response) => {
    console.log("POST /rendezvous/remove");
    let 
    missingFields : string[] = [],
    idAnnonce : number = Number.parseInt(_req.body["idAnnonce"]),
    idClient  : number = Number.parseInt(_req.body["idClient"]);
    

    if(!idAnnonce){
        missingFields.push("idAnnonce");
    }

    if(!idClient){
        missingFields.push("idClient");
    }
    if(missingFields.length > 0){
        _res.json({
            error : `missing required fields : [${missingFields.join(", ")}]`
        })
        return;
    }

    dbc.DelRendezVous({idAnnonce : idAnnonce, idClient : idClient})
        .then(result=> _res.json({result:result}))
        .catch(error => _res.json({error:error})); 
});

APP.post("/upload/image", (_req : express.Request, _res : express.Response) => {
    console.log("POST /upload/image");
    
    let 
        missingFields : string[] = [],
        id : number = _req.body["userID"],
        imgB64 : string = _req.body["imgB64"],
        format : string = _req.body["format"],
        type : string = _req.body["type"];

    
    console.log("image is being uploaded...");

    if(!id){
        missingFields.push("id");
    } 
    if(!imgB64){
        missingFields.push("imgB64");
    } 
    if(!format){
        missingFields.push("format");
    } 
    if(!type){
        missingFields.push("type");
    } 

    if(missingFields.length > 0){
        _res.json({
            error : `missing required fields : [${missingFields.join(", ")}]`
        })
        return;
    }

    const buffer = Buffer.from(imgB64, 'base64');
    let dir : string = "unknown",
        fileprefix : string = "unknown";

    switch (type) {
        case "AVATAR":
            dir = "client";
            fileprefix = "imageClient";
            break;
        case "ANNONCE":
            dir = "annonce";
            fileprefix = "imageAnnonce";
            break;
        default:
            break;
    }

    fs.writeFile(`images/${dir}/${fileprefix}-[${id}].${format}`, buffer, (err) => {
        if (err) {
            console.error(err);
            _res.send('Error saving image');
        } else {
            console.log("image uploaded.");
            _res.send('Image saved successfully');
        }
    });
})

APP.post("/download/image", async (_req : express.Request, _res : express.Response) => {
    console.log("POST /download/image");
    
    if(_req.body["imgName"] === undefined){
        let message = "missing parameter : imgName"
        console.error(message);
        _res.json(
            {error : message}
        );
        return;
    }
    console.log("sending image...");
    
    let 
        name = _req.body["imgName"];

    await findFile("./images", name)
    .then((p) =>{
        if(p) {
            console.log(p);
            const image = fs.readFile(`${p}`,(err:any, data:any) => {
                if(err) {
                    console.log("image not found.");
                    _res.json({error: "image not found."});
                }
                else{
                    const buffer = Buffer.from(data).toString("base64");
                    _res.json({imgB64 : buffer});
                    console.log("image sent.");
                }
            });
        }
        else console.log("path not found");
    });
    
    
    
}) 

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


const findFile =  (dir:string, file:string) : Promise<string | null> => {
    return new Promise<string | null>((resolve, reject) =>
    {
        fs.readdir(dir, (err, data) => {
            if(err){
                console.log(err);
                reject(null);
            }
            
            for (const i in data){
                const item = data[i];
                const itemPath = path.join(dir, item);
                fs.stat(itemPath, (err, stat:fs.Stats) => {
                    if (err) {
                        console.log(err);
                        reject(null);
                    }
                    if(stat.isDirectory()){
    
                        findFile(itemPath, file).then(p =>{
                            if(p) resolve(p);
                        });                    
                    }
                    else{
                        if(stat.isFile() && item == file){
                            resolve(itemPath);
                        }
                    }
                })
            }
        })
    }
    )
    


};