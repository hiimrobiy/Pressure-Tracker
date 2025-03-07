const express = require("express");
const app = express();
const reminderRouter = require("./routers/reminder");
const inputsRouter = require("./routers/inputs");
const descriptionRemidner =require("./routers/description");
const authRouter =require("./routers/auth");
const jwt = require("jsonwebtoken")

app.use(express.json());

app.use("/auth",authRouter);

app.use(authenticateToken)
app.use("/reminder",reminderRouter);
app.use("/inputs",inputsRouter);
app.use("/description",descriptionRemidner);
app.listen(process.env.PORT || 3000,()=>console.log("Listening on port 3000"));

function authenticateToken(req,res,next){
    const authHeader = req.headers['authorization']
    const token = authHeader && authHeader.split(" ")[1];
    if(token==null) return res.sendStatus(401);
    jwt.verify(token,"fze8o1u4IQ6AYfZlXewu",(err,user)=>{
        if(err){ 
            console.log("isteko")
            return res.sendStatus(403)
        }
        req.user=user;
        next();
    })

}