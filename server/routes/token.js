var express = require('express');
var fs = require('fs');
var jws = require('jws');
var SKs = fs.readFileSync(__dirname + '/ecdsa-private.pem');
var PKs = fs.readFileSync(__dirname + '/ecdsa-public.pem');
var dateTime = require('node-datetime');


module.exports = {
    sign: function(uid,uip,urole,sip) {
		
		const tokenheader = { 
			alg: 'ES256', 
			typ: 'JWT' 
			};
		const dataStream = {
			iss: sip,									//發行者的 token
			sub: "userToken",		 							//主題的 token
			exp: Math.floor(Date.now() / 1000)+(7*24*60*60),		//這可能是 Registered Claims 最常用的，定義數字格式的有效期限，重點是有效期限一定要大於現在的時間
			nbf: Math.floor(Date.now() / 1000) - 30	,			//生效時間，定義一個時間在這個時間之前 JWT 不能進行處理
			iat: Math.floor(Date.now() / 1000) - 30	,			//發行的時間，可以被用來判斷 JWT 已經發出了多久
			UID: uid,
			UIP: uip,
			role: urole
				
			};
			
		const sigStream = jws.sign({
			header: tokenheader,
			payload: dataStream,
			privateKey: SKs,
			encoding: 'utf8',
			});
		 
			
        return sigStream
    },
    verify : function(utoken,id,ip,g,r) {
		
		try {
		
			jws.verify(utoken,'ES256', PKs);
			
			var decoded = jws.decode(utoken);
			var role = decoded.payload.role
			var uid = decoded.payload.UID
			var uip = decoded.payload.UIP
			var exp = decoded.payload.exp
			var nbf = decoded.payload.nbf
			
			var count = 0;
			var booRole ;
			
			if(exp < Date.now() / 1000) return "Token overdue";
			
			if(nbf > Date.now() / 1000) return "Token illagel";
			
			if(uip!=ip) return "ip";
			
			if(uid!=id) return "id";
		
			role.forEach(function(item){ 
				var arr = item.split("_");
				var date =  new Date(arr[3])
				if (arr[0] != g || arr[1] != r || Math.floor(date.getTime() / 1000) < Math.floor(Date.now() / 1000)){
					count++;
					if (count == item.length) booRole = false;
				}else {
					booRole = true;
					return false;
				}
			});
			
			if (!booRole) return "Role"
							
			return "ok";
			
			} catch(err) {
				return "error"
			}
		
		
        
    }
};