import { DBC } from "./DBConnection";
import express from 'express';
import cors from 'cors';
import { sign } from "crypto";

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
    _res.send("hello from Server");
});

APP.post("/user",
    async (_req : express.Request, _res : express.Response)=>{
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
            .catch(error => _res.json({error : error }))

});

APP.post("/search", (_req : express.Request , _res : express.Response) => {
    

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
        

    
})

APP.listen(PORT, ()=>{
    console.info("Server listening on Port : 5000");
});

