# Account Service
- MSA 기반 회원 계정 서비스
- 개발 언어: Kotlin
- 통합 프레임워크: Spring Boot2
  - Spring Security Reactive
  - Spring Webflux
  - Spring Data Mongo Reactive
- Kotlin 의 Coroutine 도입 함으로서, 동시성 처리 지원
- 데이터 베이스: Mongodb
## API 명세
<details>
  <summary>API 보기</summary>

<!-- Generator: Widdershins v4.0.1 -->

<h1 id="account-api">Account API v1.0.0-edge</h1>

> Scroll down for code samples, example requests and responses. Select a language for code samples from the tabs above or the mobile navigation menu.

Base URLs:

* <a href="{host}">{host}</a>

    * **host** -  Default: api.commerce.io

        * api.commerce.io

        * api.commerce.co.kr

# Authentication

- HTTP Authentication, scheme: bearer 

<h1 id="account-api-shippingaddress">ShippingAddress</h1>

## updateShippingAddress

<a id="opIdupdateShippingAddress"></a>

> Code samples

```http
PUT {host}/account/{customerId}/shipping-addresses/{shippingAddressId} HTTP/1.1

Content-Type: application/json

```

`PUT /account/{customerId}/shipping-addresses/{shippingAddressId}`

*배송지 수정*

배송지 수정

> Body parameter

```json
{
  "name": "string",
  "recipient": "string",
  "primaryPhoneNumber": "string",
  "secondaryPhoneNumber": "string",
  "zipCode": "string",
  "line1": "string",
  "line2": "string",
  "primary": true
}
```

<h3 id="updateshippingaddress-parameters">Parameters</h3>

|Name|In|Type|Required|Description|
|---|---|---|---|---|
|customerId|path|string(uuid)|true|계정 ID|
|shippingAddressId|path|string|true|배송지 ID|
|body|body|[ShippingAddressPayload](#schemashippingaddresspayload)|true|배송지 수정 request body|

<h3 id="updateshippingaddress-responses">Responses</h3>

|Status|Meaning|Description|Schema|
|---|---|---|---|
|204|[No Content](https://tools.ietf.org/html/rfc7231#section-6.3.5)|No Content|None|

<aside class="warning">
To perform this operation, you must be authenticated by means of one of the following methods:
aegis
</aside>

## deleteShippingAddress

<a id="opIddeleteShippingAddress"></a>

> Code samples

```http
DELETE {host}/account/{customerId}/shipping-addresses/{shippingAddressId} HTTP/1.1

```

`DELETE /account/{customerId}/shipping-addresses/{shippingAddressId}`

*배송지 삭제*

배송지 삭제

<h3 id="deleteshippingaddress-parameters">Parameters</h3>

|Name|In|Type|Required|Description|
|---|---|---|---|---|
|customerId|path|string(uuid)|true|계정 ID|
|shippingAddressId|path|string|true|배송지 ID|

<h3 id="deleteshippingaddress-responses">Responses</h3>

|Status|Meaning|Description|Schema|
|---|---|---|---|
|204|[No Content](https://tools.ietf.org/html/rfc7231#section-6.3.5)|No Content|None|

<aside class="warning">
To perform this operation, you must be authenticated by means of one of the following methods:
aegis
</aside>

## getShippingAddresses

<a id="opIdgetShippingAddresses"></a>

> Code samples

```http
GET {host}/account/{customerId}/shipping-addresses HTTP/1.1

Accept: application/json

```

`GET /account/{customerId}/shipping-addresses`

*배송지 목록*

배송지 목록

<h3 id="getshippingaddresses-parameters">Parameters</h3>

|Name|In|Type|Required|Description|
|---|---|---|---|---|
|customerId|path|string(uuid)|true|계정 ID|

> Example responses

> 200 Response

```json
[
  {
    "id": "string",
    "name": "string",
    "recipient": "string",
    "primaryPhoneNumber": "string",
    "secondaryPhoneNumber": "string",
    "zipCode": "string",
    "line1": "string",
    "line2": "string",
    "primary": true
  }
]
```

<h3 id="getshippingaddresses-responses">Responses</h3>

|Status|Meaning|Description|Schema|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|OK|Inline|

<h3 id="getshippingaddresses-responseschema">Response Schema</h3>

Status Code **200**

|Name|Type|Required|Restrictions|Description|
|---|---|---|---|---|
|*anonymous*|[[ShippingAddressView](#schemashippingaddressview)]|false|none|[배송지 조회 결과]|
|» id|string|true|none|배송지 ID|
|» name|string|false|none|배송지명|
|» recipient|string|true|none|수령인|
|» primaryPhoneNumber|string|true|none|수령인 연락처|
|» secondaryPhoneNumber|string|false|none|수령인 연락처2|
|» zipCode|string|true|none|우편 번호|
|» line1|string|true|none|배송지 주소|
|» line2|string|false|none|배송지 주소 상세|
|» primary|boolean|true|none|기본 주소지|

<aside class="warning">
To perform this operation, you must be authenticated by means of one of the following methods:
aegis
</aside>

## createShippingAddress

<a id="opIdcreateShippingAddress"></a>

> Code samples

```http
POST {host}/account/{customerId}/shipping-addresses HTTP/1.1

Content-Type: application/json

```

`POST /account/{customerId}/shipping-addresses`

*배송지 생성*

배송지 생성

> Body parameter

```json
{
  "name": "string",
  "recipient": "string",
  "primaryPhoneNumber": "string",
  "secondaryPhoneNumber": "string",
  "zipCode": "string",
  "line1": "string",
  "line2": "string",
  "primary": true
}
```

<h3 id="createshippingaddress-parameters">Parameters</h3>

|Name|In|Type|Required|Description|
|---|---|---|---|---|
|customerId|path|string(uuid)|true|계정 ID|
|body|body|[ShippingAddressPayload](#schemashippingaddresspayload)|true|배송지 생성 request body|

<h3 id="createshippingaddress-responses">Responses</h3>

|Status|Meaning|Description|Schema|
|---|---|---|---|
|201|[Created](https://tools.ietf.org/html/rfc7231#section-6.3.2)|Created|None|

<aside class="warning">
To perform this operation, you must be authenticated by means of one of the following methods:
aegis
</aside>

## getPrimaryShippingAddress

<a id="opIdgetPrimaryShippingAddress"></a>

> Code samples

```http
GET {host}/account/{customerId}/shipping-addresses/primary HTTP/1.1

Accept: application/json

```

`GET /account/{customerId}/shipping-addresses/primary`

*기본 배송지 조회*

기본 배송지 조회

<h3 id="getprimaryshippingaddress-parameters">Parameters</h3>

|Name|In|Type|Required|Description|
|---|---|---|---|---|
|customerId|path|string(uuid)|true|계정 ID|

> Example responses

> 200 Response

```json
{
  "id": "string",
  "name": "string",
  "recipient": "string",
  "primaryPhoneNumber": "string",
  "secondaryPhoneNumber": "string",
  "zipCode": "string",
  "line1": "string",
  "line2": "string",
  "primary": true
}
```

<h3 id="getprimaryshippingaddress-responses">Responses</h3>

|Status|Meaning|Description|Schema|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|OK|[ShippingAddressView](#schemashippingaddressview)|

<aside class="warning">
To perform this operation, you must be authenticated by means of one of the following methods:
aegis
</aside>

<h1 id="account-api-account">Account</h1>

## updateProfilePhoneNumber

<a id="opIdupdateProfilePhoneNumber"></a>

> Code samples

```http
PUT {host}/account/profile/{customerId}/phone-number HTTP/1.1

Content-Type: application/json

```

`PUT /account/profile/{customerId}/phone-number`

*마이페이지 휴대폰 번호 수정*

마이페이지 휴대폰 번호 수정

> Body parameter

```json
{
  "phoneNumber": "string"
}
```

<h3 id="updateprofilephonenumber-parameters">Parameters</h3>

|Name|In|Type|Required|Description|
|---|---|---|---|---|
|customerId|path|string(uuid)|true|고객 ID|
|body|body|[UpdatePhoneNumberPayload](#schemaupdatephonenumberpayload)|true|휴대폰 번호 수정 request body|

<h3 id="updateprofilephonenumber-responses">Responses</h3>

|Status|Meaning|Description|Schema|
|---|---|---|---|
|204|[No Content](https://tools.ietf.org/html/rfc7231#section-6.3.5)|No Content|None|

<aside class="warning">
To perform this operation, you must be authenticated by means of one of the following methods:
aegis
</aside>

## updateProfilePassword

<a id="opIdupdateProfilePassword"></a>

> Code samples

```http
PUT {host}/account/profile/{customerId}/password HTTP/1.1

Content-Type: application/json

```

`PUT /account/profile/{customerId}/password`

*마이페이지 비밀번호 재설정*

마이페이지 비밀번호 재설정

> Body parameter

```json
{
  "phoneNumber": "string",
  "password": "string"
}
```

<h3 id="updateprofilepassword-parameters">Parameters</h3>

|Name|In|Type|Required|Description|
|---|---|---|---|---|
|customerId|path|string(uuid)|true|고객 ID|
|body|body|[UpdatePasswordPayload](#schemaupdatepasswordpayload)|true|비밀번호 재설정 필드|

<h3 id="updateprofilepassword-responses">Responses</h3>

|Status|Meaning|Description|Schema|
|---|---|---|---|
|204|[No Content](https://tools.ietf.org/html/rfc7231#section-6.3.5)|No Content|None|

<aside class="warning">
To perform this operation, you must be authenticated by means of one of the following methods:
aegis
</aside>

## updateProfileName

<a id="opIdupdateProfileName"></a>

> Code samples

```http
PUT {host}/account/profile/{customerId}/name HTTP/1.1

Content-Type: application/json

```

`PUT /account/profile/{customerId}/name`

*마이페이지 이름 수정*

마이페이지 이름 수정

> Body parameter

```json
{
  "name": "string"
}
```

<h3 id="updateprofilename-parameters">Parameters</h3>

|Name|In|Type|Required|Description|
|---|---|---|---|---|
|customerId|path|string(uuid)|true|고객 ID|
|body|body|[UpdateNamePayload](#schemaupdatenamepayload)|true|이름 수정 필드 데이터|

<h3 id="updateprofilename-responses">Responses</h3>

|Status|Meaning|Description|Schema|
|---|---|---|---|
|204|[No Content](https://tools.ietf.org/html/rfc7231#section-6.3.5)|No Content|None|

<aside class="warning">
To perform this operation, you must be authenticated by means of one of the following methods:
aegis
</aside>

## updateProfileImage

<a id="opIdupdateProfileImage"></a>

> Code samples

```http
PUT {host}/account/profile/{customerId}/image HTTP/1.1

Content-Type: application/json

```

`PUT /account/profile/{customerId}/image`

*마이페이지 이미지 경로 수정*

마이페이지 이미지 경로 수정

> Body parameter

```json
{
  "image": "string"
}
```

<h3 id="updateprofileimage-parameters">Parameters</h3>

|Name|In|Type|Required|Description|
|---|---|---|---|---|
|customerId|path|string(uuid)|true|고객 ID|
|body|body|[UpdateProfileImagePayload](#schemaupdateprofileimagepayload)|true|이미지 경로|

<h3 id="updateprofileimage-responses">Responses</h3>

|Status|Meaning|Description|Schema|
|---|---|---|---|
|204|[No Content](https://tools.ietf.org/html/rfc7231#section-6.3.5)|No Content|None|

<aside class="warning">
To perform this operation, you must be authenticated by means of one of the following methods:
aegis
</aside>

## updateProfileEmail

<a id="opIdupdateProfileEmail"></a>

> Code samples

```http
PUT {host}/account/profile/{customerId}/email HTTP/1.1

Content-Type: application/json

```

`PUT /account/profile/{customerId}/email`

*마이페이지 이메일 수정*

마이페이지 이메일 수정

> Body parameter

```json
{
  "email": "string"
}
```

<h3 id="updateprofileemail-parameters">Parameters</h3>

|Name|In|Type|Required|Description|
|---|---|---|---|---|
|customerId|path|string(uuid)|true|고객 ID|
|body|body|[UpdateEmailPayload](#schemaupdateemailpayload)|true|이메일 수정 request body|

<h3 id="updateprofileemail-responses">Responses</h3>

|Status|Meaning|Description|Schema|
|---|---|---|---|
|204|[No Content](https://tools.ietf.org/html/rfc7231#section-6.3.5)|No Content|None|

<aside class="warning">
To perform this operation, you must be authenticated by means of one of the following methods:
aegis
</aside>

## updateProfileBirthday

<a id="opIdupdateProfileBirthday"></a>

> Code samples

```http
PUT {host}/account/profile/{customerId}/birthday HTTP/1.1

Content-Type: application/json

```

`PUT /account/profile/{customerId}/birthday`

*마이페이지 생일 수정*

마이페이지 생일 수정

> Body parameter

```json
{
  "birthday": "string"
}
```

<h3 id="updateprofilebirthday-parameters">Parameters</h3>

|Name|In|Type|Required|Description|
|---|---|---|---|---|
|customerId|path|string(uuid)|true|고객 ID|
|body|body|[UpdateBirthdayPayload](#schemaupdatebirthdaypayload)|true|생일 수정 필드 데이터|

<h3 id="updateprofilebirthday-responses">Responses</h3>

|Status|Meaning|Description|Schema|
|---|---|---|---|
|204|[No Content](https://tools.ietf.org/html/rfc7231#section-6.3.5)|No Content|None|

<aside class="warning">
To perform this operation, you must be authenticated by means of one of the following methods:
aegis
</aside>

## updateProfileAgreement

<a id="opIdupdateProfileAgreement"></a>

> Code samples

```http
PUT {host}/account/profile/{customerId}/agreement HTTP/1.1

Content-Type: application/json

```

`PUT /account/profile/{customerId}/agreement`

*마이페이지 마케팅 동의 항목 수정*

마이페이지 마케팅 동의 항목 수정

> Body parameter

```json
{
  "type": "email",
  "active": true
}
```

<h3 id="updateprofileagreement-parameters">Parameters</h3>

|Name|In|Type|Required|Description|
|---|---|---|---|---|
|customerId|path|string(uuid)|true|고객 ID|
|body|body|[UpdateAgreementPayload](#schemaupdateagreementpayload)|true|동의 항목 필드|

<h3 id="updateprofileagreement-responses">Responses</h3>

|Status|Meaning|Description|Schema|
|---|---|---|---|
|204|[No Content](https://tools.ietf.org/html/rfc7231#section-6.3.5)|No Content|None|

<aside class="warning">
To perform this operation, you must be authenticated by means of one of the following methods:
aegis
</aside>

## legacyActivation

<a id="opIdlegacyActivation"></a>

> Code samples

```http
PUT {host}/account/legacy/activation/{customerId} HTTP/1.1

Content-Type: application/json

```

`PUT /account/legacy/activation/{customerId}`

*v1 회원 계정 로그인 이후 프로필 업데이트*

v1 회원 계정 로그인 이후 프로필 업데이트

> Body parameter

```json
{
  "email": "string",
  "name": "string",
  "phoneNumber": "string",
  "password": "string",
  "agreement": {
    "email": true,
    "sms": true,
    "serviceTerm": true,
    "privacyTerm": true
  }
}
```

<h3 id="legacyactivation-parameters">Parameters</h3>

|Name|In|Type|Required|Description|
|---|---|---|---|---|
|customerId|path|string(uuid)|true|회원 고유 번호|
|body|body|[LegacyActivateProfilePayload](#schemalegacyactivateprofilepayload)|true|프로필 업데이트 필드|

<h3 id="legacyactivation-responses">Responses</h3>

|Status|Meaning|Description|Schema|
|---|---|---|---|
|204|[No Content](https://tools.ietf.org/html/rfc7231#section-6.3.5)|No Content|None|

<aside class="warning">
To perform this operation, you must be authenticated by means of one of the following methods:
aegis
</aside>

## activation

<a id="opIdactivation"></a>

> Code samples

```http
PUT {host}/account/activation/{customerId} HTTP/1.1

Content-Type: application/json

```

`PUT /account/activation/{customerId}`

*로그인 이후 프로필 업데이트*

로그인 이후 프로필 업데이트

> Body parameter

```json
{
  "email": "string",
  "name": "string",
  "phoneNumber": "string",
  "agreement": {
    "email": true,
    "sms": true,
    "serviceTerm": true,
    "privacyTerm": true
  }
}
```

<h3 id="activation-parameters">Parameters</h3>

|Name|In|Type|Required|Description|
|---|---|---|---|---|
|customerId|path|string(uuid)|true|계정 ID|
|body|body|[ActivateProfilePayload](#schemaactivateprofilepayload)|true|프로필 업데이트 request body|

<h3 id="activation-responses">Responses</h3>

|Status|Meaning|Description|Schema|
|---|---|---|---|
|204|[No Content](https://tools.ietf.org/html/rfc7231#section-6.3.5)|No Content|None|

<aside class="warning">
To perform this operation, you must be authenticated by means of one of the following methods:
aegis
</aside>

## validation

<a id="opIdvalidation"></a>

> Code samples

```http
POST {host}/account/validation HTTP/1.1

Content-Type: application/json

```

`POST /account/validation`

*회원 정보 유효성 검사*

회원 정보 유효성 검사

> Body parameter

```json
{
  "email": "string",
  "password": "string",
  "name": "string",
  "phoneNumber": "string",
  "agreement": {
    "serviceTerm": true,
    "privacyTerm": true
  }
}
```

<h3 id="validation-parameters">Parameters</h3>

|Name|In|Type|Required|Description|
|---|---|---|---|---|
|body|body|[AccountValidationPayload](#schemaaccountvalidationpayload)|true|질의 필드|

<h3 id="validation-responses">Responses</h3>

|Status|Meaning|Description|Schema|
|---|---|---|---|
|204|[No Content](https://tools.ietf.org/html/rfc7231#section-6.3.5)|No Content|None|

<aside class="success">
This operation does not require authentication
</aside>

## updatePasswordVerify

<a id="opIdupdatePasswordVerify"></a>

> Code samples

```http
POST {host}/account/update-password/verify/sms HTTP/1.1

Content-Type: application/json
Accept: application/json

```

`POST /account/update-password/verify/sms`

*마이페이지 비밀번호 재설정 휴대폰 번호 인증*

마이페이지 비밀번호 재설정 휴대폰 번호 인증

> Body parameter

```json
{
  "key": "string",
  "code": "string"
}
```

<h3 id="updatepasswordverify-parameters">Parameters</h3>

|Name|In|Type|Required|Description|
|---|---|---|---|---|
|body|body|[VerificationPayload](#schemaverificationpayload)|true|인증 필드 데이터|

> Example responses

> 201 Response

```json
{
  "expiredIn": 0,
  "expiredAt": 0
}
```

<h3 id="updatepasswordverify-responses">Responses</h3>

|Status|Meaning|Description|Schema|
|---|---|---|---|
|201|[Created](https://tools.ietf.org/html/rfc7231#section-6.3.2)|Created|[VerificationView](#schemaverificationview)|
|204|[No Content](https://tools.ietf.org/html/rfc7231#section-6.3.5)|No Content|Inline|

<h3 id="updatepasswordverify-responseschema">Response Schema</h3>

<aside class="warning">
To perform this operation, you must be authenticated by means of one of the following methods:
aegis
</aside>

## resetPasswordVerify

<a id="opIdresetPasswordVerify"></a>

> Code samples

```http
POST {host}/account/reset-password/verify/email HTTP/1.1

Content-Type: application/json
Accept: application/json

```

`POST /account/reset-password/verify/email`

*비밀번호 초기화 이메일 인증*

비밀번호 초기화 이메일 인증

> Body parameter

```json
{
  "key": "string",
  "code": "string"
}
```

<h3 id="resetpasswordverify-parameters">Parameters</h3>

|Name|In|Type|Required|Description|
|---|---|---|---|---|
|body|body|[VerificationPayload](#schemaverificationpayload)|true|인증 필드 데이터|

> Example responses

> 201 Response

```json
{
  "expiredIn": 0,
  "expiredAt": 0
}
```

<h3 id="resetpasswordverify-responses">Responses</h3>

|Status|Meaning|Description|Schema|
|---|---|---|---|
|201|[Created](https://tools.ietf.org/html/rfc7231#section-6.3.2)|Created|[VerificationView](#schemaverificationview)|
|204|[No Content](https://tools.ietf.org/html/rfc7231#section-6.3.5)|No Content|Inline|

<h3 id="resetpasswordverify-responseschema">Response Schema</h3>

<aside class="success">
This operation does not require authentication
</aside>

## resetPassword

<a id="opIdresetPassword"></a>

> Code samples

```http
POST {host}/account/reset-password HTTP/1.1

Content-Type: application/json

```

`POST /account/reset-password`

*비밀번호 초기화*

비밀번호 초기화

> Body parameter

```json
{
  "email": "string",
  "password": "string"
}
```

<h3 id="resetpassword-parameters">Parameters</h3>

|Name|In|Type|Required|Description|
|---|---|---|---|---|
|body|body|[ResetPasswordPayload](#schemaresetpasswordpayload)|true|비밀번호 초기화 필드 데이터|

<h3 id="resetpassword-responses">Responses</h3>

|Status|Meaning|Description|Schema|
|---|---|---|---|
|204|[No Content](https://tools.ietf.org/html/rfc7231#section-6.3.5)|No Content|None|

<aside class="success">
This operation does not require authentication
</aside>

## registerVerify

<a id="opIdregisterVerify"></a>

> Code samples

```http
POST {host}/account/register/verify/{type} HTTP/1.1

Content-Type: application/json
Accept: application/json

```

`POST /account/register/verify/{type}`

*회원 가입시 이메일/sms 인증*

회원 가입시 이메일/sms 인증

> Body parameter

```json
{
  "key": "string",
  "code": "string"
}
```

<h3 id="registerverify-parameters">Parameters</h3>

|Name|In|Type|Required|Description|
|---|---|---|---|---|
|type|path|string|true|인증 수단 (email/sms)|
|body|body|[VerificationPayload](#schemaverificationpayload)|true|인증 필드 데이터|

> Example responses

> 201 Response

```json
{
  "expiredIn": 0,
  "expiredAt": 0
}
```

<h3 id="registerverify-responses">Responses</h3>

|Status|Meaning|Description|Schema|
|---|---|---|---|
|201|[Created](https://tools.ietf.org/html/rfc7231#section-6.3.2)|Created|[VerificationView](#schemaverificationview)|
|204|[No Content](https://tools.ietf.org/html/rfc7231#section-6.3.5)|No Content|Inline|

<h3 id="registerverify-responseschema">Response Schema</h3>

<aside class="success">
This operation does not require authentication
</aside>

## register

<a id="opIdregister"></a>

> Code samples

```http
POST {host}/account/register HTTP/1.1

Content-Type: application/json

```

`POST /account/register`

*이메일 회원 가입*

이메일 회원 가입

> Body parameter

```json
{
  "email": "string",
  "password": "string",
  "name": "string",
  "phoneNumber": "string",
  "agreement": {
    "email": true,
    "sms": true,
    "serviceTerm": true,
    "privacyTerm": true
  }
}
```

<h3 id="register-parameters">Parameters</h3>

|Name|In|Type|Required|Description|
|---|---|---|---|---|
|body|body|[RegisterPayload](#schemaregisterpayload)|true|회원가입 필드 데이터|

<h3 id="register-responses">Responses</h3>

|Status|Meaning|Description|Schema|
|---|---|---|---|
|201|[Created](https://tools.ietf.org/html/rfc7231#section-6.3.2)|Created|None|

<aside class="success">
This operation does not require authentication
</aside>

## profileVerify

<a id="opIdprofileVerify"></a>

> Code samples

```http
POST {host}/account/profile/verify/{type} HTTP/1.1

Content-Type: application/json
Accept: application/json

```

`POST /account/profile/verify/{type}`

*마이페이지 수정 이메일/휴대폰 번호 인증*

마이페이지 수정 이메일/휴대폰 번호 인증

> Body parameter

```json
{
  "key": "string",
  "code": "string"
}
```

<h3 id="profileverify-parameters">Parameters</h3>

|Name|In|Type|Required|Description|
|---|---|---|---|---|
|type|path|string|true|인증 수단 (email/sms)|
|body|body|[VerificationPayload](#schemaverificationpayload)|true|인증 필드 데이터|

> Example responses

> 201 Response

```json
{
  "expiredIn": 0,
  "expiredAt": 0
}
```

<h3 id="profileverify-responses">Responses</h3>

|Status|Meaning|Description|Schema|
|---|---|---|---|
|201|[Created](https://tools.ietf.org/html/rfc7231#section-6.3.2)|Created|[VerificationView](#schemaverificationview)|
|204|[No Content](https://tools.ietf.org/html/rfc7231#section-6.3.5)|No Content|Inline|

<h3 id="profileverify-responseschema">Response Schema</h3>

<aside class="warning">
To perform this operation, you must be authenticated by means of one of the following methods:
aegis
</aside>

## legacyLogin

<a id="opIdlegacyLogin"></a>

> Code samples

```http
POST {host}/account/legacy/migrate HTTP/1.1

Content-Type: application/json

```

`POST /account/legacy/migrate`

*v1 회원 계정 이관*

v1 회원 계정 이관
 400 에러는 클라이언트에서 별도 처리하지 않는다.

> Body parameter

```json
{
  "email": "string",
  "password": "string"
}
```

<h3 id="legacylogin-parameters">Parameters</h3>

|Name|In|Type|Required|Description|
|---|---|---|---|---|
|body|body|[LegacyAccountLoginPayload](#schemalegacyaccountloginpayload)|true|v1 계정 로그인 요청 정보|

<h3 id="legacylogin-responses">Responses</h3>

|Status|Meaning|Description|Schema|
|---|---|---|---|
|204|[No Content](https://tools.ietf.org/html/rfc7231#section-6.3.5)|No Content|None|

<aside class="success">
This operation does not require authentication
</aside>

## activationVerify

<a id="opIdactivationVerify"></a>

> Code samples

```http
POST {host}/account/activation/verify/{type} HTTP/1.1

Content-Type: application/json
Accept: application/json

```

`POST /account/activation/verify/{type}`

*로그인 이후 프로필 업데이트 이메일/휴대폰 번호 인증*

로그인 이후 프로필 업데이트 이메일/SMS 번호 인증

> Body parameter

```json
{
  "key": "string",
  "code": "string"
}
```

<h3 id="activationverify-parameters">Parameters</h3>

|Name|In|Type|Required|Description|
|---|---|---|---|---|
|type|path|string|true|인증 수단 (email/sms)|
|body|body|[VerificationPayload](#schemaverificationpayload)|true|인증 필드 데이터|

> Example responses

> 201 Response

```json
{
  "expiredIn": 0,
  "expiredAt": 0
}
```

<h3 id="activationverify-responses">Responses</h3>

|Status|Meaning|Description|Schema|
|---|---|---|---|
|201|[Created](https://tools.ietf.org/html/rfc7231#section-6.3.2)|Created|[VerificationView](#schemaverificationview)|
|204|[No Content](https://tools.ietf.org/html/rfc7231#section-6.3.5)|No Content|Inline|

<h3 id="activationverify-responseschema">Response Schema</h3>

<aside class="warning">
To perform this operation, you must be authenticated by means of one of the following methods:
aegis
</aside>

## profile

<a id="opIdprofile"></a>

> Code samples

```http
GET {host}/account/profile/{customerId} HTTP/1.1

Accept: application/json

```

`GET /account/profile/{customerId}`

*프로필 조회*

프로필 조회

<h3 id="profile-parameters">Parameters</h3>

|Name|In|Type|Required|Description|
|---|---|---|---|---|
|customerId|path|string(uuid)|true|고객 번호|

> Example responses

> 200 Response

```json
{
  "email": "string",
  "name": "string",
  "phoneNumber": "string",
  "birthday": "2019-08-24",
  "smsAgreed": true,
  "emailAgreed": true,
  "agreement": {
    "sms": true,
    "email": true
  },
  "identityProviders": [
    "naver"
  ],
  "shippingAddresses": [
    {
      "id": "string",
      "name": "string",
      "recipient": "string",
      "primaryPhoneNumber": "string",
      "secondaryPhoneNumber": "string",
      "zipCode": "string",
      "line1": "string",
      "line2": "string",
      "primary": true
    }
  ]
}
```

<h3 id="profile-responses">Responses</h3>

|Status|Meaning|Description|Schema|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|OK|[ProfileView](#schemaprofileview)|

<aside class="warning">
To perform this operation, you must be authenticated by means of one of the following methods:
aegis
</aside>

<h1 id="account-api-admin">Admin</h1>

## disableAccount

<a id="opIddisableAccount"></a>

> Code samples

```http
POST {host}/admin/account/{customerId}/disable HTTP/1.1

```

`POST /admin/account/{customerId}/disable`

*관리자 권한 회원 비활성화*

관리자 권한 회원 비활성화

<h3 id="disableaccount-parameters">Parameters</h3>

|Name|In|Type|Required|Description|
|---|---|---|---|---|
|customerId|path|string|true|none|

<h3 id="disableaccount-responses">Responses</h3>

|Status|Meaning|Description|Schema|
|---|---|---|---|
|204|[No Content](https://tools.ietf.org/html/rfc7231#section-6.3.5)|No Content|None|

<aside class="warning">
To perform this operation, you must be authenticated by means of one of the following methods:
aegis
</aside>

## searchProfiles

<a id="opIdsearchProfiles"></a>

> Code samples

```http
GET {host}/admin/account/profiles/search?query=string HTTP/1.1

Accept: application/json

```

`GET /admin/account/profiles/search`

*관리자 프로필 통합 검색*

주어진 키워드로 프로필을 검색합니다.

<h3 id="searchprofiles-parameters">Parameters</h3>

|Name|In|Type|Required|Description|
|---|---|---|---|---|
|query|query|string|true|none|
|page|query|integer(int32)|false|none|

> Example responses

> 200 Response

```json
[
  {
    "id": "string",
    "customerId": "string",
    "enabled": true,
    "email": "string",
    "emailVerified": true,
    "name": "string",
    "phoneNumber": "string",
    "phoneNumberVerified": true,
    "identityProviders": [
      "naver"
    ],
    "birthday": "2019-08-24",
    "agreement": {
      "email": true,
      "sms": true,
      "serviceTerm": true,
      "privacyTerm": true
    },
    "orderCount": 0,
    "createdAt": "2019-08-24T14:15:22Z",
    "updatedAt": "2019-08-24T14:15:22Z"
  }
]
```

<h3 id="searchprofiles-responses">Responses</h3>

|Status|Meaning|Description|Schema|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|OK|Inline|

<h3 id="searchprofiles-responseschema">Response Schema</h3>

Status Code **200**

|Name|Type|Required|Restrictions|Description|
|---|---|---|---|---|
|*anonymous*|[[Profile](#schemaprofile)]|false|none|[회원 프로필]|
|» id|string|false|none|프로필 ID|
|» customerId|string|true|none|고객 ID|
|» enabled|boolean|true|none|활성화 여부|
|» email|string|true|none|이메일|
|» emailVerified|boolean|true|none|이메일 인증 여부|
|» name|string|true|none|이름|
|» phoneNumber|string|true|none|휴대폰 번호|
|» phoneNumberVerified|boolean|true|none|휴대폰 번호 인증여부|
|» identityProviders|[string]|false|none|소셜 연동 리스트|
|» birthday|string(date)|false|none|생년 월일|
|» agreement|[Agreement](#schemaagreement)|true|none|동의 항목|
|»» email|boolean|true|none|이메일 수신 동의|
|»» sms|boolean|true|none|sms 수신 동의|
|»» serviceTerm|boolean|true|none|서비스 이용 약관 동의|
|»» privacyTerm|boolean|true|none|개인정보 수집 및 동의|
|» orderCount|integer(int32)|true|none|마감일 기준 주문 건수|
|» createdAt|string(date-time)|false|none|프로필 생성일|
|» updatedAt|string(date-time)|false|none|프로필 수정일|

<aside class="warning">
To perform this operation, you must be authenticated by means of one of the following methods:
aegis
</aside>

## allFilteredProfiles

<a id="opIdallFilteredProfiles"></a>

> Code samples

```http
GET {host}/admin/account/profiles HTTP/1.1

Accept: application/json

```

`GET /admin/account/profiles`

*관리자 전체 프로필 조회*

필터 기반 프로필 리스트을 조회합니다.

<h3 id="allfilteredprofiles-parameters">Parameters</h3>

|Name|In|Type|Required|Description|
|---|---|---|---|---|
|createdAtRange|query|array[string]|false|회원 생성일 기간|
|identityProviders|query|array[string]|false|소셜 리스트|
|agreement.email|query|boolean|false|이메일 수신 동의|
|agreement.sms|query|boolean|false|SMS 수신 동의|
|enabled|query|boolean|false|계정 활성화 여부|
|page|query|integer(int32)|false|none|

#### Enumerated Values

|Parameter|Value|
|---|---|
|identityProviders|naver|
|identityProviders|kakao|
|identityProviders|facebook|

> Example responses

> 200 Response

```json
{
  "content": [
    {
      "id": "string",
      "customerId": "string",
      "enabled": true,
      "email": "string",
      "emailVerified": true,
      "name": "string",
      "phoneNumber": "string",
      "phoneNumberVerified": true,
      "identityProviders": [
        "naver"
      ],
      "birthday": "2019-08-24",
      "agreement": {
        "email": true,
        "sms": true,
        "serviceTerm": true,
        "privacyTerm": true
      },
      "orderCount": 0,
      "createdAt": "2019-08-24T14:15:22Z",
      "updatedAt": "2019-08-24T14:15:22Z"
    }
  ],
  "page": {
    "totalPages": 0,
    "totalElements": 0,
    "last": true,
    "size": 0,
    "number": 0,
    "first": true
  }
}
```

<h3 id="allfilteredprofiles-responses">Responses</h3>

|Status|Meaning|Description|Schema|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|OK|[PagedViewProfile](#schemapagedviewprofile)|

<aside class="warning">
To perform this operation, you must be authenticated by means of one of the following methods:
aegis
</aside>

# Schemas

<h2 id="tocS_ShippingAddressPayload">ShippingAddressPayload</h2>
<!-- backwards compatibility -->
<a id="schemashippingaddresspayload"></a>
<a id="schema_ShippingAddressPayload"></a>
<a id="tocSshippingaddresspayload"></a>
<a id="tocsshippingaddresspayload"></a>

```json
{
  "name": "string",
  "recipient": "string",
  "primaryPhoneNumber": "string",
  "secondaryPhoneNumber": "string",
  "zipCode": "string",
  "line1": "string",
  "line2": "string",
  "primary": true
}

```

배송지 추가/수정

### Properties

|Name|Type|Required|Restrictions|Description|
|---|---|---|---|---|
|name|string|false|none|배송지명|
|recipient|string|true|none|수령인|
|primaryPhoneNumber|string|true|none|휴대폰 번호|
|secondaryPhoneNumber|string|false|none|연락처|
|zipCode|string|true|none|우편 번호|
|line1|string|true|none|배송지 주소|
|line2|string|false|none|배송지 주소 상세|
|primary|boolean|true|none|기본 주소지|

<h2 id="tocS_UpdatePhoneNumberPayload">UpdatePhoneNumberPayload</h2>
<!-- backwards compatibility -->
<a id="schemaupdatephonenumberpayload"></a>
<a id="schema_UpdatePhoneNumberPayload"></a>
<a id="tocSupdatephonenumberpayload"></a>
<a id="tocsupdatephonenumberpayload"></a>

```json
{
  "phoneNumber": "string"
}

```

휴대폰 번호 수정

### Properties

|Name|Type|Required|Restrictions|Description|
|---|---|---|---|---|
|phoneNumber|string|true|none|휴대폰 번호|

<h2 id="tocS_UpdatePasswordPayload">UpdatePasswordPayload</h2>
<!-- backwards compatibility -->
<a id="schemaupdatepasswordpayload"></a>
<a id="schema_UpdatePasswordPayload"></a>
<a id="tocSupdatepasswordpayload"></a>
<a id="tocsupdatepasswordpayload"></a>

```json
{
  "phoneNumber": "string",
  "password": "string"
}

```

비밀번호 재설정

### Properties

|Name|Type|Required|Restrictions|Description|
|---|---|---|---|---|
|phoneNumber|string|true|none|휴대폰 번호|
|password|string|true|none|비밀번호|

<h2 id="tocS_UpdateNamePayload">UpdateNamePayload</h2>
<!-- backwards compatibility -->
<a id="schemaupdatenamepayload"></a>
<a id="schema_UpdateNamePayload"></a>
<a id="tocSupdatenamepayload"></a>
<a id="tocsupdatenamepayload"></a>

```json
{
  "name": "string"
}

```

이름 수정

### Properties

|Name|Type|Required|Restrictions|Description|
|---|---|---|---|---|
|name|string|true|none|이름|

<h2 id="tocS_UpdateProfileImagePayload">UpdateProfileImagePayload</h2>
<!-- backwards compatibility -->
<a id="schemaupdateprofileimagepayload"></a>
<a id="schema_UpdateProfileImagePayload"></a>
<a id="tocSupdateprofileimagepayload"></a>
<a id="tocsupdateprofileimagepayload"></a>

```json
{
  "image": "string"
}

```

프로필 이미지 Bucket 경로 수정

### Properties

|Name|Type|Required|Restrictions|Description|
|---|---|---|---|---|
|image|string|true|none|프로필 이미지 Bucket 경로|

<h2 id="tocS_UpdateEmailPayload">UpdateEmailPayload</h2>
<!-- backwards compatibility -->
<a id="schemaupdateemailpayload"></a>
<a id="schema_UpdateEmailPayload"></a>
<a id="tocSupdateemailpayload"></a>
<a id="tocsupdateemailpayload"></a>

```json
{
  "email": "string"
}

```

이메일 수정

### Properties

|Name|Type|Required|Restrictions|Description|
|---|---|---|---|---|
|email|string|true|none|이메일|

<h2 id="tocS_UpdateBirthdayPayload">UpdateBirthdayPayload</h2>
<!-- backwards compatibility -->
<a id="schemaupdatebirthdaypayload"></a>
<a id="schema_UpdateBirthdayPayload"></a>
<a id="tocSupdatebirthdaypayload"></a>
<a id="tocsupdatebirthdaypayload"></a>

```json
{
  "birthday": "string"
}

```

생일 수정

### Properties

|Name|Type|Required|Restrictions|Description|
|---|---|---|---|---|
|birthday|string|true|none|생년월일|

<h2 id="tocS_UpdateAgreementPayload">UpdateAgreementPayload</h2>
<!-- backwards compatibility -->
<a id="schemaupdateagreementpayload"></a>
<a id="schema_UpdateAgreementPayload"></a>
<a id="tocSupdateagreementpayload"></a>
<a id="tocsupdateagreementpayload"></a>

```json
{
  "type": "email",
  "active": true
}

```

동의 항목 수정 필드 데이터

### Properties

|Name|Type|Required|Restrictions|Description|
|---|---|---|---|---|
|type|string|true|none|동의 항목 구분|
|active|boolean|true|none|활성화 여부|

#### Enumerated Values

|Property|Value|
|---|---|
|type|email|
|type|sms|

<h2 id="tocS_AgreementPayload">AgreementPayload</h2>
<!-- backwards compatibility -->
<a id="schemaagreementpayload"></a>
<a id="schema_AgreementPayload"></a>
<a id="tocSagreementpayload"></a>
<a id="tocsagreementpayload"></a>

```json
{
  "email": true,
  "sms": true,
  "serviceTerm": true,
  "privacyTerm": true
}

```

회원 가입, 로그인 이후 프로필 업데이트 동의 항목

### Properties

|Name|Type|Required|Restrictions|Description|
|---|---|---|---|---|
|email|boolean|true|none|이메일 수신 동의|
|sms|boolean|true|none|sms 수신 동의|
|serviceTerm|boolean|true|none|서비스 이용 약관 동의|
|privacyTerm|boolean|true|none|개인정보 수집 및 동의|

<h2 id="tocS_LegacyActivateProfilePayload">LegacyActivateProfilePayload</h2>
<!-- backwards compatibility -->
<a id="schemalegacyactivateprofilepayload"></a>
<a id="schema_LegacyActivateProfilePayload"></a>
<a id="tocSlegacyactivateprofilepayload"></a>
<a id="tocslegacyactivateprofilepayload"></a>

```json
{
  "email": "string",
  "name": "string",
  "phoneNumber": "string",
  "password": "string",
  "agreement": {
    "email": true,
    "sms": true,
    "serviceTerm": true,
    "privacyTerm": true
  }
}

```

v1 회원 계정 로그인 이후 프로필 업데이트

### Properties

|Name|Type|Required|Restrictions|Description|
|---|---|---|---|---|
|email|string|true|none|이메일|
|name|string|true|none|이름|
|phoneNumber|string|true|none|휴대폰 번호|
|password|string|true|none|비밀 번호|
|agreement|[AgreementPayload](#schemaagreementpayload)|true|none|회원 가입, 로그인 이후 프로필 업데이트 동의 항목|

<h2 id="tocS_ActivateProfilePayload">ActivateProfilePayload</h2>
<!-- backwards compatibility -->
<a id="schemaactivateprofilepayload"></a>
<a id="schema_ActivateProfilePayload"></a>
<a id="tocSactivateprofilepayload"></a>
<a id="tocsactivateprofilepayload"></a>

```json
{
  "email": "string",
  "name": "string",
  "phoneNumber": "string",
  "agreement": {
    "email": true,
    "sms": true,
    "serviceTerm": true,
    "privacyTerm": true
  }
}

```

로그인 이후 프로필 업데이트

### Properties

|Name|Type|Required|Restrictions|Description|
|---|---|---|---|---|
|email|string|true|none|이메일|
|name|string|true|none|이름|
|phoneNumber|string|true|none|휴대폰 번호|
|agreement|[AgreementPayload](#schemaagreementpayload)|true|none|회원 가입, 로그인 이후 프로필 업데이트 동의 항목|

<h2 id="tocS_AccountValidationPayload">AccountValidationPayload</h2>
<!-- backwards compatibility -->
<a id="schemaaccountvalidationpayload"></a>
<a id="schema_AccountValidationPayload"></a>
<a id="tocSaccountvalidationpayload"></a>
<a id="tocsaccountvalidationpayload"></a>

```json
{
  "email": "string",
  "password": "string",
  "name": "string",
  "phoneNumber": "string",
  "agreement": {
    "serviceTerm": true,
    "privacyTerm": true
  }
}

```

회원 정보 유효성 질의 필드

### Properties

|Name|Type|Required|Restrictions|Description|
|---|---|---|---|---|
|email|string|false|none|이메일|
|password|string|false|none|패스워드|
|name|string|false|none|이름|
|phoneNumber|string|false|none|휴대폰 번호|
|agreement|[AgreementValidationPayload](#schemaagreementvalidationpayload)|false|none|회원 가입, 로그인 이후 프로필 업데이트 동의 항목|

<h2 id="tocS_AgreementValidationPayload">AgreementValidationPayload</h2>
<!-- backwards compatibility -->
<a id="schemaagreementvalidationpayload"></a>
<a id="schema_AgreementValidationPayload"></a>
<a id="tocSagreementvalidationpayload"></a>
<a id="tocsagreementvalidationpayload"></a>

```json
{
  "serviceTerm": true,
  "privacyTerm": true
}

```

회원 가입, 로그인 이후 프로필 업데이트 동의 항목

### Properties

|Name|Type|Required|Restrictions|Description|
|---|---|---|---|---|
|serviceTerm|boolean|false|none|서비스 이용 약관 동의|
|privacyTerm|boolean|false|none|개인정보 수집 및 동의|

<h2 id="tocS_VerificationView">VerificationView</h2>
<!-- backwards compatibility -->
<a id="schemaverificationview"></a>
<a id="schema_VerificationView"></a>
<a id="tocSverificationview"></a>
<a id="tocsverificationview"></a>

```json
{
  "expiredIn": 0,
  "expiredAt": 0
}

```

인증 요청 결과

### Properties

|Name|Type|Required|Restrictions|Description|
|---|---|---|---|---|
|expiredIn|integer(int32)|true|none|유효 시간(s)|
|expiredAt|integer(int64)|true|none|만료 시간 timestamp|

<h2 id="tocS_VerificationPayload">VerificationPayload</h2>
<!-- backwards compatibility -->
<a id="schemaverificationpayload"></a>
<a id="schema_VerificationPayload"></a>
<a id="tocSverificationpayload"></a>
<a id="tocsverificationpayload"></a>

```json
{
  "key": "string",
  "code": "string"
}

```

인증 요청/검증

### Properties

|Name|Type|Required|Restrictions|Description|
|---|---|---|---|---|
|key|string|true|none|인증 수단 (이메일/sms)|
|code|string|false|none|인증 코드|

<h2 id="tocS_ResetPasswordPayload">ResetPasswordPayload</h2>
<!-- backwards compatibility -->
<a id="schemaresetpasswordpayload"></a>
<a id="schema_ResetPasswordPayload"></a>
<a id="tocSresetpasswordpayload"></a>
<a id="tocsresetpasswordpayload"></a>

```json
{
  "email": "string",
  "password": "string"
}

```

비밀번호 초기화

### Properties

|Name|Type|Required|Restrictions|Description|
|---|---|---|---|---|
|email|string|true|none|이메일|
|password|string|true|none|비밀번호|

<h2 id="tocS_RegisterPayload">RegisterPayload</h2>
<!-- backwards compatibility -->
<a id="schemaregisterpayload"></a>
<a id="schema_RegisterPayload"></a>
<a id="tocSregisterpayload"></a>
<a id="tocsregisterpayload"></a>

```json
{
  "email": "string",
  "password": "string",
  "name": "string",
  "phoneNumber": "string",
  "agreement": {
    "email": true,
    "sms": true,
    "serviceTerm": true,
    "privacyTerm": true
  }
}

```

회원 가입

### Properties

|Name|Type|Required|Restrictions|Description|
|---|---|---|---|---|
|email|string|true|none|이메일|
|password|string|true|none|패스워드|
|name|string|true|none|이름|
|phoneNumber|string|true|none|휴대폰 번호|
|agreement|[AgreementPayload](#schemaagreementpayload)|true|none|회원 가입, 로그인 이후 프로필 업데이트 동의 항목|

<h2 id="tocS_LegacyAccountLoginPayload">LegacyAccountLoginPayload</h2>
<!-- backwards compatibility -->
<a id="schemalegacyaccountloginpayload"></a>
<a id="schema_LegacyAccountLoginPayload"></a>
<a id="tocSlegacyaccountloginpayload"></a>
<a id="tocslegacyaccountloginpayload"></a>

```json
{
  "email": "string",
  "password": "string"
}

```

v1 계정 로그인

### Properties

|Name|Type|Required|Restrictions|Description|
|---|---|---|---|---|
|email|string|true|none|이메일|
|password|string|true|none|패스워드|

<h2 id="tocS_Agreement">Agreement</h2>
<!-- backwards compatibility -->
<a id="schemaagreement"></a>
<a id="schema_Agreement"></a>
<a id="tocSagreement"></a>
<a id="tocsagreement"></a>

```json
{
  "email": true,
  "sms": true,
  "serviceTerm": true,
  "privacyTerm": true
}

```

동의 항목

### Properties

|Name|Type|Required|Restrictions|Description|
|---|---|---|---|---|
|email|boolean|true|none|이메일 수신 동의|
|sms|boolean|true|none|sms 수신 동의|
|serviceTerm|boolean|true|none|서비스 이용 약관 동의|
|privacyTerm|boolean|true|none|개인정보 수집 및 동의|

<h2 id="tocS_Profile">Profile</h2>
<!-- backwards compatibility -->
<a id="schemaprofile"></a>
<a id="schema_Profile"></a>
<a id="tocSprofile"></a>
<a id="tocsprofile"></a>

```json
{
  "id": "string",
  "customerId": "string",
  "enabled": true,
  "email": "string",
  "emailVerified": true,
  "name": "string",
  "phoneNumber": "string",
  "phoneNumberVerified": true,
  "identityProviders": [
    "naver"
  ],
  "birthday": "2019-08-24",
  "agreement": {
    "email": true,
    "sms": true,
    "serviceTerm": true,
    "privacyTerm": true
  },
  "orderCount": 0,
  "createdAt": "2019-08-24T14:15:22Z",
  "updatedAt": "2019-08-24T14:15:22Z"
}

```

회원 프로필

### Properties

|Name|Type|Required|Restrictions|Description|
|---|---|---|---|---|
|id|string|false|none|프로필 ID|
|customerId|string|true|none|고객 ID|
|enabled|boolean|true|none|활성화 여부|
|email|string|true|none|이메일|
|emailVerified|boolean|true|none|이메일 인증 여부|
|name|string|true|none|이름|
|phoneNumber|string|true|none|휴대폰 번호|
|phoneNumberVerified|boolean|true|none|휴대폰 번호 인증여부|
|identityProviders|[string]|false|none|소셜 연동 리스트|
|birthday|string(date)|false|none|생년 월일|
|agreement|[Agreement](#schemaagreement)|true|none|동의 항목|
|orderCount|integer(int32)|true|none|마감일 기준 주문 건수|
|createdAt|string(date-time)|false|none|프로필 생성일|
|updatedAt|string(date-time)|false|none|프로필 수정일|

<h2 id="tocS_PageMetadata">PageMetadata</h2>
<!-- backwards compatibility -->
<a id="schemapagemetadata"></a>
<a id="schema_PageMetadata"></a>
<a id="tocSpagemetadata"></a>
<a id="tocspagemetadata"></a>

```json
{
  "totalPages": 0,
  "totalElements": 0,
  "last": true,
  "size": 0,
  "number": 0,
  "first": true
}

```

### Properties

|Name|Type|Required|Restrictions|Description|
|---|---|---|---|---|
|totalPages|integer(int32)|true|none|총 페이지 개수|
|totalElements|integer(int64)|true|none|총 데이터 개수|
|last|boolean|true|none|마지막 페이지 여부|
|size|integer(int32)|true|none|페이지 사이즈|
|number|integer(int32)|true|none|현재 페이지 번호|
|first|boolean|true|none|첫번째 페이지 여부|

<h2 id="tocS_PagedViewProfile">PagedViewProfile</h2>
<!-- backwards compatibility -->
<a id="schemapagedviewprofile"></a>
<a id="schema_PagedViewProfile"></a>
<a id="tocSpagedviewprofile"></a>
<a id="tocspagedviewprofile"></a>

```json
{
  "content": [
    {
      "id": "string",
      "customerId": "string",
      "enabled": true,
      "email": "string",
      "emailVerified": true,
      "name": "string",
      "phoneNumber": "string",
      "phoneNumberVerified": true,
      "identityProviders": [
        "naver"
      ],
      "birthday": "2019-08-24",
      "agreement": {
        "email": true,
        "sms": true,
        "serviceTerm": true,
        "privacyTerm": true
      },
      "orderCount": 0,
      "createdAt": "2019-08-24T14:15:22Z",
      "updatedAt": "2019-08-24T14:15:22Z"
    }
  ],
  "page": {
    "totalPages": 0,
    "totalElements": 0,
    "last": true,
    "size": 0,
    "number": 0,
    "first": true
  }
}

```

Pagination Response View

### Properties

|Name|Type|Required|Restrictions|Description|
|---|---|---|---|---|
|content|[[Profile](#schemaprofile)]|true|none|데이터|
|page|[PageMetadata](#schemapagemetadata)|true|none|none|

<h2 id="tocS_ShippingAddressView">ShippingAddressView</h2>
<!-- backwards compatibility -->
<a id="schemashippingaddressview"></a>
<a id="schema_ShippingAddressView"></a>
<a id="tocSshippingaddressview"></a>
<a id="tocsshippingaddressview"></a>

```json
{
  "id": "string",
  "name": "string",
  "recipient": "string",
  "primaryPhoneNumber": "string",
  "secondaryPhoneNumber": "string",
  "zipCode": "string",
  "line1": "string",
  "line2": "string",
  "primary": true
}

```

배송지 조회 결과

### Properties

|Name|Type|Required|Restrictions|Description|
|---|---|---|---|---|
|id|string|true|none|배송지 ID|
|name|string|false|none|배송지명|
|recipient|string|true|none|수령인|
|primaryPhoneNumber|string|true|none|수령인 연락처|
|secondaryPhoneNumber|string|false|none|수령인 연락처2|
|zipCode|string|true|none|우편 번호|
|line1|string|true|none|배송지 주소|
|line2|string|false|none|배송지 주소 상세|
|primary|boolean|true|none|기본 주소지|

<h2 id="tocS_AgreementView">AgreementView</h2>
<!-- backwards compatibility -->
<a id="schemaagreementview"></a>
<a id="schema_AgreementView"></a>
<a id="tocSagreementview"></a>
<a id="tocsagreementview"></a>

```json
{
  "sms": true,
  "email": true
}

```

### Properties

|Name|Type|Required|Restrictions|Description|
|---|---|---|---|---|
|sms|boolean|true|none|sms 수신 동의|
|email|boolean|true|none|이메일 수신 동의|

<h2 id="tocS_ProfileView">ProfileView</h2>
<!-- backwards compatibility -->
<a id="schemaprofileview"></a>
<a id="schema_ProfileView"></a>
<a id="tocSprofileview"></a>
<a id="tocsprofileview"></a>

```json
{
  "email": "string",
  "name": "string",
  "phoneNumber": "string",
  "birthday": "2019-08-24",
  "smsAgreed": true,
  "emailAgreed": true,
  "agreement": {
    "sms": true,
    "email": true
  },
  "identityProviders": [
    "naver"
  ],
  "shippingAddresses": [
    {
      "id": "string",
      "name": "string",
      "recipient": "string",
      "primaryPhoneNumber": "string",
      "secondaryPhoneNumber": "string",
      "zipCode": "string",
      "line1": "string",
      "line2": "string",
      "primary": true
    }
  ]
}
```

### Properties

|Name|Type|Required|Restrictions|Description|
|---|---|---|---|---|
|email|string|true|none|이메일|
|name|string|true|none|이름|
|phoneNumber|string|true|none|휴대폰 번호|
|birthday|string(date)|false|none|생년월일|
|smsAgreed|boolean|true|none|sms 수신 동의|
|emailAgreed|boolean|true|none|이메일 수신 동의|
|agreement|[AgreementView](#schemaagreementview)|true|none|none|
|identityProviders|[string]|false|none|소셜 미디어 리스트|
|shippingAddresses|[[ShippingAddressView](#schemashippingaddressview)]|false|none|배송지 리스트|
</details>

## 이벤트
- 회원 가입 이후 이벤트 발생
  - 이벤트는 회원 혜택 서비스에서 처리

  - 회원 서비스 -> 회원 혜택 서비스
## 마이크로 서비스 구조
![마이크로 서비스 drawio](https://user-images.githubusercontent.com/55565835/215039176-47faceaf-50e9-4c4a-9612-48b8caf399a6.png)
---

