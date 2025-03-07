const {Pool} = require("pg");
const pool = new Pool({
    user:"postgres",
    password:"bazepodataka",
    host:"localhost",
    port:5432,
    database:"PressureTracker"
});
module.exports={
    query : (text,params)=> pool.query(text,params),
    connect: ()=>pool.connect()
    }
  
    
