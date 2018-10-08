<?php

	header('Content-Type: application/json');

	$db_host = "localhost";
	$db_user = "kebin1104";
	$db_password = "dbsksl04";
	$db_name = "kebin1104";

	//안드로이드로부터 전송받을 변수
	$user_facebook_id = $_POST['user_facebook_id'];	//페이스북 고유 ID
	$user_email = $_POST['user_email'];	//이메일
	$user_name = $_POST['user_name'];	//암호화된 비밀번호

	$json_result = array();

	$conn = mysqli_connect($db_host, $db_user, $db_password, $db_name);
	if(mysqli_connect_errno($conn)){
		echo "데이터 베이스 연결 실패 : " . mysqli_connect_error();
	}
	else{

		mysqli_query($conn, "SET NAMES UTF8");

		$sql = "SELECT * FROM ssm_user WHERE facebook_id = '".$user_facebook_id."'";

		$result = mysqli_query($conn, $sql);

		$total_record = mysqli_num_rows($result);

		//중복가입 방지를 위한 검사
		if($total_record != 0){
			$json_result['result'] = 'facebook_overlap';
		}
		else{

			$sql = "SELECT * FROM ssm_user WHERE email = '".$user_email."'";
			$result = mysqli_query($conn, $sql);
			$total_record_email = mysqli_num_rows($result);

			if($total_record_email != 0){
				$json_result['result'] = 'email_overlap';
			}
			else{
				$sql = "INSERT INTO ssm_user (email, name, facebook_id) VALUES ('$user_email', '$user_name', '$user_facebook_id')";

				$result = mysqli_query($conn, $sql);

				if($result){
					$json_result['result'] = 'ok';
				}
				else{
					$json_result['result'] = 'fail';
				}
			}
		}

		mysqli_close($conn);
	}
	

	$output = json_encode($json_result);

	echo $output;
?>