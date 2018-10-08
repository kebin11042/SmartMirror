<?php
	header('Content-Type: application/json');

	$db_host = "localhost";
	$db_user = "kebin1104";
	$db_password = "dbsksl04";
	$db_name = "kebin1104";

	$device_serial_number = $_POST['device_serial_number'];	//스마트 거울 기기 시리얼 번호
	$device_gcm_token = $_POST['device_gcm_token'];					//스마트 거울 기기 토큰

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

		$total_record = mysqli_num_rows($result);
		//시리얼 넘버가 일치하는 데이터가 없다면
		if($total_record == 0){
			$json_result['result'] = 'not_exist';
		}
		//시리얼 넘버가 일치하는게 1개가 있는게 정상이다.
		else if($total_record == 1){

			$sql = "UPDATE ssm_device SET gcm_token = '$device_gcm_token' WHERE serial_number = '$device_serial_number'";
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