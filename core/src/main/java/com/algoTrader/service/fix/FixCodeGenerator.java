package com.algoTrader.service.fix;

import quickfix.codegen.MessageCodeGenerator;
import quickfix.codegen.MessageCodeGenerator.Task;

public class FixCodeGenerator {

    public static void main(String[] args) {

        MessageCodeGenerator codeGenerator = new MessageCodeGenerator();

        //        Task task44 = new Task();
        //        task44.setName("FIX 4.4");
        //        task44.setSpecification("src/main/resources/FIX44.xml");
        //        task44.setTransformDirectory("D:/quickfixj/core/src/main/java/quickfix/codegen");
        //        task44.setOutputBaseDirectory("src/main/java");
        //        task44.setMessagePackage("quickfix.fix44");
        //        task44.setFieldPackage("quickfix.field");

        //        codeGenerator.generate(task44);

        Task task42 = new Task();
        task42.setName("FIX 4.2");
        task42.setSpecification("src/main/resources/FIX42.xml");
        task42.setTransformDirectory("D:/quickfixj/core/src/main/java/quickfix/codegen");
        task42.setOutputBaseDirectory("src/main/java");
        task42.setMessagePackage("quickfix.fix42");
        task42.setFieldPackage("quickfix.field");

        codeGenerator.generate(task42);

    }
}
