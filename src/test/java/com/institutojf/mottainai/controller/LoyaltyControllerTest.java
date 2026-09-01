package com.institutojf.mottainai.controller;

import com.institutojf.mottainai.dto.request.RedeemRewardRequest;
import com.institutojf.mottainai.dto.response.LoyaltyAccountResponse;
import com.institutojf.mottainai.dto.response.LoyaltyTransactionResponse;
import com.institutojf.mottainai.model.Customer;
import com.institutojf.mottainai.security.CustomerAccess;
import com.institutojf.mottainai.service.LoyaltyService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.TestingAuthenticationToken;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoyaltyControllerTest {
    @Mock
    private LoyaltyService loyaltyService;
    @Mock
    private CustomerAccess customerAccess;
    @InjectMocks
    private LoyaltyController controller;

    @Test
    void shouldGetBalanceForAuthenticatedCustomer() {
        Customer customer = customer(5);
        LoyaltyAccountResponse response = new LoyaltyAccountResponse(1, 5, "Customer", "customer@example.com", 10, null, true, null);
        TestingAuthenticationToken authentication = authentication();
        when(customerAccess.currentCustomer(authentication)).thenReturn(customer);
        when(loyaltyService.getLoyaltyAccount(5)).thenReturn(response);

        assertEquals(response, controller.getBalance(authentication));
    }

    @Test
    void shouldGetTransactionsForAuthenticatedCustomer() {
        Customer customer = customer(5);
        TestingAuthenticationToken authentication = authentication();
        List<LoyaltyTransactionResponse> response = List.of();
        when(customerAccess.currentCustomer(authentication)).thenReturn(customer);
        when(loyaltyService.getTransactions(5)).thenReturn(response);

        assertEquals(response, controller.getTransactions(authentication));
    }

    @Test
    void shouldRedeemForAuthenticatedCustomer() {
        Customer customer = customer(5);
        TestingAuthenticationToken authentication = authentication();
        RedeemRewardRequest request = new RedeemRewardRequest(3);
        when(customerAccess.currentCustomer(authentication)).thenReturn(customer);

        controller.redeemReward(request, authentication);

        verify(loyaltyService).redeemReward(5, request);
    }

    private Customer customer(Integer id) {
        Customer customer = new Customer();
        customer.setId(id);
        return customer;
    }

    private TestingAuthenticationToken authentication() {
        TestingAuthenticationToken authentication = new TestingAuthenticationToken("firebase-uid", null);
        authentication.setAuthenticated(true);
        return authentication;
    }
}
