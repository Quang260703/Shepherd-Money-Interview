package com.shepherdmoney.interviewproject.controller;

import com.shepherdmoney.interviewproject.model.BalanceHistory;
import com.shepherdmoney.interviewproject.model.CreditCard;
import com.shepherdmoney.interviewproject.model.User;
import com.shepherdmoney.interviewproject.repository.CreditCardRepository;
import com.shepherdmoney.interviewproject.repository.UserRepository;
import com.shepherdmoney.interviewproject.vo.request.AddCreditCardToUserPayload;
import com.shepherdmoney.interviewproject.vo.request.UpdateBalancePayload;
import com.shepherdmoney.interviewproject.vo.response.CreditCardView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
public class CreditCardController {

    // wire in CreditCard repository here (~1 line)
    @Autowired
    private CreditCardRepository creditCardRepository;

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/credit-card")
    public ResponseEntity<Integer> addCreditCardToUser(@RequestBody AddCreditCardToUserPayload payload) {
        try {
            // Create a credit card entity, and then associate that credit card with user
            // with given userId
            // Return 200 OK with the credit card id if the user exists and credit card is
            // successfully associated with the user
            // Return other appropriate response code for other exception cases
            // Do not worry about validating the card number, assume card number could be
            // any arbitrary format and length
            CreditCard creditCard = new CreditCard();

            Optional<User> user = userRepository.findById(payload.getUserId());

            // Return 400 Bad Request if no user exists
            if (!user.isPresent()) {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }

            // Set credit card data
            creditCard.setIssuanceBank(payload.getCardIssuanceBank());
            creditCard.setNumber(payload.getCardNumber());

            // Add credit card to owner's list of credit cards
            user.get().addCreditCard(creditCard);

            // Save both user and credit card to repository
            creditCardRepository.save(creditCard);
            userRepository.save(user.get());

            return new ResponseEntity<>(creditCard.getId(), HttpStatus.OK);
        } catch (Exception exception) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/credit-card:all")
    public ResponseEntity<List<CreditCardView>> getAllCardOfUser(@RequestParam int userId) {
        try {
            // return a list of all credit card associated with the given userId, using
            // CreditCardView class
            // if the user has no credit card, return empty list, never return null
            Optional<User> user = userRepository.findById(userId);

            // Return 400 Bad Request if no user exists
            if (!user.isPresent()) {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }

            // Get the list of all credit card associated with the user
            Set<CreditCard> creditCards = user.get().getCreditCards();
            List<CreditCardView> creditCardList = new ArrayList<CreditCardView>();
            for (var creditCard : creditCards) {
                CreditCardView creditCardView = new CreditCardView(creditCard.getIssuanceBank(),
                        creditCard.getNumber());
                creditCardList.add(creditCardView);
            }

            return new ResponseEntity<>(creditCardList, HttpStatus.OK);
        } catch (Exception exception) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/credit-card:user-id")
    public ResponseEntity<Integer> getUserIdForCreditCard(@RequestParam String creditCardNumber) {
        try {
            // Given a credit card number, efficiently find whether there is a user
            // associated with the credit card
            // If so, return the user id in a 200 OK response. If no such user exists,
            // return 400 Bad Request
            Optional<CreditCard> creditCard = creditCardRepository.findByNumber(creditCardNumber);

            // Return 400 Bad Request if no credit card exists
            if (!creditCard.isPresent()) {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }

            User user = creditCard.get().getOwner();

            // Return 400 Bad Request if no user exists
            if (user == null) {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }

            return new ResponseEntity<>(user.getId(), HttpStatus.OK);
        } catch (Exception exception) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/credit-card:update-balance")
    public ResponseEntity<Integer> updateBalance(@RequestBody UpdateBalancePayload[] payload) {
        try {
            // Given a list of transactions, update credit cards' balance history.
            // 1. For the balance history in the credit card
            // 2. If there are gaps between two balance dates, fill the empty date with the
            // balance of the previous date
            // 3. Given the payload `payload`, calculate the balance different between the
            // payload and the actual balance stored in the database
            // 4. If the different is not 0, update all the following budget with the
            // difference
            // For example: if today is 4/12, a credit card's balanceHistory is [{date:
            // 4/12, balance: 110}, {date: 4/10, balance: 100}],
            // Given a balance amount of {date: 4/11, amount: 110}, the new balanceHistory
            // is
            // [{date: 4/12, balance: 120}, {date: 4/11, balance: 110}, {date: 4/10,
            // balance: 100}]
            // Return 200 OK if update is done and successful, 400 Bad Request if the given
            // card number
            // is not associated with a card.
            for (var transaction : payload) {
                Optional<CreditCard> creditCard = creditCardRepository.findByNumber(transaction.getCreditCardNumber());
                // Return 400 Bad Request if no credit card exists
                if (!creditCard.isPresent()) {
                    return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
                }

                // Get balance history
                TreeSet<BalanceHistory> balanceHistory = new TreeSet<>(creditCard.get().getBalanceHistory());

                // Get the transaction date
                BalanceHistory date = new BalanceHistory();
                date.setDate(transaction.getBalanceDate());
                date.setBalance(transaction.getBalanceAmount());

                // Add date if balance history is empty
                if (balanceHistory.isEmpty()) {
                    creditCard.get().addBalance(date);
                } else {
                    // Filling the gaps in balance history
                    creditCard.get().fillingBalanceHistory();
                    // Update all the following budget with the difference
                    creditCard.get().updateBalanceHistory(date);
                }
                // Save to repository
                creditCardRepository.save(creditCard.get());
            }

            return new ResponseEntity<>(HttpStatus.OK);
        } catch (Exception exception) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

}
