package pro.documentum.persistence.common.query.expression.functions;

import org.datanucleus.query.expression.InvokeExpression;

import pro.documentum.persistence.common.query.expression.DQLExpression;

/**
 * @author Andrey B. Panfilov <andrey@panfilov.tel>
 */
public class DQLDateFloor extends DQLExpression {

    public DQLDateFloor(final String text) {
        super(text);
    }

    public static boolean isDateFloor(final InvokeExpression invokeExpr) {
        String op = invokeExpr.getOperation();
        return "datefloor".equalsIgnoreCase(op);
    }

}
