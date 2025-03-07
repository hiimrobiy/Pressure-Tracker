const express = require("express");
const router = express.Router();

const TABLE_USER = "public.user"
const GET_PASSWORD_FOR_USERNAME_QUERY =`select * from ${TABLE_USER} as u where u.username = $1`
const INSERT_NEW_USER_QUERY = `insert into ${TABLE_USER} (username,password,email) VALUES ($1,$2,$3) RETURNING id` 
const db = require("../db");
const bcrypt = require("bcryptjs")
const jwt = require("jsonwebtoken")


router.use(express.json());

router.post("/login",async (req,res)=>{
    console.log("Req")
    const{username,password} = req.body;
    try {
        if(!username || !password) throw "Invalid data"
        const result = await db.query(GET_PASSWORD_FOR_USERNAME_QUERY,[username])
       
        if(!result || !result.rows ) return res.sendStatus(500);
        if(result.rowCount==0) return res.send(400);
        const hashedPassword = result.rows[0].password
        const id = result.rows[0].id;
        if(await bcrypt.compare(password,hashedPassword) ){
            const user={username:username,user_id:id}
            const accessToken=jwt.sign(user,"fze8o1u4IQ6AYfZlXewu",{expiresIn: "15m" })
            const refreshToken=jwt.sign(user,"koFitvEL47MF7y8FhvjR",{expiresIn: "300m" })
            res.status(200).json({accessToken:accessToken,refreshToken:refreshToken})
        }
        else
            res.status(400).send()
    } 
    catch (e) {
        console.log(e)
        res.status(500).send()
    }
});
router.post("/register",async (req,res)=>{
    const newUser = req.body;
    if(newUser){
        const {username ,password,email} = req.body;
        if(username && password && email){
            try{
                const hashedPassword = await bcrypt.hash(newUser.password,10);
                const result = await db.query(INSERT_NEW_USER_QUERY,[username,hashedPassword,email])
                if(!result || !result.rows)return res.sendStatus(500)  
                if(result.rowCount==0) return res.sendStatus(400);
                const user={username:username,user_id:result.rows[0].id}
                const accessToken=jwt.sign(user,"fze8o1u4IQ6AYfZlXewu",{expiresIn: "15m" })
                const refreshToken=jwt.sign(user,"koFitvEL47MF7y8FhvjR",{expiresIn: "300m" })
                res.status(200).json({accessToken:accessToken,refreshToken:refreshToken})
            }
            catch(e){
                console.log(e);
                res.sendStatus(400)
            }
        }
    }
    else
        res.status(400).send()

})

router.post("/verifyAccessToken",async (req,res)=>{
    const{accessToken,refreshToken} = req.body;
    try {
        if(accessToken){
            jwt.verify(accessToken,"fze8o1u4IQ6AYfZlXewu",(err,result)=>{
                if(!err) return res.status(201).json({accessToken:jwt.sign({username:result.username,user_id:result.user_id},"fze8o1u4IQ6AYfZlXewu",{expiresIn: "15m" }),refreshToken:refreshToken});
                if(refreshToken==null) return res.status(403).json({accessToken:accessToken,refreshToken:refreshToken});
                jwt.verify(refreshToken,"koFitvEL47MF7y8FhvjR",(err,result)=>{
                    if(!err) {
                        const accessToken = jwt.sign({username:result.username,user_id:result.user_id},"fze8o1u4IQ6AYfZlXewu",{expiresIn: "15m" })
                        return res.status(201).json({accessToken:accessToken,refreshToken:refreshToken})
                    }
                    res.status(403).json({accessToken:accessToken,refreshToken:refreshToken});
                })
            })
        }
        else if(refreshToken){
             jwt.verify(refreshToken,"koFitvEL47MF7y8FhvjR",(err,result)=>{
                    if(!err) {
                        const accessToken = jwt.sign({username:result.username,user_id:result.user_id},"fze8o1u4IQ6AYfZlXewu",{expiresIn: "15m" })
                        return res.status(201).json({accessToken:accessToken,refreshToken:refreshToken})
                    }
                     res.status(403).json({accessToken:accessToken,refreshToken:refreshToken});
                })
        }
        else
             res.status(403).json({accessToken:accessToken,refreshToken:refreshToken});
    } 
    catch (error) {
         res.status(403).json({accessToken:accessToken,refreshToken:refreshToken});
    }
});

module.exports = router;