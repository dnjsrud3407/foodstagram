var newPasswordCheckResult = false;
var newPasswordConfirmCheckResult = false;

function newPasswordCheck() {
    // 초기화
    newPasswordCheckResult = false;
    newPasswordConfirmCheckResult = false;
    document.getElementById('checkNewPasswordResult').innerHTML = '';
    document.getElementById('newPassword').classList.remove("fieldError");
    var passwordErr = document.getElementById('newPasswordErr');
    if(passwordErr != null) {
        passwordErr.innerHTML = '';
    }

    // 비밀번호 정규식 확인
    var password = document.getElementById('newPassword').value;
    let regex = new RegExp('^[a-zA-Z0-9`~!@#$%^&*()+-_=|:<>,.?/]{8,16}$');

    // 정규식 통과
    if(regex.test(password)) {
        newPasswordCheckResult = true;
    } else { // 정규식에 맞지 않을 때
        document.getElementById('checkNewPasswordResult').innerHTML = formatPassword;
        document.getElementById('newPassword').classList.add("fieldError");
    }

    // 비밀번호 확인에 값이 있다면 함수 실행
    var passwordConfirm = document.getElementById('newPasswordConfirm').value;
    if(passwordConfirm != '') {
        newPasswordConfirmCheck();
    }
}

function newPasswordConfirmCheck() {
    // 초기화
    newPasswordConfirmCheckResult = false;
    document.getElementById('checkNewPasswordConfirmResult').innerHTML = '';
    document.getElementById('newPasswordConfirm').classList.remove("fieldError");
    var passwordConfirmErr = document.getElementById('newPasswordConfirmErr');
    if(passwordConfirmErr != null) {
        passwordConfirmErr.innerHTML = '';
    }

    // 비밀번호, 비밀번호 확인 일치한지 확인
    var password = document.getElementById('newPassword').value;
    var passwordConfirm = document.getElementById('newPasswordConfirm').value;

    if(password == passwordConfirm) {
        newPasswordConfirmCheckResult = true;
    } else {
        document.getElementById('checkNewPasswordConfirmResult').innerHTML = equalNewPasswordConfirm;
        document.getElementById('newPasswordConfirm').classList.add("fieldError");
    }
}

function checkForm() {
    if(!newPasswordCheckResult) {
        document.getElementById('newPassword').focus();
        return false;
    }

    if(!newPasswordConfirmCheckResult) {
        document.getElementById('newPasswordConfirm').focus();
        return false;
    }

    return true;
}

// 백에서 오류발생한다면
var globalErr = document.getElementById('globalErr');
if(globalErr != null) {
    var newPasswordErr = document.getElementById('newPasswordErr');
    var newPasswordConfirmErr = document.getElementById('newPasswordConfirmErr');

    if(newPasswordErr == null) {
        newPasswordCheckResult = true;
    }

    if(newPasswordConfirmErr == null) {
        newPasswordConfirmCheckResult = true;
    }
}