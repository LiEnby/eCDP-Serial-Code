<?php
	if(isset($_GET["mac0"]) && isset($_GET["mac1"]) && isset($_GET["mac2"]) && isset($_GET["mac3"]) && isset($_GET["mac4"]) && isset($_GET["mac5"]) && isset($_GET['store']) && isset($_GET['management']))
	{
		$full_mac = "";

		$full_mac .= $_GET["mac0"];
		$full_mac .= $_GET["mac1"];
		$full_mac .= $_GET["mac2"];
		$full_mac .= $_GET["mac3"];
		$full_mac .= $_GET["mac4"];
		$full_mac .= $_GET["mac5"];
		$full_mac = strtoupper($full_mac);
		
		$management = $_GET['management'];
		$store = $_GET['store'];
		if((strlen($full_mac) == 12 && preg_match_all ("/^[A-F0-9]*$/",$full_mac)) && (strlen($management) == 6 && preg_match_all ("/^[0-9]*$/",$management)) && (strlen($store) == 6 && preg_match_all ("/^[0-9]*$/",$store)))
		{
			echo(exec("./eCDPSerialGenerator.elf ".escapeshellarg($full_mac)." ".escapeshellarg($store)." ".escapeshellarg($management)));
			exit();
		}

		http_response_code(500);
		echo("invalid query");
		exit();
	
	}

	http_response_code(500);
	echo("invalid request");
	exit();
?>