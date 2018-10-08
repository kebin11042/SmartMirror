<?php

	header('Content-Type: application/json');

	$db_host = "localhost";
	$db_user = "kebin1104";
	$db_password = "dbsksl04";
	$db_name = "kebin1104";

	$user_id = $_POST['user_id'];
	$memo_subject = $_POST['memo_subject'];
	$memo_date = $_POST['memo_date'];

	//응답 준비
	$json_result = array();

	$conn = mysqli_connect($db_host, $db_user, $db_password, $db_name);
	if(mysqli_connect_errno($conn)){
		echo "데이터 베이스 연결 실패 : " . mysqli_connect_error();
	}
	else{

		mysqli_query($conn, "SET NAMES UTF8");

		$created = date("Y-m-d H:i:s");

		$sql = "INSERT INTO ssm_memo (user_id, subject, date, created) VALUES ('$user_id', '$memo_subject', '$memo_date', '$created')";

		$result = mysqli_query($conn, $sql);

		if($result){
			$json_result['result'] = 'ok';
		}
		else{
			$json_result['result'] = 'fail';
		}

		mysqli_close($conn);
	}

	$output = json_encode($json_result);

	echo $output;
?>