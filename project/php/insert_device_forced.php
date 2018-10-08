<?php

	header('Content-Type: application/json');

	$db_host = "localhost";
	$db_user = "kebin1104";
	$db_password = "dbsksl04";
	$db_name = "kebin1104";

	$user_id = $_POST['user_id'];
	$device_serial_number = $_POST['device_serial_number'];

	$json_result = array();

	$conn = mysqli_connect($db_host, $db_user, $db_password, $db_name);
	if(mysqli_connect_errno($conn)){
		echo "데이터 베이스 연결 실패 : " . mysqli_connect_error();
		$json_result['result'] = 'fail';
	}
	else{
		mysqli_query($conn, "SET NAMES UTF8");

		$sql = "SELECT * FROM ssm_device WHERE serial_number = '$device_serial_number'";
		$result = mysqli_query($conn, $sql);

		if($result){
			$row = mysqli_fetch_array($result);
			$device_id = $row[id];

			$sql = "UPDATE ssm_user SET device_id = 0 WHERE device_id = $device_id";
			$result = mysqli_query($conn, $sql);

			$sql = "UPDATE ssm_user SET device_id = $device_id WHERE id = $user_id";
			$result = mysqli_query($conn, $sql);
			
			if($result){
				$json_result['result'] = 'ok';
			}
			else{
				$json_result['result'] = 'fail';
			}
		}
		else{
			$json_result['result'] = 'fail';
		}

		mysqli_close($conn);
	}

	$output = json_encode($json_result);

	echo $output;
?>