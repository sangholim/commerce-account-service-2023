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
## 이벤트
- 회원 가입 이후 이벤트 발생
  - 이벤트는 회원 혜택 서비스에서 처리

  - 회원 서비스 -> 회원 혜택 서비스
## 마이크로 서비스 구조
![마이크로 서비스 drawio](https://user-images.githubusercontent.com/55565835/215039176-47faceaf-50e9-4c4a-9612-48b8caf399a6.png)
---

