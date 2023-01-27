package io.commerce.accountservice.shippingAddress

import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException

class ShippingAddressMaxSizeException : ResponseStatusException(HttpStatus.BAD_REQUEST, "배송지는 최대 20개까지 등록할 수 있어요!")
