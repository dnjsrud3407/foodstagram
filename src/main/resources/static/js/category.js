const modal = document.getElementById("modal");

// 모달창의 X 버튼, 취소 버튼을 누르면 모달창 꺼지게 하기
const closeArea = modal.querySelector(".close-area");
const closeBtn = modal.querySelector(".close-btn");
closeArea.addEventListener("click", e => {
    closeModal();
});
closeBtn.addEventListener("click", e => {
    closeModal();
});

// 모달창 바깥 영역을 클릭하면 모달창 꺼지게 하기
modal.addEventListener("click", e => {
    const evTarget = e.target;
    if(evTarget.classList.contains("modal-overlay")) {
        closeModal();
    }
});

// ESC 키를 누르면 모달창 꺼지게 하기
window.addEventListener("keyup", e => {
    if(modal.style.display === "flex" && e.key === "Escape") {
        closeModal();
    }
});

function closeModal() {
    document.getElementById('name').value = "";

    modal.style.display = "none";
    document.getElementById('name').classList.remove("fieldError");
    document.getElementById("nameErr").innerHTML = "";
    document.getElementById("globalErr").innerHTML = "";
}

function createModal() {
    document.getElementById("modal").style.display = "flex";
}

// 카테고리 등록
function createCategory() {
    // 이름이 중복인지, 유효성 확인
    $.ajax({
        url: '/admin/category/create',
        data: $("#form").serialize(),
        type: 'POST',
        dataType: 'json',
        success: function(data) {
            location.reload();
        },
        error: function(data){
            console.log(data);
            if(data.responseJSON.code == "name") {
                document.getElementById('name').classList.add("fieldError");
                document.getElementById("nameErr").innerHTML = data.responseJSON.message;
            } else {
                document.getElementById("globalErr").innerHTML = data.responseJSON.message;
            }
        }
    });
}

// 수정 폼 생성
function modifyCategoryForm(id) {
    // 수정 중인 카테고리 있다면 중지
    if(document.querySelectorAll("[id='modifyBtn']").length > 0) {
        alert('수정 중인 카테고리가 있습니다.');
        return;
    }

    // name 태그 생성
    document.getElementById("originalName_" + id).style.display = "none";
    var originalName = document.getElementById('originalName_' + id).innerHTML;
    var inputNameTag = "<input type='search' id='modifyName' value='" + originalName + "'>";
    var errTag = "<p class='text-error' id='modifyNameErr'></p>";
    $("#td-name_" + id).append(inputNameTag);
    $("#td-name_" + id).append(errTag);


    // isDel 버튼 활성화
    document.getElementById("isDel_" + id).disabled = false;

    // 버튼 수정 - 수정 버튼 비활성화 / 완료, 취소 버튼 추가
    document.getElementById("modifyFormBtn_" + id).style.display = "none";
    var modifyBtnTag = "<input type='button' value='완료' id='modifyBtn' onclick='modifyCategory(" + id + ")'>";
    var cancelBtnTag = "<input type='button' value='취소' id='modifyCancelBtn' onclick='location.reload();'>";
    $("#td-btn_" + id).append(modifyBtnTag);
    $("#td-btn_" + id).append(cancelBtnTag);
}

function modifyCategory(id) {
    var name = document.getElementById("modifyName").value;
    var isDel = $("#isDel_" + id).prop('checked');

    var param = {"id":id, "name":name, "isDel":isDel};

    // 이름이 중복인지, 비활성화 가능한지 유효성 확인
    $.ajax({
        url: '/admin/category/modify',
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
                document.getElementById('modifyName').classList.add("fieldError");
            }

            document.getElementById('modifyNameErr').innerHTML = data.responseJSON.message;
        }
    });
}