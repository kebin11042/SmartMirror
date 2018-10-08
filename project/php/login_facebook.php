<?php

   $db_host = "localhost";
   $db_user = "kebin1104";
   $db_password = "dbsksl04";
   $db_name = "kebin1104";

   //안드로이드 어플리케이션으로부터 전송받을 변수
   $user_facebook_id = $_POST['user_facebook_id']; //페이스북 고유 ID

   $json_response = array();

   $conn = mysqli_connect($db_host, $db_user, $db_password, $db_name);
   if(mysqli_connect_errno($conn)){
      echo "데이터 베이스 연결 실패 : " . mysqli_connect_error();
   }
   else{

      mysqli_query($conn, "SET NAMES UTF8");

      $sql = "SELECT * FROM ssm_user WHERE facebook_id='$user_facebook_id'";

      $result = mysqli_query($conn, $sql);

      $total_record = mysqli_num_rows($result);

      //가입되어 있는 회원이라면 반드시 검색된 데이터는 1개
      if($total_record == 1){
         $row = mysqli_fetch_array($result);
         $user_id = $row[id];
         $user_email = $row[email];
         $user_device_id = $row[device_id];
         $user_name = $row[name];
         $user_lat = $row[lat];
         $user_lng = $row[lng];
         $user_addr = $row[addr];

         if($user_device_id != 0) {
            $sql = "SELECT * FROM ssm_device WHERE id = $user_device_id";
            $result = mysqli_query($conn, $sql);
            $row = mysqli_fetch_array($result);

            $device_serial_number = $row[serial_number];

            $json_device_info = array();
            $json_device_info['serial_number'] = $device_serial_number;

            $json_response['device_info'] = $json_device_info;
         }
         

         //로그인 성공시 안드로이드 어플리케이션에게 줄 회원정보 준비
         $json_response['result'] = "ok";

         $json_user_info = array();
         $json_user_info['id'] = $user_id;
         $json_user_info['email'] = $user_email;
         $json_user_info['device_id'] = $user_device_id;
         $json_user_info['name'] = $user_name;
         $json_user_info['lat'] = $user_lat;
         $json_user_info['lng'] = $user_lng;
         $json_user_info['addr'] = $user_addr;

         $json_response['user_info'] = $json_user_info;
      }
      else{
         $json_response['result'] = "fail";
      }

      mysqli_close($conn);
   }

   $output = json_encode($json_response);

   echo $output;
?>