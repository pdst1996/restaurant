package com.ps.restaurant.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ps.restaurant.dto.ErrorResponseException.ConflictException;
import com.ps.restaurant.dto.ErrorResponseException;
import com.ps.restaurant.dto.OrderRequest;
import com.ps.restaurant.dto.OrderResponse;
import com.ps.restaurant.dto.filters.FilterOrderRequest;
import com.ps.restaurant.interfaces.OrderService;
import com.ps.restaurant.model.Allergy;
import com.ps.restaurant.model.Order;
import com.ps.restaurant.model.OrderStatus;
import com.ps.restaurant.model.Table;
import com.ps.restaurant.model.User;
import com.ps.restaurant.repository.AllergyRepository;
import com.ps.restaurant.repository.OrderRepository;
import com.ps.restaurant.repository.TableRepository;
import com.ps.restaurant.repository.UserRepository;
import com.ps.restaurant.repository.specification.OrderSpecifications;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderServiceImpl implements OrderService {

    private final AllergyRepository allergyRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final TableRepository tableRepository;
    private final OrderWorkflow workflow;

    @Override
    public Optional<Order> getOrderById(Long id) {
        return orderRepository.findById(id);
    }

    @Override
    public List<OrderResponse> filterOrders(FilterOrderRequest filter) {

        ServiceSupport.validateDateRange(filter.fromDate(), filter.toDate());

        var specification = OrderSpecifications.withFilters(filter);
        var orders = filter.page() == null && filter.size() == null
                ? orderRepository.findAll(specification)
                : orderRepository.findAll(specification, PageRequest.of(
                        filter.page() == null ? 0 : filter.page(), filter.size() == null ? 50 : filter.size(),
                        Sort.by(Sort.Direction.DESC, "id"))).getContent();
        return orders.stream()
                .map(OrderResponse::from)
                .toList();
    }

    @Override
    public long countOrders(FilterOrderRequest filter) {
        ServiceSupport.validateDateRange(filter.fromDate(), filter.toDate());
        return orderRepository.count(OrderSpecifications.withFilters(filter));
    }

    @Override
    @Transactional
    public OrderResponse createOrder(OrderRequest orderRequest) {
        if (orderRepository.findByOrderNumber(orderRequest.orderNumber()).isPresent()) {
            throw new ConflictException("Order with this orderNumber already exists");
        }
        User waiter = ServiceSupport.requireById(userRepository, orderRequest.waiterId(), "Waiter");
        workflow.enforceOwnership(waiter);
        Table table = orderRequest.tableId() == null ? null
                : ServiceSupport.requireById(tableRepository, orderRequest.tableId(), "Table");

        List<Allergy> allergies = new ArrayList<>();
        if (orderRequest.allergyIds() != null) {
            for (Long id : orderRequest.allergyIds().stream().distinct().toList()) {
                allergies.add(ServiceSupport.requireById(allergyRepository, id, "Allergy"));
            }
        }
        Order order = new Order();
        order.setAllergies(allergies);
        order.setCustomer(orderRequest.customer());
        order.setOrderNumber(orderRequest.orderNumber());
        order.setTogo(orderRequest.togo());
        if (orderRequest.status() != null && orderRequest.status() != OrderStatus.DRAFT) {
            if (orderRequest.status() != OrderStatus.OPEN) {
                throw new ErrorResponseException(422, "INVALID_INITIAL_ORDER_STATUS", "Initial status must be DRAFT or OPEN");
            }
            order.setStatus(OrderStatus.OPEN);
        }
        order.setOtherAllergies(orderRequest.otherAllergies());
        order.setNotes(orderRequest.notes());
        order.setTable(table);
        order.setWaiter(waiter);
        orderRepository.save(order);
        workflow.created(order);
        return OrderResponse.from(order);
    }

    @Override
    @Transactional
    public OrderResponse updateOrder(Long id, OrderRequest orderRequest) {

        // 1. Retrieve the existing order
        Order order = workflow.lock(id);
        workflow.requireEditable(order);

        // 2. Validate order number uniqueness (excluding current order)
        orderRepository.findByOrderNumber(orderRequest.orderNumber())
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(existing -> {
                    throw new ConflictException(
                            "Order with this orderNumber already exists");
                });

        // 3. Retrieve waiter
        User waiter = ServiceSupport.requireById(
                userRepository,
                orderRequest.waiterId(),
                "Waiter");
        workflow.enforceOwnership(waiter);

        // 4. Retrieve table (nullable)
        Table table = orderRequest.tableId() == null
                ? null
                : ServiceSupport.requireById(
                        tableRepository,
                        orderRequest.tableId(),
                        "Table");

        // 5. Retrieve allergies
        List<Allergy> allergies = new ArrayList<>();

        if (orderRequest.allergyIds() != null) {
            for (Long allergyId : orderRequest.allergyIds().stream()
                    .distinct()
                    .toList()) {

                allergies.add(ServiceSupport.requireById(
                        allergyRepository,
                        allergyId,
                        "Allergy"));
            }
        }

        // 6. Update existing entity
        order.setAllergies(allergies);
        order.setCustomer(orderRequest.customer());
        order.setOrderNumber(orderRequest.orderNumber());
        order.setTogo(orderRequest.togo());
        if (orderRequest.status() != null && orderRequest.status() != order.getStatus()
                && !(order.getStatus() == OrderStatus.DRAFT && orderRequest.status() == OrderStatus.OPEN)) {
            throw new ErrorResponseException(422, "USE_ORDER_ACTION", "Use a dedicated action to change this order status");
        }
        order.setOtherAllergies(orderRequest.otherAllergies());
        order.setNotes(orderRequest.notes());
        order.setTable(table);
        order.setWaiter(waiter);
        if (order.getStatus() == OrderStatus.DRAFT && orderRequest.status() == OrderStatus.OPEN) {
            workflow.apply(order, OrderStatus.OPEN, null);
        }
        return OrderResponse.from(order);
    }

}
