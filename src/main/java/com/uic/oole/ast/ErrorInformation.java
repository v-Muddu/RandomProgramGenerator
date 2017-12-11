package com.uic.oole.ast;

import com.github.javaparser.ast.Node;

import java.util.HashSet;
import java.util.Set;

/**
 * Class Name: Error Information
 * Has a Set that holds all the error that are being sent to
 * The getter error nodes returns all the errors from the add method
 */
public class ErrorInformation {

    public static Set<Node> errorNodes = new HashSet<>();

    public static void addNode(Node n){
            errorNodes.add(n);
    }

    public static Set<Node> getErrorNodes(){
        return errorNodes;
    }
}
