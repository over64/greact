package com.greact.generate;

import com.greact.generate.util.JSOut;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.util.Trees;
import com.sun.tools.javac.processing.JavacProcessingEnvironment;
import com.sun.tools.javac.tree.JCTree;

import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class TypeGen {
    public static record MethodInfo(boolean isStatic, List<String> argTypes) {
    }
    public static record OverloadInfo(int n, boolean isOverloaded, MethodInfo mi) {
    }
    public static record TContext(
        CompilationUnitTree cu,
        JCTree.JCClassDecl typeTree,
        Trees trees,
        TypeElement typeEl,
        HashMap<String, List<MethodInfo>> overloadMap
    ) { }

    final JSOut out;
    final CompilationUnitTree cu;
    final Trees trees;

    public TypeGen(JSOut out, CompilationUnitTree cu, JavacProcessingEnvironment env) {
        this.out = out;
        this.cu = cu;
        this.trees = Trees.instance(env);
    }

    public void type(int deep, TypeElement typeEl) {

        if (typeEl.getKind() == ElementKind.INTERFACE)
            return;

        out.write(deep, "class ");
        out.write(deep, cu.getPackage().getPackageName().toString().replace(".", "$"));
        out.write(0, "$");
        out.write(0, typeEl.getSimpleName().toString());

        var methods = typeEl.getEnclosedElements().stream()
            .filter(el -> el.getKind() == ElementKind.METHOD)
            .map(el -> (ExecutableElement) el)
            .collect(Collectors.toList());

        var overloadMap = new HashMap<String, List<MethodInfo>>();
        methods.forEach(m -> {
            var methodName = m.getSimpleName().toString();
            var overloads = overloadMap.getOrDefault(methodName, new ArrayList<>());

            var argTypes = m.getParameters().stream()
                .map(p -> trees.getTypeMirror(trees.getPath(p)).toString())
                .collect(Collectors.toList());

            overloads.add(new MethodInfo(m.getModifiers().contains(Modifier.STATIC), argTypes));
            overloadMap.put(methodName, overloads);
        });

        var thisType = (JCTree.JCClassDecl) trees.getTree(typeEl);
        var ctx = new TContext(cu, thisType, trees, typeEl, overloadMap);
        var mGen = new MethodGen(ctx, out);
        out.mkString(methods, mGen::method, " {\n", "\n\n", "\n}");
    }
    // ENUM
}
