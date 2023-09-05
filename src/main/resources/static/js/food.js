// 별점 초기화
const drawScore = (target) => {
  document.querySelector('.score span').style.width = `${target.value * 20}%`;
  document.querySelector('input[name="score"]').value = `${target.value}`;
  document.getElementById('scoreDisplay').innerHTML = `${target.value}`;
}

function readImage(input) {
    // 이전 파일들 삭제
    document.getElementById("preview-thumbnail").src = "https://dummyimage.com/200x250/fff/000&text=image";
    document.getElementById("thumbnail-fake").value = "*.jpg/png/gif Only";

    // 파일 수정 시 사용
    var oTag = document.getElementById('oldThumbnailName');
    if(oTag != null) {
        oTag.value = '';
    }

    // 인풋 태그에 파일이 있는 경우
    if(input.files && input.files[0]) {
        // 이미지 파일인지 검사
        var fileRegex = /(.*?)\.(jpg|jpeg|png|gif|bmp|pdf)$/;
        if(!input.files[0].name.match(fileRegex)) {
            document.getElementById("thumbnail").value = "";
            document.getElementById("thumbnail-fake").value = "*.jpg/png/gif Only";

            alert('이미지 파일만 업로드 가능합니다.');
            return;
        }

        // 이미지 파일 사이즈 체크
        var maxSize = 1024 * 1024 * 10;
        if(input.files[0].size > maxSize) {
            document.getElementById("thumbnail").value = "";
            document.getElementById("thumbnail-fake").value = "*.jpg/png/gif Only";

            alert('파일 사이즈는 10MB 까지 가능합니다.');
            return;
        }

        // FileReader 인스턴스 생성
        const reader = new FileReader();
        // 이미지가 로드가 된 경우
        reader.onload = e => {
            document.getElementById("preview-thumbnail").src = e.target.result;
        }
        // reader가 이미지 읽도록 하기
        reader.readAsDataURL(input.files[0]);

        if(input.files[0].name.length > 20) {
            document.getElementById("thumbnail-fake").value = input.files[0].name.substr(0, 20) + "...";
        } else {
            document.getElementById("thumbnail-fake").value = input.files[0].name;
        }
    }
}

function readMultipleImage(input) {
    // 이전 파일들 삭제
    document.getElementById("preview-picture1").src = "https://dummyimage.com/200x250/fff/000&text=image";
    document.getElementById("preview-picture2").src = "https://dummyimage.com/200x250/fff/000&text=image";
    document.getElementById("preview-picture3").src = "https://dummyimage.com/200x250/fff/000&text=image";
    document.getElementById("preview-picture4").src = "https://dummyimage.com/200x250/fff/000&text=image";

    document.getElementById("picture-fake").value = "*.jpg/png/gif Only";

    // 파일 수정 시 사용
    var oTag = document.getElementById('oldFoodFileNames');
    if(oTag != null) {
        oTag.value = '';
    }

    // 인풋 태그에 파일들이 있는 경우
    if(input.files && input.files[0]) {
        // 파일 수가 4개가 넘을 때
        if(input.files.length > 4) {
            document.getElementById("foodPictures").value = "";
            document.getElementById("picture-fake").value = "*.jpg/png/gif Only";

            alert('파일 개수는 4개까지 가능합니다.');
            return;
        }

        // 유사배열을 배열로 변환 (반복문으로 처리하기 위해)
        const fileArr = Array.from(input.files);

        // 이미지 파일인지 검사
        var fileRegex = /(.*?)\.(jpg|jpeg|png|gif|bmp|pdf)$/;
        var isValidRegex = true;

        // 이미지 파일 사이즈 체크
        var maxSize = 1024 * 1024 * 10;
        var isValidSize = true;

        fileArr.every(function(file) {
            if(!file.name.match(fileRegex)) {
                isValidRegex = false;
                document.getElementById("foodPictures").value = "";
                document.getElementById("picture-fake").value = "*.jpg/png/gif Only";

                alert('이미지 파일만 업로드 가능합니다.');
                return false;
            }

            if(file.size > maxSize) {
                isValidSize = false;
                document.getElementById("foodPictures").value = "";
                document.getElementById("picture-fake").value = "*.jpg/png/gif Only";

                alert('파일 하나당 최대 10MB 까지 가능합니다.');
                return false;
            }
        });

        if(isValidRegex && isValidSize) {
            var fileName = "";
            fileArr.forEach(function(file, index) {
                fileName += file.name;

                const reader = new FileReader();

                reader.onload = e => {
                    if(index == 0) {
                        document.getElementById("preview-picture1").src = e.target.result;
                    }
                    else if(index == 1) {
                        document.getElementById("preview-picture2").src = e.target.result;
                    }
                    else if(index == 2) {
                        document.getElementById("preview-picture3").src = e.target.result;
                    }
                    else if(index == 3) {
                        document.getElementById("preview-picture4").src = e.target.result;
                    }
                }

                reader.readAsDataURL(file);
            });

            if(fileName.length > 20) {
                document.getElementById("picture-fake").value = fileName.substr(0, 20) + "...";
            } else {
                document.getElementById("picture-fake").value = fileName;
            }
        }
    }
}

function foodPicturesDelete() {
    // 이전 파일들 삭제
    document.getElementById("preview-picture1").src = "https://dummyimage.com/200x250/fff/000&text=image";
    document.getElementById("preview-picture2").src = "https://dummyimage.com/200x250/fff/000&text=image";
    document.getElementById("preview-picture3").src = "https://dummyimage.com/200x250/fff/000&text=image";
    document.getElementById("preview-picture4").src = "https://dummyimage.com/200x250/fff/000&text=image";

    document.getElementById("picture-fake").value = "*.jpg/png/gif Only";
    document.getElementById("foodPictures").value = "";

    var oTag = document.getElementById('oldFoodFileNames');
    var foodPicturesTag = document.getElementById('foodPictures');

    if(oTag != null) {
        oTag.value = '';
    }

    if(foodPicturesTag != null) {
        foodPicturesTag.value = '';
    }
}

function findAddress() {
    var win = window.open("/food/findAddress", "주소찾기", "width=1210,height=690,top=200,left=300");
}

function checkForm(isCreate) {
    // 카테고리 선택 X
    if(document.querySelector('input[name="categoryIds"]:checked') == null) {
        document.getElementById("categoryErr").innerHTML = "카테고리를 하나 이상 선택해주세요.";
        window.scrollTo(0,0);
        return false;
    }

    // FileController 파일 용량이 허용범위인지 체크 -> 성공시 FoodController 실행
    var formData = new FormData();

    var thumbnail = $("input[name='thumbnail']");
    var thumbnailFile = thumbnail[0].files;

    var foodPictures = $("input[name='foodPictures']");
    var foodPicturesFile = foodPictures[0].files;

    formData.append("files", thumbnailFile[0]);
    for(var i = 0; i < foodPicturesFile.length; i++) {
        formData.append("files", foodPicturesFile[i]);
    }

    var fileSaved = false;
    $.ajax({
        url: '/file/sizeValidate',
        processData: false,
        contentType: false,
        data: formData,
        type: 'POST',
        dataType: 'json',
        success: function(data) {
            submitForm(isCreate);
        },
        error: function(data){
            console.log(data);
            if(data.responseJSON.code == "file too large") {
                document.getElementById("foodPicturesErr").innerHTML = data.responseJSON.message;
            } else {
                document.getElementById("globalErr").innerHTML = "관리자에게 문의하세요.";
            }
        }
    });
}

function submitForm(isCreate) {
    if(isCreate) {
        document.form.action = "/food/create";
    } else {
        var foodId = document.getElementById('foodId').value;
        document.form.action = "/food/modify/" + foodId;
    }

    document.form.submit();
}

function listSearch(pageNum) {
    // 검색 조건 변경 시 페이지 번호 0으로 설정
    if(isNaN(pageNum)) {
        pageNum = 0;
    }

    document.getElementById('page').value = pageNum;
    document.form.submit();
}

function visitDateAll() {
    document.getElementById('visitDateStart').value = null;
    document.getElementById('visitDateEnd').value = null;

    listSearch();
}

function visitDateSpec() {
    document.getElementById('visitDateAll').classList.add("link");

    document.getElementById('visitDateSpec').classList.remove("link");
    document.getElementById('visitDateSpan').style.display = "inline";
}

function reset() {
    location.href = '/food/list';
}



















