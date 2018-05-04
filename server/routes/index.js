var express = require('express');
var MariasqlDB = require('mariasql')
var dateTime = require('node-datetime');


var gsu = '2000'
var spu = '1000'

var mariaclient = new MariasqlDB({
	host:'localhost',
    port:3306,
    user:'root',
    password:'root',
    db:'iot',
    charset:'utf8'
});

var router = express.Router()
var token = require('./token.js');

var fs = require('fs');
var createKeccakHash = require('keccak');
var crypto = require('crypto');
var nodeCmd = require('node-cmd'); 
var secp256k1 = require('secp256k1/elliptic');
var request = require('request');

router.get('/', function(req, res) {
	var hash = crypto.createHash('sha256').update('1').digest('base64');
	
	console.log(hash);
	res.send("ok");
})

router.post('/register', function(req, res) {
	var regdata = req.body.RegisterData
    var uid = regdata.split("_")[0]
	var upw = regdata.split("_")[1]
	var uemail = regdata.split("_")[2]
	var uname = regdata.split("_")[3]
	var rid = regdata.split("_")[4]
	//hash pwd
	var h_upw = crypto.createHash('sha256').update(upw).digest('base64');
	//creat sk pk
	var sk_buffer = crypto.randomBytes(32);
	var sk = sk_buffer.toString('hex');
	var pk_buffer = secp256k1.publicKeyCreate(sk_buffer, false).slice(1);
	var pk = pk_buffer.toString('hex');
	var addr = getAddress(pk_buffer);
	WriteKeyIntoTxt(sk);
	/* console.log("private Key: ",sk);
	console.log("Public Key: ",pk);
	console.log("Address: ",addr); */
	getChainAddr(function(B_addr) {
		B_addr = B_addr.substring(10,50);
		/* console.log("Blockchain Address: ",B_addr);
		console.log("Same Address? ", B_addr == addr); */
	}); 
	 
	//creat token 
	var sip = "140.119.164.149"
	var uip = res.connection.remoteAddress;
	var utoken = token.sign(uid,uip,null,sip);
	
    mariaclient.query('INSERT INTO iot.userlnfo SET USER_ID=?, USER_PASSWORD=?, USER_EMAIL=?, USER_NAME=?, ROLE=?, PUBLICKEY=?', [uid, h_upw, uemail, uname, rid, pk],function(err, rows) {
        if (err) throw err;
        else {
            res.json({"response": "yes","token": utoken,"PK":pk, "SK":sk});
        }
   });    
})

router.post('/login', function(req, res) {
    var uid = req.body.UID
	var upw = req.body.PW
	var uhpw = req.body.hPW
	//hash pwd
	var h_upw = crypto.createHash('sha256').update(upw).digest('base64');
	//update token 
	var sip = "140.119.164.149"
	var uip = res.connection.remoteAddress;
	var utoken = token.sign(uid,uip,null,sip);
    mariaclient.query('SELECT USER_PASSWORD FROM iot.userlnfo WHERE USER_ID = ?', [uid],function(err, rows) {//check identity
        if (err) throw err;
        else {
			var isHower
			
			// check is HomeOwner? 
			mariaclient.query('SELECT count(USER_ID) as id FROM iot.userlnfo where USER_ID=? and ROLE= ?', [uid, "0"],function(err, rows2) {//check is homewoner
				if (err) throw err;
				else isHower = (rows2[0].id != 0)? "yes" : "no";
			
			
			});
			
            if(h_upw==rows[0].USER_PASSWORD){// check password
				// for token role list 
				mariaclient.query('SELECT GATEWAY_ID,ROLE_ID,startime,deadline FROM iot.rel_gwayuserid WHERE USER_ID = ? and deadline >= now()', [uid],function(err, rows1) {
					var tmp = new Array()
					var o = {};
					o["0"] = [];
					o["1"] = [];
					o["2"] = [];
					o["3"] = [];
					o["4"] = [];
					
					if (err) throw err;
					else if (rows1.length == 0) tmp = null;
					else{
						rows1.forEach(function(item){
						tmp.push(item.GATEWAY_ID+"_"+item.ROLE_ID+"_"+item.startime+"_"+item.deadline);
						switch(item.ROLE_ID) {
									case "0":
										o["0"].push(item.GATEWAY_ID);
										break;
									case "1":
										o["1"].push(item.GATEWAY_ID);
										break;
									case "2":
										o["2"].push(item.GATEWAY_ID);
										break;
									case "3":
										o["3"].push(item.GATEWAY_ID);
										break;
									case "4":
										o["4"].push(item.GATEWAY_ID);
										break;
									default:
										//console.log("ff");
										break;
								}
						});	
						
					}
					
					var utoken = token.sign(uid,uip,tmp,sip);
					
					// simple check role for demo 
					var index
					if(o["0"].length != 0) index  = "0";
					if(o["1"].length != 0) index  = "1";
					if(o["2"].length != 0) index  = "2";
					if(o["3"].length != 0) index  = "3";
					if(o["4"].length != 0) index  = "4";
					var output={};
					output[index] = [];
					o[index].forEach(function(item){
							output[index].push(item);
					});
					
					res.json({"RoleID": index,"Role": output,"token":utoken,"login":"yes","isAdmin":isHower});
					
				});  			   
			}
			
        }
   });    
})



//router.post('/updateToken', function(req, res) {
//    var username = decrypt(req.body.username, gsu)
//    var token = decrypt(req.body.token, gsu)
//    mariaclient.query("UPDATE Server_User SET token = ? WHERE username = ?", [token, username])
//    res.send("ok");
//})

router.post('/userManagement', function(req, res) {

	var Gid = req.body.GATEWAY_ID
	
    mariaclient.query("select USER_NAME, ROLE_NAME, U.USER_ID from rel_gwayuserid as G inner join userlnfo as U on U.USER_ID = G.USER_ID inner join rel_rolename R on R.ROLE_ID = G.ROLE_ID where G.GATEWAY_ID = ? ",[Gid], function(err, rows) {
		var USER_NAME = [];
		var ROLE_NAME = [];
		var USER_ID = [];
		if (err) throw err;
		else {	
			rows.forEach(function(item){
				USER_NAME.push(item.USER_NAME)
				ROLE_NAME.push(item.ROLE_NAME)
				USER_ID.push(item.USER_ID)
			});	
			//console.log(rows);					
			res.json({"uName": USER_NAME,"rName":ROLE_NAME,"uID":USER_ID});                        
		}
	});
});

router.post('/insertdata', function(req, res) {
    var dt = dateTime.create().format('Y-m-d H:M:S');
    mariaclient.query("INSERT INTO health SET heartbeat = ?, time = ?", [parseFloat(req.body.heartbeat), dt]);
    res.send("ok");
});

router.post('/gethistorydata', function(req, res) {

	var uid = req.body.UserID
	var utoken1 = req.body.utoken
	var datatype = req.body.datatype
	var Gid = req.body.gatewayid
	var role = req.body.roleid
	var uip = req.connection.remoteAddress;

	//verify token  
	var response = token.verify(utoken1,uid,uip,Gid,role);

	if (response != "ok") res.json({"data": "Wrong Token"});
	else{
		mariaclient.query("SELECT TimeStamp, Value FROM Server_sensor WHERE DataType = ?",[datatype], function(err, rows) {
						
						if (err) throw err;
						else {	
							console.log(rows);					
							res.json({"data": rows});                        
						}
					});
	}
});


router.post('/getrealtimedata', function(req, res) {
	
	

	var uid = req.body.UserID
	var utoken1 = req.body.utoken
	var datatype = req.body.datatype
	var Gid = req.body.gatewayid
	var role = req.body.roleid
	var uip = req.connection.remoteAddress;
	
	//verify token  
	//console.log(utoken1+" "+uid+" "+uip+" "+Gid+" "+role);
	var response = token.verify(utoken1,uid,uip,Gid,role);
	/* console.log(response);
	console.log(uid); */
	//Here
	if (response != "ok") res.json({"data": "Wrong Token"});
	
	else{
		mariaclient.query("SELECT TimeStamp, Value FROM Device_value WHERE DataType = ?",[datatype], function(err, rows) {
			if (err) throw err;
			else {	
				//console.log(rows);					
				res.json({"data": rows});                        
			}
		});
		
	}
	
	
	//這邊是沒有成功的(server連gateway部分)
	/*var client = request.createClient('http://140.119.164.149:8003/');

	var data = {
		title: 'my title',
		content: 'my content'
	};
	client.patch('getPost', data, function(err, res, body) {
		//if (err) throw err;
		if(err) console.error(err);
		return console.log(res.statusCode);
	});*/
});



router.post('/userDeleteUID', function(req, res) {

	var uid = req.body.User_ID	

	mariaclient.query("DELETE FROM gatewayuserid WHERE BINARY USER_ID =?",[uid], function(err, rows) {
		if (err) throw err;
		else {	
			//console.log(rows);					
			res.json({"data": JSON.stringify(rows)});                        
		}
	});

});


router.post('/gatewayhistorydata', function(req, res) {

    var data = req.body.data
    var jo = JSON.parse(data)
    var ja = jo["data"]

    ja.forEach(function(item) {
        //console.log(item)
        var DataType = item["DataType"]
        var Value = item["Value"]
        var name = item["name"]
        var reportid = item["reportid"]
        var DeviceId = item["DeviceId"]
        var TimeStamp = item["TimeStamp"]

        mariaclient.query("INSERT INTO Server_sensor SET Value=?, DataType=?, name=?, reportid=?, DeviceId=?, TimeStamp=?", [Value, DataType, name, reportid, DeviceId, TimeStamp], function(err, rows) {
            if (err) {
            }else {				
				
            }
        });
    });
    res.json(data)
});




router.post('/insertNewAuthUser', function(req, res) {
	
	var uid = req.body.USER_ID
	var gid = req.body.GATEWAY_ID
	var rid = req.body.ROLE_ID
	var sDate = req.body.FROM_DATE
	var eDate = req.body.EXPIRE_DATE
	var sip = "140.119.164.149"
	var uip = res.connection.remoteAddress;

	mariaclient.query("select count(USER_ID) as count from iot.userlnfo where USER_ID = ?", [uid], function(err, rows) {
		if(err) throw err;
		else{

			if (rows[0].count == 0) res.json({"response": "No","token": null});	
		}
	});

    mariaclient.query("INSERT INTO iot.rel_gwayuserid SET  USER_ID=?, GATEWAY_ID=?, ROLE_ID=?, startime=?, deadline=? ", [uid, gid, rid, sDate, eDate], function(err, rows) {
            if (err) throw err;
            else {
				mariaclient.query('SELECT GATEWAY_ID,ROLE_ID,startime,deadline FROM iot.rel_gwayuserid WHERE USER_ID = ? and deadline >= now()', [uid],function(err, rows1) {
					var tmp = new Array()
					if (err) throw err;
					else if (rows1.length == 0) tmp = null;
					else{
						rows1.forEach(function(item){
						tmp.push(item.GATEWAY_ID+"_"+item.ROLE_ID+"_"+item.startime+"_"+item.deadline);
						
						
					});
					}
					
					var utoken = token.sign(uid,uip,tmp,sip);
					res.json({"response": "yes","token": utoken});			
					
				}); 
					
            }
        });

});


router.post('/insertNewGateway', function(req, res) {
	
	var gid = req.body.GID
	var uid = req.body.UID
	/* console.log(gid);
	console.log(uid); */
    mariaclient.query('INSERT INTO iot.rel_gwayuserid SET USER_ID=?, GATEWAY_ID=?, ROLE_ID=?', [uid, gid, '0'],function(err, rows) {
        if (err) throw err;
        else {
            res.json({"response": "yes"});	
        }
	});

});

// old decrypt&encrypt
function decrypt(input, key) {
    return xor(input, key);
}

function encrypt(input, key) {
    return xor(input, key);
}

function xor(input, key) {
    var output = [];

    for (var i = 0; i < input.length; i++) {
        var charCode = input.charCodeAt(i) ^ key[i % key.length].charCodeAt(0);
        output.push(String.fromCharCode(charCode));
    }
    return output.join("");
}
// for blockchain key
function WriteKeyIntoTxt(key){
	//將私鑰寫入文字檔
    fs.writeFile(__dirname + '/Keychain/pwd.txt', "password", function(err) {
        if(err) {
            //console.log(err);
        }
    });
    fs.writeFile(__dirname + '/Keychain/test.txt', key, function(err) {
        if(err) {
            //console.log(err);
        }
    });
}

function getChainAddr(callback) { 
    nodeCmd.get(  
        'geth --datadir C:\\server\\Keychain account import C:\\server\\Keychain\\test.txt --password C:\\server\\Keychain\\pwd.txt',  //**
        function(err, data, stderr){
            callback(data);
        }  
    );
}

function getAddress(SK){
    var addr = createKeccakHash("keccak256").update(SK).digest().slice(-20);
    addr = addr.toString('hex');
    return addr;
    
}


module.exports = router;
