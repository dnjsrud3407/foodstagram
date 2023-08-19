// 리스트 추가
function createListForm(obj) {
    // input 태그 추가
    var htmlString = "<label for='name'>리스트 이름</label>";
    htmlString += "<input type='text' id='name' name='name' required>";
    htmlString += "<p class='text-error' id='nameErr'></p>";

    document.getElementById('createInput').innerHTML = htmlString;

    // 버튼 추가
    var btnString = "<input type='button' value='등록' onclick='makeList()'>";
    btnString += "<input type='button' value='취소' onclick='makeListCancel()'>";
    document.getElementById('createBtn').innerHTML = btnString;

}

function makeList() {
    var name = document.getElementById('name').value;

    var param = {"name":name};

    $.ajax({
        url: '/list/create',
        data: JSON.stringify(param),
        contentType: "application/json; charset=UTF-8",
        type: 'POST',
        dataType: 'json',
        success: function(data) {
            location.reload();
        },
        error: function(data){
            console.log(data);
            if(data.responseJSON.code == "name") {
                document.getElementById('name').classList.add("fieldError");
            }

            document.getElementById('nameErr').innerHTML = data.responseJSON.message;
        }
    });
}

// 리스트 추가 취소
function makeListCancel() {
    var htmlString = "<span style='cursor: pointer;' onclick='createListForm(this)'>+ 새 리스트 만들기</span>";
    document.getElementById('createInput').innerHTML = htmlString;
    document.getElementById('createBtn').innerHTML = "";
}


// 리스트 삭제
function deleteList(listId) {
    var param = {"listId":listId};

    $.ajax({
        url: '/list/delete',
        data: {"listId":listId},
        contentType: "application/x-www-form-urlencoded",
        type: 'POST',
        dataType: 'json',
        success: function(data) {
            location.reload();
        },
        error: function(data){
            document.getElementById('globalErr').innerHTML = data.responseJSON.message;
        }
    });
}