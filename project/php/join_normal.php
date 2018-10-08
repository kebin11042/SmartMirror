<?php

	header('Content-Type: application/json');

	$db_host = "localhost";
	$db_user = "kebin1104";
	$db_password = "dbsksl04";
	$db_name = "kebin1104";

	$user_email = $_POST['user_email'];	//이메일
	$user_name = $_POST['user_name'];	//이름
	$user_password = $_POST['user_password'];	//암호화된 비밀번호

	$json_result = array();

	$conn = mysqli_connect($db_host, $db_user, $db_password, $db_name);
	if(mysqli_connect_errno($conn)){
		echo "데이터 베이스 연결 실패 : " . mysqli_connect_error();
	}
	else{

		mysqli_query($conn, "SET NAMES UTF8");

		$sql = "SELECT * FROM ssm_user WHERE email = '".$user_email."'";

		$result = mysqli_query($conn, $sql);

		$total_record = mysqli_num_rows($result);

		//이미 가입되어 있는 회원가입 방지 -> 이메일 검사
		if($total_record != 0){
			$json_result['result'] = 'overlap';
		}
		else{
			
			$sql = "INSERT INTO ssm_user (email, password, name) VALUES ('$user_email', '$user_password', '$user_name')";

			$result = mysqli_query($conn, $sql);

			if($result){
				$json_result['result'] = 'ok';
			}
			else{
				$json_result['result'] = 'fail';
			}
		}

		mysqli_close($conn);
	}
	

	$output = json_encode($json_result);

	echo $output;
?>