const express = require('express')
const router = express.Router()
const bcrypt = require("bcrypt");
const TABLE_NAME = "public.user"
const INSERT_QUERY = `insert into ${TABLE_NAME} (username,password,email) VALUES ($1,$2,$3) RETURNING id` 
const db = require("../db")

router.post("/",async (req,response)=>{
    console.log("tu sam")
    const newUser = req.body;
    if(newUser){
        const {username ,password,email} = req.body;
        if(username && password && email){
            try{
                const hashedPassword = await bcrypt.hash(newUser.password,10);
                console.log(hashedPassword);
                const res = await db.query(INSERT_QUERY,[username,hashedPassword,email])
                response.status(201).send(res.rows)
            }
            catch(e){
                console.log(e);
                response.status(404).send()
            }
        }
    }
})
module.exports = router;