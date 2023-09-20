# Foodstagram
프로젝트 URL: <a href="http://144.24.91.13:8080/">푸드스타그램</a>

## 프로젝트 소개
맛있는 음식점을 기억하고 감상평을 남기기 위해 만든 개인 웹 프로젝트입니다.
다른 사용자와 공유하지 않은 개인적인 공간으로 게시글을 작성할 수 있습니다.
음식점에 점수를 매기거나 리스트로 등록하기, 지도 기능 등 가게에 대한 정보를 편리하게 확인할 수 있습니다.
<br><br>
SpringBoot로 개발하였고 Kakao OAuth2 로그인 기능, 로그인시 JWT 발급하여 Redis에 저장, Oracle Cloud로 배포 등 여러 기능을 사용하여 구현하였습니다.

<br>

## 개발 환경 및 사용된 기술
개발 환경
- SpringBoot 3.1.0, Gradle, Java 17
- Oracle Database 19c
- Oracle cloud instance, Oracle cloud ATP
<br><br>

사용된 기술
- Redis memory, JWT
- HTML, CSS, JS, JQuery, Thymeleaf
- Git, Github
- Kakao map, OAuth2 API, Gmail smtp
- Spring Security

<br>

## Database ERD
<img title="db_erd" src="https://user-images.githubusercontent.com/52884298/269186668-46981a0c-25bf-40ee-804c-acdeef65a8aa.png" width="600px">

<br><br>
<hr><br>

## 주요 기능

프로젝트 주요기능은 게시글, 리스트, 마이페이지, 계정 서비스, 관리자 서비스 입니다.

1. 게시글<br>
   게시글 관리 기능으로 음식점에 대한 간단한 정보와 주소, 사진 등록을 등록합니다.<br>
   저장한 게시글은 검색조건을 설정하여 검색할 수 있고 목록 형태로 확인할 수 있습니다.


2. 리스트<br>
   게시글을 리스트로 관리할 수 있습니다.<br>
   예를 들어 경주 여행이나 부산 맛집 등 리스트를 생성하여 리스트 별로 게시글을 한 번에 확인할 수 있습니다.


3. 마이페이지<br>
   회원이 작성한 게시글, 리스트 수를 확인할 수 있습니다.<br> 
   또한 비밀번호 변경, 회원 탈퇴, 로그 아웃 및 계정과 관련된 기능을 진행할 수 있습니다.


4. 계정 서비스<br>
   계정 서비스에는 회원가입, 로그인, 아이디, 비밀번호 찾기 기능이 있습니다.<br> 
   회원가입, 아이디, 비밀번호 찾기는 이메일을 인증하여 진행합니다.<br>
   로그인으로 일반 회원가입과 Kakao 간편 회원가입이 있습니다.<br>
   로그인 시 Redis In memory에 JWT 토큰을 관리하여 최대 2주동안 로그인 유지가 가능합니다.


5. 관리자 서비스<br>
   게시글 카테고리를 관리할 수 있습니다.

<br>

## 시연 영상
- 로그인 및 회원가입<br>
<img title="join_login" src="https://user-images.githubusercontent.com/52884298/268448285-0b872e22-4538-4b54-8cc1-3bb94e698738.gif" width="1000px">

<br>

- 카카오 로그인<br>
<img title="kakaoLogin" src="https://user-images.githubusercontent.com/52884298/268448542-2f675b23-d2fe-48c7-a47c-4667f832376f.gif" width="1000px">

<br>

- 게시글 검색 및 목록<br>

https://github.com/dnjsrud3407/foodstagram/assets/52884298/61e74790-c74b-4da2-9b42-36ca97202c7d

<br><br>

- 게시글 상세보기<br>

https://github.com/dnjsrud3407/foodstagram/assets/52884298/07c34f3d-83d0-44df-9d97-610a9634db4e

<br><br>

- 리스트 목록 및 등록하기
<img title="list_detail_create" src="https://user-images.githubusercontent.com/52884298/268457514-409a7e11-4824-4ffb-8017-3da0012bd706.gif" width="1000px">
