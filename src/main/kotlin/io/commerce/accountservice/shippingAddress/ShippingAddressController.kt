package io.commerce.accountservice.shippingAddress

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.bson.types.ObjectId
import org.springframework.http.HttpStatus.CREATED
import org.springframework.http.HttpStatus.NO_CONTENT
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.*
import java.util.*
import javax.validation.Valid

@Tag(name = "ShippingAddress")
@RestController
@RequestMapping(
    value = ["account"],
    produces = [MediaType.APPLICATION_JSON_VALUE]
)
@SecurityRequirement(name = "aegis")
class ShippingAddressController(
    val shippingAddressService: ShippingAddressService
) {

    /**
     * 배송지 목록
     * @param customerId 계정 ID
     */
    @Operation(summary = "배송지 목록")
    @GetMapping("/{customerId}/shipping-addresses")
    fun getShippingAddresses(@PathVariable customerId: UUID): Flow<ShippingAddressView> =
        shippingAddressService.getShippingAddresses(customerId.toString()).map(ShippingAddress::toShippingAddressView)

    /**
     * 기본 배송지 조회
     * @param customerId 계정 ID
     */
    @Operation(summary = "기본 배송지 조회")
    @GetMapping("/{customerId}/shipping-addresses/primary")
    suspend fun getPrimaryShippingAddress(@PathVariable customerId: UUID): ShippingAddressView =
        shippingAddressService.getPrimaryShippingAddress(customerId.toString()).toShippingAddressView()

    /**
     * 배송지 생성
     * @param customerId 계정 ID
     * @param payload 배송지 생성 request body
     */
    @Operation(summary = "배송지 생성")
    @PostMapping("/{customerId}/shipping-addresses", consumes = [MediaType.APPLICATION_JSON_VALUE])
    @ResponseStatus(CREATED)
    suspend fun createShippingAddress(
        @PathVariable customerId: UUID,
        @Valid @RequestBody
        payload: ShippingAddressPayload
    ) {
        shippingAddressService.createShippingAddress(customerId.toString(), payload)
    }

    /**
     * 배송지 삭제
     * @param customerId 계정 ID
     * @param shippingAddressId 배송지 ID
     */
    @Operation(summary = "배송지 삭제")
    @DeleteMapping("/{customerId}/shipping-addresses/{shippingAddressId}")
    @ResponseStatus(NO_CONTENT)
    suspend fun deleteShippingAddress(
        @PathVariable customerId: UUID,
        @PathVariable shippingAddressId: ObjectId
    ) = shippingAddressService.deleteShippingAddress(shippingAddressId, customerId.toString())

    /**
     * 배송지 수정
     * @param customerId 계정 ID
     * @param shippingAddressId 배송지 ID
     * @param payload 배송지 수정 request body
     */
    @Operation(summary = "배송지 수정")
    @PutMapping("/{customerId}/shipping-addresses/{shippingAddressId}", consumes = [MediaType.APPLICATION_JSON_VALUE])
    @ResponseStatus(NO_CONTENT)
    suspend fun updateShippingAddress(
        @PathVariable customerId: UUID,
        @PathVariable shippingAddressId: ObjectId,
        @Valid @RequestBody
        payload: ShippingAddressPayload
    ) {
        shippingAddressService.updateShippingAddress(shippingAddressId, customerId.toString(), payload)
    }
}
