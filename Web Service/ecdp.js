// Keygen for McDonald's eCDP (eCrew Development Program),
// a Nintendo DS software to train employees.
// This keygen is for the only dumped Japanese version of eCDP.
// ROM: https://archive.org/details/mcdonalds-japan-ecdp-rom-training-nintendo-ds-cartridge-dump

// Usage: Select the third option in main menu, enter two 6-digit numbers as you like,
// and use this script to calculate the third code.

// Web/JavaScript port info:
// This is based on LazyDog's keygen in Python, located at the URL below:
// https://gist.github.com/lazydogP/57c431cc7fddb5b969559c4c8cd8c881
//
// I know there is one that beats him by 2 days
// (https://github.com/KuromeSan/eCDP-Serial-Code ),
// but that one is written in C which I hardly understand in the first place,
// and also uses some library that I have no idea what that is.
// At least this Python one I can understand.


function toUnsigned(n){
	if(n>=0)return n;
	return 4294967296+n;
}

function rol(n, rotation){
	// Original rol function has an optional parameter "width" that defaults
	// to 32. I'm gonna hard code it because the only instance of this
	// function call doesn't include this parameter, ... and the fact that
	// new JS standards kinda make me feel confused about what you can and
	// can't do in JS.
	var width=32;
	rotation%=width;
	if(rotation==0){
		return n;
	}
	n&=(2**width-1);
	// Apparently bitwise operators treat numbers as a **signed** 32-bits
	// Did conversion to match the Python
	return toUnsigned((n<<rotation) | (n>> (width-rotation)));
}

function gen_index(sum_offset, length){
	if(length==0){
		return 0;
	}
	if(length <= sum_offset){
		var count=0x1c;
		var seed=sum_offset>>4;
		if(length <= seed>>0xc){
			count-=0x10;
			seed=seed>>0x10;
		}
		if(length <= seed>>4){
			count-=8;
			seed=seed>>8;
		}
		if(length<=seed){
			count-=4;
			seed=seed>>4;
		}
		
		// Did someone say bitwise sucks in Python?
		// It sucks here too
		// Some bitwise replaced with equivalent regular maths because
		// maths can handle numbers larger than 32 bits while
		// bitwise can't
		sum_offset=rol(sum_offset, count);
		sum_offset*=2;
		//carry=(sum_offset&(2**32))!=0;
		carry=Math.floor(sum_offset/(2**32))%2
		for(var i=count; i<32; i++){
			seed= seed*2 + (2**32-length);
			if(carry){
				seed+=1;
			}
			carry=Math.floor(seed/(2**32))%2
			seed%=(2**32)
			if(!carry){
				seed+=length;
				seed%=(2**32)
			}
			sum_offset*=2;
			carry=Math.floor(sum_offset/(2**32))%2;
			sum_offset%=(2**32)
		}
		// why is there a `pass` in the original? There is no block to pass
		return seed;
	}else{
		return sum_offset;
	}
}

function gen_index_2(sum_offset, length){
	return sum_offset%length;
}

function calc_shorten_index(input_merge){
	var hex_char="0123456789ABCDEF";
	var sum_offset=0;
	for(var c of input_merge){
		sum_offset+=hex_char.indexOf(c);
	}
	return gen_index(sum_offset, 7);
	// and here's another `pass`
}

function shuffle(input_merge, shuffle_index){
	var shuffle_map = [
		[0x01,0x0A,0x16,0x04,0x07,0x18,0x0C,0x10,0x05,0x17,0x09,0x03,0x12,0x08,0x15,0x13,0x0B,0x02,0x0F,0x0D,0x11,0x0E,0x06,0x14],
		[0x07,0x0C,0x0E,0x11,0x09,0x16,0x10,0x06,0x14,0x0D,0x01,0x02,0x12,0x08,0x13,0x0B,0x0F,0x0A,0x18,0x15,0x04,0x05,0x03,0x17],
		[0x0F,0x04,0x09,0x03,0x06,0x07,0x11,0x12,0x15,0x16,0x02,0x08,0x05,0x17,0x0C,0x0D,0x01,0x18,0x0B,0x14,0x0E,0x10,0x13,0x0A],
		[0x02,0x0A,0x0E,0x12,0x0B,0x03,0x0C,0x06,0x13,0x07,0x11,0x09,0x15,0x18,0x10,0x17,0x14,0x0F,0x04,0x01,0x05,0x08,0x16,0x0D],
		[0x0B,0x02,0x09,0x16,0x14,0x01,0x12,0x11,0x15,0x06,0x0F,0x17,0x07,0x10,0x0C,0x0E,0x08,0x18,0x13,0x03,0x0A,0x0D,0x04,0x05],
		[0x09,0x0F,0x05,0x0D,0x16,0x15,0x12,0x11,0x03,0x0A,0x04,0x10,0x0E,0x14,0x02,0x01,0x13,0x0C,0x06,0x0B,0x17,0x18,0x07,0x08],
		[0x12,0x02,0x0C,0x09,0x0D,0x0E,0x04,0x07,0x16,0x14,0x17,0x01,0x11,0x03,0x10,0x15,0x08,0x0A,0x05,0x13,0x0B,0x18,0x0F,0x06]
	];
	var shuffle_method=shuffle_map[shuffle_index];
	var shuffled_str="";
	for(var i=0; i<0x18; i++){
		shuffled_str+=input_merge[shuffle_method[i]-1];
	}
	return shuffled_str;
}

function hex2ints(hex_str){
	var result=[];
	for(var i=0; i<6; i++){
		result.push(parseInt("0x"+hex_str.slice(4*i, 4*i+4)));
		// JS parseInt recognizes 0x hex numbers, but can't otherwise handle
		// other bases
	}
	return result;
}

function ints_encrypt(numbers){
	// never mind the bitwise nonsenses
	var key=0x3e0f83e1; //good news it's 32 bits
	result=[];
	for(var i=0; i<6; i++){
		var r0=Math.floor(numbers[i]/(2**0x1f));
		var mul=key*numbers[i]; //wait how many bits are we getting here?
		var r2= mul%(2**32);
		var r6= Math.floor(mul/(2**32));
		r6=r0+r6>>3;
		mul=0x21*r6;
		r0=mul%(2**32);
		r2=Math.floor(mul/(2**32));
		r6=numbers[i]-r0;
		result.push(r6+1);
	}
	return result;
}

function ints2str(numbers){
	var chars="123456789ABCDEFGHJKLMNPQRSTUVWXYZ";
	var s="";
	for(var i of numbers){
		s+=chars[i-1];
	}
	return s;
}

function calc_serial_code(mac_code, code1, code2){
	var full_code=mac_code.toUpperCase()+code1+code2;
	var shorten_index=calc_shorten_index(full_code);
	var shuffle_str=shuffle(full_code, shorten_index);
	var shuffle_numbers=hex2ints(shuffle_str);
	var encrypted_numbers=ints_encrypt(shuffle_numbers);
	var serial_code=ints2str(encrypted_numbers);
	return serial_code;
}

// starting here is not present in the original code
// made these for web and such
function input_sanitizer(mac_code, code1, code2){
	var mac=""
	for(var c of mac_code){
		if(("0123456789AaBbCcDdEeFf").indexOf(c)!=-1){
			mac+=c
		}
	}
	var c1=""
	for(var c of code1){
		if(("0123456789").indexOf(c)!=-1){
			c1+=c
		}
	}
	var c2=""
	for(var c of code2){
		if(("0123456789").indexOf(c)!=-1){
			c2+=c
		}
	}
	if(mac.length!=12 || c1.length!=6 || c2.length!=6){
		return ""
	}
	return calc_serial_code(mac, c1, c2)
}

/*
* SilicaAndPina Code start here
*/

window.addEventListener("load", function() {
    dsiMacs = [dsiMac0, dsiMac1, dsiMac2, dsiMac3, dsiMac4, dsiMac5];
});


function placeOffering()
{
	var macAddress = "";
	dsiMacs.forEach(function(i){ macAddress += i.value });
	
	var sId = storeId.value;
	var mId = managementId.value;
	var key = input_sanitizer(macAddress, sId, mId)
	if(key != "")
		offering.innerText = "Donald McDonald speaks with a cryptic message: "+key;
	else
		offering.innerText = "Donald McDonald looks at you, disgusted, throwing your Big Mac right back at you.";
}

function getRandomInt(min, max) {
    min = Math.ceil(min);
    max = Math.floor(max);
    return Math.floor(Math.random() * (max - min + 1)) + min;
}

function randomizeStoreNumber()
{
	storeId.value = getRandomInt(0,999999).toString().padStart(6, "0");
}

function randomizeManagerNumber()
{
	managementId.value = getRandomInt(0,999999).toString().padStart(6, "0");
}

function numbersOnly(elem)
{
	elem.value = elem.value.replace(/[^0-9]/g, '');	
}
function detectChanges(index)
{
	var elem = dsiMacs[index];
	elem.value = elem.value.toUpperCase().replace(/[^A-F0-9]/g, '');
	
	if(elem.value.length >= elem.maxLength)
	{
		if(index+1 < dsiMacs.length)
		{
			dsiMacs[index+1].select();
		}
	}
	
	
}
