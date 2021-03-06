package lowering;

import org.junit.jupiter.api.Test;
import util.CompileAssert;

import java.io.IOException;

import static util.CompileAssert.assertCompiledMany;

public class _10AnonInnerClass {
    @Test void simpleDeclaration() throws IOException {
        assertCompiledMany(
            new CompileAssert.CompileCase("js.A",
                """
                    package js;
                    public class A {
                    }""",
                """
                    class js$A extends Object {
                      constructor() {
                        super();
                      }
                    }"""),
            new CompileAssert.CompileCase("js.B",
                """
                    package js;
                    class B {
                      void foo() {
                        new A() {
                        };
                      }
                    }""",
                """
                    class js$B extends Object {
                      constructor() {
                        super();
                      }
                      
                      foo() {
                        new class extends js$A {
                          constructor() {
                            super();
                          }
                        }();
                      }
                    }"""));
    }
}
