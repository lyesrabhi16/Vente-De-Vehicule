import { Connection, MysqlError, Query, createConnection} from "mysql";
import { DB_HOST, DB_NAME, DB_PASSWORD, DB_USERNAME } from "./configs";
import argon2  from "argon2";

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

    signup = <T>(user : {
                            nomClient : string, 
                            prenomClient: string,
                            ageClient : string,
                            email : string,
                            numTel : string,
                            password : string
                        }
                ) : Promise<T> => {
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
                            let pass = result[0]["password"];
                            if (await argon2.verify(pass, password)){
                                delete result[0].password;
                                resolve(result);
                            }
                            else{
                                reject("Invalid password.");
                            }
                            
                        }
                    )
                    .catch((err) => reject(err))
                
            
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
    }

}

