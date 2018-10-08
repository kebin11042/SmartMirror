<?php

	// http://api.openweathermap.org/data/2.5/weather?lat={lat}&lon={lon}&APPID=1fbd6ca90216be56ef4ea3f6d02e9d6f
	// http://api.openweathermap.org/data/2.5/weather?lat=37.39214630626963&lon=126.81460943691906&APPID=1fbd6ca90216be56ef4ea3f6d02e9d6f
	header('Content-Type: application/json');

	$db_host = "localhost";
	$db_user = "kebin1104";
	$db_password = "dbsksl04";
	$db_name = "kebin1104";

	$user_id = $_POST['user_id'];	//사용자 고유 번호
	$device_serial_number = $_POST['device_serial_number'];	//스마트 거울 기기 시리얼 번호

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
			//시리얼 넘버가 일치하는것이 있다면 등록된 기기인지 검사를 해야함

			$row = mysqli_fetch_array($result);
			$device_id = $row[id];

			$sql = "SELECT * FROM ssm_user WHERE device_id = $device_id";
			$result = mysqli_query($conn, $sql);

			if($result){
				$total_record = mysqli_num_rows($result);

				//등록된 기기가 없을 경우
				if($total_record == 0){
					$sql = "UPDATE ssm_user SET device_id = $device_id WHERE id = $user_id";
					$result = mysqli_query($conn, $sql);
					if($result){
						$json_result['result'] = 'ok';
					}
					else{
						$json_result['result'] = 'fail';
					}
				}
				//이미 등록된 기기일 경우
				else if($total_record == 1){
					$row = mysqli_fetch_array($result);
					$user_name = $row[name];

					$json_result['result'] = 'overlap';
					$json_result['user_name'] = $user_name;
				}
				else{
					$json_result['result'] = 'fail';
				}
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