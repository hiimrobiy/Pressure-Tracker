const express = require('express')
const router = express.Router()
const TABLE_REMINDER = "public.reminder"
const TABLE_USER = "public.user"
const INSERT_REMINDER_QUERY = `insert into ${TABLE_REMINDER} (medicine_name,hours,minutes,days,user_id) VALUES ($1,$2,$3,$4,$5) RETURNING id`
const DELETE_REMINDER_QUERY = `delete from ${TABLE_REMINDER}  where id = $1` 
const GET_REMINDERS_FOR_USER_QUERY = `select id as reminderid,
                                            medicine_name as medicinename,
                                            hours,
                                            minutes,
                                            days
                                        from ${TABLE_REMINDER} as r 
                                        where r.user_id=$1`  
const FIND_USER_QUERY = `select * from ${TABLE_USER} where id = $1` 
const db = require("../db")

router.post("/add",async (req,res)=>{
    const{medicinename,hours,minutes,days} = req.body;
    const {user_id} = req.user;
    if(medicinename && hours && minutes && days && user_id){
        try {
             const result = await db.query(INSERT_REMINDER_QUERY,[medicinename,hours,minutes,days,user_id])
             if(!result || !result.rows || !result.rows[0] || !result.rows[0].id) throw "Invalid data"
             res.status(201).send(result.rows[0].id.toString())
            } 
        catch (e) {
            console.log(e);
            res.sendStatus(500);
        } 
    }
     else
        res.sendStatus(400);
    })

router.delete("/delete/:reminder_id",async (req,response)=>{
    const {reminder_id} = req.params;
    if(!reminder_id) return response.sendStatus(400);
    try {
        const result = await db.query(DELETE_REMINDER_QUERY,[reminder_id])
        response.status(200).send(reminder_id);
        } 
    catch (error) {
        response.sendStatus(500);
        }
    })
router.get("/getReminders",async (req,res)=>{
       const {user_id} = req.user;
        if(!user_id) return res.sendStatus(400);
        try {
            const result = await db.query(GET_REMINDERS_FOR_USER_QUERY,[user_id])
            res.status(200).send(result.rows)
            } 
        catch (error) {
            res.sendStatus(500);
        }
});
module.exports = router;