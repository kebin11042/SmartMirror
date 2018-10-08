<?php
   $db_host = "localhost";
   $db_user = "kebin1104";
   $db_password = "dbsksl04";
   $db_name = "kebin1104";

   //안드로이드 어플리케이션으로부터 전송받을 변수
   $user_id = $_POST['user_id'];
   $gcm_mode = $_POST['gcm_mode'];
   $gcm_contents = $_POST['gcm_contents'];

   $json_response = array();

   $conn = mysqli_connect($db_host, $db_user, $db_password, $db_name);
   if(mysqli_connect_errno($conn)){
      echo "데이터 베이스 연결 실패 : " . mysqli_connect_error();
   }
   else{

      mysqli_query($conn, "SET NAMES UTF8");
      //이메일 AND 비밀번호 일치하는지 검사
      $sql = "SELECT d.gcm_token 'gcm_token' FROM ssm_user u, ssm_device d WHERE u.device_id = d.id AND u.id = $user_id";

      $result = mysqli_query($conn, $sql);

      if($result) {
      	$total_record = mysqli_num_rows($result);

	      //데이터베이스에 있는 회원정보면 반드시 1개
	      if($total_record == 1){
	         $row = mysqli_fetch_array($result);
	         $device_gcm_token = $row[gcm_token];

	         sendGCM($device_gcm_token, $gcm_mode, $gcm_contents);

	         $json_response['result'] = "ok";
	      }
	      else{
	         $json_response['result'] = "fail";
	      }
      }
      else{
      	$json_response['result'] = "fail";	
      }

      

      mysqli_close($conn);
   }

   $output = json_encode($json_response);
   echo $output;


	function sendGCM($gcm_token, $gcm_mode, $gcm_contents){
	 // Replace with the real server API key from Google APIs
		 $apiKey = "AIzaSyCOxS-9_9uh6b-OblfCEcS3kzDGz089fm4"; //구글에서 발급받은 API키값

		 // Replace with the real client registration IDs
		 $registrationIDs = array( $gcm_token ); 

		 // Set POST variables
		 $url = 'https://android.googleapis.com/gcm/send'; //GCM 전송URL

		 $fields = array(
		  'registration_ids' => $registrationIDs,
		  'data' => array( "gcm_mode" => $gcm_mode, "gcm_contents" => $gcm_contents ),
		 );
		 $headers = array(
		  'Authorization: key=' . $apiKey,
		  'Content-Type: application/json'
		 );

		 // Open connection
		 $ch = curl_init();

		 // Set the URL, number of POST vars, POST data
		 curl_setopt( $ch, CURLOPT_URL, $url);
		 curl_setopt( $ch, CURLOPT_POST, true);
		 curl_setopt( $ch, CURLOPT_HTTPHEADER, $headers);
		 curl_setopt( $ch, CURLOPT_RETURNTRANSFER, true);
		 //curl_setopt( $ch, CURLOPT_POSTFIELDS, json_encode( $fields));

		 curl_setopt($ch, CURLOPT_SSL_VERIFYPEER, false);
		 // curl_setopt($ch, CURLOPT_POST, true);
		 // curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
		 curl_setopt($ch, CURLOPT_POSTFIELDS, json_encode( $fields));

		 // Execute post
		 $result = curl_exec($ch);

		 // Close connection
		 curl_close($ch);
		 //echo $result;
	}

?>