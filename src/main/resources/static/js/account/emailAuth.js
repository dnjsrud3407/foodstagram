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
    document.getElementById('authNum').value = '';
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

// 인증번호 동일한지 체크
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