package com.shepherdmoney.interviewproject.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;

@Entity
@Getter
@Setter
@ToString
@RequiredArgsConstructor
public class CreditCard {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

    private String issuanceBank;

    private String number;

    // Credit card's owner. For detailed hint, please see User class
    // Some field here <> owner;
    @ManyToOne
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    // Credit card's balance history. It is a requirement that the dates in the
    // balanceHistory
    // list must be in chronological order, with the most recent date appearing
    // first in the list.
    // Additionally, the last object in the "list" must have a date value that
    // matches today's date,
    // since it represents the current balance of the credit card. For example:
    // [
    // {date: '2023-04-10', balance: 800},
    // {date: '2023-04-11', balance: 1000},
    // {date: '2023-04-12', balance: 1200},
    // {date: '2023-04-13', balance: 1100},
    // {date: '2023-04-16', balance: 900},
    // ]
    // ADDITIONAL NOTE: For the balance history, you can use any data structure that
    // you think is appropriate.
    // It can be a list, array, map, pq, anything. However, there are some
    // suggestions:
    // 1. Retrieval of a balance of a single day should be fast
    // 2. Traversal of the entire balance history should be fast
    // 3. Insertion of a new balance should be fast
    // 4. Deletion of a balance should be fast
    // 5. It is possible that there are gaps in between dates (note the 04-13 and
    // 04-16)
    // 6. In the condition that there are gaps, retrieval of "closest" balance date
    // should also be fast. Aka, given 4-15, return 4-16 entry tuple
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "creditCard")
    private SortedSet<BalanceHistory> balanceHistory = new TreeSet<BalanceHistory>(
            (BalanceHistory his1, BalanceHistory his2) -> his1.getDate().compareTo(his2.getDate()));

    // Function to add a balance to history and set this credit card to that
    // balanceHistory
    public void addBalance(BalanceHistory balance) {
        balance.setCreditCard(this);
        balanceHistory.add(balance);
    }

    // Function that fills the gap in the balance history
    public void fillingBalanceHistory() {
        TreeSet<BalanceHistory> updatedHistory = new TreeSet<>(balanceHistory);
        // Create a list to store the gap dates
        List<BalanceHistory> balanceGap = new ArrayList<BalanceHistory>();
        // Using two pointers
        BalanceHistory left = updatedHistory.first();
        BalanceHistory right = updatedHistory.higher(left);
        while (right != null) {
            // Getting the differences
            long days = ChronoUnit.DAYS.between(left.getDate(), right.getDate());
            if (days > 1) {
                // Filling the gaps
                for (int i = 1; i < days; i++) {
                    BalanceHistory newHistory = new BalanceHistory();
                    newHistory.setBalance(left.getBalance());
                    newHistory.setDate(left.getDate().plusDays(i));
                    balanceGap.add(newHistory);
                }
            }
            left = right;
            right = updatedHistory.higher(left);
        }
        // Update the history
        for (var gap : balanceGap) {
            addBalance(gap);
        }
    }

    // Function that update balanceHistory using the difference in balance
    public void updateBalanceHistory(BalanceHistory date) {
        TreeSet<BalanceHistory> updatedHistory = new TreeSet<>(balanceHistory);
        // Getting the closest date to the transaction date
        BalanceHistory closestDate = updatedHistory.ceiling(date);
        if (closestDate != null && closestDate.getDate().equals(date.getDate())) {
            // Update balance for each day counting from transaction date
            double balance = date.getBalance() - closestDate.getBalance();
            closestDate.setBalance(closestDate.getBalance() + balance);
            // Two pointer
            BalanceHistory left = closestDate;
            BalanceHistory right = updatedHistory.higher(left);
            while (right != null) {
                right.setBalance(right.getBalance() + balance);
                left = right;
                right = updatedHistory.higher(left);
            }
            // Set the new history
            balanceHistory = updatedHistory;
        } else {
            // If transaction date not in the history, add it to the set and fill the gaps
            addBalance(date);
            fillingBalanceHistory();
        }
    }
}
