package com.expensemanager.dto;

import com.expensemanager.enums.CategoryType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionDto implements Comparable<TransactionDto> {

    private Long id;
    private String title;
    private BigDecimal amount;
    private String categoryName;
    private LocalDate transactionDate;
    private CategoryType type; // INCOME or EXPENSE
    private String description;

    @Override
    public int compareTo(TransactionDto other) {
        // Sort descending by date, then by id descending
        int dateCompare = other.transactionDate.compareTo(this.transactionDate);
        if (dateCompare != 0) {
            return dateCompare;
        }
        return other.id.compareTo(this.id);
    }
}
