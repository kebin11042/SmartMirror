<?php

	header('Content-Type: application/json');

	$db_host = "localhost";
	$db_user = "kebin1104";
	$db_password = "dbsksl04";
	$db_name = "kebin1104";

	//안드로이드 어플리케이션으로 부터 전송받을 변수
	$user_id = $_POST['user_id'];	//회원 고유 번호
	$user_lat = $_POST['user_lat'];	//위도
	$user_lng = $_POST['user_lng'];	//경도
	$user_addr = $_POST['user_addr'];	//한글 주소

	$json_result = array();

	$conn = mysqli_connect($db_host, $db_user, $db_password, $db_name);
	if(mysqli_connect_errno($conn)){
		echo "데이터 베이스 연결 실패 : " . mysqli_connect_error();
	}
	else{

		mysqli_query($conn, "SET NAMES UTF8");
		//회원정보 갱신
		$sql = "UPDATE ssm_user SET lat = '".$user_lat."', lng = '".$user_lng."', addr = '".$user_addr."' WHERE id = ".$user_id;

		$result = mysqli_query($conn, $sql);
		//응답 준비
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