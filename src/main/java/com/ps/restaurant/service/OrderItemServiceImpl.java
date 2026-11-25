package com.ps.restaurant.service;

import java.util.List;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ps.restaurant.dto.ApiResponse;
import com.ps.restaurant.dto.OrderItemRequest;
import com.ps.restaurant.dto.OrderItemResponse;
import com.ps.restaurant.dto.filters.FilterOrderItemRequest;
import com.ps.restaurant.interfaces.OrderItemService;
import com.ps.restaurant.model.ItemAttribute;
import com.ps.restaurant.model.OrderItem;
import com.ps.restaurant.repository.ItemAttributesRepository;
import com.ps.restaurant.repository.ItemSizeRepository;
import com.ps.restaurant.repository.ItemsRepository;
import com.ps.restaurant.repository.OrderItemRepository;
import com.ps.restaurant.repository.OrderRepository;
import com.ps.restaurant.repository.specification.OrderItemSpecifications;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderItemServiceImpl implements OrderItemService {

    private final OrderItemRepository orderItemRepository;
    private final OrderRepository orderRepository;
    private final ItemsRepository itemsRepository;
    private final ItemSizeRepository sizeRepository;
    private final ItemAttributesRepository itemAttributeRepository;

    @Override
    public Optional<OrderItem> getOrderItemById(Long id) {
        return orderItemRepository.findById(id);
    }

    @Override
    public List<OrderItemResponse> filterOrderItems(FilterOrderItemRequest filter) {
        ServiceSupport.validateDateRange(filter.fromDate(), filter.toDate());
        return orderItemRepository.findAll(OrderItemSpecifications.withFilters(filter)).stream()
                .map(OrderItemResponse::from)
                .toList();
    }

    @Override
    public long countOrderItems(FilterOrderItemRequest filter) {
        ServiceSupport.validateDateRange(filter.fromDate(), filter.toDate());
        return orderItemRepository.count(OrderItemSpecifications.withFilters(filter));
    }

    @Override
    @Transactional
    public OrderItemResponse createOrderItem(OrderItemRequest request) {
        List<ItemAttribute> itemAttributes = request.attributeIds() == null ? List.of()
                : request.attributeIds().stream().distinct()
                        .map(id -> ServiceSupport.requireById(itemAttributeRepository, id, "Item attribute"))
                        .toList();
        OrderItem item = new OrderItem();
        item.setOrder(ServiceSupport.requireById(orderRepository, request.orderId(), "Order"));
        item.setItem(ServiceSupport.requireById(itemsRepository, request.itemId(), "Item"));
        item.setSize(ServiceSupport.requireById(sizeRepository, request.sizeId(), "Item size"));
        item.setQuantity(request.quantity());
        item.setAttributes(itemAttributes);
        item.setUnitPrice(request.unitPrice());
        item.setNotes(request.notes());
        item.getOrder().getItems().add(item);
        return OrderItemResponse.from(orderItemRepository.save(item));
    }

    @Override
    @Transactional
    public ApiResponse<String> deleteOrderItem(Long orderItemId) {
        OrderItem orderItem = orderItemRepository.findById(orderItemId).orElseThrow(
                () -> new ErrorBuilder("ORDER_ITEM_NOT_FOUND", "OrderItemService.deleteOrderItem", null)
                        .statusCode(HttpStatus.NOT_FOUND)
                        .createException());

        orderItem.getOrder().getItems().remove(orderItem);
        orderItemRepository.delete(orderItem);

        return ApiResponse.ok("Item " + orderItem.getId() + " deleted successfully", null);
    }
}
