package util;

import com.greact.TranspilerPlugin;
import com.sun.tools.javac.api.BasicJavacTask;

import javax.tools.ToolProvider;
import java.io.StringWriter;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;

public class TestCompiler {
    public static Map<String, StringJsFile> compile(List<StringSourceFile> compilationUnits) {
        var output = new StringWriter();

        var compiler = ToolProvider.getSystemJavaCompiler();
        var fileManager = new SimpleFileManager(
            compiler.getStandardFileManager(null, null, null));

        var arguments = asList("--enable-preview", "--source", "14",
            "-classpath", System.getProperty("java.class.path"),
            "-Xplugin:" + TranspilerPlugin.NAME);

        var task = (BasicJavacTask) compiler.getTask(output, fileManager,
            diagnostic -> System.out.println(diagnostic.toString()),
            arguments, null, compilationUnits);

        if (!task.call())
            throw new RuntimeException("compilation error");

        return fileManager.getCompiled();
    }
}
