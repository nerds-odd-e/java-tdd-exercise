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
        Logger.getLogger(this.getClass().getName()).info("Starting to calculate the budgeted amount for Budget between " + startDate + " and " + endDate + ".");

        //If Start and End are in the same budget period
        if (startDate.withDayOfMonth(1).equals(endDate.withDayOfMonth(1))){
//			Logger.getLogger().info("Start Date and End Date are in the same period.");
            long amount = getBudgetAmount(startDate);
//			Logger.getLogger().info("Amount = " + amount);
            long daysInPeriod = getBudgetDaysCount(startDate);
//			Logger.getLogger().info("Days in Period = " + daysInPeriod);
            long daysBetween = startDate.until(endDate, DAYS) + 1;
//			Logger.getLogger().info("Days Between = " + daysBetween);

//			Logger.getLogger().info("Returning " + (long) (((double) amount / (double) daysInPeriod) * daysBetween));
//			Logger.getLogger().info("Finished calculating the budget amount.\n\n");
            return amount / daysInPeriod * daysBetween;
        }

        //If the area between Start and End overlap at least two budget periods.
        if (YearMonth.from(startDate).isBefore(YearMonth.from(endDate))){
//			Logger.getLogger().info("Start Date and End Date are in different budget periods.");
            long amountStartPeriod = getBudgetAmount(startDate);
//			Logger.getLogger().info("Amount Start Period = " + amountStartPeriod);
            long daysInStartPeriod = getBudgetDaysCount(startDate);
//			Logger.getLogger().info("Days in Start Period = " + daysInStartPeriod);
            long daysAfterStartDateInStartPeriod = startDate.until(startDate.withDayOfMonth(startDate.lengthOfMonth()), DAYS) + 1;
//			Logger.getLogger().info("Days After Start Date in Start Period = " + daysAfterStartDateInStartPeriod);
            double totalStartPeriod = (((double) amountStartPeriod / (double) daysInStartPeriod) * daysAfterStartDateInStartPeriod);
//			Logger.getLogger().info("Total in Start Period = " + totalStartPeriod);

            double totalInMiddle = 0;
            for (Budget budget : getBudgetsBetween(startDate, endDate)) {
                totalInMiddle += budget.getAmount();
                Logger.getLogger(this.getClass().getName()).info("Added " + budget.getMonth() + " to total for one period in between; current value is " + totalInMiddle);
            }
//			Logger.getLogger().info("Total in Middle = " + totalInMiddle);

            long amountEndPeriod = getBudgetAmount(endDate);
//			Logger.getLogger().info("Amount End Period = " + amountEndPeriod);
            long daysInEndPeriod = getBudgetDaysCount(endDate);
//			Logger.getLogger().info("Days in End Period = " + daysInEndPeriod);
            long daysBeforeEndDateInEndPeriod = endDate.getDayOfMonth();
//			Logger.getLogger().info("Days before End Period = " + daysBeforeEndDateInEndPeriod);
            double totalEndPeriod = (long) (((double) amountEndPeriod / (double) daysInEndPeriod) * daysBeforeEndDateInEndPeriod);
//			Logger.getLogger().info("Total in End Period = " + totalEndPeriod);

//			Logger.getLogger().info("Sum of Start Period, Middle, and End Period = " + (totalStartPeriod + totalInMiddle + totalEndPeriod));
//			Logger.getLogger().info("Finished Calculating the Budget Amount\n\n");
            return (long) (totalStartPeriod + totalInMiddle + totalEndPeriod);
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
