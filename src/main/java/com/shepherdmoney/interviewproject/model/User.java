package com.shepherdmoney.interviewproject.model;

import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Getter
@Setter
@ToString
@RequiredArgsConstructor
@Table(name = "MyUser")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

    private String name;

    private String email;

    // User's credit card
    // HINT: A user can have one or more, or none at all. We want to be able to
    // query credit cards by user
    // and user by a credit card.
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "owner")
    // Map the credit cards to property owner. Since each credit card is unique,
    // HashSet is implemented.
    private Set<CreditCard> creditCards = new HashSet<CreditCard>();

    // Add a credit card to user's list of credit cards
    public void addCreditCard(CreditCard card) {
        creditCards.add(card);
    }
}
