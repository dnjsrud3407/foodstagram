function login() {
    var loginId = document.getElementById('loginId').value;
    var password = document.getElementById('password').value;

    if(loginId == '') {
        document.getElementById('loginId').focus();
        document.getElementById('globalErr').innerHTML = requiredLoginId;
        return;
    }

    if(password == '') {
        document.getElementById('password').focus();
        document.getElementById('globalErr').innerHTML = requiredPassword;
        return;
    }

    var param = {"loginId":loginId, "password":password}
    $.ajax({
        url: '/login',
        data: JSON.stringify(param),
        contentType: "application/json; charset=UTF-8",
        type: 'POST',
        dataType: 'json',
        success: function(data, textStatus, request) {
            // 메인 페이지로 이동
            location.href = '/';
        },
        error:function(data){
            document.getElementById('globalErr').innerHTML = data.responseJSON.message;
        }
    });
}
