package lowering;

import org.junit.jupiter.api.Test;

import java.io.IOException;

import static util.CompileAssert.assertCompiled;

public class _09RecordDeclaration {
    @Test void simple() throws IOException {
        assertCompiled(
                """
                    package js;
                    record Test(int x, int y, int z) {}
                    """,
                """
                    class js$Test extends Object {
                      constructor(x, y, z) {
                        let __init__ = () => {
                          this.x = 0
                          this.y = 0
                          this.z = 0
                        };
                        super();
                        __init__();
                        this.x = x;
                        this.y = y;
                        this.z = z;
                      }
                    }""");
    }
}