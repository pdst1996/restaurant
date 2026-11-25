package com.ps.restaurant.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;

import com.ps.restaurant.dto.filters.FilterOrderRequest;
import com.ps.restaurant.dto.OrderResponse;
import com.ps.restaurant.model.Order;
import com.ps.restaurant.model.User;
import com.ps.restaurant.repository.AllergyRepository;
import com.ps.restaurant.repository.OrderRepository;
import com.ps.restaurant.repository.TableRepository;
import com.ps.restaurant.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTests {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private AllergyRepository allergyRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TableRepository tableRepository;

    private OrderServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new OrderServiceImpl(allergyRepository, orderRepository, userRepository, tableRepository);
    }

    @Test
    void findsOrderDirectlyById() {
        Order order = new Order();
        order.setId(1L);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        assertThat(service.getOrderById(1L)).containsSame(order);
        verify(orderRepository).findById(1L);
        verifyNoMoreInteractions(orderRepository);
    }

    @Test
    void returnsEmptyWhenOrderDoesNotExist() {
        when(orderRepository.findById(99L)).thenReturn(Optional.empty());

        assertThat(service.getOrderById(99L)).isEmpty();
        verify(orderRepository).findById(99L);
        verifyNoMoreInteractions(orderRepository);
    }

    @Test
    void returnsOrdersUsingSpecificationWithoutDateFilters() {
        User waiter = new User();
        waiter.setId(2L);
        waiter.setRole(new com.ps.restaurant.model.Role());
        Order order = new Order();
        order.setId(1L);
        order.setWaiter(waiter);
        List<Order> orders = List.of(order);
        when(orderRepository.findAll(ArgumentMatchers.<Specification<Order>>any())).thenReturn(orders);

        assertThat(service.filterOrders(filter(null, null))).containsExactly(OrderResponse.from(order));
        verify(orderRepository).findAll(ArgumentMatchers.<Specification<Order>>any());
    }

    @Test
    void acceptsEqualAndOpenEndedDateRanges() {
        Instant date = Instant.parse("2026-10-26T12:00:00Z");
        when(orderRepository.findAll(ArgumentMatchers.<Specification<Order>>any())).thenReturn(List.of());

        assertThat(service.filterOrders(filter(date, date))).isEmpty();
        assertThat(service.filterOrders(filter(date, null))).isEmpty();
        assertThat(service.filterOrders(filter(null, date))).isEmpty();
    }

    @Test
    void rejectsReversedDateRangeBeforeQueryingRepository() {
        Instant date = Instant.parse("2026-10-26T12:00:00Z");

        assertThatThrownBy(() -> service.filterOrders(filter(date.plusSeconds(1), date)))
                .isInstanceOf(com.ps.restaurant.dto.ErrorResponseException.class)
                .hasMessage("fromDate must be before or equal to toDate");
        verifyNoInteractions(orderRepository);
    }

    @Test
    void countsOrdersWithoutLoadingThem() {
        when(orderRepository.count(ArgumentMatchers.<Specification<Order>>any())).thenReturn(5L);

        assertThat(service.countOrders(filter(null, null))).isEqualTo(5);
        verify(orderRepository).count(ArgumentMatchers.<Specification<Order>>any());
        verifyNoMoreInteractions(orderRepository);
    }

    @Test
    void rejectsReversedDateRangeBeforeCounting() {
        Instant date = Instant.parse("2026-10-26T12:00:00Z");
        assertThatThrownBy(() -> service.countOrders(filter(date.plusSeconds(1), date)))
                .isInstanceOf(com.ps.restaurant.dto.ErrorResponseException.class);
        verifyNoInteractions(orderRepository);
    }

    private FilterOrderRequest filter(Instant fromDate, Instant toDate) {
        return new FilterOrderRequest(null, null, null, null, null, fromDate, toDate);
    }
}
