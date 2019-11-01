var express = require("express");

const destinationDir = ".";

var app = express();
app.use( express.static(destinationDir)); //Serves resources from public folder
var server = app.listen(5000);
console.log("Server started in port 5000");