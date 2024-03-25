package com.somoa.serviceback.domain.order.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.somoa.serviceback.domain.order.dto.OrderSaveDto;
import com.somoa.serviceback.domain.order.dto.OrderStatusUpdateDto;
import com.somoa.serviceback.domain.order.entity.Order;
import com.somoa.serviceback.domain.order.repository.OrderRepository;
//import com.somoa.serviceback.global.fcm.dto.FcmSendDto;
//import com.somoa.serviceback.global.fcm.repository.FcmRepository;
//import com.somoa.serviceback.global.fcm.service.FcmService;

import lombok.RequiredArgsConstructor;

import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
//    private final FcmRepository fcmRepository;
//    final FcmService fcmService;

    @Transactional
    public Mono<Map<String, Object>> saveOrder(OrderSaveDto orderSaveDto) {
        return orderRepository.findByOrderStoreId(orderSaveDto.getOrderStoreId())
                .hasElement()
                .filter(exists -> !exists)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("이미 등록된 주문입니다.")))
                .then(orderRepository.save(Order.builder()
                        .groupId(orderSaveDto.getGroupId())
                        .supplyId(orderSaveDto.getSupplyId())
                        .orderStatus(orderSaveDto.getOrderStatus())
                        .productName(orderSaveDto.getProductName())
                        .orderStore(orderSaveDto.getOrderStore())
                        .orderStoreId(orderSaveDto.getOrderStoreId())
                        .productImg(orderSaveDto.getProductImg())
                        .orderCount(orderSaveDto.getOrderCount())
                        .orderAmount(orderSaveDto.getOrderAmount())
                        .build()))
                // TODO: 그룹 ID에 해당하는 유저들의 token list로 보내게 로직 수정해야함
//                .flatMap(order -> {
//                    return fcmRepository.findAll()
//                            .flatMap(fcmToken -> {
//                                FcmSendDto fcmSendDto = new FcmSendDto(fcmToken.getToken(), "새로운 주문", order.getOrderStore() + "에서 " + order.getProductName() + " 주문을 완료하였습니다.");
//                                try{
//                                    fcmService.sendMessageTo(fcmSendDto);
//                                } catch (Exception e) {
//                                    return Mono.error(new IllegalArgumentException("FCM 알림 전송에 실패했습니다."));
//                                }
//                                return Mono.just(order);
//                            })
//                            .then(Mono.just(order));
//                })
                // TODO: supplyId에 해당하는 소모품 임시 용량 추가
//                .flatMap(order -> {
//                    return supplyRepository.findById(order.getSupplyId())
//                            .flatMap(supply -> {
//                                supply.setAmountTmp(supply.getAmountTmp() + order.getOrderCount());
//                                return supplyRepository.save(supply);
//                            })
//                            .thenReturn(order);
//                })
                .map(order -> {
                    Map<String, Object> response = new HashMap<>();
                    response.put("id", order.getId());
                    return response;
                });
    }

    @Transactional
    public Mono<Order> updateOrderStatus(String orderStoreId, OrderStatusUpdateDto orderStatusUpdateDto) {
        return orderRepository.findByOrderStoreId(orderStoreId)
                .flatMap(order -> {
                    order.setOrderStatus(orderStatusUpdateDto.getOrderStatus());
                    return orderRepository.save(order);
                })
                // TODO: 주문 상태 = 배송 완료로 변경 시 supplyId에 해당하는 소모품 임시 용량 값 -> 실제 용량 값에 반영
//                .flatMap(order -> {
//                    if ("배송 완료".equals(order.getOrderStatus())) {
//                        return supplyRepository.findById(order.getSupplyId())
//                                .flatMap(supply -> {
//                                    supply.setAmount(supply.getAmount() + supply.getAmountTmp());
//                                    supply.setAmountTmp(0);
//                                    return supplyRepository.save(supply);
//                                })
//                                .thenReturn(order);
//                    } else {
//                        return Mono.just(order);
//                    }
//                })
                // TODO: 그룹 ID에 해당하는 유저들의 token list로 보내게 로직 수정해야함
//                .flatMap(order -> {
//                    return fcmRepository.findAll()
//                            .flatMap(fcmToken -> {
//                                String title;
//                                if ("주문 취소".equals(order.getOrderStatus())) {
//                                    title = "주문 상태 변경";
//                                } else {
//                                    title = "배송 상태 변경";
//                                }
//                                FcmSendDto fcmSendDto = new FcmSendDto(fcmToken.getToken(), title, order.getOrderStatus() + " : " + order.getOrderStore() + "에서 주문한 " + order.getProductName());
//                                try{
//                                    fcmService.sendMessageTo(fcmSendDto);
//                                } catch (Exception e) {
//                                    return Mono.error(new IllegalArgumentException("FCM 알림 전송에 실패했습니다."));
//                                }
//                                return Mono.just(order);
//                            })
//                            .then(Mono.just(order));
//                })
                .switchIfEmpty(Mono.error(new IllegalArgumentException("주문을 찾을 수 없습니다.")));
    }
}
