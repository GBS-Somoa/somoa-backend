<div align="center"> 
    <img src="./docs-images/service-logo.png" width="500" />
    <h2>소모아 백엔드</h2>
</div>
<br/>

# 기술 스택

|                       Java 17                        |                     Spring Boot 3.2.3                      |                     Spring Webflux 3.2.3                      |                     Spring Security 6.2.2                      |                         Firebase                         |
| :--------------------------------------------------: | :--------------------------------------------------------: | :-----------------------------------------------------------: | :------------------------------------------------------------: | :------------------------------------------------------: |
| <img src="./docs-images/icon/Java.png" height="100"> | <img src="./docs-images/icon/SpringBoot.png" height="100"> | <img src="./docs-images/icon/SpringWebflux.png" height="100"> | <img src="./docs-images/icon/SpringSecurity.png" height="100"> | <img src="./docs-images/icon/Firebase.png" height="100"> |

|                     MariaDB 11.4.1                      |                         MongoDB                         |
| :-----------------------------------------------------: | :-----------------------------------------------------: |
| <img src="./docs-images/icon/MariaDB.png" height="100"> | <img src="./docs-images/icon/MongoDB.png" height="100"> |

# ERD

<img alt="erd" src="./docs-images/erd.png">

# API

## 인증

|                                     Method                                      | URI               | 설명            |                                     우선순위                                      |
| :-----------------------------------------------------------------------------: | ----------------- | --------------- | :-------------------------------------------------------------------------------: |
| <img src="https://img.shields.io/badge/POST-7acaff?style=flat&logoColor=white"> | /api/user/signup  | 회원가입        |  <img src="https://img.shields.io/badge/Low-f4ff59?style=flat&logoColor=white">   |
| <img src="https://img.shields.io/badge/POST-7acaff?style=flat&logoColor=white"> | /api/user/login   | 로그인          | <img src="https://img.shields.io/badge/Middle-00ba3e?style=flat&logoColor=white"> |
| <img src="https://img.shields.io/badge/POST-7acaff?style=flat&logoColor=white"> | /api/user/logout  | 로그아웃        |  <img src="https://img.shields.io/badge/Low-f4ff59?style=flat&logoColor=white">   |
| <img src="https://img.shields.io/badge/POST-7acaff?style=flat&logoColor=white"> | /api/user/refresh | 엑세스토큰 갱신 | <img src="https://img.shields.io/badge/Middle-00ba3e?style=flat&logoColor=white"> |

## 그룹

|                                      Method                                       | URI                                               | 설명                       |                                     우선순위                                      |
| :-------------------------------------------------------------------------------: | ------------------------------------------------- | -------------------------- | :-------------------------------------------------------------------------------: |
|  <img src="https://img.shields.io/badge/POST-7acaff?style=flat&logoColor=white">  | /api/groups                                       | 그룹 생성                  |  <img src="https://img.shields.io/badge/High-ff281c?style=flat&logoColor=white">  |
|  <img src="https://img.shields.io/badge/GET-ffd769?style=flat&logoColor=white">   | /api/groups                                       | 그룹 목록 조회             |  <img src="https://img.shields.io/badge/High-ff281c?style=flat&logoColor=white">  |
|  <img src="https://img.shields.io/badge/GET-ffd769?style=flat&logoColor=white">   | /api/groups/{group_id}                            | 그룹 상세 조회             |  <img src="https://img.shields.io/badge/High-ff281c?style=flat&logoColor=white">  |
| <img src="https://img.shields.io/badge/PATCH-a1ffa1?style=flat&logoColor=white">  | /api/groups/{group_id}                            | 그룹 이름 수정             |  <img src="https://img.shields.io/badge/Low-f4ff59?style=flat&logoColor=white">   |
| <img src="https://img.shields.io/badge/DELETE-ff8ff8?style=flat&logoColor=white"> | /api/groups/{group_id}                            | 그룹 삭제                  |  <img src="https://img.shields.io/badge/Low-f4ff59?style=flat&logoColor=white">   |
| <img src="https://img.shields.io/badge/PATCH-a1ffa1?style=flat&logoColor=white">  | /api/groups/{group_id}/users/{user_id}/permission | 그룹 멤버의 권한 수정      | <img src="https://img.shields.io/badge/Middle-00ba3e?style=flat&logoColor=white"> |
| <img src="https://img.shields.io/badge/DELETE-ff8ff8?style=flat&logoColor=white"> | /api/groups/{group_id}/leave                      | 그룹 나가기                | <img src="https://img.shields.io/badge/Middle-00ba3e?style=flat&logoColor=white"> |
|  <img src="https://img.shields.io/badge/POST-7acaff?style=flat&logoColor=white">  | /api/groups/{group_id}/users                      | 그룹 멤버 추가             |  <img src="https://img.shields.io/badge/High-ff281c?style=flat&logoColor=white">  |
|  <img src="https://img.shields.io/badge/GET-ffd769?style=flat&logoColor=white">   | /api/groups/{group_id}/users                      | 그룹에 속한 멤버 조회      |  <img src="https://img.shields.io/badge/High-ff281c?style=flat&logoColor=white">  |
| <img src="https://img.shields.io/badge/DELETE-ff8ff8?style=flat&logoColor=white"> | /api/groups/{group_id}/users                      | 그룹 멤버 삭제             |  <img src="https://img.shields.io/badge/Low-f4ff59?style=flat&logoColor=white">   |
|  <img src="https://img.shields.io/badge/GET-ffd769?style=flat&logoColor=white">   | /api/groups/{group_id}/devices                    | 그룹에 속한 기기 목록 조회 |  <img src="https://img.shields.io/badge/High-ff281c?style=flat&logoColor=white">  |
|  <img src="https://img.shields.io/badge/GET-ffd769?style=flat&logoColor=white">   | /api/groups/{group_id}/orders                     | 그룹에 속한 주문 목록 조회 |  <img src="https://img.shields.io/badge/High-ff281c?style=flat&logoColor=white">  |
| <img src="https://img.shields.io/badge/PATCH-a1ffa1?style=flat&logoColor=white">  | /api/groups/{group_id}/alarm                      | 그룹 알람 여부 변경        |  <img src="https://img.shields.io/badge/Low-f4ff59?style=flat&logoColor=white">   |
| <img src="https://img.shields.io/badge/PATCH-a1ffa1?style=flat&logoColor=white">  | /api/groups/group-order                           | 그룹 표시 순서 변경        |  <img src="https://img.shields.io/badge/Low-f4ff59?style=flat&logoColor=white">   |

## 기기

|                                      Method                                       | URI                      | 설명                |                                    우선순위                                     |
| :-------------------------------------------------------------------------------: | ------------------------ | ------------------- | :-----------------------------------------------------------------------------: |
|  <img src="https://img.shields.io/badge/POST-7acaff?style=flat&logoColor=white">  | /api/devices             | 기기 등록           | <img src="https://img.shields.io/badge/High-ff281c?style=flat&logoColor=white"> |
|  <img src="https://img.shields.io/badge/GET-ffd769?style=flat&logoColor=white">   | /api/devices/{device_id} | 기기 상세 조회      | <img src="https://img.shields.io/badge/High-ff281c?style=flat&logoColor=white"> |
| <img src="https://img.shields.io/badge/PATCH-a1ffa1?style=flat&logoColor=white">  | /api/devices/{device_id} | 기기 이름 수정      | <img src="https://img.shields.io/badge/Low-f4ff59?style=flat&logoColor=white">  |
| <img src="https://img.shields.io/badge/DELETE-ff8ff8?style=flat&logoColor=white"> | /api/devices/{device_id} | 기기 삭제           | <img src="https://img.shields.io/badge/Low-f4ff59?style=flat&logoColor=white">  |
|  <img src="https://img.shields.io/badge/POST-7acaff?style=flat&logoColor=white">  | /api/devices/{device_id} | 기기 상태 정보 받음 | <img src="https://img.shields.io/badge/High-ff281c?style=flat&logoColor=white"> |

## 소모품

|                                      Method                                      | URI                             | 설명                                     |                                     우선순위                                      |
| :------------------------------------------------------------------------------: | ------------------------------- | ---------------------------------------- | :-------------------------------------------------------------------------------: |
|  <img src="https://img.shields.io/badge/GET-ffd769?style=flat&logoColor=white">  | /api/supplies/barcode           | 소모품 바코드 조회                       |  <img src="https://img.shields.io/badge/Low-f4ff59?style=flat&logoColor=white">   |
| <img src="https://img.shields.io/badge/PATCH-a1ffa1?style=flat&logoColor=white"> | /api/supplies/{supply_id}       | 소모품 보유량 수정                       |  <img src="https://img.shields.io/badge/High-ff281c?style=flat&logoColor=white">  |
|  <img src="https://img.shields.io/badge/GET-ffd769?style=flat&logoColor=white">  | /api/supplies                   | 조건에 맞는 소모품 목록                  | <img src="https://img.shields.io/badge/Middle-00ba3e?style=flat&logoColor=white"> |
|  <img src="https://img.shields.io/badge/GET-ffd769?style=flat&logoColor=white">  | /api/supplies/all               | 유저가 속한 그룹의 전체 소모품 목록      | <img src="https://img.shields.io/badge/Middle-00ba3e?style=flat&logoColor=white"> |
|  <img src="https://img.shields.io/badge/GET-ffd769?style=flat&logoColor=white">  | /api/supplies/groupsupply       | 유저가 속한 특정 그룹의 전체 소모품 조회 |  <img src="https://img.shields.io/badge/Low-f4ff59?style=flat&logoColor=white">   |
| <img src="https://img.shields.io/badge/PATCH-a1ffa1?style=flat&logoColor=white"> | /api/supplies/limit/{supply_id} | 소모품 알림기준 수정                     | <img src="https://img.shields.io/badge/Middle-00ba3e?style=flat&logoColor=white"> |

## 주문

|                                      Method                                      | URI                                  | 설명                                                    |                                     우선순위                                      |
| :------------------------------------------------------------------------------: | ------------------------------------ | ------------------------------------------------------- | :-------------------------------------------------------------------------------: |
| <img src="https://img.shields.io/badge/POST-7acaff?style=flat&logoColor=white">  | /api/orders                          | 주문 등록                                               |  <img src="https://img.shields.io/badge/High-ff281c?style=flat&logoColor=white">  |
| <img src="https://img.shields.io/badge/PATCH-a1ffa1?style=flat&logoColor=white"> | /api/orders/{order_store}/{order_id} | 주문 수정 - 배송 현황                                   |  <img src="https://img.shields.io/badge/High-ff281c?style=flat&logoColor=white">  |
|  <img src="https://img.shields.io/badge/GET-ffd769?style=flat&logoColor=white">  | /api/orders                          | 주문 내역 조회                                          | <img src="https://img.shields.io/badge/Middle-00ba3e?style=flat&logoColor=white"> |
|  <img src="https://img.shields.io/badge/GET-ffd769?style=flat&logoColor=white">  | /api/orders/in-progress              | 배송 중인 주문 내역 조회                                | <img src="https://img.shields.io/badge/Middle-00ba3e?style=flat&logoColor=white"> |
|  <img src="https://img.shields.io/badge/GET-ffd769?style=flat&logoColor=white">  | /api/user/orders                     | 주문 목록 중 "배송 중", "주문 목록" 상태 주문 목록 조회 |  <img src="https://img.shields.io/badge/High-ff281c?style=flat&logoColor=white">  |
