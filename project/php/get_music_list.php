<?php

	header('Content-Type: application/json');

	$db_host = "localhost";
	$db_user = "kebin1104";
	$db_password = "dbsksl04";
	$db_name = "kebin1104";

	$blockCnt = 10;	//한페이지당 10개 씩의 정보
	//안드로이드 어플리케이션으로부터 받을 변수
	$user_id = $_POST['user_id'];	//회원 고유 번호
	$currentPage = $_POST['currentPage'];	//불러올 페이지 번호

	$json_result = array();

	$conn = mysqli_connect($db_host, $db_user, $db_password, $db_name);
	if(mysqli_connect_errno($conn)){
		echo "데이터 베이스 연결 실패 : " . mysqli_connect_error();
	}
	else{

		mysqli_query($conn, "SET NAMES UTF8");

		$sql = "SELECT * FROM ssm_music WHERE user_id = '$user_id'";

		$result = mysqli_query($conn, $sql);

		$totalCnt = mysqli_num_rows($result);	//데이터 총 갯수
		$totalPage = ceil( $totalCnt / $blockCnt );	//총 페이지 갯수

		$startNum = ( $currentPage - 1 ) * $blockCnt;

		$sql = "SELECT * FROM ssm_music WHERE user_id = '$user_id' ORDER BY created DESC LIMIT $startNum , $blockCnt";

		$result = mysqli_query($conn, $sql);

		if($result){

			$json_result['result'] = 'ok';
			$json_result['totalCnt'] = $totalCnt;
			$json_result['totalPage'] = $totalPage;
			$json_result['currentPage'] = $currentPage;

			$rows = mysqli_num_rows($result);
			while($row = mysqli_fetch_array($result)){
				//노래 데이터 정보를 JSON 배열로 뿌려주기 위한 준비
				$music_info = array();
				$music_info['id'] = $row[id];
				$music_info['subject'] = $row[subject];
				$music_info['link_url'] = $row[link_url];
				$music_info['thumbnail_url'] = $row[thumbnail_url];
				$music_info['stream_url'] = $row[stream_url];
				$music_info['created'] = $row[created];

				$music_list[] = $music_info;
			}
			$json_result['music_list'] = $music_list;

		}
		else{
			$json_result['result'] = 'fail';
		}

		mysqli_close($conn);
	}
	

	$output = json_encode($json_result);

	echo $output;
?>