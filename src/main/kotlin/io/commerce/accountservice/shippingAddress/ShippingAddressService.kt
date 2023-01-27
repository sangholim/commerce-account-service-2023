package io.commerce.accountservice.shippingAddress

import io.commerce.accountservice.core.NotFoundException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.count
import kotlinx.coroutines.flow.firstOrNull
import org.bson.types.ObjectId
import org.springframework.stereotype.Service

@Service
class ShippingAddressService(
    val shippingAddressRepository: ShippingAddressRepository
) {

    private val maxSize = 20

    /**
     * 배송지 조회
     * @param customerId 고객 번호
     */
    fun getShippingAddresses(customerId: String): Flow<ShippingAddress> =
        shippingAddressRepository.findByCustomerId(customerId)

    /**
     * 기본 배송지 조회
     * @param customerId 고객 번호
     * @return Boolean
     */
    suspend fun getPrimaryShippingAddress(customerId: String): ShippingAddress =
        shippingAddressRepository.findFirstByCustomerIdAndPrimary(customerId, true) ?: throw NotFoundException()

    /**
     * 배송지 생성
     * 처음 생성하는 배송지는 기본 배송지로 설정한다.
     * @param customerId 고객 번호
     * @param payload 배송지 필드 데이터
     */
    suspend fun createShippingAddress(customerId: String, payload: ShippingAddressPayload): ShippingAddress {
        val shippingAddresses = shippingAddressRepository.findByCustomerId(customerId)
        if (shippingAddresses.count() == maxSize) throw ShippingAddressMaxSizeException()
        val shippingAddress = if (shippingAddresses.count() == 0) ShippingAddress.ofPrimary(customerId, payload)
        else ShippingAddress.of(customerId, payload)
        resetPrimary(payload.primary, shippingAddresses)
        return shippingAddressRepository.save(shippingAddress)
    }

    /**
     * 배송지 삭제
     * 기본 배송지를 삭제시
     * 남은 배송지중 하나를 기본 배송지로 선정
     * @param id 배송지 번호
     * @param customerId 고객 번호
     */
    suspend fun deleteShippingAddress(id: ObjectId, customerId: String) {
        val shippingAddresses = shippingAddressRepository.findByCustomerId(customerId)
        val deleteShippingAddress = shippingAddresses.firstOrNull { it.id == id } ?: throw Exception()
        val shippingAddress = shippingAddresses.firstOrNull { it.id != id }
        shippingAddressRepository.delete(deleteShippingAddress)
        if (!deleteShippingAddress.isPrimary() || shippingAddress == null) {
            return
        }
        shippingAddressRepository.save(shippingAddress.enablePrimary())
    }

    /**
     * 배송지 수정
     * 기본 배송지를 비활성화 하는 경우 예외 처리
     * @param id 배송지 번호
     * @param customerId 고객 번호
     * @param payload 배송지 필드 데이터
     */
    suspend fun updateShippingAddress(
        id: ObjectId,
        customerId: String,
        payload: ShippingAddressPayload
    ): ShippingAddress {
        val shippingAddresses = shippingAddressRepository.findByCustomerId(customerId)
        val shippingAddress = shippingAddresses.firstOrNull { it.id == id } ?: throw Exception()
        if (shippingAddress.primary && !payload.primary) throw Exception()
        resetPrimary(payload.primary, shippingAddresses)
        return shippingAddressRepository.save(shippingAddress.update(payload))
    }

    /**
     * 기본 배송지 초기화
     * @param primary 활성화 여부
     * @param shippingAddresses 배송지 리스트
     */
    private suspend fun resetPrimary(primary: Boolean, shippingAddresses: Flow<ShippingAddress>) {
        if (!primary) return
        val disablePrimaryShippingAddress = shippingAddresses.firstOrNull { it.primary }?.disablePrimary() ?: return
        shippingAddressRepository.save(disablePrimaryShippingAddress)
    }
}
