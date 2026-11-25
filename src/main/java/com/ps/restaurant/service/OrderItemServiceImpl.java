package com.ps.restaurant.service;

import java.util.List;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ps.restaurant.dto.ApiResponse;
import com.ps.restaurant.dto.ErrorResponseException;
import com.ps.restaurant.dto.ErrorResponseException.ConflictException;
import com.ps.restaurant.model.Order;
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
import com.ps.restaurant.repository.specification.OrderItemSpecifications;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderItemServiceImpl implements OrderItemService {

    private final OrderItemRepository orderItemRepository;
    private final OrderWorkflow workflow;
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
        Order order = workflow.lock(request.orderId());
        workflow.requireEditable(order);
        List<ItemAttribute> itemAttributes = request.attributeIds() == null ? List.of()
                : request.attributeIds().stream().distinct()
                        .map(id -> ServiceSupport.requireById(itemAttributeRepository, id, "Item attribute"))
                        .toList();
        OrderItem item = new OrderItem();
        var product = ServiceSupport.requireById(itemsRepository, request.itemId(), "Item");
        var size = ServiceSupport.requireById(sizeRepository, request.sizeId(), "Item size");
        if (!product.isAvailable()) throw new ConflictException("This menu item is unavailable");
        if (product.getSizes().stream().noneMatch(s -> s.getId().equals(size.getId()))
                || itemAttributes.stream().anyMatch(a -> product.getAvailableAttributes().stream()
                        .noneMatch(allowed -> a.getId().equals(allowed.getId())))) {
            throw new ErrorResponseException(422, "INVALID_ITEM_OPTIONS", "Size or attribute does not belong to this menu item");
        }
        item.setOrder(order);
        item.setItem(product);
        item.setSize(size);
        item.setQuantity(request.quantity());
        item.setAttributes(itemAttributes);
        item.setUnitPrice(product.getPrice());
        item.setNotes(request.notes());
        order.getItems().add(item);
        workflow.updateTotal(order);
        return OrderItemResponse.from(orderItemRepository.save(item));
    }

    @Override
    @Transactional
    public ApiResponse<String> deleteOrderItem(Long orderItemId) {
        Long orderId = orderItemRepository.findParentId(orderItemId).orElseThrow(
                () -> new ErrorBuilder("ORDER_ITEM_NOT_FOUND", "OrderItemService.deleteOrderItem", null)
                        .statusCode(HttpStatus.NOT_FOUND)
                        .createException());

        Order order = workflow.lock(orderId);
        workflow.requireEditable(order);
        OrderItem orderItem = ServiceSupport.requireById(orderItemRepository, orderItemId, "Order item");
        order.getItems().remove(orderItem);
        workflow.updateTotal(order);
        orderItemRepository.delete(orderItem);

        return ApiResponse.ok("Item " + orderItem.getId() + " deleted successfully", null);
    }
}
