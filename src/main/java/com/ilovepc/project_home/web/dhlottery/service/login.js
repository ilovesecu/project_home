
	var rsa = new RSAKey();

	var LoginM = {
		//path : "/mbrsrvc",

	}

	$(document).ready(function(){
		const errorMessage = '';
		const inpUserId = '';
		const errorCode = '';

		if(!cmmUtil.isEmpty(errorMessage)) {
			$.alert(errorMessage);
		}

		// 로그인 버튼 클릭
		$("#btnLogin").click(function(){
			login();
		});
		// 아이디/비밀번호 찾기 버튼 클릭
		$("#btnFindMbrIdPswd").click(function(){
			location.href = "/mbrsrvc/findMbrIdPswd";	// 아이디/비밀번호 찾기 페이지
		});
		// 회원가입 버튼 클릭
		$("#btnMbrJoin").click(function(){
			location.href = "/mbrsrvc/mbrJoin";	// 회원가입 페이지
		});

		if(!cmmUtil.isEmpty(inpUserId)){
			$("#inpUserId").val(inpUserId);
		}else if(getCookie("inpUserId")){
			$("#inpUserId").val(getCookie("inpUserId"));
			$("input:checkbox[id='chkIdSave']").prop('checked', true);
		}

		selectRsaModulus();
	});

	/**
	* Validation Function
	*/
	function fn_validation(target, autoFocus){
		const option = {autoFocus: autoFocus};	// 유효성 false시 포커스 여부
		return cmmUtil.validate(target, option);
	}
	function login(){
		const isValid = fn_validation($("#loginForm"), true);
		if(!isValid) return false;

		const isIdSave = $("input:checkbox[id='chkIdSave']").is(":checked");
		
		checkCookieSaveUserId(isIdSave);

		$("#userId").val(fnRSAencrypt($("#inpUserId").val()));
		$("#userPswdEncn").val(fnRSAencrypt($("#inpUserPswdEncn").val()));
		$("#loginForm").attr("action","/login/securityLoginCheck.do");
		$("#loginForm").submit();
	}
	function selectRsaModulus(){
		const param = {}
		const options = {
			"method": "GET",
			"async" : true
		}
		ajaxUtil.sendHttpJson(param, "/login/selectRsaModulus.do", options, function(code, msg, {data}){
			if(data){
				rsa.setPublic(data.rsaModulus, data.publicExponent);
			}
		});
	}
	function enterUserLogin() {
		if (event.keyCode == 13 ){
			login();
			return;
		}
	}
	function setCookie(name, value, expire) {
		document.cookie = name + "=" + escape(value) + ( (expire) ? "; expires=" + expire.toGMTString() : "");
	}
	function removeCookie(name) {
		document.cookie = name + "=" + escape("") + "; expires=0";
	}
	function getCookie(uName) {
		var flag = document.cookie.indexOf(uName+'=');
		if (flag != -1)
		{
			flag += uName.length + 1;
			end = document.cookie.indexOf(';', flag);
			if (end == -1) end = document.cookie.length;
			return unescape(document.cookie.substring(flag, end));
		}
	}
	function register(uName){
		var today = new Date();
		var expire = new Date(today.getTime() + 60*60*24*31*1000);
		setCookie("inpUserId", uName, expire)
	}
	function checkCookieSaveUserId(isIdSave){
		if(isIdSave){
			register($("#inpUserId").val());
		} else {
			removeCookie("inpUserId");
		}
	}
	function fnRSAencrypt(str){
		return rsa.encrypt(str);
	}
	function detectCapsLock(event){
		var isCapsLock = event.getModifierState && event.getModifierState('CapsLock');
		if(isCapsLock) {
			$("#divPswd").addClass("fail");
		} else {
			$("#divPswd").removeClass("fail");
		}
	}
	function fn_loginFormChk(event){
		if(!$("#loginForm").attr("action")){
			event.preventDefault();
		}
	}
	function onchangeInpUserId(event){
		$(event.target).val($(event.target).val().replace(/[^a-zA-Z0-9]/g,''));
	}
