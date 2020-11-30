package com.greact.generate;

import com.greact.generate.TypeGen.TContext;
import com.greact.generate.util.JSOut;
import com.greact.generate.util.Overloads;
import com.sun.source.tree.*;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.code.Types;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeInfo;
import com.sun.tools.javac.util.Names;
import com.sun.tools.javac.util.Pair;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import java.util.Collections;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static com.greact.generate.util.Overloads.Mode.*;

public class ExpressionGen {
    final JSOut out;
    final TContext ctx;
    final StatementGen stmtGen;

    public ExpressionGen(JSOut out, TContext ctx, StatementGen stmtGen) {
        this.out = out;
        this.ctx = ctx;
        this.stmtGen = stmtGen;
    }

    void expr(int deep, ExpressionTree expr) {
        if (expr instanceof LiteralTree) {
            var value = ((LiteralTree) expr).getValue();
            switch (expr.getKind()) {
                case CHAR_LITERAL, STRING_LITERAL -> {
                    out.write(0, "'");
                    out.write(0, value.toString().replace("\n", "\\\n"));
                    out.write(0, "'");
                }
                case NULL_LITERAL -> out.write(0, "null");
                default -> out.write(0, value.toString());

            }
        } else if (expr instanceof AssignmentTree assign) {
            expr(deep, assign.getVariable());
            out.write(0, " = ");
            expr(deep, assign.getExpression());
        } else if (expr instanceof IdentifierTree id) {
            var jcIdent = ((JCTree.JCIdent) id);
            if (jcIdent.sym instanceof Symbol.VarSymbol varSym) {
                if (varSym.owner instanceof Symbol.MethodSymbol)
                    out.write(0, id.getName().toString());
                else {
                    if (jcIdent.sym.getModifiers().contains(Modifier.STATIC)) {
                        var owner = (Symbol.ClassSymbol) jcIdent.sym.owner;
                        var fullName = owner.fullname.toString().replace(".", "$");
                        out.write(0, fullName);
                        out.write(0, ".");
                    } else {
                        if (!id.getName().toString().equals("this"))
                            out.write(0, "this.");
                    }

                    out.write(0, id.getName().toString());

                }

            } else if (jcIdent.sym instanceof Symbol.ClassSymbol cl) {
                var fullName = cl.fullname.toString().replace(".", "$");
                out.write(0, fullName);
            } else {
                if (jcIdent.sym.getModifiers().contains(Modifier.STATIC)) {
                    var owner = (Symbol.ClassSymbol) jcIdent.sym.owner;
                    var fullName = owner.fullname.toString().replace(".", "$");
                    out.write(0, fullName);
                    out.write(0, ".");
                }

                out.write(0, id.getName().toString());
            }
        } else if (expr instanceof ConditionalExpressionTree ternary) {
            expr(deep, ternary.getCondition());
            out.write(0, " ? ");
            expr(deep, ternary.getTrueExpression());
            out.write(0, " : ");
            expr(deep, ternary.getFalseExpression());
        } else if (expr instanceof UnaryTree unary) {
            var opAndIsPrefix = switch (expr.getKind()) {
                case POSTFIX_INCREMENT -> Pair.of("++", false);
                case POSTFIX_DECREMENT -> Pair.of("--", false);
                case PREFIX_INCREMENT -> Pair.of("++", true);
                case PREFIX_DECREMENT -> Pair.of("--", true);
                case UNARY_PLUS -> Pair.of("+", true);
                case UNARY_MINUS -> Pair.of("-", true);
                case BITWISE_COMPLEMENT -> Pair.of("~", true);
                case LOGICAL_COMPLEMENT -> Pair.of("!", true);
                default -> throw new RuntimeException("Unknown kind " + expr.getKind());
            };

            if (opAndIsPrefix.snd) out.write(0, opAndIsPrefix.fst);
            expr(deep, unary.getExpression());
            if (!opAndIsPrefix.snd) out.write(0, opAndIsPrefix.fst);
        } else if (expr instanceof BinaryTree binary) {
            var op = switch (binary.getKind()) {
                case MULTIPLY -> "*";
                case DIVIDE -> "/";
                case REMAINDER -> "%";
                case PLUS -> "+";
                case MINUS -> "-";
                case LEFT_SHIFT -> "<<";
                case RIGHT_SHIFT -> ">>";
                case UNSIGNED_RIGHT_SHIFT -> ">>>";
                case LESS_THAN -> "<";
                case GREATER_THAN -> ">";
                case LESS_THAN_EQUAL -> "<=";
                case GREATER_THAN_EQUAL -> ">=";
                case EQUAL_TO -> "==";
                case NOT_EQUAL_TO -> "!=";
                case AND -> "&";
                case XOR -> "^";
                case OR -> "|";
                case CONDITIONAL_AND -> "&&";
                case CONDITIONAL_OR -> "||";
                default -> throw new RuntimeException("unexpected kind: " + binary.getKind());
            };

            expr(deep, binary.getLeftOperand());
            out.write(0, " ");
            out.write(0, op);
            out.write(0, " ");
            expr(deep, binary.getRightOperand());
        } else if (expr instanceof CompoundAssignmentTree compoundAssign) {
            expr(deep, compoundAssign.getVariable());
            var op = switch (expr.getKind()) {
                case MULTIPLY_ASSIGNMENT -> "*";
                case DIVIDE_ASSIGNMENT -> "/";
                case REMAINDER_ASSIGNMENT -> "%";
                case PLUS_ASSIGNMENT -> "+";
                case MINUS_ASSIGNMENT -> "-";
                case LEFT_SHIFT_ASSIGNMENT -> "<<";
                case RIGHT_SHIFT_ASSIGNMENT -> ">>";
                case UNSIGNED_RIGHT_SHIFT_ASSIGNMENT -> ">>>";
                case AND_ASSIGNMENT -> "&";
                case XOR_ASSIGNMENT -> "^";
                case OR_ASSIGNMENT -> "|";
                default -> throw new RuntimeException("unknown kind: " + expr.getKind());
            };
            out.write(0, " ");
            out.write(0, op);
            out.write(0, "= ");
            expr(deep, compoundAssign.getExpression());
        } else if (expr instanceof NewArrayTree newArray) {
            var init = newArray.getInitializers();
            if (init == null) init = Collections.emptyList();
            out.mkString(init, e -> expr(deep, e), "[", ", ", "]");
        } else if (expr instanceof ArrayAccessTree accessTree) {
            expr(deep, accessTree.getExpression());
            out.write(0, "[");
            out.write(0, accessTree.getIndex().toString());
            out.write(0, "]");
        } else if (expr instanceof MemberSelectTree memberSelect) {
            expr(deep, memberSelect.getExpression());
            if (((JCTree.JCExpression) memberSelect.getExpression()).type instanceof Type.PackageType)
                out.write(0, "$");
            else
                out.write(0, ".");
            out.write(0, memberSelect.getIdentifier().toString());
        } else if (expr instanceof TypeCastTree cast) {
            expr(deep, cast.getExpression()); // erased
        } else if (expr instanceof ParenthesizedTree parens) {
            out.write(0, "(");
            expr(deep, parens.getExpression());
            out.write(0, ")");
        } else if (expr instanceof LambdaExpressionTree lambda) {
            out.mkString(lambda.getParameters(), (arg) ->
                out.write(0, arg.getName().toString()), "(", ", ", ") =>");
            stmtGen.block(deep, lambda.getBody());
        } else if (expr instanceof SwitchExpressionTree switchExpr) {
            out.write(0, "(() => {\n");
            out.write(deep + 2, "switch");
            expr(deep + 2, switchExpr.getExpression());
            out.write(0, " {\n");
            var cases = switchExpr.getCases();
            cases.forEach(caseStmt -> {
                if (caseStmt.getExpressions().isEmpty())
                    out.write(deep + 4, "default:\n");
                else
                    caseStmt.getExpressions().forEach((caseExpr) -> {
                        out.write(deep + 4, "case ");
                        expr(deep + 4, caseExpr);
                        out.write(0, ":\n");
                    });

                var body = caseStmt.getBody();

                if (body instanceof BlockTree block) {
                    block.getStatements().forEach((bStmt) -> {
                        stmtGen.stmt(deep + 6, bStmt);
                        out.write(0, "\n");
                    });
                } else if (body instanceof StatementTree stmt) {
                    stmtGen.stmt(deep + 6, stmt);
                } else if (body instanceof ExpressionTree caseResult) {
                    out.write(deep + 6, "return ");
                    expr(deep + 6, caseResult);
                    out.write(0, "\n");
                } else
                    throw new RuntimeException("unknown kind: " + body.getKind());
            });
            out.write(deep + 2, "}\n");
            out.write(deep, "})()");
        } else if (expr instanceof MethodInvocationTree call) {
            var select = call.getMethodSelect();
            var methodSym = (Symbol.MethodSymbol) TreeInfo.symbol((JCTree) select);
            var methodOwnerSym = (Symbol.ClassSymbol) methodSym.owner;

            var names = Names.instance(ctx.context());

            if (methodOwnerSym.fullname.equals(names.fromString("com.greact.model.JSExpression")) ||
                methodSym.name.equals(names.fromString("of"))) {
                var unescaped = call.getArguments().get(0).toString();
                out.write(0, unescaped.substring(1, unescaped.length() - 1));
            } else {

                var shimmedType = ctx.stdShim().findShimmedType(methodOwnerSym.type);
                final Overloads.Info info;
                if (shimmedType != null) {
                    info = Overloads.methodInfo(ctx.types(),
                        (TypeElement) shimmedType.tsym, ctx.stdShim().findShimmedMethod(shimmedType, methodSym));
                } else
                    info = Overloads.methodInfo(ctx.types(),
                        (TypeElement) methodOwnerSym.type.tsym, methodSym);


                // FIXME: on-demand static import, foreign module call
                if (select instanceof IdentifierTree ident) { // call local
                    var name = ident.getName().toString();

                    if (!name.equals("super")) out.write(0, "this.");
                    if (info.mode() == Overloads.Mode.STATIC) out.write(0, "constructor.");
                    out.write(0, name);
                    out.write(0, "(");
                } else if (select instanceof MemberSelectTree prop) {
                    if (info.mode() == Overloads.Mode.INSTANCE) {
                        if (ctx.types().isFunctionalInterface(methodOwnerSym.type))
                            expr(deep, prop.getExpression());
                        else
                            expr(deep, prop);
                        out.write(0, "(");
                    } else {
                        var onType = shimmedType != null ? shimmedType : methodOwnerSym.type;
                        out.write(0, onType.toString().replace(".", "$"));
                        out.write(0, ".");
                        out.write(0, prop.getIdentifier().toString());

                        if (info.mode() == Overloads.Mode.AS_STATIC) {
                            out.write(0, ".call(");
                            expr(deep, prop.getExpression());
                            out.write(0, ", ");
                        } else
                            out.write(0, "(");
                    }
                } else
                    throw new RuntimeException("unknown kind: " + select.getKind());

                if (info.isOverloaded()) {
                    out.write(0, "" + info.n());
                    out.write(0, ", ");
                }

                out.mkString(call.getArguments(), (arg) ->
                    expr(deep, arg), "", ", ", ")");
            }
        } else if (expr instanceof MemberReferenceTree memberRef) {
            var types = Types.instance(ctx.context()); // FIXME: move types to ctx, remove context from ctx

            var tSym = TreeInfo.symbol((JCTree) memberRef.getQualifierExpression());
            var mSym = TreeInfo.symbol((JCTree) memberRef);
            var info = Overloads.methodInfo(types, (TypeElement) tSym.type.asElement(), (ExecutableElement) mSym);

            if (info.mode() == Overloads.Mode.STATIC) {
                var fullClassName = tSym.packge().toString().replace(".", "$") +
                    "$" + memberRef.getQualifierExpression();
                out.write(0, fullClassName);
                out.write(0, ".");
                out.write(0, memberRef.getName().toString());
                out.write(0, ".bind(");
                out.write(0, fullClassName);
            } else {
                expr(deep, memberRef.getQualifierExpression());
                out.write(0, ".");
                out.write(0, memberRef.getName().toString());
                out.write(0, ".bind(this");
            }

            if (info.isOverloaded()) out.write(0, ", " + info.n() + ")");
            else out.write(0, ")");

        } else if (expr instanceof InstanceOfTree instanceOf) {
            var ofType = TreeInfo.symbol((JCTree) instanceOf.getType())
                .getQualifiedName().toString();

            // FIXME: disable for arrays (aka x instanceof String[])
            Consumer<Runnable> checkGen = switch (ofType) {
                case "java.lang.String" -> eGen -> {
                    out.write(0, "(($x) => {return typeof $x === 'string' || $x instanceof String})(");
                    eGen.run();
                    out.write(0, ")");
                };
                case "java.lang.Integer", "java.lang.Long", "java.lang.Float" -> eGen -> {
                    out.write(0, "typeof ");
                    eGen.run();
                    out.write(0, " == 'number'");
                };
                default -> eGen -> {
                    eGen.run();
                    out.write(0, " instanceof ");
                    out.write(0, ofType);
                };
            };

            var pattern = instanceOf.getPattern();
            if (pattern == null)
                checkGen.accept(() -> expr(deep, instanceOf.getExpression()));
            else {
                // FIXME:
                //  1. before method body gen
                //    - find all insanceof
                //    - write all pattern vars at function begin
                var name = ((BindingPatternTree) pattern).getBinding().toString();
                out.write(0, "(");
                out.write(0, name);
                out.write(0, " = ");
                expr(deep, instanceOf.getExpression());
                out.write(0, ", ");
                checkGen.accept(() -> out.write(0, name));
                out.write(0, ")");
            }
        } else if (expr instanceof NewClassTree newClass) {
            // FIXME:
            //  - deal with overload
            //  - anon inner classes?
            out.write(0, "new ");
            expr(deep, newClass.getIdentifier());
            out.mkString(newClass.getArguments(), arg -> expr(deep, arg), "(", ", ", ")");
        }
    }
}
