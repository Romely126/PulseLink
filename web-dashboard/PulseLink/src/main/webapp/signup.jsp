<%@ page contentType="text/html; charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>회원가입</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <script src="https://code.jquery.com/jquery-3.7.1.min.js"></script>
    <style>
        #confirmPassword:invalid {
            border-color: red;
        }
        #confirmPassword.valid {
            border-color: green !important;
        }
        #confirmPassword.invalid {
            border-color: red !important;
        }

        /* 폼 전체 너비를 제한 */
        .container {
            max-width: 600px;
        }

        /* 전화번호 입력 칸 조정 */
        .phone-group select,
        .phone-group input {
            max-width: 120px; /* 최대 너비 설정 */
        }

        /* 폼 요소 간의 여백 추가 */
        .form-control {
            margin-bottom: 10px;
        }
    </style>
</head>
<body>
<div class="container mt-5">
    <h2 class="mb-4">회원가입</h2>
    <form action="signupProcess.jsp" method="post" id="signupForm">
        <!-- 아이디 -->
        <div class="mb-3">
            <label for="id" class="form-label">아이디</label>
            <div class="input-group">
                <input type="text" class="form-control" id="id" name="id" required>
                <button type="button" class="btn btn-secondary" onclick="checkDuplicate()">중복 확인</button>
            </div>
            <div id="idCheckResult" class="form-text"></div>
        </div>

        <!-- 비밀번호 -->
        <div class="mb-3">
            <label for="password" class="form-label">비밀번호</label>
            <input type="password" class="form-control" id="password" name="password" required>
        </div>

        <!-- 비밀번호 확인 -->
        <div class="mb-3">
            <label for="confirmPassword" class="form-label">비밀번호 확인</label>
            <input type="password" class="form-control" id="confirmPassword" name="confirmPassword" required>
        </div>

        <!-- 이름 -->
        <div class="mb-3">
            <label for="name" class="form-label">이름</label>
            <input type="text" class="form-control" id="name" name="name" required>
        </div>

        <!-- 성별 -->
        <div class="mb-3">
            <label class="form-label">성별</label><br>
            <div class="form-check form-check-inline">
                <input class="form-check-input" type="radio" name="gender" id="male" value="남성" required>
                <label class="form-check-label" for="male">남성</label>
            </div>
            <div class="form-check form-check-inline">
                <input class="form-check-input" type="radio" name="gender" id="female" value="여성">
                <label class="form-check-label" for="female">여성</label>
            </div>
        </div>

        <!-- 생년월일 -->
        <div class="mb-3">
            <label for="birth" class="form-label">생년월일</label>
            <input type="date" class="form-control" id="birth" name="birth" required>
        </div>

<!-- 전화번호 -->
<div class="mb-3">
    <label for="phone" class="form-label">전화번호</label>
    <div class="d-flex">
        <select class="form-select" id="phonePrefix" name="phonePrefix" style="width: 100px; height: 38px; margin-right: 10px; -webkit-appearance: none; -moz-appearance: none; appearance: none; padding-right: 25px;">
            <option value="010">010</option>
            <option value="011">011</option>
            <option value="019">019</option>
            <option value="012">012</option>
        </select>
        <input type="text" class="form-control" id="phoneMid" name="phoneMid" maxlength="4" style="width: 100px; height: 38px; margin-right: 10px;" required>
        <input type="text" class="form-control" id="phoneEnd" name="phoneEnd" maxlength="4" style="width: 100px; height: 38px;" required>
    </div>
</div>



        <!-- 이메일 -->
        <div class="mb-3">
            <label for="email" class="form-label">이메일</label>
            <input type="email" class="form-control" id="email" name="email">
        </div>

        <!-- 주소 -->
        <div class="mb-3">
            <label for="post" class="form-label">우편번호</label>
            <div class="input-group mb-2">
                <input type="text" class="form-control" id="post" name="post" readonly>
                <button type="button" class="btn btn-outline-secondary" onclick="execDaumPostcode()">주소 찾기</button>
            </div>
            <input type="text" class="form-control mb-2" id="address" name="address" placeholder="기본주소" readonly>
            <input type="text" class="form-control" id="detailAddress" name="detailAddress" placeholder="상세주소">
        </div>

        <button type="submit" class="btn btn-primary">회원가입</button>
    </form>
</div>

<script>
    // 아이디 중복 확인 AJAX
    function checkDuplicate() {
        const id = $('#id').val();
        if (id.trim() === '') {
            $('#idCheckResult').text("아이디를 입력하세요.").css("color", "red");
            return;
        }

        $.post("checkDuplicate.jsp", { id: id }, function(response) {
            if (response.trim() === "ok") {
                $('#idCheckResult').text("사용 가능한 아이디입니다.").css("color", "green");
            } else {
                $('#idCheckResult').text("이미 사용 중인 아이디입니다.").css("color", "red");
            }
        });
    }

    // 비밀번호 확인 실시간 검사
    $('#confirmPassword, #password').on('input', function() {
        const pw = $('#password').val();
        const cpw = $('#confirmPassword').val();
        const input = $('#confirmPassword');

        if (cpw === '') {
            input.removeClass('valid invalid');
        } else if (pw === cpw) {
            input.removeClass('invalid').addClass('valid');
        } else {
            input.removeClass('valid').addClass('invalid');
        }
    });
</script>
<script src="https://t1.daumcdn.net/mapjsapi/bundle/postcode/prod/postcode.v2.js"></script>
<script>
function execDaumPostcode() {
    new daum.Postcode({
        oncomplete: function(data) {
            // data.zonecode: 우편번호
            // data.roadAddress or data.jibunAddress: 기본 주소
            document.getElementById('post').value = data.zonecode;
            document.getElementById('address').value = data.roadAddress || data.jibunAddress;
            document.getElementById('detailAddress').focus();
        }
    }).open();
}
</script>
</body>
</html>
