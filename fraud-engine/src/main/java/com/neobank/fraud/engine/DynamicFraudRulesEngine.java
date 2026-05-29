package com.neobank.fraud.engine;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.util.*;
import java.util.concurrent.*;

@Component
@Slf4j
public class DynamicFraudRulesEngine {
    private final List<FraudRule> rules = new CopyOnWriteArrayList<>();
    private final ScriptEngine groovyEngine = new ScriptEngineManager().getEngineByName("groovy");

    public DynamicFraudRulesEngine() {
        // Load default rules
        rules.add(new AmountBasedRule());
        rules.add(new VelocityBasedRule());
        rules.add(new LocationBasedRule());
    }

    public FraudDecision evaluate(TransactionContext ctx) {
        List<RuleEvaluation> evaluations = new ArrayList<>();

        for (FraudRule rule : rules) {
            if (rule.isActive() && rule.evaluate(ctx)) {
                evaluations.add(new RuleEvaluation(rule, rule.getAction(), rule.getReason()));
            }
        }

        return resolveConflicts(evaluations);
    }

    private FraudDecision resolveConflicts(List<RuleEvaluation> evaluations) {
        Optional<RuleEvaluation> highest = evaluations.stream()
                .max(Comparator.comparingInt(e -> e.rule().getPriority()));

        if (highest.isEmpty()) return FraudDecision.APPROVE;

        String action = highest.get().action();
        String reason = highest.get().reason();

        if ("BLOCK".equals(action)) {
            return FraudDecision.BLOCK.withReason(reason);
        } else if ("REVIEW".equals(action)) {
            return FraudDecision.REVIEW.withReason(reason);
        } else {
            return FraudDecision.APPROVE;
        }
    }

    interface FraudRule {
        String getName();
        int getPriority();
        boolean evaluate(TransactionContext ctx);
        boolean isActive();
        String getAction();
        String getReason();
    }

    class AmountBasedRule implements FraudRule {
        @Override
        public String getName() { return "Amount Threshold"; }

        @Override
        public int getPriority() { return 100; }

        @Override
        public boolean evaluate(TransactionContext ctx) {
            // FIXED: Use ctx.amount() not ctx.getAmount()
            return ctx.amount() > 10000;
        }

        @Override
        public boolean isActive() { return true; }

        @Override
        public String getAction() { return "BLOCK"; }

        @Override
        public String getReason() { return "Amount exceeds limit"; }
    }

    class VelocityBasedRule implements FraudRule {
        @Override
        public String getName() { return "Transaction Velocity"; }

        @Override
        public int getPriority() { return 90; }

        @Override
        public boolean evaluate(TransactionContext ctx) {
            // FIXED: Use ctx.velocity() not ctx.getVelocity()
            return ctx.velocity() > 5;
        }

        @Override
        public boolean isActive() { return true; }

        @Override
        public String getAction() { return "REVIEW"; }

        @Override
        public String getReason() { return "Too many transactions"; }
    }

    class LocationBasedRule implements FraudRule {
        @Override
        public String getName() { return "Location Check"; }

        @Override
        public int getPriority() { return 80; }

        @Override
        public boolean evaluate(TransactionContext ctx) {
            // FIXED: Use ctx.country() not ctx.getCountry()
            return Set.of("XX", "YY", "NK", "IR").contains(ctx.country());
        }

        @Override
        public boolean isActive() { return true; }

        @Override
        public String getAction() { return "BLOCK"; }

        @Override
        public String getReason() { return "Suspicious country"; }
    }

    record RuleEvaluation(FraudRule rule, String action, String reason) {}
}

// Records defined outside the class
record TransactionContext(String transactionId, Double amount, String userId, int velocity, String country) {}

record FraudDecision(String status, String reason) {
    public static final FraudDecision APPROVE = new FraudDecision("APPROVE", null);
    public static final FraudDecision BLOCK = new FraudDecision("BLOCK", null);
    public static final FraudDecision REVIEW = new FraudDecision("REVIEW", null);

    public FraudDecision withReason(String reason) {
        return new FraudDecision(status, reason);
    }
}