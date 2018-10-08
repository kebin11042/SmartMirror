<?php

	header('Content-Type: application/json');

	$db_host = "localhost";
	$db_user = "kebin1104";
	$db_password = "dbsksl04";
	$db_name = "kebin1104";

	//&lat=37.22087979754348&lon=127.20174058203169
	$weather_api_url = "http://api.openweathermap.org/data/2.5/forecast?units=metric&APPID=1fbd6ca90216be56ef4ea3f6d02e9d6f";

	$device_serial_number = $_POST['device_serial_number'];	//스마트 기기 serial number
	// $device_serial_number = "1234";

	$json_result = array();

	$conn = mysqli_connect($db_host, $db_user, $db_password, $db_name);
	if(mysqli_connect_errno($conn)){
		echo "데이터 베이스 연결 실패 : " . mysqli_connect_error();
		$json_result['result'] = 'fail';
	}
	else{

		mysqli_query($conn, "SET NAMES UTF8");
		//user, device 테이블과 join하여 serial number가 일치하는지 검사
		//등록한 device serial number가 있는지 검사
		$sql = "SELECT u.id 'user_id', u.name 'user_name', u.addr 'user_addr', u.lat 'user_lat', u.lng 'user_lng'
		FROM ssm_user u, ssm_device d 
		WHERE u.device_id = d.id
		AND d.serial_number = '$device_serial_number'";

		$result = mysqli_query($conn, $sql);

		if($result){
			$totalCnt = mysqli_num_rows($result);	//데이터 총 갯수
			if($totalCnt == 0){
				$json_result['result'] = 'not_connected';
			}
			else if($totalCnt == 1){
				$json_result['result'] = 'ok';

				$row = mysqli_fetch_array($result);

				//회원정보
				$user_info = array();
				$user_info['id'] = $row[user_id];
				$user_info['name'] = $row[user_name];
				$user_info['addr'] = $row[user_addr];

				$user_id = $row[user_id];
				$device_id = $row[device_id];

				$json_result['user_info'] = $user_info;

				////////////////////////////////////////////////////////////////////////////////////////////////////////////

				//날씨정보 준비
				$weather_api_url = $weather_api_url."&lat=$row[user_lat]&lon=$row[user_lng]";
				//curl 준비
				$ch = curl_init();
				curl_setopt($ch, CURLOPT_URL, $weather_api_url);
				curl_setopt($ch, CURLOPT_FOLLOWLOCATION, true);	//API가 redirect 해주기 때문에 필요함
				curl_setopt($ch,CURLOPT_RETURNTRANSFER, true);
				$output = curl_exec($ch);
				curl_close($ch);

				$weather = json_decode($output);
				$weather_list = $weather->list;
				//오전, 오후, 저녁, 밤 시간
				$weather_date_morning = date("Y-m-d")." 00:00:00";
				$weather_date_noon = date("Y-m-d")." 06:00:00";
				$weather_date_eve = date("Y-m-d")." 09:00:00";
				$weather_date_night = date("Y-m-d")." 15:00:00";
				$weather_date_next = date("Y-m-d", strtoTime("+1 day"))." 00:00:00";
				//오전, 오후 객체 데이터
				$weather_morning = array();
				$weather_noon = array();
				$weather_eve = array();
				$weather_night = array();
				$weather_next = array();
				//날짜, 시간 일치하는 객체 찾기
				for($i=0;$i<count($weather_list);$i++){
					//필요한 것만 골라서 정보 뿌려주기
					if($weather_date_morning == $weather_list[$i]->dt_txt){
						$weather_morning['temp'] = $weather_list[$i]->main->temp;
						$weather_morning['icon'] = $weather_list[$i]->weather[0]->icon;
					}

					if($weather_date_noon == $weather_list[$i]->dt_txt){
						$weather_noon['temp'] = $weather_list[$i]->main->temp;
						$weather_noon['icon'] = $weather_list[$i]->weather[0]->icon;
					}

					if($weather_date_eve == $weather_list[$i]->dt_txt){
						$weather_eve['temp'] = $weather_list[$i]->main->temp;
						$weather_eve['icon'] = $weather_list[$i]->weather[0]->icon;
					}

					if($weather_date_night == $weather_list[$i]->dt_txt){
						$weather_night['temp'] = $weather_list[$i]->main->temp;
						$weather_night['icon'] = $weather_list[$i]->weather[0]->icon;
					}
					if($weather_date_next == $weather_list[$i]->dt_txt){
						$weather_next['temp'] = $weather_list[$i]->main->temp;
						$weather_next['icon'] = $weather_list[$i]->weather[0]->icon;
					}
				}
				$json_result['weather_morning'] = $weather_morning;
				$json_result['weather_noon'] = $weather_noon;
				$json_result['weather_eve'] = $weather_eve;
				$json_result['weather_night'] = $weather_night;
				$json_result['weather_next'] = $weather_next;

				////////////////////////////////////////////////////////////////////////////////////////////////////////////

				//노래 리스트 준비
				$sql = "SELECT * FROM ssm_music WHERE user_id = '$user_id' ORDER BY created DESC";
				$result = mysqli_query($conn, $sql);

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

				//메모 리스트 준비
				$now_date = date("Y-m-d");
				$sql = "SELECT * FROM ssm_memo WHERE user_id = '$user_id' AND date = '$now_date'";
				$result = mysqli_query($conn, $sql);

				while($row = mysqli_fetch_array($result)){
					//노래 데이터 정보를 JSON 배열로 뿌려주기 위한 준비
					$memo_info = array();
					$memo_info['id'] = $row[id];
					$memo_info['subject'] = $row[subject];
					$memo_info['date'] = $row[date];
					$memo_info['created'] = $row[created];

					$memo_list[] = $memo_info;
				}
				$json_result['memo_list'] = $memo_list;
			}
			else{
				$json_result['result'] = 'overlap';
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