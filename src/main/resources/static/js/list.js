// 리스트 추가
function createListForm() {
    // input 태그 추가
    var htmlString = "<label for='name' class='mr1p5r'>리스트 이름</label>";
    htmlString += "<input type='text' id='name' required class='form-control input-name'>";
    htmlString += "<p class='text-error inline' id='nameErr'></p>";

    document.getElementById('createInput').innerHTML = htmlString;

    // 버튼 추가
    var btnString = "<input type='button' value='등록' onclick='makeList()' class='btn btn-dark btn-list'>";
    btnString += "<input type='button' value='취소' onclick='makeListCancel()' class='btn btn-outline-dark btn-list'>";
    document.getElementById('createBtn').innerHTML = btnString;
}

function makeList() {
    var name = document.getElementById('name').value;

    if(!!name?.trim()) {
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
    } else {
        document.getElementById('name').classList.add("fieldError");
        document.getElementById('nameErr').innerHTML = '리스트 이름을 입력해주세요.';
    }
}

// 리스트 추가 취소
function makeListCancel() {
    var htmlString = "<img src='/image/plus.png' onclick='createListForm()' style='cursor: pointer; margin-right: 0.3rem'>";
    htmlString += "<span onclick='createListForm()' style='cursor: pointer;'>새 리스트 만들기</span>";
    document.getElementById('createInput').innerHTML = htmlString;
    document.getElementById('createBtn').innerHTML = "";
}


// 리스트 삭제
function deleteList(listId) {
    var param = {"listId":listId};

    $.ajax({
        url: '/list/delete',
        data: JSON.stringify(param),
        contentType: "application/json; charset=UTF-8",
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


// 리스트 수정
function modifyListForm(listId, listName, totalCnt, obj) {
    var modifyTags = obj.parentElement.parentElement.childNodes;

    for(var i = 0; i < modifyTags.length; i++) {
        if(modifyTags[i].className != undefined && modifyTags[i].className != '') {

            if(modifyTags[i].classList.contains("modifyInput")) {
                var modifyInputTag = modifyTags[i];

                // input 태그 추가
                var htmlString = "<input type='text' id='modifyName' value='" + listName + "' required class='form-control input-name'>";
                htmlString += "<input type='hidden' id='originalName' value='" + listName + "'>";
                htmlString += "<input type='hidden' id='totalCnt' value='" + totalCnt + "'>";
                htmlString += "<p class='text-error' id='modifyNameErr inline'></p>";

                modifyInputTag.innerHTML = htmlString;
            }
            else if(modifyTags[i].classList.contains("modifyBtn")) {
                var modifyBtnTag = modifyTags[i];

                // 버튼 추가
                var btnString = "<input type='button' value='수정' onclick='modifyList(" + listId + ")' class='btn btn-dark btn-list'>";
                btnString += `<input type='button' value='취소' onclick='modifyListCancel(${listId}, this)' class='btn btn-outline-dark btn-list'>`;
                modifyBtnTag.innerHTML = btnString;
            }
        }
    }
}

function modifyList(listId) {
    var modifyName = document.getElementById('modifyName').value;

    if(!!modifyName?.trim()) {
        var param = {"listId":listId, "modifyName":modifyName};

        $.ajax({
            url: '/list/modify',
            data: JSON.stringify(param),
            contentType: "application/json; charset=UTF-8",
            type: 'POST',
            dataType: 'json',
            success: function(data) {
                location.reload();
            },
            error: function(data){
                console.log(data);
                if(data.responseJSON.code == "modifyName") {
                    document.getElementById('modifyName').classList.add("fieldError");
                    document.getElementById('modifyNameErr').innerHTML = data.responseJSON.message;
                }
                else if(data.responseJSON.code == "global") {
                    location.reload();
                }
            }
        });
    } else {
        document.getElementById('modifyName').classList.add("fieldError");
        document.getElementById('modifyNameErr').innerHTML = '리스트 이름을 입력해주세요.';
    }
}

// 리스트 수정 취소
function modifyListCancel(listId, obj) {
    var originalName = document.getElementById('originalName').value;
    var totalCnt = document.getElementById('totalCnt').value;
    var modifyTags = obj.parentElement.parentElement.childNodes;

    for(var i = 0; i < modifyTags.length; i++) {
        if(modifyTags[i].className != undefined && modifyTags[i].className != '') {

            if(modifyTags[i].classList.contains("modifyInput")) {
                var modifyInputTag = modifyTags[i];

                // input 태그
                var htmlString = "<span class='fw-600 mr0p4r'>" + originalName + "</span>";
                htmlString += "<span>" + totalCnt + "</span>";

                modifyInputTag.innerHTML = htmlString;
            }
            else if(modifyTags[i].classList.contains("modifyBtn")) {
                var modifyBtnTag = modifyTags[i];

                // 버튼
                var btnString = "";
                if(totalCnt > 0) {
                    btnString += `<span onclick="location.href=&quot;/list/${listId}&quot;" class='link'>상세보기</span>`;
                }

                btnString += `<span onclick="modifyListForm(${listId}, &quot;${originalName}&quot;, ${totalCnt}, this)" class='link ml0p8r'>수정</span>`;

                if(totalCnt == 0) {
                    btnString += `<span onclick='deleteList(${listId})' class='link ml0p8r'>삭제</span>`;
                }

                modifyBtnTag.innerHTML = btnString;
            }
        }
    }
}