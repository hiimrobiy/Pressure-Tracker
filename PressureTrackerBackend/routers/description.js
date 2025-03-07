const express = require('express')
const router = express.Router()
const TABLE_DESCRIPTION = "public.description"
const INPUT_DESCRIPTION_QUERY =`insert into ${TABLE_DESCRIPTION} (feelings,meals,pain_description,activity,stress_level,input_id) VALUES ($1,$2,$3,$4,$5,$6)RETURNING id`

const db = require("../db")
router.post("/add",async(req,res)=>{
    const {input_id} = req.body;
    if(input_id){
        try{
            const {feelings,meals,pain,activity,stress} = req.body;
            const result = await db.query(INPUT_DESCRIPTION_QUERY,[feelings,meals,pain,activity,stress,input_id]);
            if(!result || !result.rows || !result.rows[0] || !result.rows[0].id) throw "Invalid data"
            res.send("ok");
        }
        catch(e){
            console.log(e);
            res.status(404).send()
        }
    }
    else
        res.status(404).send()
});

module.exports = router;