var emailCheckResult = false;

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
        emailCheckResult = true;
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
    document.getElementById('checkEmailResult').innerHTML = "이메일 전송 중입니다. 잠시만 기다려주세요.";
    var email = document.getElementById('email').value;
    var param = {"email":email}

    $.ajax({
        url: '/api/mail/email',
        data: JSON.stringify(param),
        contentType: "application/json; charset=UTF-8",
        type: 'POST',
        dataType: 'json',
        success: function(data) {
            document.getElementById('checkEmailResult').innerHTML = "이메일 전송이 완료되었습니다. 인증번호를 입력해주세요.";
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
}

// 인증번호 유효성 체크
function authNumCheck() {
    // 초기화
    document.getElementById('checkEmailResult').innerHTML = '';
    document.getElementById('authNum').classList.remove("fieldError");
    var authNumErr = document.getElementById('authNumErr');
    if(authNumErr != null) {
        authNumErr.innerHTML = '';
    }

    var authNum = document.getElementById('authNum').value;

    if(authNum != '' && authNum.length == 6) {
        emailAuthResult = true;
    }
}

function checkForm() {
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
    var emailErr = document.getElementById('emailErr');
    var authNumErr = document.getElementById('authNumErr');

    emailCheckResult = false;
    emailAuthResult = false;

    if(emailErr == null) {
        emailCheckResult = true;
    }
}