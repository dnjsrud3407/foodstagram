var emailCheckResult = false;
var emailAuthResult = false;

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

    if(authNumErr != null && authNumErr.innerHTML == expiredEmail) {
        emailCheckResult = true;
        emailAuthResult = true;
        document.getElementById('emailSendBtn').focus();
    }
}