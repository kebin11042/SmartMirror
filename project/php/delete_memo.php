<?php

	header('Content-Type: application/json');

	$db_host = "localhost";
	$db_user = "kebin1104";
	$db_password = "dbsksl04";
	$db_name = "kebin1104";

	$music_id = $_POST['memo_id'];

	//응답 준비
	$json_result = array();

	$conn = mysqli_connect($db_host, $db_user, $db_password, $db_name);
	if(mysqli_connect_errno($conn)){
		echo "데이터 베이스 연결 실패 : " . mysqli_connect_error();

		$json_result['result'] = 'db_err';
	}
	else{

		mysqli_query($conn, "SET NAMES UTF8");

		$sql = "DELETE FROM ssm_memo WHERE id = $memo_id";
		$result = mysqli_query($conn, $sql);
		//echo $sql;

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