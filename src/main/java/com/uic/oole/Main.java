package com.uic.oole;

import com.uic.oole.languagecheck.JavaCodeGenerateServiceImpl;
import com.uic.oole.languagecheck.CodeGenerateService;
import com.uic.oole.parser.IParser;
import com.uic.oole.parser.JavaParser;
import com.uic.oole.utility.ApplicationConstraints;
import com.uic.oole.utility.GeneralUtils;
import com.uic.oole.utility.GrammarMapGenerator;
import javafx.util.Pair;

import java.io.IOException;

public class Main {

    public static void main(String[] args) throws IOException {

        GrammarMapGenerator.buildGrammarMap();
        try {
            CodeGenerateService codeGenerateService = new JavaCodeGenerateServiceImpl();
            ApplicationConstraints appConstraint = new ApplicationConstraints();
            IParser iParser = new JavaParser();

            do {

                //Generate code using java grammar
                String generatedCode = codeGenerateService.generateCodeForNonTerminal(codeGenerateService.getStartSymbol(), GrammarMapGenerator.grammarMap);

                //Parse the generated code to validate against JLS rules
                Pair<String, String> fileNameAndCode = iParser.parse(generatedCode);

                //Store the generated code into the file system
                GeneralUtils.storeFileToFileSystem(fileNameAndCode);

            }while (appConstraint.reached());




        } catch (StackOverflowError e){
            System.out.println("Stackoverflow error");
        }

    }
}
