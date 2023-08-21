var loginIdCheckResult = false;
var passwordCheckResult = false;
var passwordConfirmCheckResult = false;
var emailCheckResult = false;
var emailAuthResult = false;

function loginIdCheck() {
    // 초기화
    loginIdCheckResult = false;
    document.getElementById('checkLoginIdResult').innerHTML = '';
    document.getElementById('loginId').classList.remove("fieldError");
    var loginIdErr = document.getElementById('loginIdErr');
    if(loginIdErr != null) {
        loginIdErr.innerHTML = '';
    }

    // 1. 아이디 정규식 확인
    var loginId = document.getElementById('loginId').value;
    let regex = new RegExp('^[a-zA-Z0-9-_]{5,20}$');

    if(regex.test(loginId)) {
        // 2. 아이디 중복 검사
        var param = {"loginId":loginId}
        $.ajax({
            url: '/user/loginIdCheck',
            data: JSON.stringify(param),
            contentType: "application/json; charset=UTF-8",
            type: 'POST',
            dataType: 'json',
            success: function(data) {
                loginIdCheckResult = true;
            },
            error: function(data){
                if(data.responseJSON.code == "loginId") {
                    document.getElementById('checkLoginIdResult').innerHTML = data.responseJSON.message;
                } else {
                    document.getElementById('checkLoginIdResult').innerHTML = error;
                    document.getElementById('loginId').classList.add("fieldError");
                }
            }
        });
    } else { // 정규식에 맞지 않을 때
        document.getElementById('checkLoginIdResult').innerHTML = formatLoginId;
        document.getElementById('loginId').classList.add("fieldError");
    }
}

function passwordCheck() {
    // 초기화
    passwordCheckResult = false;
    passwordConfirmCheckResult = false;
    document.getElementById('checkPasswordResult').innerHTML = '';
    document.getElementById('password').classList.remove("fieldError");
    var passwordErr = document.getElementById('passwordErr');
    if(passwordErr != null) {
        passwordErr.innerHTML = '';
    }

    // 비밀번호 정규식 확인
    var password = document.getElementById('password').value;
    let regex = new RegExp('^[a-zA-Z0-9`~!@#$%^&*()+-_=|:<>,.?/]{8,16}$');

    // 정규식 통과
    if(regex.test(password)) {
        passwordCheckResult = true;
    } else { // 정규식에 맞지 않을 때
        document.getElementById('checkPasswordResult').innerHTML = formatPassword;
        document.getElementById('password').classList.add("fieldError");
    }

    // 비밀번호 확인에 값이 있다면 함수 실행
    var passwordConfirm = document.getElementById('passwordConfirm').value;
    if(passwordConfirm != '') {
        passwordConfirmCheck();
    }
}

function passwordConfirmCheck() {
    // 초기화
    passwordConfirmCheckResult = false;
    document.getElementById('checkPasswordConfirmResult').innerHTML = '';
    document.getElementById('passwordConfirm').classList.remove("fieldError");
    var passwordConfirmErr = document.getElementById('passwordConfirmErr');
    if(passwordConfirmErr != null) {
        passwordConfirmErr.innerHTML = '';
    }

    // 비밀번호, 비밀번호 확인 일치한지 확인
    var password = document.getElementById('password').value;
    var passwordConfirm = document.getElementById('passwordConfirm').value;

    if(password == passwordConfirm) {
        passwordConfirmCheckResult = true;
    } else {
        document.getElementById('checkPasswordConfirmResult').innerHTML = equalPasswordConfirm;
        document.getElementById('passwordConfirm').classList.add("fieldError");
    }
}

function emailCheck() {
    // 초기화
    emailCheckResult = false;
    emailAuthResult = false;
    document.getElementById('checkEmailResult').innerHTML = '';
    document.getElementById('email').classList.remove("fieldError");
    document.getElementById('authNum').value = '';
    document.getElementById('authNum').classList.remove("fieldError");
    var emailErr = document.getElementById('emailErr');
    if(emailErr != null) {
        emailErr.innerHTML = '';
    }
    var authNumErr = document.getElementById('authNumErr');
    if(authNumErr != null) {
        authNumErr.innerHTML = '';
    }

    // 1. 이메일 정규식 확인
    var email = document.getElementById('email').value;
    let regex = new RegExp('^[a-zA-Z0-9-_]+@[a-z]+\\.[a-z]{2,3}$');

    if(regex.test(email)) {
        // 2. 이메일 중복 검사
        var param = {"email":email}
        $.ajax({
            url: '/user/emailCheck',
            data: JSON.stringify(param),
            contentType: "application/json; charset=UTF-8",
            type: 'POST',
            dataType: 'json',
            success: function(data) {
                emailCheckResult = true;
            },
            error: function(data){
                if(data.responseJSON.code == "email") {
                    document.getElementById('checkEmailResult').innerHTML = data.responseJSON.message;
                } else {
                    document.getElementById('checkEmailResult').innerHTML = error;
                    document.getElementById('email').classList.add("fieldError");
                }
            }
        });
    } else { // 정규식에 맞지 않을 때
        document.getElementById('checkEmailResult').innerHTML = formatEmail;
        document.getElementById('email').classList.add("fieldError");
    }
}

// 이메일 인증 메일 보내기
function emailAuth() {
    // 이메일 유효성 체크 후 진행
    if(!emailCheckResult) {
        document.getElementById('email').focus();
        return;
    }

    // 초기화
    emailAuthResult = false;
    document.getElementById('checkEmailResult').innerHTML = '';
    document.getElementById('authNum').classList.remove("fieldError");
    var authNumErr = document.getElementById('authNumErr');
    if(authNumErr != null) {
        authNumErr.innerHTML = '';
    }

    // 이메일 인증 메일 보내기
    document.getElementById('checkEmailResult').innerHTML = "이메일 전송이 완료되었습니다. 인증번호를 입력해주세요.";
    var email = document.getElementById('email').value;
    var param = {"email":email}

    $.ajax({
        url: '/api/mail/email',
        data: JSON.stringify(param),
        contentType: "application/json; charset=UTF-8",
        type: 'POST',
        dataType: 'json',
        error: function(data){
            if(data.responseJSON.code == "email") {
                document.getElementById('checkEmailResult').innerHTML = data.responseJSON.message;
            } else {
                document.getElementById('checkEmailResult').innerHTML = error;
                document.getElementById('email').classList.add("fieldError");
            }
        }
    });
}

// 인증번호 맞는지 체크
function authNumCheck() {
    // 초기화
    emailAuthResult = false;
    document.getElementById('checkEmailResult').innerHTML = '';
    document.getElementById('authNum').classList.remove("fieldError");
    var authNumErr = document.getElementById('authNumErr');
    if(authNumErr != null) {
        authNumErr.innerHTML = '';
    }

    var email = document.getElementById('email').value;
    var authNum = document.getElementById('authNum').value;

    if(authNum != '' && authNum.length == 6 && emailCheckResult) {
        var param = {"email":email, "authNum":authNum}
        $.ajax({
            url: '/api/mail/email/check',
            data: JSON.stringify(param),
            contentType: "application/json; charset=UTF-8",
            type: 'POST',
            dataType: 'json',
            success: function(data) {
                emailAuthResult = true;
            },
            error: function(data){
                if(data.responseJSON.code == "email") {
                    document.getElementById('checkEmailResult').innerHTML = data.responseJSON.message;
                } else if(data.responseJSON.code == "authNum") {
                    document.getElementById('checkEmailResult').innerHTML = data.responseJSON.message;
                    document.getElementById('authNum').classList.add("fieldError");
                }
            }
        });
    }
}

function checkForm() {
    if(!loginIdCheckResult) {
        document.getElementById('loginId').focus();
        return false;
    }

    if(!passwordCheckResult) {
        document.getElementById('password').focus();
        return false;
    }

    if(!passwordConfirmCheckResult) {
        document.getElementById('passwordConfirm').focus();
        return false;
    }

    if(!emailCheckResult) {
        document.getElementById('email').focus();
        return false;
    }

    if(!emailAuthResult) {
        document.getElementById('authNum').focus();
        return false;
    }

    return true;
}

// 백에서 오류발생한다면
var globalErr = document.getElementById('globalErr');
if(globalErr != null) {
    var loginIdErr = document.getElementById('loginIdErr');
    var emailErr = document.getElementById('emailErr');
    var authNumErr = document.getElementById('authNumErr');

    loginIdCheckResult = false;
    emailCheckResult = false;
    emailAuthResult = false;

    if(loginIdErr == null) {
        loginIdCheckResult = true;
    }

    if(emailErr == null) {
        emailCheckResult = true;
    }

    if(emailErr == null && authNumErr == null) {
        emailCheckResult = true;
        emailAuthResult = true;
    }
}