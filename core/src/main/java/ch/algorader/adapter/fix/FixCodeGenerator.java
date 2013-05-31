/***********************************************************************************
 * AlgoTrader Enterprise Trading Framework
 *
 * Copyright (C) 2013 Flury Trading - All rights reserved
 *
 * All information contained herein is, and remains the property of Flury Trading.
 * The intellectual and technical concepts contained herein are proprietary to
 * Flury Trading. Modification, translation, reverse engineering, decompilation,
 * disassembly or reproduction of this material is strictly forbidden unless prior
 * written permission is obtained from Flury Trading
 *
 * Fur detailed terms and conditions consult the file LICENSE.txt or contact
 *
 * Flury Trading
 * Badenerstrasse 16
 * 8004 Zurich
 ***********************************************************************************/
package ch.algorader.adapter.fix;

import quickfix.codegen.MessageCodeGenerator;
import quickfix.codegen.MessageCodeGenerator.Task;

/**
 * Generates a Java representation based on the specified FIX definition XML.
 *
 * @author <a href="mailto:andyflury@gmail.com">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
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
