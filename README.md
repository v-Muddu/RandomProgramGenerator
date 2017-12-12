## _Objective_
To develop a randomly generated program that adheres to the Java Language Specification and Java grammar rules. The randomly generated code can be compiled, however the generate code is a meaning less program that has no logic to itself. The Application was built using the Java programming language that supports Java 1.8 JDK. IntelliJ has been promising over the years with cool features that improves dev time, so IntelliJ was the ideal IDE for us to start working on the project. 

The three main functions of the application are the following:

•	Implementing the production rules (Java Grammar) and generate code.

•	Parse the generated code, and later traverse the parse tree and implement JLS rules.

•	The semantically correct code is then stored as a .java file.

![alt text](images/flowChart.png)

## _Implementing production rules (java grammar) and generate code_
The grammar is read from java_grammar.txt file and subsequent maps are created for the terminal and non-terminal nodes. The program recurses through the grammar map by growing the non-terminal nodes, and terminates until a terminal node is reached for each of the recursion. The generated code is appended in a string form. 

**GrammarMapGenerator.java** class builds the grammar maps from the java_grammar.txt

This is the code snippet of the Grammar generator, all the production rules from the text file are read using stream reader and then all the nodes which are the terminals and non terminals are recursively called until an expression is assigned with a literal.
Likewise, this pattern continues till all the expressions are assigned with a literal which would yield to a randomly generated program that is syntactically correct but semantically meaning less.
````
public class GrammarMapGenerator {

    public static Map<String,List<String>> grammarMap = new HashMap<>();

    public static void buildGrammarMap(){

        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(("java_grammar.txt")));
            String s = null;
            Set<String> optionalConstructs = new HashSet<>();
            /**
             * maintain a set of optional constructs in the grammar. Eg <expression>?
             */

            while((s = reader.readLine()) != null){
                if(s == "" || s == null || s.length() == 0)
                    continue;
                String[] statement = s.split("::=");
                List<String> rhs = new ArrayList<>();
                String rhsExpression = statement[1].trim();

                if(rhsExpression.contains("|")) {
                    String[] pipeSeparatedArray = rhsExpression.split("\\|");
                    for(String exp : pipeSeparatedArray){
                        rhs.add(exp.trim());
                    }
                } else {
                    rhs.add(rhsExpression.trim());
                }

                for (String exp : rhs){
                    checkOptionalConstructs(exp,optionalConstructs);
                }
                if(optionalConstructs.contains(statement[0].trim())){
                    rhs.add(" ");
                }
                //System.out.println(statement[0].trim() + "==" + rhs);
                grammarMap.put(statement[0].trim(), rhs);
            }
            //.out.println(grammarMap);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void checkOptionalConstructs(String exp, Set<String> optionalConstructs) {

        String patternString = "(<.*?>)(\\?)";

        Pattern pattern = Pattern.compile(patternString);
        Matcher matcher = pattern.matcher(exp);

        while(matcher.find()) {
            StringBuilder str = new StringBuilder("");
            for(int i = matcher.start(); i <= matcher.end()-2; i++){
                str.append(exp.charAt(i));
            }
            optionalConstructs.add(str.toString());
        }
    }
}
````

**JavaCodeGenerateServiceImpl.java** class creates random code by recursing through grammar maps.

````
@Override
    public String generateCodeForNonTerminal(String key, Map<String, List<String>> grammarMap) {

        System.out.println(key);
        if ((key.equals ("<identifier>")) || (key.equals ("<input character>" ))) {
            if(identifierType == IdentifierType.variableIdentifier)
                return ProgramGeneratorUtils.generateIdentifierForVariables(this);
            else
                return RandomStrGen.randomStrGenerator();
        } else if(key.equals("<block>") && identifierType.equals(IdentifierType.methodBody)){
            if(!identifierType.getType().equals(IdentifierType.Type.voidType)) {
                String body = "{ " + identifierType.getType().getValue();
                String defaultValue = identifierType.getType().getDefaultValue();
                identifierType = IdentifierType.variableIdentifier;
                String identifierName = generateCodeForNonTerminal("<identifier>", grammarMap);
                body += " " + identifierName + "=" + defaultValue + "; return " + identifierName + "; }";
                return body;
            } else {
                return "{}";
            }

        } else if(key.equals("<return>") && identifierType.equals(IdentifierType.returnStatement)){

            return "return " + identifierType.getType().getDefaultValue() + ";";
        }

````

## _Parsing the generated code and implementing java language rules_
The generated code is then parsed to create a parse tree using JAVA parser. Appropriate java syntax rules are implemented as and when tree is traversed through each node. The output of the parse tree is then written into .java class file. 

**LanguageValidationServiceImpl.java** class file generates the parse tree and checks for java language rules to check the syntax of the randomly generated code.

The following snippet has one of the methods that semantically checks for errors and handles them complying to the JLS rules.
The validateReturnStatement method visits the return type node for the method and validates whether the semantics of the program complies with the JLS rules.

````
@Override
    public void validateReturnStatement(CClass cClass, Method method, Node parent, Node returnNode) {
        if(null == returnNode){
            IdentifierType identifierType = IdentifierType.returnStatement;
            identifierType.getTypeByValue(method.getType());

            CodeGenerateService codeGenerateService = new JavaCodeGenerateServiceImpl(cClass, method, identifierType);
            String returnBlock = codeGenerateService.generateCodeForNonTerminal("<return>", GrammarMapGenerator.grammarMap);

            ReturnStmt stmt = (ReturnStmt) JavaParser.parseStatement(returnBlock);
            BlockStmt block = (BlockStmt) parent;
            block.addStatement(stmt);
        } else {

            List<Symbol> symbolsAvailableInScope = new ArrayList<>();

            symbolsAvailableInScope.addAll(cClass.symbolTable.values());
            symbolsAvailableInScope.addAll(method.getInitializedVariables());
            symbolsAvailableInScope.addAll(method.getParameterList());
            boolean symbolFound = false;

            for(Symbol symbol : symbolsAvailableInScope){
                if(symbol.isField() && symbol.getType().equals(method.getType())){
                    symbolFound = true;
                    String returnStatement = "return " + symbol.getName() + ";";
                    ReturnStmt stmt = (ReturnStmt) JavaParser.parseStatement(returnStatement);
                    parent.replace(returnNode,stmt);
                }
            }

            if(!symbolFound) {
                IdentifierType identifierType = IdentifierType.returnStatement;
                identifierType.getTypeByValue(method.getType());

                CodeGenerateService codeGenerateService = new JavaCodeGenerateServiceImpl(cClass, method, identifierType);
                String returnBlock = codeGenerateService.generateCodeForNonTerminal("<return>", GrammarMapGenerator.grammarMap);

                ReturnStmt stmt = (ReturnStmt) JavaParser.parseStatement(returnBlock);
                parent.replace(returnNode, stmt);
            }
        }
    }
````

The output of the parsed tree is then stored as an .java class file in the file system.

The following code snippet is an instance of a randomly generated program, you will notice that the code is syntactically and semantically correct that is being handled in the application. Henceforth, the objective of this application.

````
 package oole.random.gwvsuq;

import oole.random.*;

public class TenKLOCrkmpjw {

    protected TenKLOCrkmpjw(byte kyhifw) {
        if (jdmejt != lplgdq) {
            if (sljgpt == sezqal) {
                boolean rhffrq, ceudhy;
                float bnyacc;
                while (jxbyvm == yschqe) {
                }
            } else {
            }
            if (vllmpe != zefsap) {
            }
        }
        int qgkivi;
    }

    TenKLOCrkmpjw(boolean udkcco, float pmyjoy, boolean hjqofj, double mgqldn) {
    }

    public boolean kzqafg, iubkny;

    public TenKLOCrkmpjw() {
    }

    public int jdmejt;

    public int lplgdq;

    public int sljgpt;

    public int sezqal;

    public int jxbyvm;

    public int yschqe;

    public int vllmpe;

    public int zefsap;
}

````

## _Configuration parameters_
The configuration parameters as mentioned in the project description are read from the xml file, and the same constraints are realized during the code generation. 

**XmlFileReader.java** class is used to read the constraints specified in the configuration file.

The readXmlFile method that's implemented in the XmlFileReader class reads all the attributes from the xml file that has all the upper and lower bounds to successfully generate a program.
As the readXmlFile method is invoked in the constructor, all the getter methods are available to get values. As you can notice the applicationConstraints class resembles a builder pattern. 

```public class XmlFileReader {
    private ApplicationConstraints ac;
    
    public XmlFileReader(ApplicationConstraints _applicationConstraints){
        this.ac = _applicationConstraints;
        this.readXmlFile();
    }
    
public void readXmlFile() {
    DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
    DocumentBuilder dBuilder = null;
    try {
        dBuilder = dbFactory.newDocumentBuilder();
    } catch (ParserConfigurationException e1) {
        e1.printStackTrace();
    }
    Document doc = null;
    try {
        doc = dBuilder.parse(new File("constraints.xml"));
        doc.getDocumentElement().normalize();
    } catch (IOException e1) {
        e1.printStackTrace();
    } catch (org.xml.sax.SAXException e) {
        e.printStackTrace();
    }
    NodeList nList = doc.getElementsByTagName("ProgGen");
    try{
        for(int i = 0; i <= nList.getLength(); i++){
            Element element = (Element) nList.item(i);
            if(element != null){
                getConstraints(element);
            }
        }
    }catch(Exception e){
        e.printStackTrace();
    }
  }
```
    

## _How to run the application?_
•	Please resolve all your dependencies before running the application

•	The randomly generated program in saved in the **_GeneratedClasses_** folder that resides in the **_D:/_** drive in your file system

•	You need to create a folder named **_GeneratedClasses_** in the **_D:/_** drive


## _How to build the application?_
`gradle build`

## _Running the unit tests_
Test cases for all the possible scenarios were implemented. Test Coverage was pretty decent.

**_Reflection_** was used in accessing modifiers that were private

**_Mockito_** was implemented to mock classes

**_PowerMockito_** was used in mocking static members of the class

**_JUnit_** was used for running assertions between the actual and expected values

## _How to run the test suit?_
`gradle test`

## _Dependencies_
`testCompile group: 'junit', name: 'junit', version: '4.12'`

`testCompile group: 'org.mockito', name: 'mockito-all', version: '1.10.19'`

`testCompile group: 'org.powermock', name: 'powermock-api-mockito', version: '1.7.3'`

`testCompile group: 'org.powermock', name: 'powermock-module-junit4', version: '1.7.3'`

`compile group: 'sax', name: 'sax', version: '2.0.1'`

`compile group: 'org.apache.logging.log4j', name: 'log4j-core', version: '2.9.1'`
    
`compile group: 'org.apache.commons', name: 'commons-lang3', version: '3.0'`
    
`compile 'com.github.javaparser:javaparser-core:3.5.4'`

`compile group: 'commons-io', name: 'commons-io', version: '2.5'`