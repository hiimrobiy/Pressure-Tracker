const express = require('express')
const router = express.Router()
const TABLE_INPUTS = "public.inputs"
const TABLE_BLOOD_PRESSURE = "public.bloodpressureinput"
const TABLE_BLOOD_SUGAR = "public.glucoselevelinput"
const TABLE_DESCRIPTION = "public.description"
const types=["bloodpressureinput","glucoselevelinput"]
const INSERT_INPUT_QUERY = `INSERT into ${TABLE_INPUTS} (user_id,date) VALUES ( $1,$2 ) RETURNING id`
const DELETE_INPUT_QUERY = `DELETE from ${TABLE_INPUTS} where id = $1 `
const INSERT_BLOOD_PRESSURE_QUERY = `INSERT into ${TABLE_BLOOD_PRESSURE} (systolic,diastolic,heart_rate,super_class_id) VALUES ($1,$2,$3,$4 )`
const INSERT_BLOOD_SUGAR_QUERY = `INSERT into ${TABLE_BLOOD_SUGAR} (levels,super_class_id) VALUES ($1,$2)`
const INPUT_DESCRIPTION_QUERY =`insert into ${TABLE_DESCRIPTION} (feelings,meals,pain_description,activity,stress_level,input_id) VALUES ($1,$2,$3,$4,$5,$6)RETURNING id`
const db = require("../db")


router.get("/view/:type",async (req,res)=>{
    const {user_id} = req.user;
    const {type} = req.params;
    if(!user_id|| (type!=types[0] && type!=types[1])) return res.status(400).send();
    try {
        const queryPressure =  `select 	i.id as inputId,
                                systolic,
                                diastolic,
                                heart_rate as heartRate,
                                date,
                                d.id as descriptionId,
                                feelings,
                                meals,
                                pain_description as painDescription,
                                activity as activityDescription,
                                stress_level as stressLevel
                                from ${TABLE_INPUTS} as i 
                                JOIN bloodpressureinput as t 
                                ON i.id=t.super_class_id 
                                LEFT JOIN ${TABLE_DESCRIPTION} as d 
                                ON i.id=d.input_id
                                WHERE i.user_id = $1`
        const querySugarLevel = `select i.id as inputId,
                                levels,
                                date,
                                d.id as descriptionId,
                                feelings,
                                meals,
                                pain_description as painDescription,
                                activity as activityDescription,
                                stress_level as stressLevel
                                from ${TABLE_INPUTS} as i 
                                JOIN glucoselevelinput as t 
                                ON i.id=t.super_class_id 
                                LEFT JOIN ${TABLE_DESCRIPTION} as d 
                                ON i.id=d.input_id
                                WHERE i.user_id = $1`
        const result = await db.query(type==types[0] ?  queryPressure:querySugarLevel,[user_id]);
        res.status(200).json(result.rows);
    } catch (error) {
        console.log(error);
        res.sendStatus(500);
        
    }

});
router.post("/add/:type",async (req,response)=>{
    const {type} = req.params;
    const {user_id} =req.user
 
    if((type===types[0] || type==types[1]) && user_id){
        try{
            const client = await db.connect();
            try{
                await client.query("BEGIN");
                const {date} = req.body;
                const result = await client.query(INSERT_INPUT_QUERY,[user_id,date]);
                if(!result || !result.rows || !result.rows[0] || !result.rows[0].id) throw "Invalid data"
                const super_class_id = result.rows[0].id;
                if(type==types[0]){
                    const{systolic,diastolic,heartRate} = req.body;
                    if(!systolic || !diastolic || !heartRate) throw "Invalid data"
                        const result = await client.query(INSERT_BLOOD_PRESSURE_QUERY,[systolic,diastolic,heartRate,super_class_id])
                        if(req.body.description){ 
                            const {feelings,meals,pain,activity,stress} = req.body.description;
                            await client.query(INPUT_DESCRIPTION_QUERY,[feelings,meals,pain,activity,stress,super_class_id]);
                        }
                        await client.query("COMMIT")
                        response.status(201).json(super_class_id.toString())
                    }
                else if(type==types[1]){
                    const{level} = req.body;
                    if(!level) throw "Invalid data"
                        const result = await client.query(INSERT_BLOOD_SUGAR_QUERY,[level,super_class_id])
                        if(req.body.description){ 
                            const {feelings,meals,pain,activity,stress} = req.body.description;
                            await client.query(INPUT_DESCRIPTION_QUERY,[feelings,meals,pain,activity,stress,super_class_id]);
                        }
                        await client.query("COMMIT")
                        response.status(201).json(super_class_id.toString())
                    }
            }
            catch(e){
                await client.query('ROLLBACK')
                throw e
            }
            finally{
                client.release()    
            }
                
        }
        catch(e){
            console.log(e)
            response.status(400).send()
        }
}
});

router.delete("/delete/:input_id",async (req,res)=>{
    const {input_id} = req.params;
    if(!input_id) return res.status(400).send();
    try {
       await db.query(DELETE_INPUT_QUERY,[input_id]);
       res.status(200).send(input_id.toString());
    } 
    catch (error) {
        console.log(error)
         res.status(500).send(input_id.toString());
    }
});


module.exports = router;