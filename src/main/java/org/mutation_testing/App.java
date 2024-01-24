package org.mutation_testing;

import java.util.ArrayList;
import java.util.List;
import com.github.javaparser.ast.*;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.body.MethodDeclaration;

/**
 * Hello world!
 *
 */
public class App {
    public static void main(String[] args) {

    }

    public List<ClassOrInterfaceDeclaration> parseFile(String filename) {
        CompilationUnit cu = StaticJavaParser.parse(filename);
        ArrayList<ClassOrInterfaceDeclaration> classes = new ArrayList<>();
        NodeList<TypeDeclaration<?>> types = cu.getTypes();
        for (TypeDeclaration<?> type : types) {
            if (type instanceof ClassOrInterfaceDeclaration) {
                classes.add((ClassOrInterfaceDeclaration) type);
            }
        }

        return classes;
    }

    public List<MethodDeclaration> getMethods(ClassOrInterfaceDeclaration clazz) {
        return clazz.getMethods();
    }
}
