<?php

	header('Content-Type: application/json');

	$db_host = "localhost";
	$db_user = "kebin1104";
	$db_password = "dbsksl04";
	$db_name = "kebin1104";

	//사운드클라우드 API 주소 및 KEY
	$soundcloud_api_url = 'http://api.soundcloud.com/resolve';
	$soundcloud_client_id = '&client_id=532f9c69b70212f0c23fe71bc29b2605';
	//안드로이드 어플리케이션으로부터 받을 변수들
	$user_id = $_POST['user_id'];				//회원 고유 번호
	$music_subject = $_POST['music_subject'];	//노래 제목
	$music_link_url = $_POST['music_link_url'];	//음악 링크 주소

	//응답 준비
	$json_result = array();

	//사운드 클라우드 API URI 준비
	$soundcloud_api_url = $soundcloud_api_url.'?url='.$music_link_url.$soundcloud_client_id;
	
	//curl을 이용한 http 접속
	$ch = curl_init();

	curl_setopt($ch, CURLOPT_URL, $soundcloud_api_url);
	curl_setopt($ch, CURLOPT_FOLLOWLOCATION, true);	//API가 redirect 해주기 때문에 필요함
	curl_setopt($ch,CURLOPT_RETURNTRANSFER, true);
 
    $output = curl_exec($ch);

    curl_close($ch);

    if($output == false){
    	$json_result['result'] = 'curl_fail';
    }
    else{
    	//json의 데이터 형식으로 오기 때문에 파싱
	    $soundcloud = json_decode($output);

	    //스트림 가능 여부 판단
	    $soundcloud_streamable = $soundcloud->streamable;
	    if($soundcloud_streamable == true){
	    	$music_thumbnail_url = $soundcloud->artwork_url;
	    	$music_stream_url = $soundcloud->stream_url;

	    	$conn = mysqli_connect($db_host, $db_user, $db_password, $db_name);
			if(mysqli_connect_errno($conn)){
				echo "데이터 베이스 연결 실패 : " . mysqli_connect_error();
			}
			else{

				mysqli_query($conn, "SET NAMES UTF8");

				$created = date("Y-m-d H:i:s");

				$sql = "INSERT INTO ssm_music (user_id, subject, link_url, thumbnail_url, stream_url, created) VALUES ('$user_id', '$music_subject', '$music_link_url', '$music_thumbnail_url', '$music_stream_url', '$created')";

				$result = mysqli_query($conn, $sql);

				if($result){
					$json_result['result'] = 'ok';
				}
				else{
					$json_result['result'] = 'fail';
				}

				mysqli_close($conn);
			}
	    }
	    else{
	    	$json_result['result'] = 'not_streamable';
	    }
    }

	$output = json_encode($json_result);

	echo $output;
?>