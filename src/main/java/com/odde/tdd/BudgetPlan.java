package com.odde.tdd;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static java.time.temporal.ChronoUnit.DAYS;

public class BudgetPlan {
    private final BudgetRepo repo;

    public BudgetPlan(BudgetRepo repo) {
        this.repo = repo;
    }

    public long query(LocalDate startDate, LocalDate endDate){
        //If Start and End are in the same budget period
        if (startDate.withDayOfMonth(1).equals(endDate.withDayOfMonth(1))){
            long amount = getBudgetAmount(startDate);
           long daysInPeriod = getBudgetDaysCount(startDate);
            long daysBetween = startDate.until(endDate, DAYS) + 1;
            return amount / daysInPeriod * daysBetween;
        }

        //If the area between Start and End overlap at least two budget periods.
        if (YearMonth.from(startDate).isBefore(YearMonth.from(endDate))){
            long amountStartPeriod = getBudgetAmount(startDate);
            long daysInStartPeriod = getBudgetDaysCount(startDate);
            long daysAfterStartDateInStartPeriod = startDate.until(startDate.withDayOfMonth(startDate.lengthOfMonth()), DAYS) + 1;
            long totalStartPeriod =  amountStartPeriod / daysInStartPeriod * daysAfterStartDateInStartPeriod;

            long totalInMiddle = 0;
            for (Budget budget : getBudgetsBetween(startDate, endDate)) {
                totalInMiddle += budget.getAmount();
                Logger.getLogger(this.getClass().getName()).info("Added " + budget.getMonth() + " to total for one period in between; current value is " + totalInMiddle);
            }

            long amountEndPeriod = getBudgetAmount(endDate);
            long daysInEndPeriod = getBudgetDaysCount(endDate);
            long daysBeforeEndDateInEndPeriod = endDate.getDayOfMonth();
            long totalEndPeriod = amountEndPeriod /  daysInEndPeriod * daysBeforeEndDateInEndPeriod;

            return totalStartPeriod + totalInMiddle + totalEndPeriod;
        }

        throw new RuntimeException("You should not be here.  We have returned all legitimate numbers from getAmount(Date, Date) in BudgetCategoryImpl.  Please contact Wyatt Olson with details on how you got here (what steps did you perform in Buddi to get this error message).");
    }

    private long getBudgetDaysCount(LocalDate date) {
        Optional<Budget> budget = getBudgetContaining(date);
        return budget.map(value -> value.getMonth().lengthOfMonth()).orElse(1);
    }

    private Optional<Budget> getBudgetContaining(LocalDate date){
        List<Budget> budgets = repo.findAll();
        return budgets.stream().filter(budget -> budget.getMonth().atDay(1).equals(date.withDayOfMonth(1))).findFirst();
    }

    private long getBudgetAmount(LocalDate date) {
        Optional<Budget> budget = getBudgetContaining(date);
        return budget.map(Budget::getAmount).orElse(0L);
    }

    public List<Budget> getBudgetsBetween(LocalDate startDate, LocalDate endDate){
        List<Budget> budgets = repo.findAll();
        return budgets.stream().filter(budget -> budget.getMonth().atDay(1).isAfter(startDate) && budget.getMonth().atEndOfMonth().isBefore(endDate)).collect(Collectors.toList());
    }
}
