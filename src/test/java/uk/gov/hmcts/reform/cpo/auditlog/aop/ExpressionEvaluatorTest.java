package uk.gov.hmcts.reform.cpo.auditlog.aop;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.expression.AnnotatedElementKey;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.spel.SpelEvaluationException;
import org.springframework.util.ReflectionUtils;
import uk.gov.hmcts.reform.BaseTest;
import uk.gov.hmcts.reform.cpo.domain.CasePaymentOrder;

import java.lang.reflect.Method;
import java.util.Objects;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.util.AssertionErrors.assertEquals;
import static org.springframework.test.util.AssertionErrors.assertNull;

class ExpressionEvaluatorTest implements BaseTest {

    private ExpressionEvaluator evaluator;

    @BeforeEach
    void setUp() {
        evaluator = new ExpressionEvaluator();
    }

    @Test
    @DisplayName("should create valuation context")
    void shouldCreateEvaluationContext() {

        // GIVEN
        String stringValue = "test";
        int intValue = 100;
        Method method = ReflectionUtils.findMethod(SampleMethods.class, "hello", String.class, Boolean.class);

        // WHEN
        EvaluationContext context = evaluator.createEvaluationContext(this, SampleMethods.class, method,
                                                                      new Object[]{stringValue, intValue}
        );

        // THEN
        assertEquals("Should match 1st arg on 'a0'",
                     stringValue, context.lookupVariable("a0")
        );
        assertEquals("Should match 1st arg on 'p0'",
                     stringValue, context.lookupVariable("p0")
        );
        assertEquals("Should match 1st arg on name",
                     stringValue, context.lookupVariable("foo")
        );

        assertEquals("Should match 2nd arg on 'a1'",
                     intValue, context.lookupVariable("a1")
        );
        assertEquals("Should match 2nd arg on 'p1'",
                     intValue, context.lookupVariable("p1")
        );
        assertEquals("Should match 1st arg on name",
                     intValue, context.lookupVariable("flag")
        );

        assertNull("Should return null when 3rd arg not found via index 'a2'",
                     context.lookupVariable("a2")
        );
        assertNull("Should return null when 3rd arg not found via index 'p2'",
                     context.lookupVariable("p2")
        );
    }

    @Test
    @DisplayName("should parse bean expression")
    void shouldParseBeanExpressions() {

        // GIVEN
        String stringValue = "test";
        CasePaymentOrder casePaymentOrder = createCasePaymentOrder();
        Method method = ReflectionUtils.findMethod(SampleMethods.class, "hello", String.class, CasePaymentOrder.class);

        EvaluationContext context = evaluator.createEvaluationContext(this, SampleMethods.class, method,
                                                                      new Object[]{stringValue, casePaymentOrder}
        );
        assert method != null;
        AnnotatedElementKey elementKey = new AnnotatedElementKey(method, SampleMethods.class);

        // WHEN
        UUID resultId = evaluator.condition("#casePaymentOrder.id",
                                            elementKey,
                                            context,
                                            UUID.class);
        long resultCaseId = evaluator.condition("#casePaymentOrder.caseId",
                                                elementKey,
                                                context,
                                                long.class);
        String resultOrderReference = evaluator.condition("#casePaymentOrder.orderReference",
                                                          elementKey,
                                                          context,
                                                          String.class);

        // THEN
        assertEquals("Should find bean value of type UUID",
                     casePaymentOrder.getId(), resultId);
        assertEquals("Should find bean value of type long",
                     casePaymentOrder.getCaseId(), resultCaseId);
        assertEquals("Should find bean value of type string",
                     casePaymentOrder.getOrderReference(), resultOrderReference);
    }

    @Test
    @DisplayName("should throw error when property not found")
    void shouldThrowErrorWhenPropertyNotFound() {

        // GIVEN
        String stringValue = "test";
        CasePaymentOrder casePaymentOrder = createCasePaymentOrder();
        Method method = ReflectionUtils.findMethod(SampleMethods.class, "hello", String.class, CasePaymentOrder.class);

        EvaluationContext context = evaluator.createEvaluationContext(this, SampleMethods.class, method,
                                                                      new Object[]{stringValue, casePaymentOrder}
        );
        AnnotatedElementKey elementKey = new AnnotatedElementKey(Objects.requireNonNull(method), SampleMethods.class);

        // WHEN / THEN
        assertThatThrownBy(() -> evaluator.condition("#casePaymentOrder.unknownProperty",
                                                     elementKey,
                                                     context,
                                                     String.class))
            .isInstanceOf(SpelEvaluationException.class)
            .hasMessageContaining("EL1008E: Property or field 'unknownProperty' cannot be found");
    }

    @SuppressWarnings("unused")
    private static class SampleMethods {

        private void hello(String foo, Boolean flag) {
        }

        private void hello(String foo, CasePaymentOrder casePaymentOrder) {
        }
    }

}
