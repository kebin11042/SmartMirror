<?php

	header('Content-Type: application/json');

	//http://api.soundcloud.com/resolve?url=http://soundcloud.com/matas/hobnotropic&client_id=YOUR_CLIENT_ID

	$soundcloud_api_url = 'http://api.soundcloud.com/resolve';	//사운드클라우드 API 기본 URI
	$soundcloud_client_id = '&client_id=532f9c69b70212f0c23fe71bc29b2605';	//사운드클라우드 API KEY

	$music_link_url = '?url='.$_POST['music_link_url'];	//안드로이드 어플리케이선으로부터 받는 url

	//응답 준비
	$json_result = array();

	$soundcloud_api_url = $soundcloud_api_url.$music_link_url.$soundcloud_client_id;

	//curl을 이용한 HTTP 접속
	$ch = curl_init();

	curl_setopt($ch, CURLOPT_URL, $soundcloud_api_url);
	curl_setopt($ch, CURLOPT_FOLLOWLOCATION, true);		//API가 redirect 해주기 때문에 필요함
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
	    	$music_title = $soundcloud->title;

	    	$json_result['result'] = 'ok';
	    	$json_result['soundcloud_title'] = $music_title;
	    	$json_result['soundcloud_artwork_url'] = $music_thumbnail_url;
	    	$json_result['soundcloud_stream_url'] = $music_stream_url;
	    }
	    else{
	    	$json_result['result'] = 'not_streamable';
	    }
    }

	$output = json_encode($json_result);

	echo $output;
?>