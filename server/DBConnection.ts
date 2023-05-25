import { Connection, MysqlError, Query, createConnection,format} from "mysql";
import { DB_HOST, DB_NAME, DB_PASSWORD, DB_USERNAME } from "./configs";
import argon2  from "argon2";
import { Annonce, Message, Reservation, User, RendezVous } from './interfaces';
import { json } from "express";

export class DBC {
    constructor() {
        this.connect();
    }
    cnx : Connection | null = null;
    connect = () =>
    {
        try {
            this.cnx = createConnection({
                host     : DB_HOST,
                database : DB_NAME,
                user     : DB_USERNAME,
                password : DB_PASSWORD
            });
            this.cnx.connect(
                (error : MysqlError) => {
                    if (error){

                        this.cnx = null;
                        console.log(`[DBConnection][connect][database error] : ${error.sqlMessage}`);
                        console.log(`DB NOT connected!`);
                    
                    }
                    else{
                        console.log("DB connected.");
                    }
                }
            );
            
        } 
        catch (error : any) {
            this.cnx = null;
            console.log(`[DBConnection][connect][error] : ${error.sqlMessage}`);
            console.log(`DB NOT connected!`);

        }

    };

    

    execute = <T>(query : string, params : string[] | Object) : Promise<T> =>{
        return new Promise<T>((resolve, reject) =>
            {

                if (!this.cnx){
                    console.log("[DBConnection][execute][error] : DB connection required");
                    reject("DB is not connected");
                }
                else
                {
                    this.cnx.query(
                            query,
                            params,
                            (err, res) => {
                                if(err){
                                    console.log(`[DBConnection][execute][error] : ${err.message}`);
                                    reject(err);
                                }
                                else{
                                    resolve(res);
                                }
                            }
                        )
                }
            }
        )
    }

    getUser =  async (id : string) : Promise<Object> => {
        let sql : string = "SELECT idClient, nomClient, prenomClient, ageClient, email, numTel FROM client WHERE idClient = ?";
        let result : Object = await this.execute(sql, [id]);
        return result;
        
    }

    signup = <T>(user: User) : Promise<T> => {
                    return new Promise<T>(
                        async (resolve, reject) => {
                                let sql : string = `insert into client set ?`;
                                try{
                                    user.password = await argon2.hash(user.password);
                                    this.execute(sql,user)
                                            .then((result:any) => resolve(result) )
                                            .catch(err => reject(err));
                                    }
                                catch(err:any) {
                                    console.log("[DBConnection][signup][Password Hash][error] : "+err.message);
                                    reject("failed to hash password.");
                                }
                    })
                }

    signin = <T>(email : string|null, numTel : string|null, password : string) : Promise<T> => {
        return new Promise<T>((resolve, reject) => {
            let ident :string = "",
                col   :string = "";
            if (email) {
                col = "email";
                ident = email;
            }
            else if (numTel) {
                col = "numTel";
                ident = numTel
            }
            else {
                reject("email/numTel not found")
            }
            
            let
                sql : string = `
                    select *
                    from client
                    where ${col} = '${ident}'
                `.replace(/\n/g, " ");

            this.execute(sql, {})
                    .then(
                        async (result : any) => {
                            try {
                                let pass = result[0]["password"];    
                                if (await argon2.verify(pass, password)){
                                    console.log(result[0]);
                                    delete result[0]["password"];
                                    resolve(result);
                                }
                                else{
                                    reject("Invalid password.");
                                }                            
                            } catch (error) {
                                reject("invalid email/phone number");
                            }
                        }
                    )
                    .catch((err) => {
                        console.log(err);
                        reject(err);
                    })
                
            
        })
    }

    search = <T>(table : string, cols : string[], terms : string[]) : Promise<T>=>{
        return new Promise<T>((resolve, reject) =>{
            let sql : string = `
                                select ${cols} 
                                from ${table} 
                                where
                               `

            this.execute(`DESCRIBE ${table}`,{})
                .then(
                    (tableDesc : any) => {

                            terms.forEach(term => {
                                tableDesc.forEach((element : {Field : string}) => {
                                    if (element.Field != "password")
                                        sql = sql + ` (CONVERT(${element.Field} USING utf8)) LIKE '%${term}%' OR `;
                                });
                                sql = sql.slice(0, sql.lastIndexOf("OR"));
                                sql = sql + " AND "
                            })
                    
                            sql = sql.slice(0, sql.lastIndexOf("AND"));
                            sql = sql.replace(/\n/g, "");
                            console.log(sql);
                            this.execute(sql, [cols, table])
                                                .then(
                                                    (response : any) => {
                                                        resolve(response);
                                                    }
                                                )
                                                .catch(
                                                    (error : any) => {
                                                        reject(error);
                                                    }
                                                ); 

                
                    }
                )
                .catch(
                    (error : any) => {
                        reject(error);
                    }
                );
        });
    };

    chats = <T>(id : number) : Promise<T> =>{
        return new Promise((resolve, reject) =>{
            let sql : string = `SELECT 
                                idClient, nomClient, prenomClient,
                                r.*
                                FROM client,
                                    (
                                        SELECT * 
                                        FROM message 
                                        WHERE
                                            idClient_sender = '${id}'
                                            OR
                                            idClient_reciever = '${id}'
                                    ) as r
                                WHERE idClient = r.idClient_sender OR idClient = r.idClient_reciever
                                ORDER BY message.date DESC, idMessage DESC
                `;
            this.execute(sql, [])
                .then((result:any) =>{
                    resolve(result);
                })
                .catch((err) =>{reject(err)});

        })
    };

    messages = <T>(id1 : number, id2 : number) : Promise<T> => {
        return new Promise((resolve, reject) =>{
            let sql : string = `SELECT * from message where (idClient_sender = ${id1} and idClient_reciever = ${id2}) or (idClient_sender = ${id2} and idClient_reciever = ${id1}) ORDER BY date ASC, idMessage ASC`;
            this.execute(sql , [])
                    .then((result : any) => resolve(result))
                    .catch(err => reject(err)); 
        });
    };

    AddMessage = <T>(msg : Message) : Promise<T> => {
        return new Promise<T>((resolve, reject) =>{
            let sql : string = "insert into message set ?";
            this.execute(sql, msg)
                .then((result:any) => resolve(result))
                .catch(err=> reject(err));
        })
    };

    Annonce = <T>(field : string, idAnnonce : number) : Promise<T> => {
        return new Promise((resolve, reject) => {
            let 
                sql : string = `SELECT * from annonce where ?? = ?`;
            
            this.execute(sql, [field , idAnnonce])
                    .then((res:any) => {resolve(res)})
                    .catch(err => reject(err));
        });
    };

    Annonces = <T>(set : object) : Promise<T> => {
        return new Promise((resolve, reject) => {
            let 
                sql : string = `SELECT * from annonce where `+ JSON.stringify(set).replace("{","").replace("}","").replaceAll(":"," = ").replaceAll(","," AND ").replace(/"(\w+)"\s*=/g, '\$1 =');
            this.execute(sql, [])
                    .then((res:any) => {
                        console.error(res);
                        resolve(res)})
                    .catch((err:any) => {
                        reject(err)});
        });
    };

    AddAnnonce = <T>(annonce : Annonce) : Promise<T> => {
        return new Promise((resolve, reject) => {
            let sql : string = `INSERT INTO annonce SET ?`;
            this.execute(sql, annonce)
                    .then((res:any) => {resolve(res)})
                    .catch(err => reject(err));
        });
    };
    DelAnnonce = <T>(idAnnonce : number) : Promise<T> => {
        return new Promise((resolve, reject) => {
            let sql : string = `Delete From annonce where idAnnonce = ?`;
            this.execute(sql, idAnnonce)
                    .then((res:any) => {resolve(res)})
                    .catch(err => reject(err));
        });
    };

    Reservation = <T>(idClient : number, idAnnonce : number) : Promise<T> => {
        return new Promise((resolve, reject) => {
            let 
                sql : string = `SELECT * from reservation where idClient = ? and idAnnonce = ?`;
            
            this.execute(sql, [idClient , idAnnonce])
                    .then((res:any) => {resolve(res)})
                    .catch(err => reject(err));
        });
    };

    Reservations = <T>(set : object) : Promise<T> => {
        return new Promise((resolve, reject) => {
            let 
                sql : string = `SELECT * from reservation where `+ JSON.stringify(set).replace("{","").replace("}","").replaceAll(":"," = ").replaceAll(","," AND ").replace(/"(\w+)"\s*=/g, '\$1 =');
            this.execute(sql, [])
                    .then((res:any) => {
                        console.error(res);
                        resolve(res)})
                    .catch((err:any) => {
                        reject(err)});
        });
    };

    AddReservation = <T>(reservation : Reservation) : Promise<T> => {
        return new Promise((resolve, reject) => {
            let sql : string = `INSERT INTO reservation SET ?`;
            this.execute(sql, reservation)
                    .then((res:any) => {resolve(res)})
                    .catch(err => reject(err));
        });
    };
    DelReservation = <T>(set : object) : Promise<T> => {
        return new Promise((resolve, reject) => {
            let sql : string = `Delete From reservation where ` +JSON.stringify(set).replace("{","").replace("}","").replaceAll(":"," = ").replaceAll(","," AND ").replace(/"(\w+)"\s*=/g, '\$1 =');
            this.execute(sql, [])
                    .then((res:any) => {resolve(res)})
                    .catch(err => reject(err));
        });
    };

    RendezVous = <T>(idClient : number, idAnnonce : number) : Promise<T> => {
        return new Promise((resolve, reject) => {
            let 
                sql : string = `SELECT * from rendezvous where idClient = ? and idAnnonce = ?`;
            
            this.execute(sql, [idClient , idAnnonce])
                    .then((res:any) => {resolve(res)})
                    .catch(err => reject(err));
        });
    };

    AllRendezVous = <T>(set : object) : Promise<T> => {
        return new Promise((resolve, reject) => {
            let 
                sql : string = `SELECT * from rendezvous where `+ JSON.stringify(set).replace("{","").replace("}","").replaceAll(":"," = ").replaceAll(","," AND ").replace(/"(\w+)"\s*=/g, '\$1 =');
            console.log("sql : "+sql);
            
            this.execute(sql, [])
                    .then((res:any) => {
                        console.error(res);
                        resolve(res)})
                    .catch((err:any) => {
                        reject(err)});
        });
    };

    AddRendezVous = <T>(rendezVous : RendezVous) : Promise<T> => {
        return new Promise((resolve, reject) => {
            let sql : string = `INSERT INTO rendezvous SET ?`;
            this.execute(sql, rendezVous)
                    .then((res:any) => {resolve(res)})
                    .catch(err => reject(err));
        });
    };
    DelRendezVous = <T>(set : object) : Promise<T> => {
        return new Promise((resolve, reject) => {
            let sql : string = `Delete From rendezvous where ` +JSON.stringify(set).replace("{","").replace("}","").replaceAll(":"," = ").replaceAll(","," AND ").replace(/"(\w+)"\s*=/g, '\$1 =');
            this.execute(sql, [])
                    .then((res:any) => {resolve(res)})
                    .catch(err => reject(err));
        });
    };

}
