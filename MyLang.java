import java.io.*;
import java.util.*;


public class MyLang {

    public static HashMap<String, HashMap<String, String>> maps = new HashMap<>();
    public static HashMap<String, Boolean> boolVars = new HashMap<>();
    public static HashMap<String, List<List<String>>> nestedLists = new HashMap<>();
    public static HashMap<String, String> stringVars = new HashMap<>();
    public static HashMap<String, List<String>> functionParams = new HashMap<>();
    public static HashMap<String, List<Integer>> listVars = new HashMap<>();
    public static HashMap<String, List<Integer>> listsNum = new HashMap<>();
    public static HashMap<String, List<String>> lists = new HashMap<>();
    public static HashMap<String, Integer> vars = new HashMap<>();
    public static HashMap<String, String> functions = new HashMap<>();
    public static Scanner sc = new Scanner(System.in);
    public static boolean running = true;

    // --- Basic Arithmetic Operations ---
    public static int add(int a, int b) { return a + b; }
    public static int subtract(int a, int b) { return a - b; }
    public static int multiply(int a, int b) { return a * b; }
    public static double divide(int a, int b) {
        if (b == 0) {
            System.out.println("Cannot divide by zero!");
            return 0;
        }
        return (double) a / b;
    }

    // --- Word-to-Number Conversion ---
    public static String convertWordsToNumbers(String text) {
        text = text.replaceAll("\\bzero\\b", "0")
                .replaceAll("\\bone\\b", "1")
                .replaceAll("\\btwo\\b", "2")
                .replaceAll("\\bthree\\b", "3")
                .replaceAll("\\bfour\\b", "4")
                .replaceAll("\\bfive\\b", "5")
                .replaceAll("\\bsix\\b", "6")
                .replaceAll("\\bseven\\b", "7")
                .replaceAll("\\beight\\b", "8")
                .replaceAll("\\bnine\\b", "9");
        return text;
    }


    // --- Normalize Human-Like Commands ---
    public static String normalizeCommand(String input) {
        input = input.toLowerCase().trim();
        input = convertWordsToNumbers(input);

        if (!input.startsWith("random ") && !input.startsWith("set ") && !input.startsWith("let ") 
        && !input.startsWith("print ") && !input.startsWith("loop ") && !input.startsWith("if ")) {
        input = convertWordsToNumbers(input);
    }

        if (input.startsWith("\\bmake\\b ")) {input = input.replace("\\bmake\\b ", "\\bset\\b ");}
        if (input.startsWith("\\blist\\b ")) {input = input.replace("\\blist\\b ", " ");}
        if (input.contains(" \\bto\\b ")) {input = input.replace(" to ", " ");}
        if (input.contains(" \\bof\\b ")) {input = input.replace(" of ", " ");}
        if (input.contains("\\bbetween\\b ")) {input = input.replace("\\bbetween\\b ", " ");}
        if (input.contains("\\bfrom\\b ")) {input = input.replace("\\bfrom\\b ", " ");}
        if (input.contains("\\bplus\\b")) {input = input.replace("\\bplus\\b", "\\badd\\b");}
        if (input.contains("\\bwith\\b ")) {input = input.replace(" \\bwith\\b ", " ");}
        if (input.contains("\\band\\b ")) {input = input.replace(" \\band\\b ", " ");}
        if (input.contains("\\bbe\\b ")) {input = input.replace(" \\bbe\\b ", " ");}
        if (input.contains("  ")) {input = input.replace("  ", " ");}
        

        return input.trim();
    }

    // --- Variable Handling ---
    public static int getValue(String token, HashMap<String, Integer> vars) {
        if (vars.containsKey(token)) {
            return vars.get(token);
        } else {
            return Integer.parseInt(token);
        }
    }

    // --- Execute a single command ---
    public static String executeCommand(String cmd, HashMap<String, Integer> vars, 
                                    HashMap<String, List<String>> lists,
                                    HashMap<String, List<Integer>> listsNum,
                                    HashMap<String, String> functions,
                                    HashMap<String, List<String>> functionParams) {
        cmd = cmd.trim();
        String[] parts = cmd.split(" ");

        // === FUNCTION CALL HANDLER ===
        String funcName = parts[0];
        if (functions.containsKey(funcName)) {
            List<String> params = functionParams.get(funcName); // function parameters
            String funcBody = functions.get(funcName);          // function body

            // Create local variable map to avoid overwriting global vars
            HashMap<String, Integer> localVars = new HashMap<>(vars);

            // Map arguments to parameters
            if (params != null && parts.length - 1 >= params.size()) {
                for (int i = 0; i < params.size(); i++) {
                    localVars.put(params.get(i), getValue(parts[i + 1], vars));
                }
            }

            // Split multiple statements by ';'
            String[] statements = funcBody.split(";");
            String lastResult = "";

            for (String stmt : statements) {
                stmt = stmt.trim();
                if (stmt.isEmpty()) continue;

                if (stmt.startsWith("return ")) {
                    String returnExpr = stmt.substring(7).trim();
                    lastResult = executeCommand(returnExpr, localVars, lists, listsNum, functions, functionParams);
                    return lastResult; // immediately return
                } else {
                    lastResult = executeCommand(stmt, localVars, lists, listsNum, functions, functionParams);
                }
            }

            return lastResult; // return last statement if no explicit return
        }

        // === PRINT HANDLER ===
        if (parts[0].equals("print") || parts[0].equals("say")) {
            if (parts.length < 2) return "";
            String varName = parts[1];

            if (vars.containsKey(varName)) {
                System.out.println(vars.get(varName));
                return String.valueOf(vars.get(varName));
            } else if (lists.containsKey(varName)) {
                System.out.println(lists.get(varName));
                return lists.get(varName).toString();
            } else if (listsNum.containsKey(varName)) {
                System.out.println(listsNum.get(varName));
                return listsNum.get(varName).toString();
            } else {
                // Try parsing as a number
                try {
                    int num = Integer.parseInt(varName);
                    System.out.println(num);
                    return varName;
                } catch (NumberFormatException e) {
                    System.out.println(varName);
                    return varName;
                }
            }
        }

        // === ARITHMETIC HANDLER ===
        if (parts.length == 3) {
            String op = parts[0];
            int a = getValue(parts[1], vars);
            int b = getValue(parts[2], vars);

            switch (op) {
                case "add": case "+": return String.valueOf(add(a, b));
                case "subtract": case "-": return String.valueOf(subtract(a, b));
                case "multiply": case "*": return String.valueOf(multiply(a, b));
                case "divide": case "/": return String.valueOf(divide(a, b));
            }
        }

        // If command not recognized
        return "";
    }

    // --- Execute Script File ---
    public static void executeScript(String filename, 
                                    HashMap<String, Integer> vars,
                                    HashMap<String, List<String>> lists,
                                    HashMap<String, List<Integer>> listsNum,
                                    HashMap<String, String> functions,
                                    HashMap<String, List<String>> functionParams,
                                    HashMap<String, String> stringVars,
                                    HashMap<String, HashMap<String, String>> maps,
                                    HashMap<String, List<List<String>>> nestedLists) {
        
        System.out.println("Running script: " + filename);
        
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line;
            int lineNumber = 0;
            StringBuilder multiLineCommand = new StringBuilder();
            boolean inMultiLine = false;
            
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                line = line.trim();
                
                // Skip empty lines
                if (line.isEmpty()) {
                    System.out.println("");
                    continue;
                }
                
                // Skip comments
                if (line.startsWith("#") || line.startsWith("//")) {
                    continue;
                }
                
                // Handle multi-line function definitions
                if (line.contains("{") && !line.contains("}")) {
                    inMultiLine = true;
                    multiLineCommand.append(line).append(" ");
                    continue;
                }
                
                if (inMultiLine) {
                    multiLineCommand.append(line).append(" ");
                    if (line.contains("}")) {
                        inMultiLine = false;
                        line = multiLineCommand.toString();
                        multiLineCommand = new StringBuilder();
                    } else {
                        continue;
                    }
                }
                
                // Normalize and execute command
                try {
                    String normalizedCommand = normalizeCommand(line);
                    
                    // Create a temporary scanner for any commands that might need input
                    // For script mode, we'll skip interactive commands
                    
                    // Execute the command (we'll need to refactor main loop logic)
                    //System.out.println("[Line " + lineNumber + "] > " + line);
                    
                    // Process the command (simplified version)
                    processCommand(normalizedCommand, vars, lists, listsNum, 
                                functions, functionParams, stringVars, maps, nestedLists);
                    
                } catch (Exception e) {
                    System.err.println("Error at line " + lineNumber + ": " + e.getMessage());
                    System.err.println("Command: " + line);
                }
            }
            
            System.out.println("Script completed successfully!");
            
        } catch (FileNotFoundException e) {
            System.out.println("Script file not found: " + filename);
        } catch (IOException e) {
            System.out.println("Error reading script: " + e.getMessage());
        }
    }

    // --- Process Single Command (extracted from main loop) ---
    public static void processCommand(String input,
                                    HashMap<String, Integer> vars,
                                    HashMap<String, List<String>> lists,
                                    HashMap<String, List<Integer>> listsNum,
                                    HashMap<String, String> functions,
                                    HashMap<String, List<String>> functionParams,
                                    HashMap<String, String> stringVars,
                                    HashMap<String, HashMap<String, String>> maps,
                                    HashMap<String, List<List<String>>> nestedLists) {
        
        // For now, we'll add a simplified version
        // In a real refactor, you'd move ALL your command handlers here
        
        String[] parts = input.split(" ");
        
        // Example: handle print
        if (input.startsWith("print ")) {
            String varName = input.replace("print", "").trim();
            
            if (stringVars.containsKey(varName)) {
                System.out.println(stringVars.get(varName));
            } else if (vars.containsKey(varName)) {
                System.out.println(vars.get(varName));
            } else if (lists.containsKey(varName)) {
                System.out.println(lists.get(varName));
            } else {
                System.out.println(varName);
            }
            return;
        }
        
        // Example: handle set
        if (input.startsWith("set ")) {
            parts = input.replaceFirst("set ", "").split(" ", 2);
            if (parts.length >= 2) {
                String varName = parts[0];
                try {
                    int value = Integer.parseInt(parts[1]);
                    vars.put(varName, value);
                    System.out.println("Variable '" + varName + "' set to " + value);
                } catch (NumberFormatException e) {
                    stringVars.put(varName, parts[1]);
                    System.out.println("String '" + varName + "' set to \"" + parts[1] + "\"");
                }
            }
            return;
        }
        
        // Add more command handlers as needed
        //System.out.println("Command executed: " + input);
    }

    // --- Import Module ---
    public static void importModule(String filename,
                                HashMap<String, Integer> vars,
                                HashMap<String, List<String>> lists,
                                HashMap<String, List<Integer>> listsNum,
                                HashMap<String, String> functions,
                                HashMap<String, List<String>> functionParams,
                                HashMap<String, String> stringVars,
                                HashMap<String, HashMap<String, String>> maps) {
        
        // Add .mylang extension if not present
        if (!filename.endsWith(".mylang")) {
            filename += ".mylang";
        }
        
        System.out.println("Importing module: " + filename);
        
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line;
            int lineNumber = 0;
            boolean inFunctionDef = false;
            StringBuilder functionBody = new StringBuilder();
            String currentFuncName = "";
            List<String> currentParams = new ArrayList<>();
            
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                line = line.trim();
                
                // Skip empty lines and comments
                if (line.isEmpty() || line.startsWith("#") || line.startsWith("//")) {
                    continue;
                }
                
                // Handle function definitions
                if (line.startsWith("define ")) {
                    String rest = line.substring(7).trim();
                    
                    if (rest.contains("{")) {
                        String[] parts = rest.split(" ", 2);
                        currentFuncName = parts[0];
                        String funcRest = parts[1];
                        
                        int braceStart = funcRest.indexOf("{");
                        int braceEnd = funcRest.lastIndexOf("}");
                        
                        // Multi-line function
                        if (braceStart != -1 && braceEnd == -1) {
                            inFunctionDef = true;
                            String paramSection = funcRest.substring(0, braceStart).trim();
                            
                            if (!paramSection.isEmpty()) {
                                currentParams = Arrays.asList(paramSection.split("\\s+"));
                            } else {
                                currentParams = new ArrayList<>();
                            }
                            continue;
                        }
                        // Single-line function
                        else if (braceStart != -1 && braceEnd != -1) {
                            String paramSection = funcRest.substring(0, braceStart).trim();
                            String bodySection = funcRest.substring(braceStart + 1, braceEnd).trim();
                            
                            List<String> params = new ArrayList<>();
                            if (!paramSection.isEmpty()) {
                                params = Arrays.asList(paramSection.split("\\s+"));
                            }
                            
                            functionParams.put(currentFuncName, params);
                            functions.put(currentFuncName, bodySection);
                            System.out.println("  Imported function: " + currentFuncName + " with params " + params);
                        }
                    }
                    continue;
                }
                
                // Handle multi-line function body
                if (inFunctionDef) {
                    if (line.equals("}")) {
                        inFunctionDef = false;
                        functionParams.put(currentFuncName, currentParams);
                        functions.put(currentFuncName, functionBody.toString());
                        System.out.println("  Imported function: " + currentFuncName + " with params " + currentParams);
                        functionBody = new StringBuilder();
                        currentFuncName = "";
                        currentParams = new ArrayList<>();
                    } else {
                        functionBody.append(line).append(";");
                    }
                    continue;
                }
                
                // Handle variable definitions (that should be imported)
                if (line.startsWith("export ")) {
                    String exportLine = line.substring(7).trim();
                    
                    if (exportLine.startsWith("set ")) {
                        String[] parts = exportLine.replaceFirst("set ", "").split(" ", 2);
                        if (parts.length >= 2) {
                            String varName = parts[0];
                            try {
                                int value = Integer.parseInt(parts[1]);
                                vars.put(varName, value);
                                System.out.println("  Imported variable: " + varName + " = " + value);
                            } catch (NumberFormatException e) {
                                stringVars.put(varName, parts[1]);
                                System.out.println("  Imported string: " + varName + " = \"" + parts[1] + "\"");
                            }
                        }
                    }
                }
            }
            
            System.out.println("Module imported successfully!");
            
        } catch (FileNotFoundException e) {
            System.out.println("Module file not found: " + filename);
        } catch (IOException e) {
            System.out.println("Error reading module: " + e.getMessage());
        }
    }

    // --- Check if file exists ---
    public static boolean fileExists(String filename) {
        File file = new File(filename);
        return file.exists() && file.isFile();
    }

    public static void executeBlock(String body, 
                        HashMap<String, Integer> vars,
                        HashMap<String, List<String>> lists,
                        HashMap<String, List<Integer>> listsNum,
                        HashMap<String, String> functions,
                        HashMap<String, List<String>> functionParams,
                        HashMap<String, String> stringVars,
                        HashMap<String, Boolean> boolVars) {
    
        String[] commands = body.split(";");
        
        for (String cmd : commands) {
            cmd = cmd.trim();
            if (cmd.isEmpty()) continue;
            
            String normalized = normalizeCommand(cmd);
            
            // PRINT
            if (normalized.startsWith("print ")) {
                String varName = normalized.replace("print", "").trim();
                if (boolVars.containsKey(varName)) {
                    System.out.println(boolVars.get(varName));
                } else if (vars.containsKey(varName)) {
                    System.out.println(vars.get(varName));
                } else if (stringVars.containsKey(varName)) {
                    System.out.println(stringVars.get(varName));
                } else if (lists.containsKey(varName)) {
                    System.out.println(lists.get(varName));
                } else {
                    System.out.println(varName);
                }
            }
            // SET
            else if (normalized.startsWith("set ")) {
                String[] setParts = normalized.replaceFirst("set ", "").split(" ", 2);
                if (setParts.length >= 2) {
                    String varName = setParts[0];
                    String value = setParts[1];
                    
                    // Check for boolean
                    if (value.equals("true") || value.equals("false")) {
                        boolVars.put(varName, value.equals("true"));
                    } else {
                        try {
                            int intValue = getValue(value, vars);
                            vars.put(varName, intValue);
                        } catch (NumberFormatException e) {
                            stringVars.put(varName, value);
                        }
                    }
                }
            }
            // MATH (existing code)
            else if (normalized.split(" ").length == 3) {
                String[] mathParts = normalized.split(" ");
                try {
                    int a = getValue(mathParts[1], vars);
                    int b = getValue(mathParts[2], vars);
                    
                    int result = switch (mathParts[0]) {
                        case "add" -> add(a, b);
                        case "subtract" -> subtract(a, b);
                        case "multiply" -> multiply(a, b);
                        case "divide" -> b != 0 ? a / b : 0;
                        default -> 0;
                    };
                    
                    System.out.println("Result: " + result);
                } catch (Exception e) {
                    // Ignore errors in blocks
                }
            }
        }
    }

    // Helper method to find matching closing brace
    public static int findMatchingBrace(String str, int openBraceIndex) {
        int depth = 0;
        for (int i = openBraceIndex; i < str.length(); i++) {
            if (str.charAt(i) == '{') {
                depth++;
            } else if (str.charAt(i) == '}') {
                depth--;
                if (depth == 0) {
                    return i;
                }
            }
        }
        return -1; // No matching brace found
    }

    // --- Evaluate Condition (supports integers, strings, and booleans) ---
    public static boolean evaluateCondition(String leftToken, String operator, String rightToken,
                                        HashMap<String, Integer> vars,
                                        HashMap<String, String> stringVars,
                                        HashMap<String, Boolean> boolVars) {
        
        // Check for boolean variables first
        boolean leftIsBool = boolVars.containsKey(leftToken) || leftToken.equals("true") || leftToken.equals("false");
        boolean rightIsBool = boolVars.containsKey(rightToken) || rightToken.equals("true") || rightToken.equals("false");
        
        if (leftIsBool || rightIsBool) {
            boolean left = boolVars.getOrDefault(leftToken, leftToken.equals("true"));
            boolean right = boolVars.getOrDefault(rightToken, rightToken.equals("true"));
            
            return switch (operator) {
                case "==" -> left == right;
                case "!=" -> left != right;
                default -> {
                    System.out.println("Only == and != operators supported for booleans");
                    yield false;
                }
            };
        }
        
        // Try integer comparison
        try {
            int left = getValue(leftToken, vars);
            int right = getValue(rightToken, vars);
            
            return switch (operator) {
                case ">" -> left > right;
                case "<" -> left < right;
                case "==" -> left == right;
                case "!=" -> left != right;
                case ">=" -> left >= right;
                case "<=" -> left <= right;
                default -> false;
            };
        } catch (NumberFormatException e) {
            // String comparison
            String left = leftToken;
            String right = rightToken;
            
            if (stringVars.containsKey(leftToken)) {
                left = stringVars.get(leftToken);
            }
            if (stringVars.containsKey(rightToken)) {
                right = stringVars.get(rightToken);
            }
            
            return switch (operator) {
                case "==" -> left.equals(right);
                case "!=" -> !left.equals(right);
                default -> {
                    System.out.println("Only == and != operators supported for strings");
                    yield false;
                }
            };
        }
    }
    // --- Expression Parser and Evaluator ---
    public static int evaluateExpression(String expr, HashMap<String, Integer> vars) {
        expr = expr.trim();
        
        // Remove outer parentheses if they wrap the entire expression
        while (expr.startsWith("(") && expr.endsWith(")") && matchingParenthesis(expr, 0) == expr.length() - 1) {
            expr = expr.substring(1, expr.length() - 1).trim();
        }
        
        // Handle single values
        if (!expr.contains("+") && !expr.contains("-") && !expr.contains("*") && !expr.contains("/")) {
            return getValue(expr.trim(), vars);
        }
        
        // Find operator with lowest precedence (outside parentheses)
        int parenDepth = 0;
        int addSubIndex = -1;
        int mulDivIndex = -1;
        
        // Scan from right to left to get left-to-right evaluation
        for (int i = expr.length() - 1; i >= 0; i--) {
            char c = expr.charAt(i);
            
            if (c == ')') parenDepth++;
            else if (c == '(') parenDepth--;
            else if (parenDepth == 0) {
                if ((c == '+' || c == '-') && i > 0) {
                    // Make sure it's not a negative number
                    char prev = expr.charAt(i - 1);
                    if (prev != '(' && prev != '+' && prev != '-' && prev != '*' && prev != '/') {
                        addSubIndex = i;
                        break;
                    }
                } else if ((c == '*' || c == '/') && i > 0 && mulDivIndex == -1) {
                    mulDivIndex = i;
                }
            }
        }
        
        // Split by addition/subtraction (lower precedence)
        if (addSubIndex != -1) {
            String left = expr.substring(0, addSubIndex).trim();
            String right = expr.substring(addSubIndex + 1).trim();
            char op = expr.charAt(addSubIndex);
            
            int leftVal = evaluateExpression(left, vars);
            int rightVal = evaluateExpression(right, vars);
            
            return op == '+' ? leftVal + rightVal : leftVal - rightVal;
        }
        
        // Split by multiplication/division (higher precedence)
        if (mulDivIndex != -1) {
            String left = expr.substring(0, mulDivIndex).trim();
            String right = expr.substring(mulDivIndex + 1).trim();
            char op = expr.charAt(mulDivIndex);
            
            int leftVal = evaluateExpression(left, vars);
            int rightVal = evaluateExpression(right, vars);
            
            if (op == '*') {
                return leftVal * rightVal;
            } else {
                if (rightVal == 0) {
                    System.out.println("Error: Division by zero");
                    return 0;
                }
                return leftVal / rightVal;
            }
        }
        
        // If we get here, something went wrong
        return getValue(expr, vars);
    }

    // --- Find matching closing parenthesis ---
    public static int matchingParenthesis(String str, int openIndex) {
        int depth = 1;
        for (int i = openIndex + 1; i < str.length(); i++) {
            if (str.charAt(i) == '(') depth++;
            else if (str.charAt(i) == ')') {
                depth--;
                if (depth == 0) return i;
            }
        }
        return -1;
    }

    // ---- import/use module ----
    public static void ImportUse(String input){
        String moduleName = input.replace("import ", "").replace("use ", "").trim();
                
                // Check standard library first
                String stdlibPath = "stdlib/" + moduleName;
                if (fileExists(stdlibPath + ".mylang")) {
                    importModule(stdlibPath, vars, lists, listsNum, functions, 
                                functionParams, stringVars, maps);
                } else if (fileExists(moduleName + ".mylang")) {
                    importModule(moduleName, vars, lists, listsNum, functions, 
                                functionParams, stringVars, maps);
                } else if (fileExists(moduleName)) {
                    importModule(moduleName, vars, lists, listsNum, functions, 
                                functionParams, stringVars, maps);
                } else {
                    System.out.println("Module not found: " + moduleName);
                }
    }

    // --- list modual ---
    public static void ListModual (String input){
        System.out.println("Available modules:");
                
                // Check stdlib folder
                File stdlibDir = new File("stdlib");
                if (stdlibDir.exists() && stdlibDir.isDirectory()) {
                    File[] modules = stdlibDir.listFiles((dir, name) -> name.endsWith(".mylang"));
                    if (modules != null && modules.length > 0) {
                        System.out.println("\nStandard Library:");
                        for (File module : modules) {
                            System.out.println("  - " + module.getName().replace(".mylang", ""));
                        }
                    }
                }
                
                // Check current directory
                File currentDir = new File(".");
                File[] localModules = currentDir.listFiles((dir, name) -> name.endsWith(".mylang"));
                if (localModules != null && localModules.length > 0) {
                    System.out.println("\nLocal Modules:");
                    for (File module : localModules) {
                        System.out.println("  - " + module.getName().replace(".mylang", ""));
                    }
                }
    }

    // --- import with namepace ---
    public static void ImportAs (String input){
        String[] parts = input.split(" as ");
        String moduleName = parts[0].replace("import ", "").trim();
        String namespace = parts[1].trim();
                
        // Store functions with namespace prefix
        HashMap<String, String> tempFunctions = new HashMap<>();
        HashMap<String, List<String>> tempParams = new HashMap<>();
                
        importModule(moduleName, vars, lists, listsNum, tempFunctions, tempParams, stringVars, maps);
                
        // Add functions with namespace
        for (Map.Entry<String, String> entry : tempFunctions.entrySet()) {
            String namespacedName = namespace + "." + entry.getKey();
            functions.put(namespacedName, entry.getValue());
            functionParams.put(namespacedName, tempParams.get(entry.getKey()));
            System.out.println("  Namespaced: " + namespacedName);
        }
    }

    // --- make map ---
    public static void MakeMap (String input){
        String[] parts = input.split(" ", 3);
        String mapName = input.replace("make map ", "").trim();
        if (parts.length > 3){
            System.out.println("usage: make map <name>");
        }
        maps.put(mapName, new HashMap<>());
        System.out.println("Map '" + mapName + "' created!");
    }

    // --- put ---
    public static void Put(String input){
        String[] parts = input.split(" ", 4);
                if (parts.length > 4){
                    System.out.println("Usage: put <map> <key> <value>");
                    return;
                }

                String mapName = parts[1];
                String key = parts[2];
                String value = parts[3];

                if (!maps.containsKey(mapName)){
                    System.out.println("there is no map called " + mapName);
                    return;
                }

                maps.get(mapName).put(key, value);
                System.out.println("Added " + key + " = " + value + " to " + mapName);
    }

    // --- define function ----
    public static void DefineFunc(String input){
        String rest = input.substring(7).trim();
                
                // Check if this is brace-style definition (multi-line or single-line with braces)
                if (rest.contains("{")) {
                    String[] parts = rest.split(" ", 2);
                    if (parts.length < 2) {
                        System.out.println("Usage: define <name> <params> {body} or define <name> { commands }");
                        return;
                    }
                    
                    String funcName = parts[0];
                    String funcRest = parts[1];
                    
                    int braceStart = funcRest.indexOf("{");
                    int braceEnd = funcRest.lastIndexOf("}");

                    // int kotationStart = funcRest.indexOf("\\b\"");
                    // int kotationEnd = funcRest.indexOf("\"\\b");
                    
                    // Multi-line definition (opening brace without closing)
                    if (braceStart != -1 && braceEnd == -1) {
                        // Extract parameters before the brace (if any)
                        String paramSection = funcRest.substring(0, braceStart).trim();
                        List<String> params = new ArrayList<>();
                        if (!paramSection.isEmpty()) {
                            params = Arrays.asList(paramSection.split("\\s+"));
                        }
                        
                        StringBuilder funcBody = new StringBuilder();
                        System.out.println("Defining function '" + funcName + "'. Type '}' when done.");
                        
                        while (true) {
                            String line = sc.nextLine().trim();
                            if (line.equals("}")) break;
                            funcBody.append(line).append(";");
                        }
                        
                        functionParams.put(funcName, params);
                        functions.put(funcName, funcBody.toString());
                        System.out.println("Function '" + funcName + "' defined successfully with parameters " + params);
                    }
                    // Single-line definition with braces
                    else if (braceStart != -1 && braceEnd != -1) {
                        String paramSection = funcRest.substring(0, braceStart).trim();
                        String bodySection = funcRest.substring(braceStart + 1, braceEnd).trim();
                        
                        List<String> params = new ArrayList<>();
                        if (!paramSection.isEmpty()) {
                            params = Arrays.asList(paramSection.split("\\s+"));
                        }
                        
                        functionParams.put(funcName, params);
                        functions.put(funcName, bodySection);
                        System.out.println("Function '" + funcName + "' defined with params " + params);
                    } else {
                        System.out.println("Invalid function syntax. Use: define <name> <params> {body}");
                    }
                }
                // Return-style definition (no braces)
                else {
                    String[] tokens = rest.split(" ");
                    
                    if (tokens.length < 3) {
                        System.out.println("Usage: define <name> <param1> <param2> return <command>");
                        return;
                    }
                    
                    String funcName = tokens[0];
                    List<String> params = new ArrayList<>();
                    StringBuilder bodyBuilder = new StringBuilder();
                    
                    boolean foundReturn = false;
                    for (int i = 1; i < tokens.length; i++) {
                        if (tokens[i].equals("return")) {
                            foundReturn = true;
                            // Collect everything after "return" as the function body
                            for (int j = i + 1; j < tokens.length; j++) {
                                bodyBuilder.append(tokens[j]).append(" ");
                            }
                            break;
                        }
                        // Before "return", collect parameters
                        if (!foundReturn) {
                            // If token contains comma, split it into multiple parameters
                            if (tokens[i].contains(",")) {
                                String[] subParams = tokens[i].split(",");
                                for (String p : subParams) {
                                    if (!p.trim().isEmpty()) {
                                        params.add(p.trim());
                                    }
                                }
                            } else {
                                params.add(tokens[i]);
                            }
                        }
                    }
                    
                    String funcBody = bodyBuilder.toString().trim();
                    functions.put(funcName, funcBody);
                    functionParams.put(funcName, params);
                    System.out.println("Function '" + funcName + "' defined with parameters " + params);
                    System.out.println("Function body: " + funcBody);
                }
    }

    // --- function call ---
    public static void funcCall (String input){
            String[] callParts = input.split(" ");
            String funcName = callParts[0];
        //System.out.println("check");
                String lastReturnValue = null;
                List<String> params = functionParams.get(funcName);
                String funcBody = functions.get(funcName);

                // Replace parameters with argument values
                if (params != null && callParts.length - 1 >= params.size()) {
                    Map<String, String> paramMap = new HashMap<>();
                    for (int i = 0; i < params.size(); i++) {
                        paramMap.put(params.get(i), callParts[i + 1]);
                    }

                    for (Map.Entry<String, String> entry : paramMap.entrySet()) {
                        funcBody = funcBody.replace(entry.getKey(), entry.getValue());
                    }
                }

                // --- Handle return or execute full body ---
                if (funcBody.startsWith("return ")) {
                    //System.out.println("check");
                    String returnExpr = funcBody.substring(7).trim();
                    String[] returnParts = returnExpr.split(" ");

                    if (returnParts.length > 0) {
                        switch (returnParts[0]) {
                            case "add":
                                if (returnParts.length == 3) {
                                    int a = getValue(returnParts[1], vars);
                                    int b = getValue(returnParts[2], vars);
                                    lastReturnValue = String.valueOf(add(a, b));
                                } else {
                                    lastReturnValue = returnExpr;
                                }
                                break;
                            case "multiply":
                                if (returnParts.length == 3) {
                                    int a = getValue(returnParts[1], vars);
                                    int b = getValue(returnParts[2], vars);
                                    lastReturnValue = String.valueOf(multiply(a, b));
                                } else {
                                    lastReturnValue = returnExpr;
                                }
                                break;
                            case "divide":
                                if (returnParts.length == 3) {
                                    int a = getValue(returnParts[1], vars);
                                    int b = getValue(returnParts[2], vars);
                                    lastReturnValue = String.valueOf(divide(a, b));
                                } else {
                                    lastReturnValue = returnExpr;
                                }
                                break;
                            case "subtract":
                                if (returnParts.length == 3) {
                                    int a = getValue(returnParts[1], vars);
                                    int b = getValue(returnParts[2], vars);
                                    lastReturnValue = String.valueOf(subtract(a, b));
                                } else {
                                    lastReturnValue = returnExpr;
                                }
                                break;
                            case "print":
                                if (returnParts.length > 1) {
                                    String printVal = returnParts[1];
                                    System.out.println(printVal);
                                    lastReturnValue = printVal;
                                }
                                break;
                            case "say":
                                if (returnParts.length > 1) {
                                    String printVal = returnParts[1];
                                    System.out.println(printVal);
                                    lastReturnValue = printVal;
                                }
                                break;
                            default:
                                lastReturnValue = returnExpr;
                                break;
                        }
                    }
                    System.out.println("Returned: " + lastReturnValue);
                } else {
                    // FIXED: Execute function body properly
                    String[] lines = funcBody.split("[;\n]+");
                    for (String line : lines) {
                        line = line.trim();
                        if (!line.isEmpty()) {
                            executeCommand(line, vars, lists, listsNum, functions, functionParams);
                        }
                    }
                }
                
    }

    // --- keys ---
    public static void Keys(String input){
        String mapName = input.replace("keys ", "").trim();
                if (maps.containsKey(mapName)) {
                    System.out.println(maps.get(mapName).keySet());
                } else {
                    System.out.println("Map not found");
                }
    }

    // --- clear ---
    public static void clear (String input){
        String listName = input.replace("clear", "").trim();

                if (lists.get(listName) == null) {
                    System.out.println("List not found: " + listName);
                    return;
                }

                lists.get(listName).clear();
                System.out.println("List '" + listName + "' cleared!");
    }

    // --- remove index ---
    public static void removeIndex(String input){
        String[] parts = input.split(" ", 4);
                if (parts.length < 4){
                    System.out.println("usage: remove index <indexvalue> <listname> ");
                    return;
                } 
                String listName = parts[3].trim();
                

                if (!lists.containsKey(listName)) {
                    System.out.println("List " + "'" +listName + "'" + " does not exist");
                    return;
                }

                int indexValue = getValue(parts[2], vars);
                int listmax = lists.get(listName).size();

                if (indexValue >= listmax || indexValue < 0){
                    System.out.println("invalid index value");
                    return;
                }
                String removedValue = (lists.get(listName)).get(indexValue);
                lists.get(listName).remove(indexValue);
                System.out.println("value " + "'" + removedValue + "'" + " was removed from " + "'" + listName + "'");
    }

    // --- save command ---
    public static void save( String input){
        String[] parts = input.split(" ");
                if (parts.length < 4 || !parts[2].equals("to")) {
                    System.out.println("Usage: save <name> to <filename>");
                    return;
                }

                String name = parts[1];
                String filename = parts[3];

                try (FileWriter writer = new FileWriter(filename)) {
                    if (vars.containsKey(name)) {
                        writer.write("var " + name + " " + vars.get(name));
                    } else if (lists.containsKey(name)) {
                        writer.write("list " + name + " " + lists.get(name));
                    }else if (maps.containsKey(name)){ 
                        writer.write("list " + name + " " + maps.get(name));
                    }else {
                        System.out.println("Unknown variable or list or map: " + name);
                        return;
                    }
                    System.out.println("Saved " + name + " to " + filename);
                } catch (Exception e) {
                    System.out.println("Error saving: " + e.getMessage());
                }
    }

    // --- if / else / elif ---
    public static void ifElse(String input){
        // Handle simple if statement without braces
                if (!input.contains("{")) {
                    String[] parts = input.split(" ");
                    if (parts.length < 5) {
                        System.out.println("Usage: if <a> <operator> <b> <command>");
                        return;
                    }

                    int left = getValue(parts[1], vars);
                    String op = parts[2];
                    int right = getValue(parts[3], vars);

                    boolean condition = switch (op) {
                        case ">" -> left > right;
                        case "<" -> left < right;
                        case "==" -> left == right;
                        case "!=" -> left != right;
                        case ">=" -> left >= right;
                        case "<=" -> left <= right;
                        default -> {
                            System.out.println("Unknown operator: " + op);
                            yield false;
                        }
                    };

                    if (condition) {
                        String command = String.join(" ", Arrays.copyOfRange(parts, 4, parts.length));
                        String normalized = normalizeCommand(command);
                        
                        if (normalized.startsWith("print ")) {
                            String varName = normalized.replace("print", "").trim();
                            if (vars.containsKey(varName)) {
                                System.out.println(vars.get(varName));
                            } else if (stringVars.containsKey(varName)) {
                                System.out.println(stringVars.get(varName));
                            } else {
                                System.out.println(varName);
                            }
                        }
                    }
                    return;
                }
                
                // Handle if/elif/else with braces
                boolean conditionMet = false;
                String remaining = input;
                
                while (!remaining.isEmpty()) {
                    remaining = remaining.trim();
                    
                    // Check for ELSE block
                    if (remaining.startsWith("else")) {
                        if (conditionMet) {
                            break; // Already executed a branch
                        }
                        
                        remaining = remaining.substring(4).trim();
                        
                        if (!remaining.startsWith("{")) {
                            System.out.println("else must be followed by {");
                            break;
                        }
                        
                        // Extract else body
                        int braceStart = remaining.indexOf("{");
                        int braceEnd = findMatchingBrace(remaining, braceStart);
                        
                        if (braceEnd == -1) {
                            System.out.println("Missing closing } for else block");
                            break;
                        }
                        
                        String elseBody = remaining.substring(braceStart + 1, braceEnd).trim();
                        executeBlock(elseBody , vars, lists, listsNum, functions, functionParams, stringVars, boolVars);
                        conditionMet = true;
                        break;
                    }
                    
                    // Check for IF or ELIF block
                    boolean isElif = remaining.startsWith("elif");
                    boolean isIf = remaining.startsWith("if");
                    
                    if (!isIf && !isElif) {
                        break;
                    }
                    
                    if (isElif && conditionMet) {
                        // Skip this elif since a previous condition was met
                        // Find the end of this elif block to continue
                        int keywordLen = 4; // "elif"
                        String afterKeyword = remaining.substring(keywordLen).trim();
                        int braceStart = afterKeyword.indexOf("{");
                        if (braceStart == -1) break;
                        int braceEnd = findMatchingBrace(afterKeyword, braceStart);
                        if (braceEnd == -1) break;
                        remaining = afterKeyword.substring(braceEnd + 1).trim();
                        return;
                    }
                    
                    // Parse the condition
                    int keywordLen = isIf ? 2 : 4;
                    remaining = remaining.substring(keywordLen).trim();
                    
                    int braceStart = remaining.indexOf("{");
                    if (braceStart == -1) {
                        System.out.println("Missing { after condition");
                        break;
                    }
                    
                    String conditionStr = remaining.substring(0, braceStart).trim();

                    // Check for compound conditions
                    if (conditionStr.contains(" and ") || conditionStr.contains(" or ")) {
                        String[] conditions = conditionStr.split(" and | or ");
                        String logic = conditionStr.contains(" and ") ? "and" : "or";
                        
                        boolean result = true;
                        
                        for (String cond : conditions) {
                            String[] parts = cond.trim().split(" ");
                            int left = getValue(parts[0], vars);
                            String op = parts[1];
                            int right = getValue(parts[2], vars);
                            
                            boolean condResult = evaluateCondition(parts[0], parts[1], parts[2], vars, stringVars, boolVars);
                            
                            if (logic.equals("and")) {
                                result = result && condResult;
                            } else {
                                result = result || condResult;
                            }
                        }
                    }

                    String[] condParts = conditionStr.split(" ");
                    
                    if (condParts.length < 3) {
                        System.out.println("Invalid condition format");
                        break;
                    }

                    boolean condition = evaluateCondition(condParts[0], condParts[1], condParts[2], vars, stringVars, boolVars);
                    
                    
                    
                    // Find matching closing brace
                    int braceEnd = findMatchingBrace(remaining, braceStart);
                    if (braceEnd == -1) {
                        System.out.println("Missing closing }");
                        break;
                    }
                    
                    String body = remaining.substring(braceStart + 1, braceEnd).trim();
                    
                    if (condition) {
                        executeBlock(body, vars, lists, listsNum, functions, functionParams, stringVars, boolVars);
                        conditionMet = true;
                        break; // Don't check remaining elif/else
                    }
                    
                    // Move to next block
                    remaining = remaining.substring(braceEnd + 1).trim();
                }
    }

    // --- eval ---
    public static void eval (String input){
        String expr = input.replace("eval ", "").trim();
                
                try {
                    int result = evaluateExpression(expr, vars);
                    System.out.println("Result: " + result);
                } catch (Exception e) {
                    System.out.println("Error: " + e.getMessage());
                }
    }

    // --- calc ---
    public static void calc(String input){
        String expr = input.replace("calc ", "").trim();
                
                try {
                    int result = evaluateExpression(expr, vars);
                    System.out.println("Result: " + result);
                } catch (Exception e) {
                    System.out.println("Error: " + e.getMessage());
                }
    }

    // --- load ---
    public static void load(String input){
        String[] parts = input.split(" ");
                if (parts.length < 2) {
                    System.out.println("Usage: load <filename>");
                    return;
                }

                String filename = parts[1];

                try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
                    String line = reader.readLine();
                    if (line == null) {
                        System.out.println("File is empty.");
                        return;
                    }

                    String[] tokens = line.split(" ", 3);
                    switch (tokens[0]) {
                        case "var" -> {
                            vars.put(tokens[1], Integer.valueOf(tokens[2]));
                            System.out.println("Loaded variable: " + tokens[1]);
                        }
                        case "list" -> {
                            String cleaned = tokens[2].replace("[", "").replace("]", "");
                            List<String> loadedList = new ArrayList<>(Arrays.asList(cleaned.split(", ")));
                            lists.put(tokens[1], loadedList);
                            System.out.println("Loaded list: " + tokens[1]);
                        }
                        case "map" -> {
                            String cleaned = tokens[2].replace("{", "").replace("}", "");
                            HashMap<String, String> loadedMap = new HashMap<>();
                            
                            if (!cleaned.isEmpty()) {
                                String[] pairs = cleaned.split(", ");
                                for (String pair : pairs) {
                                    String[] keyValue = pair.split("=", 2);
                                    if (keyValue.length == 2) {
                                        loadedMap.put(keyValue[0].trim(), keyValue[1].trim());
                                    }
                                }
                            }   
                            maps.put(tokens[1], loadedMap);
                            System.out.println("Loaded map: " + tokens[1]);
                        }
                        default -> System.out.println("Invalid file format.");
                    }

                } catch (Exception e) {
                    System.out.println("Error loading: " + e.getMessage());
                }
    }

    // --- set and call ---
    public static void setCall(String input){
        String[] parts = input.split(" call ", 2);
                String varName = parts[0].replace("set", "").replace("to", "").trim();
                String functionCall = parts[1].trim();
                
                String result = executeCommand(functionCall, vars, lists, listsNum, functions, functionParams);
                
                try {
                    int value = Integer.parseInt(result);
                    vars.put(varName, value);
                    System.out.println(varName + " = " + value);
                } catch (NumberFormatException e) {
                    System.out.println("Function did not return a numeric value");
                }
    }

    // --- remove value from list ---
    public static void removeList(String input){
        String[] parts = input.split(" ",4);
                if (parts.length < 4){
                    System.out.println("usage: remove <value> <liastname>");
                    return;
                }

                String listName = parts[3];
                if (!lists.containsKey(listName)){
                    System.out.println("there is no list called " + "'" + listName + "'");
                    return;
                }
                String removeName = parts[2];

                if (!lists.get(listName).contains(removeName)){
                    System.out.println("invalid remove value: there is no " + "'" + removeName + "'" + " in list " + "'" + listName + "'");
                    return;
                }

                lists.get(listName).remove(removeName);
                System.out.println("'" + removeName + "'" + " was removed from list " + "'" + listName + "'");
    }

    // --- list lenght ---
    public static void listLenght(String input){
        String listName = input.replace("length", "").trim();

                if (lists.get(listName) == null) {
                    System.out.println("List not found: " + listName);
                    return;
                }

                System.out.println("List '" + listName + "' has " + lists.get(listName).size() + " elements.");
    }

    // --- strinng lenght ---
    public static void lengthString (String input){
        String text = input.replace("length ", "").trim();
                
                // Check if it's a string variable
                if (stringVars.containsKey(text)) {
                    System.out.println(stringVars.get(text).length());
                } else {
                    System.out.println(text.length());
                }
    }

    // --- make list ---
    public static void makeList (String input){
        input = input.replace("\\bmake\\b ", "");
                String[] parts = input.split(" ");
                if (parts.length < 4) {
                    System.out.println("Usage: list <name> <values...>");
                    return;
                }
                String listName = parts[2].trim();
                input = input.replaceFirst("list " + listName + " ", "").trim();
                
                List<String> listValues = new ArrayList<>();
                for (int i = 3; i < parts.length; i++) {
                    listValues.add(String.valueOf(parts[i]));
                }
                lists.put(listName, listValues);
                System.out.println("List '" + listName + "' created: " + listValues);
    }

    // --- concat ---
    public static void concat (String input ){
         String[] parts = input.split(" ", 2);
                if (parts.length < 2) {
                    System.out.println("Usage: concat <word1> <word2> ...");
                    return;
                }
                
                String result = parts[1].replace(" ", "");
                System.out.println(result);
                
    }

    // --- upper case ---
    public static void upper (String input){
        String text = input.replace("upper ", "").trim();
                
                if (stringVars.containsKey(text)) {
                    text = stringVars.get(text);
                }
                
                System.out.println(text.toUpperCase());
    }

    public static void replace (String input){
        String[] parts = input.split(" ", 4);
                if (parts.length < 4) {
                    System.out.println("Usage: replace <string> <old> <new>");
                    return;
                }
                
                String text = parts[1];
                String oldText = parts[2];
                String newText = parts[3];
                
                if (stringVars.containsKey(text)) {
                    text = stringVars.get(text);
                }
                
                String result = text.replace(oldText, newText);
                System.out.println(result);
    }

    // --- lower case ---
    public static void lower(String input){
        String text = input.replace("lower ", "").trim();
                
                if (stringVars.containsKey(text)) {
                    text = stringVars.get(text);
                }
                
                System.out.println(text.toLowerCase());
    }

    // --- substring ---
    public static void substring (String input){
        String[] parts = input.split(" ");
                if (parts.length < 4) {
                    System.out.println("Usage: substring <string> <start> <end>");
                    return;
                }
                
                String text = parts[1];
                int start = Integer.parseInt(parts[2]);
                int end = Integer.parseInt(parts[3]);
                
                if (stringVars.containsKey(text)) {
                    text = stringVars.get(text);
                }
                
                if (start < 0 || end > text.length() || start >= end) {
                    System.out.println("Invalid substring range");
                    return;
                }
                
                System.out.println(text.substring(start, end));
    }

    // --- split ---
    public static void split(String input){
        String[] parts = input.split(" ", 3);
                if (parts.length < 3) {
                    System.out.println("Usage: split <string> <delimiter>");
                    return;
                }
                
                String text = parts[1];
                String delimiter = parts[2];
                
                if (stringVars.containsKey(text)) {
                    text = stringVars.get(text);
                }
                
                String[] splitResult = text.split(delimiter);
                List<String> resultList = new ArrayList<>(Arrays.asList(splitResult));
                
                System.out.println(resultList);
    }


    // --- while loop ---
    public static void While (String input){
        String[] parts = input.split(" ", 4);
                if (parts.length < 4) {
                    System.out.println("Usage: while <var> <op> <value> { commands }");
                    return;
                }
                
                // Parse condition
                String varName = parts[1];
                String operator = parts[2];
                String rest = input.substring(input.indexOf(operator) + operator.length()).trim();
                
                // Find where condition ends and body starts
                String[] condAndBody = rest.split("\\{", 2);
                if (condAndBody.length < 2) {
                    System.out.println("Usage: while <condition> { commands }");
                    return;
                }
                
                String compareValueStr = condAndBody[0].trim();
                String bodyWithBrace = condAndBody[1].trim();
                
                // Check for multi-line or single-line
                boolean isMultiLine = !bodyWithBrace.contains("}");
                StringBuilder whileBody = new StringBuilder();
                
                if (isMultiLine) {
                    // Multi-line while loop
                    System.out.println("While loop started. Type '}' when done.");
                    while (true) {
                        String line = sc.nextLine().trim();
                        if (line.equals("}")) {
                            break;
                        }
                        whileBody.append(line).append(";");
                    }
                } else {
                    // Single-line while loop
                    whileBody.append(bodyWithBrace.substring(0, bodyWithBrace.lastIndexOf("}")));
                }
                
                // Execute while loop
                int maxIterations = 10000; // Prevent infinite loops
                int iterations = 0;
                boolean shouldBreak = false;
                
                while (iterations < maxIterations) {
                    iterations++;
                    
                    // Check condition
                    if (!vars.containsKey(varName)) {
                        System.out.println("Variable '" + varName + "' not found");
                        break;
                    }
                    
                    int leftValue = vars.get(varName);
                    int rightValue;
                    
                    try {
                        rightValue = getValue(compareValueStr, vars);
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid comparison value");
                        break;
                    }
                    
                    boolean condition = switch (operator) {
                        case ">" -> leftValue > rightValue;
                        case "<" -> leftValue < rightValue;
                        case ">=" -> leftValue >= rightValue;
                        case "<=" -> leftValue <= rightValue;
                        case "==" -> leftValue == rightValue;
                        case "!=" -> leftValue != rightValue;
                        default -> {
                            System.out.println("Unknown operator: " + operator);
                            yield false;
                        }
                    };
                    
                    if (!condition) {
                        break;
                    }
                    
                    // Execute body commands
                    String[] commands = whileBody.toString().split(";");
                    
                    for (String cmd : commands) {
                        cmd = cmd.trim();
                        if (cmd.isEmpty()) continue;
                        
                        // Check for break
                        if (cmd.equals("break")) {
                            shouldBreak = true;
                            break;
                        }
                        
                        // Check for continue
                        if (cmd.equals("continue")) {
                            break; // Break out of command loop, continue while loop
                        }
                        
                        // Execute command
                        String normalizedCmd = normalizeCommand(cmd);
                        
                        // Handle simple commands inline
                        if (normalizedCmd.startsWith("print ")) {
                            String printVar = normalizedCmd.replace("print", "").trim();
                            if (vars.containsKey(printVar)) {
                                System.out.println(vars.get(printVar));
                            } else if (stringVars.containsKey(printVar)) {
                                System.out.println(stringVars.get(printVar));
                            } else {
                                System.out.println(printVar);
                            }
                        } else if (normalizedCmd.startsWith("set ")) {
                            String[] setParts = normalizedCmd.replaceFirst("set ", "").split(" ", 2);
                            if (setParts.length >= 2) {
                                String setVarName = setParts[0];
                                try {
                                    int value = getValue(setParts[1], vars);
                                    vars.put(setVarName, value);
                                } catch (NumberFormatException e) {
                                    stringVars.put(setVarName, setParts[1]);
                                }
                            }
                        } else if (normalizedCmd.split(" ").length == 3) {
                            // Math operations
                            String[] mathParts = normalizedCmd.split(" ");
                            String op = mathParts[0];
                            int a = getValue(mathParts[1], vars);
                            int b = getValue(mathParts[2], vars);
                            
                            int result = switch (op) {
                                case "add" -> add(a, b);
                                case "subtract" -> subtract(a, b);
                                case "multiply" -> multiply(a, b);
                                case "divide" -> b != 0 ? a / b : 0;
                                default -> 0;
                            };
                            
                            // Auto-assign to first variable if it's a variable name
                            if (vars.containsKey(mathParts[1])) {
                                vars.put(mathParts[1], result);
                            }
                        }
                    }
                    
                    if (shouldBreak) {
                        System.out.println("Loop broken at iteration " + iterations);
                        break;
                    }
                }
                
                if (iterations >= maxIterations) {
                    System.out.println("Warning: Loop stopped after " + maxIterations + " iterations (infinite loop protection)");
                } else {
                    System.out.println("While loop completed after " + iterations + " iterations");
                }
    }

    // --- get list ---
    public static void getList(String input){
        String[] parts = input.split(" ");
                if (parts.length < 4){
                    System.out.print("Invalid get Usage: get list <listName> <index>");
                    return;
                }

                String listName = parts[2];
                String getValue = parts[3];
                int index = Integer.parseInt(getValue);

                List<String> targetList = lists.get(listName);

                if (targetList != null && index > 0 && index <= targetList.size()){
                    System.out.println("Result: " + targetList.get(index - 1));

                }else{
                    System.out.println("Invalid list name or index");
                }
    }

    // --- get map ---
    public static void getMap (String input){
        String[] parts = input.split(" ");

                if (parts.length < 4){
                    System.out.print("Invalid get Usage: get map <mapName> <key>");
                    return;
                }

                String mapName = parts[2];
                String key = parts[3];
                
                if (maps.containsKey(mapName)) {
                    String value = maps.get(mapName).get(key);
                    if (value != null) {
                        System.out.println(value);
                    } else {
                        System.out.println("Key '" + key + "' not found in " + mapName);
                    }
                    return;
                }
    }

    // --- make num list ---
    public static void makeNumlist(String input){
        String[] parts = input.split(" ");
                if (parts.length < 4) {
                    System.out.println("Usage: make numlist <name> <value1> <value2> ...");
                    return;
                }
                
                String listName = parts[2];
                List<Integer> numList = new ArrayList<>();
                
                try {
                    for (int i = 3; i < parts.length; i++) {
                        numList.add(Integer.parseInt(parts[i]));
                    }
                    listsNum.put(listName, numList);
                    System.out.println("Numeric list '" + listName + "' created: " + numList);
                } catch (NumberFormatException e) {
                    System.out.println("All values must be numbers");
                }
    }

    // --- bsearch ---
    public static void bsearch (String input){
        String[] parts = input.split(" in ");
                if (parts.length < 2) {
                    System.out.println("Usage: bsearch <value> in <sortedList>");
                    return;
                }
                
                String value = parts[0].replace("bsearch ", "").trim();
                String listName = parts[1].trim();
                
                // Check numeric lists
                if (listsNum.containsKey(listName)) {
                    try {
                        int numValue = Integer.parseInt(value);
                        List<Integer> list = listsNum.get(listName);
                        
                        int index = Collections.binarySearch(list, numValue);
                        
                        if (index >= 0) {
                            System.out.println("Found at index: " + index);
                        } else {
                            System.out.println("Value not found (list must be sorted)");
                        }
                    } catch (NumberFormatException e) {
                        System.out.println("Value must be a number");
                    }
                } 
                // Check string lists (and try to parse as numbers)
                else if (lists.containsKey(listName)) {
                    List<String> list = lists.get(listName);
                    
                    try {
                        // Try numeric search
                        int numValue = Integer.parseInt(value);
                        
                        // Convert strings to integers for search
                        List<Integer> tempNums = new ArrayList<>();
                        for (String s : list) {
                            tempNums.add(Integer.parseInt(s));
                        }
                        
                        int index = Collections.binarySearch(tempNums, numValue);
                        
                        if (index >= 0) {
                            System.out.println("Found at index: " + index);
                        } else {
                            System.out.println("Value not found (list must be sorted)");
                        }
                    } catch (NumberFormatException e) {
                        // Alphabetic binary search
                        Collections.sort(list); // Ensure it's sorted
                        int index = Collections.binarySearch(list, value);
                        
                        if (index >= 0) {
                            System.out.println("Found at index: " + index);
                        } else {
                            System.out.println("Value not found (list must be sorted alphabetically)");
                        }
                    }
                } else {
                    System.out.println("List '" + listName + "' not found");
                }
    }

    // --- sort ---
    public static void sort (String input){
        String[] parts = input.split(" ");
                if (parts.length < 2) {
                    System.out.println("Usage: sort <list> [desc]");
                    return;
                }
                
                String listName = parts[1];
                boolean descending = parts.length > 2 && parts[2].equals("desc");
                
                // Handle numeric lists
                if (listsNum.containsKey(listName)) {
                    List<Integer> list = listsNum.get(listName);
                    
                    if (descending) {
                        list.sort(Collections.reverseOrder());
                    } else {
                        Collections.sort(list);
                    }
                    
                    System.out.println("Sorted " + listName + ": " + list);
                }
                // Handle string lists
                else if (lists.containsKey(listName)) {
                    List<String> list = lists.get(listName);
                    
                    // Try to sort as numbers if possible
                    try {
                        List<Integer> tempNums = new ArrayList<>();
                        for (String s : list) {
                            tempNums.add(Integer.parseInt(s));
                        }
                        
                        if (descending) {
                            tempNums.sort(Collections.reverseOrder());
                        } else {
                            Collections.sort(tempNums);
                        }
                        
                        // Convert back to strings
                        list.clear();
                        for (Integer num : tempNums) {
                            list.add(String.valueOf(num));
                        }
                        
                        System.out.println("Sorted " + listName + ": " + list);
                    } catch (NumberFormatException e) {
                        // Sort alphabetically
                        if (descending) {
                            list.sort(Collections.reverseOrder());
                        } else {
                            Collections.sort(list);
                        }
                        System.out.println("Sorted " + listName + ": " + list);
                    }
                } else {
                    System.out.println("List '" + listName + "' not found");
                }
    }

    // --- find elements in list ---
    public static void findList(String input){
        String[] parts = input.split(" ", 4);
            if (parts.length < 4) {
                System.out.println("Usage: in list <value> <listName>");
                return;
            }
            
            String value = parts[2];
            String listName = parts[3];
                
                // Search in numeric lists
                if (listsNum.containsKey(listName)) {
                    try {
                        int numValue = Integer.parseInt(value);
                        List<Integer> list = listsNum.get(listName);
                        int index = list.indexOf(numValue);
                        
                        if (index != -1) {
                            System.out.println("Found at index: " + index);
                        } else {
                            System.out.println("Value " + value + " not found in " + listName);
                        }
                    } catch (NumberFormatException e) {
                        System.out.println("Value must be a number for numeric lists");
                    }
                }
                // Search in string lists
                else if (lists.containsKey(listName)) {
                    List<String> list = lists.get(listName);
                    int index = list.indexOf(value);
                    
                    if (index != -1) {
                        System.out.println("Found at index: " + index);
                    } else {
                        System.out.println("Value '" + value + "' not found in " + listName);
                    }
                } else {
                    System.out.println("List '" + listName + "' not found");
                }
    }

    // --- range ---
    public static void rangeList(String input){
        String[] parts = input.split(" ");
                if (parts.length < 3) {
                    System.out.println("Usage: range <start> <end> [step]");
                    return;
                }
                
                try {
                    int start = Integer.parseInt(parts[1]);
                    int end = Integer.parseInt(parts[2]);
                    int step = (parts.length > 3) ? Integer.parseInt(parts[3]) : 1;
                    
                    if (step == 0) {
                        System.out.println("Step cannot be zero");
                        return;
                    }
                    
                    List<Integer> rangeList = new ArrayList<>();
                    
                    if (step > 0) {
                        for (int i = start; i < end; i += step) {
                            rangeList.add(i);
                        }
                    } else {
                        for (int i = start; i > end; i += step) {
                            rangeList.add(i);
                        }
                    }
                    
                    System.out.println(rangeList);
                } catch (NumberFormatException e) {
                    System.out.println("Start, end, and step must be numbers");
                }
    }

    // --- range to variables ---
    public static void rangeVar (String input){
        String[] parts = input.split(" ");
                if (parts.length < 5) {
                    System.out.println("Usage: set <name> range <start> <end> [step]");
                    return;
                }
                
                String listName = parts[1];
                
                try {
                    int start = Integer.parseInt(parts[3]);
                    int end = Integer.parseInt(parts[4]);
                    int step = (parts.length > 5) ? Integer.parseInt(parts[5]) : 1;
                    
                    if (step == 0) {
                        System.out.println("Step cannot be zero");
                        return;
                    }
                    
                    List<Integer> rangeList = new ArrayList<>();
                    
                    if (step > 0) {
                        for (int i = start; i < end; i += step) {
                            rangeList.add(i);
                        }
                    } else {
                        for (int i = start; i > end; i += step) {
                            rangeList.add(i);
                        }
                    }
                    
                    listsNum.put(listName, rangeList);
                    System.out.println("List '" + listName + "' created: " + rangeList);
                } catch (NumberFormatException e) {
                    System.out.println("Start, end, and step must be numbers");
                }
    }

    // --- repeat ---
    public static void repeat(String input){
        String[] parts = input.split(" ", 3);
                if (parts.length < 3) {
                    System.out.println("Usage: repeat <value> <times>");
                    return;
                }
                
                String value = parts[1];
                
                try {
                    int times = Integer.parseInt(parts[2]);
                    
                    if (times < 0) {
                        System.out.println("Times must be positive");
                        return;
                    }
                    
                    List<String> repeatedList = new ArrayList<>();
                    for (int i = 0; i < times; i++) {
                        repeatedList.add(value);
                    }
                    
                    System.out.println(repeatedList);
                } catch (NumberFormatException e) {
                    System.out.println("Times must be a number");
                }
    }

    // --- filter ---
    public static void filter (String input){
        String[] parts = input.split(" where ");
                if (parts.length < 2) {
                    System.out.println("Usage: filter <list> where <condition>");
                    return;
                }
                
                String listName = parts[0].replace("filter ", "").trim();
                String condition = parts[1].trim();
                
                // Parse condition: e.g., "> 5" or "< 10" or "== 3"
                String[] condParts = condition.split(" ");
                if (condParts.length < 2) {
                    System.out.println("Invalid condition format. Use: > value, < value, == value, etc.");
                    return;
                }
                
                String operator = condParts[0];
                
                // Handle numeric lists
                if (listsNum.containsKey(listName)) {
                    try {
                        int compareValue = Integer.parseInt(condParts[1]);
                        List<Integer> list = listsNum.get(listName);
                        List<Integer> filtered = new ArrayList<>();
                        
                        for (int num : list) {
                            boolean matches = switch (operator) {
                                case ">" -> num > compareValue;
                                case "<" -> num < compareValue;
                                case ">=" -> num >= compareValue;
                                case "<=" -> num <= compareValue;
                                case "==" -> num == compareValue;
                                case "!=" -> num != compareValue;
                                default -> false;
                            };
                            
                            if (matches) {
                                filtered.add(num);
                            }
                        }
                        
                        System.out.println("Filtered: " + filtered);
                    } catch (NumberFormatException e) {
                        System.out.println("Comparison value must be a number");
                    }
                }
                // Handle string lists
                else if (lists.containsKey(listName)) {
                    List<String> list = lists.get(listName);
                    List<String> filtered = new ArrayList<>();
                    
                    // Try numeric comparison
                    try {
                        int compareValue = Integer.parseInt(condParts[1]);
                        
                        for (String s : list) {
                            int num = Integer.parseInt(s);
                            boolean matches = switch (operator) {
                                case ">" -> num > compareValue;
                                case "<" -> num < compareValue;
                                case ">=" -> num >= compareValue;
                                case "<=" -> num <= compareValue;
                                case "==" -> num == compareValue;
                                case "!=" -> num != compareValue;
                                default -> false;
                            };
                            
                            if (matches) {
                                filtered.add(s);
                            }
                        }
                        
                        System.out.println("Filtered: " + filtered);
                    } catch (NumberFormatException e) {
                        // String comparison
                        String compareValue = condParts[1];
                        
                        for (String s : list) {
                            boolean matches = switch (operator) {
                                case "==" -> s.equals(compareValue);
                                case "!=" -> !s.equals(compareValue);
                                default -> false;
                            };
                            
                            if (matches) {
                                filtered.add(s);
                            }
                        }
                        
                        System.out.println("Filtered: " + filtered);
                    }
                } else {
                    System.out.println("List '" + listName + "' not found");
                }
    }

    // --- map ---
    public static void map (String input){
        String[] parts = input.split(" ", 3);
                if (parts.length < 3) {
                    System.out.println("Usage: map <list> <operation>");
                    return;
                }
                
                String listName = parts[1];
                String operation = parts[2];
                
                // Parse operation: e.g., "+ 5", "* 2", "- 1"
                String[] opParts = operation.split(" ");
                if (opParts.length < 2) {
                    System.out.println("Invalid operation. Use: + value, - value, * value, / value");
                    return;
                }
                
                String operator = opParts[0];
                
                if (listsNum.containsKey(listName)) {
                    try {
                        int operand = Integer.parseInt(opParts[1]);
                        List<Integer> list = listsNum.get(listName);
                        List<Integer> mapped = new ArrayList<>();
                        
                        for (int num : list) {
                            int result = switch (operator) {
                                case "+" -> num + operand;
                                case "-" -> num - operand;
                                case "*" -> num * operand;
                                case "/" -> operand != 0 ? num / operand : num;
                                default -> num;
                            };
                            mapped.add(result);
                        }
                        
                        System.out.println("Mapped: " + mapped);
                    } catch (NumberFormatException e) {
                        System.out.println("Operand must be a number");
                    }
                } else if (lists.containsKey(listName)) {
                    // Try to apply to string list
                    try {
                        int operand = Integer.parseInt(opParts[1]);
                        List<String> list = lists.get(listName);
                        List<Integer> mapped = new ArrayList<>();
                        
                        for (String s : list) {
                            int num = Integer.parseInt(s);
                            int result = switch (operator) {
                                case "+" -> num + operand;
                                case "-" -> num - operand;
                                case "*" -> num * operand;
                                case "/" -> operand != 0 ? num / operand : num;
                                default -> num;
                            };
                            mapped.add(result);
                        }
                        
                        System.out.println("Mapped: " + mapped);
                    } catch (NumberFormatException e) {
                        System.out.println("List must contain numbers for arithmetic operations");
                    }
                } else {
                    System.out.println("List '" + listName + "' not found");
                }
    }

    // --- set filtered list ---
    public static void setFilter (String input){
        // Format: set newList filter oldList where > 5
                String[] mainParts = input.split(" filter ");
                if (mainParts.length < 2) {
                    System.out.println("Usage: set <newList> filter <oldList> where <condition>");
                    return;
                }
                
                String newListName = mainParts[0].replace("set ", "").trim();
                String[] filterParts = mainParts[1].split(" where ");
                
                if (filterParts.length < 2) {
                    System.out.println("Usage: set <newList> filter <oldList> where <condition>");
                    return;
                }
                
                String oldListName = filterParts[0].trim();
                String condition = filterParts[1].trim();
                
                String[] condParts = condition.split(" ");
                if (condParts.length < 2) {
                    System.out.println("Invalid condition");
                    return;
                }
                
                String operator = condParts[0];
                
                if (listsNum.containsKey(oldListName)) {
                    try {
                        int compareValue = Integer.parseInt(condParts[1]);
                        List<Integer> oldList = listsNum.get(oldListName);
                        List<Integer> newList = new ArrayList<>();
                        
                        for (int num : oldList) {
                            boolean matches = switch (operator) {
                                case ">" -> num > compareValue;
                                case "<" -> num < compareValue;
                                case ">=" -> num >= compareValue;
                                case "<=" -> num <= compareValue;
                                case "==" -> num == compareValue;
                                case "!=" -> num != compareValue;
                                default -> false;
                            };
                            
                            if (matches) {
                                newList.add(num);
                            }
                        }
                        
                        listsNum.put(newListName, newList);
                        System.out.println("List '" + newListName + "' created: " + newList);
                    } catch (NumberFormatException e) {
                        System.out.println("Comparison value must be a number");
                    }
                } else if (lists.containsKey(oldListName)) {
                    try {
                        int compareValue = Integer.parseInt(condParts[1]);
                        List<String> oldList = lists.get(oldListName);
                        List<String> newList = new ArrayList<>();
                        
                        for (String s : oldList) {
                            int num = Integer.parseInt(s);
                            boolean matches = switch (operator) {
                                case ">" -> num > compareValue;
                                case "<" -> num < compareValue;
                                case ">=" -> num >= compareValue;
                                case "<=" -> num <= compareValue;
                                case "==" -> num == compareValue;
                                case "!=" -> num != compareValue;
                                default -> false;
                            };
                            
                            if (matches) {
                                newList.add(s);
                            }
                        }
                        
                        lists.put(newListName, newList);
                        System.out.println("List '" + newListName + "' created: " + newList);
                    } catch (NumberFormatException e) {
                        System.out.println("List must contain numbers");
                    }
                } else {
                    System.out.println("List '" + oldListName + "' not found");
                }
    }

    // --- contains list ---
    public static void containsList(String input){
        String[] parts = input.split(" ", 4);
                
                //System.out.println("DEBUG: parts.length = " + parts.length);
                for (int i = 0; i < parts.length; i++) {
                    //System.out.println("DEBUG: parts[" + i + "] = '" + parts[i] + "'");
                }
                
                if (parts.length < 4) {
                    System.out.println("Usage: contains list <listName> <value>");
                    return;
                }
                
                String listName = parts[2];
                String value = parts[3];
                
                //System.out.println("DEBUG: Looking for '" + value + "' in list '" + listName + "'");
                
                // Check numeric lists
                if (listsNum.containsKey(listName)) {
                    //System.out.println("DEBUG: Found in listsNum");
                    try {
                        int numValue = Integer.parseInt(value);
                        boolean found = listsNum.get(listName).contains(numValue);
                        //System.out.println("DEBUG: Searching for number " + numValue + ", found = " + found);
                        System.out.println(found);
                    } catch (NumberFormatException e) {
                        //System.out.println("DEBUG: Not a number");
                        System.out.println(false);
                    }
                }
                // Check string lists
                else if (lists.containsKey(listName)) {
                    //System.out.println("DEBUG: Found in lists (string list)");
                    //System.out.println("DEBUG: List contents: " + lists.get(listName));
                    boolean found = lists.get(listName).contains(value);
                    //System.out.println("DEBUG: Result = " + found);
                    System.out.println(found);
                } else {
                    //System.out.println("DEBUG: List not found in either listsNum or lists");
                    System.out.println("List not found");
                }
    }

    // --- contains string ---
    public static void containsWord(String input){
        String[] parts = input.split(" ", 4);
                if (parts.length < 4) {
                    System.out.println("Usage: contains <string> <search>");
                    return;
                }
                
                String text = parts[2];
                String search = parts[3];
                
                if (stringVars.containsKey(text)) {
                    text = stringVars.get(text);
                }
                
                boolean found = text.contains(search);
                System.out.println(found);
    }

    // --- min ---
    public static void Min (String input){
        String listName = input.replace("min ", "").trim();
                
                if (listsNum.containsKey(listName)) {
                    List<Integer> list = listsNum.get(listName);
                    
                    if (list.isEmpty()) {
                        System.out.println("List is empty");
                        return;
                    }
                    
                    int min = Collections.min(list);
                    System.out.println("Minimum: " + min);
                } else if (lists.containsKey(listName)) {
                    List<String> list = lists.get(listName);
                    
                    if (list.isEmpty()) {
                        System.out.println("List is empty");
                        return;
                    }
                    
                    // Try numeric comparison
                    try {
                        List<Integer> nums = new ArrayList<>();
                        for (String s : list) {
                            nums.add(Integer.parseInt(s));
                        }
                        int min = Collections.min(nums);
                        System.out.println("Minimum: " + min);
                    } catch (NumberFormatException e) {
                        // Alphabetic comparison
                        String min = Collections.min(list);
                        System.out.println("Minimum: " + min);
                    }
                } else {
                    System.out.println("List '" + listName + "' not found");
                }
    }

    // --- max ---
    public static void Max (String input){
        String listName = input.replace("max ", "").trim();
                
                if (listsNum.containsKey(listName)) {
                    List<Integer> list = listsNum.get(listName);
                    
                    if (list.isEmpty()) {
                        System.out.println("List is empty");
                        return;
                    }
                    
                    int max = Collections.max(list);
                    System.out.println("Maximum: " + max);
                } else if (lists.containsKey(listName)) {
                    List<String> list = lists.get(listName);
                    
                    if (list.isEmpty()) {
                        System.out.println("List is empty");
                        return;
                    }
                    
                    // Try numeric comparison
                    try {
                        List<Integer> nums = new ArrayList<>();
                        for (String s : list) {
                            nums.add(Integer.parseInt(s));
                        }
                        int max = Collections.max(nums);
                        System.out.println("Maximum: " + max);
                    } catch (NumberFormatException e) {
                        // Alphabetic comparison
                        String max = Collections.max(list);
                        System.out.println("Maximum: " + max);
                    }
                } else {
                    System.out.println("List '" + listName + "' not found");
                }
    }

    // --- unique ---
    public static void unique (String input){
        String listName = input.replace("unique ", "").trim();
                
                if (listsNum.containsKey(listName)) {
                    List<Integer> list = listsNum.get(listName);
                    List<Integer> uniqueList = new ArrayList<>(new LinkedHashSet<>(list));
                    listsNum.put(listName, uniqueList);
                    System.out.println("Unique " + listName + ": " + uniqueList);
                } else if (lists.containsKey(listName)) {
                    List<String> list = lists.get(listName);
                    List<String> uniqueList = new ArrayList<>(new LinkedHashSet<>(list));
                    lists.put(listName, uniqueList);
                    System.out.println("Unique " + listName + ": " + uniqueList);
                } else {
                    System.out.println("List '" + listName + "' not found");
                }
    }

    // --- combines two list ---
    public static void union(String input){
        String[] parts = input.split(" and ");
                if (parts.length < 2) {
                    System.out.println("Usage: union <list1> and <list2>");
                    return;
                }
                
                String list1Name = parts[0].replace("union ", "").trim();
                String list2Name = parts[1].trim();
                
                // Handle string lists
                if (lists.containsKey(list1Name) && lists.containsKey(list2Name)) {
                    List<String> list1 = lists.get(list1Name);
                    List<String> list2 = lists.get(list2Name);
                    
                    Set<String> unionSet = new LinkedHashSet<>(list1);
                    unionSet.addAll(list2);
                    
                    List<String> result = new ArrayList<>(unionSet);
                    System.out.println("Union: " + result);
                }
                // Handle numeric lists
                else if (listsNum.containsKey(list1Name) && listsNum.containsKey(list2Name)) {
                    List<Integer> list1 = listsNum.get(list1Name);
                    List<Integer> list2 = listsNum.get(list2Name);
                    
                    Set<Integer> unionSet = new LinkedHashSet<>(list1);
                    unionSet.addAll(list2);
                    
                    List<Integer> result = new ArrayList<>(unionSet);
                    System.out.println("Union: " + result);
                } else {
                    System.out.println("Both lists must exist and be of the same type");
                }
    }

    // --- intersect ---
    public static void intersect (String input){
        String[] parts = input.split(" and ");
                if (parts.length < 2) {
                    System.out.println("Usage: intersect <list1> and <list2>");
                    return;
                }
                
                String list1Name = parts[0].replace("intersect ", "").trim();
                String list2Name = parts[1].trim();
                
                // Handle string lists
                if (lists.containsKey(list1Name) && lists.containsKey(list2Name)) {
                    List<String> list1 = lists.get(list1Name);
                    List<String> list2 = lists.get(list2Name);
                    
                    List<String> intersection = new ArrayList<>(list1);
                    intersection.retainAll(list2);
                    
                    System.out.println("Intersection: " + intersection);
                }
                // Handle numeric lists
                else if (listsNum.containsKey(list1Name) && listsNum.containsKey(list2Name)) {
                    List<Integer> list1 = listsNum.get(list1Name);
                    List<Integer> list2 = listsNum.get(list2Name);
                    
                    List<Integer> intersection = new ArrayList<>(list1);
                    intersection.retainAll(list2);
                    
                    System.out.println("Intersection: " + intersection);
                } else {
                    System.out.println("Both lists must exist and be of the same type");
                }
    }

    // --- difference ---
    public static void difference (String input){
        String[] parts = input.split(" and ");
                if (parts.length < 2) {
                    System.out.println("Usage: difference <list1> and <list2>");
                    return;
                }
                
                String list1Name = parts[0].replace("difference ", "").trim();
                String list2Name = parts[1].trim();
                
                // Handle string lists
                if (lists.containsKey(list1Name) && lists.containsKey(list2Name)) {
                    List<String> list1 = lists.get(list1Name);
                    List<String> list2 = lists.get(list2Name);
                    
                    List<String> difference = new ArrayList<>(list1);
                    difference.removeAll(list2);
                    
                    System.out.println("Difference: " + difference);
                }
                // Handle numeric lists
                else if (listsNum.containsKey(list1Name) && listsNum.containsKey(list2Name)) {
                    List<Integer> list1 = listsNum.get(list1Name);
                    List<Integer> list2 = listsNum.get(list2Name);
                    
                    List<Integer> difference = new ArrayList<>(list1);
                    difference.removeAll(list2);
                    
                    System.out.println("Difference: " + difference);
                } else {
                    System.out.println("Both lists must exist and be of the same type");
                }
    }

    // --- symdiff ---
    public static void symdiff(String input){
        String[] parts = input.split(" and ");
                String list1Name = parts[0].replace("symdiff ", "").trim();
                String list2Name = parts[1].trim();
                
                if (lists.containsKey(list1Name) && lists.containsKey(list2Name)) {
                    List<String> list1 = new ArrayList<>(lists.get(list1Name));
                    List<String> list2 = new ArrayList<>(lists.get(list2Name));
                    
                    List<String> onlyInList1 = new ArrayList<>(list1);
                    onlyInList1.removeAll(list2);
                    
                    List<String> onlyInList2 = new ArrayList<>(list2);
                    onlyInList2.removeAll(list1);
                    
                    List<String> symDiff = new ArrayList<>(onlyInList1);
                    symDiff.addAll(onlyInList2);
                    
                    System.out.println("Symmetric Difference: " + symDiff);
                }
    }

    // --- shuffle ---
    public static void shuffle(String input){
        String listName = input.replace("shuffle ", "").trim();
                
                if (listsNum.containsKey(listName)) {
                    List<Integer> list = listsNum.get(listName);
                    Collections.shuffle(list);
                    System.out.println("Shuffled " + listName + ": " + list);
                } else if (lists.containsKey(listName)) {
                    List<String> list = lists.get(listName);
                    Collections.shuffle(list);
                    System.out.println("Shuffled " + listName + ": " + list);
                } else {
                    System.out.println("List '" + listName + "' not found");
                }
    }

    // --- distinct ---
    public static void distinct (String input){
        String listName = input.replace("distinct ", "").trim();
                
                if (listsNum.containsKey(listName)) {
                    List<Integer> list = listsNum.get(listName);
                    Set<Integer> uniqueSet = new HashSet<>(list);
                    System.out.println("Distinct count: " + uniqueSet.size());
                    System.out.println("Unique values: " + uniqueSet);
                } else if (lists.containsKey(listName)) {
                    List<String> list = lists.get(listName);
                    Set<String> uniqueSet = new HashSet<>(list);
                    System.out.println("Distinct count: " + uniqueSet.size());
                    System.out.println("Unique values: " + uniqueSet);
                } else {
                    System.out.println("List '" + listName + "' not found");
                }
    }

    // --- copy list ---
    public static void copyList (String input){
         String[] parts = input.split(" to ");
                if (parts.length < 2) {
                    System.out.println("Usage: copy <sourceList> to <newList>");
                    return;
                }
                
                String sourceName = parts[0].replace("copy ", "").trim();
                String newName = parts[1].trim();
                
                if (listsNum.containsKey(sourceName)) {
                    List<Integer> newList = new ArrayList<>(listsNum.get(sourceName));
                    listsNum.put(newName, newList);
                    System.out.println("Copied " + sourceName + " to " + newName);
                } else if (lists.containsKey(sourceName)) {
                    List<String> newList = new ArrayList<>(lists.get(sourceName));
                    lists.put(newName, newList);
                    System.out.println("Copied " + sourceName + " to " + newName);
                } else {
                    System.out.println("List '" + sourceName + "' not found");
                }
    }

    // --- merge --- 
    public static void merge (String input){
        String[] parts = input.split(" and ");
                if (parts.length < 2) {
                    System.out.println("Usage: merge <list1> and <list2>");
                    return;
                }
                
                String list1Name = parts[0].replace("merge ", "").trim();
                String list2Name = parts[1].trim();
                
                if (lists.containsKey(list1Name) && lists.containsKey(list2Name)) {
                    List<String> merged = new ArrayList<>(lists.get(list1Name));
                    merged.addAll(lists.get(list2Name));
                    System.out.println("Merged: " + merged);
                } else if (listsNum.containsKey(list1Name) && listsNum.containsKey(list2Name)) {
                    List<Integer> merged = new ArrayList<>(listsNum.get(list1Name));
                    merged.addAll(listsNum.get(list2Name));
                    System.out.println("Merged: " + merged);
                } else {
                    System.out.println("Both lists must exist and be of the same type");
                }
    }

    // --- reverse ---
    public static void reverse (String input){
        String listName = input.replace("reverse ", "").trim();
                
                if (listsNum.containsKey(listName)) {
                    List<Integer> list = listsNum.get(listName);
                    Collections.reverse(list);
                    System.out.println("Reversed " + listName + ": " + list);
                } else if (lists.containsKey(listName)) {
                    List<String> list = lists.get(listName);
                    Collections.reverse(list);
                    System.out.println("Reversed " + listName + ": " + list);
                } else {
                    System.out.println("List '" + listName + "' not found");
                }
    }

    // --- count ---
    public static void count(String input){
        String[] parts = input.split(" in ");
                if (parts.length < 2) {
                    System.out.println("Usage: count <value> in <list>");
                    return;
                }
                
                String value = parts[0].replace("count ", "").trim();
                String listName = parts[1].trim();
                
                if (listsNum.containsKey(listName)) {
                    try {
                        int numValue = Integer.parseInt(value);
                        int count = Collections.frequency(listsNum.get(listName), numValue);
                        System.out.println("Count: " + count);
                    } catch (NumberFormatException e) {
                        System.out.println("Value must be a number");
                    }
                } else if (lists.containsKey(listName)) {
                    int count = Collections.frequency(lists.get(listName), value);
                    System.out.println("Count: " + count);
                } else {
                    System.out.println("List not found");
                }
    }

    // --- sum ---
    public static void sum (String input){
        String listName = input.replace("sum ", "").trim();
                
                if (listsNum.containsKey(listName)) {
                    List<Integer> list = listsNum.get(listName);
                    int sum = 0;
                    for (int num : list) {
                        sum += num;
                    }
                    System.out.println("Sum: " + sum);
                } else if (lists.containsKey(listName)) {
                    List<String> list = lists.get(listName);
                    try {
                        int sum = 0;
                        for (String s : list) {
                            sum += Integer.parseInt(s);
                        }
                        System.out.println("Sum: " + sum);
                    } catch (NumberFormatException e) {
                        System.out.println("List contains non-numeric values");
                    }
                } else {
                    System.out.println("List not found");
                }
    }

    // --- average ---
    public static void average(String input){
        String listName = input.replace("average ", "").trim();
                
                if (listsNum.containsKey(listName)) {
                    List<Integer> list = listsNum.get(listName);
                    
                    if (list.isEmpty()) {
                        System.out.println("Cannot calculate average of empty list");
                        return;
                    }
                    
                    int sum = 0;
                    for (int num : list) {
                        sum += num;
                    }
                    double avg = (double) sum / list.size();
                    System.out.println("Average: " + avg);
                } else if (lists.containsKey(listName)) {
                    List<String> list = lists.get(listName);
                    
                    if (list.isEmpty()) {
                        System.out.println("Cannot calculate average of empty list");
                        return;
                    }
                    
                    try {
                        int sum = 0;
                        for (String s : list) {
                            sum += Integer.parseInt(s);
                        }
                        double avg = (double) sum / list.size();
                        System.out.println("Average: " + avg);
                    } catch (NumberFormatException e) {
                        System.out.println("List contains non-numeric values");
                    }
                } else {
                    System.out.println("List not found");
                }
    }

    // --- list slicing with "to" ---
    public static void Listslicing (String input){
        String[] parts = input.split(" ");
                // Format: get listName start to end
                if (parts.length < 5 || !parts[3].equals("to")) {
                    System.out.println("Usage: get <list> <start> to <end>");
                    return;
                }
                
                String listName = parts[1];
                
                if (!lists.containsKey(listName) && !listsNum.containsKey(listName)) {
                    System.out.println("List '" + listName + "' not found");
                    return;
                }
                
                try {
                    int start = Integer.parseInt(parts[2]);
                    int end = Integer.parseInt(parts[4]);

                    if (input.contains(" step ")) {
                        // Parse step value
                        int step = Integer.parseInt(parts[6]);
                        List<String> targetList = lists.get(listName);
                        
                        List<String> steppedList = new ArrayList<>();
                        for (int i = start; i < end; i += step) {
                            steppedList.add(targetList.get(i));
                        }
                        System.out.println(steppedList);
                    }
                    
                    // Handle string lists
                    else if (lists.containsKey(listName)) {
                        List<String> targetList = lists.get(listName);
                        int size = targetList.size();
                        
                        // Handle negative indices
                        if (start < 0) start = size + start;
                        if (end < 0) end = size + end;
                        
                        // Validate range
                        if (start < 0 || end > size || start >= end) {
                            System.out.println("Invalid slice range");
                            return;
                        }
                        
                        List<String> sliced = targetList.subList(start, end);
                        System.out.println(sliced);
                    }
                    // Handle numeric lists
                    else if (listsNum.containsKey(listName)) {
                        List<Integer> targetList = listsNum.get(listName);
                        int size = targetList.size();
                        
                        // Handle negative indices
                        if (start < 0) start = size + start;
                        if (end < 0) end = size + end;
                        
                        // Validate range
                        if (start < 0 || end > size || start >= end) {
                            System.out.println("Invalid slice range");
                            return;
                        }
                        
                        List<Integer> sliced = targetList.subList(start, end);
                        System.out.println(sliced);
                    }
                    
                } catch (NumberFormatException e) {
                    System.out.println("Start and end must be numbers");
                }
    }

    // --- SLICE command (alternative syntax) ---
    public static void slice (String input){
        String[] parts = input.split(" ");
                if (parts.length < 4) {
                    System.out.println("Usage: slice <list> <start> <end>");
                    return;
                }
                
                String listName = parts[1];
                
                if (!lists.containsKey(listName) && !listsNum.containsKey(listName)) {
                    System.out.println("List '" + listName + "' not found");
                    return;
                }
                
                try {
                    int start = Integer.parseInt(parts[2]);
                    int end = Integer.parseInt(parts[3]);
                    
                    // Handle string lists
                    if (lists.containsKey(listName)) {
                        List<String> targetList = lists.get(listName);
                        int size = targetList.size();
                        
                        // Handle negative indices
                        if (start < 0) start = size + start;
                        if (end < 0) end = size + end;
                        
                        // Validate range
                        if (start < 0 || end > size || start >= end) {
                            System.out.println("Invalid slice range");
                            return;
                        }
                        
                        List<String> sliced = new ArrayList<>(targetList.subList(start, end));
                        System.out.println(sliced);
                    }
                    // Handle numeric lists
                    else if (listsNum.containsKey(listName)) {
                        List<Integer> targetList = listsNum.get(listName);
                        int size = targetList.size();
                        
                        // Handle negative indices
                        if (start < 0) start = size + start;
                        if (end < 0) end = size + end;
                        
                        // Validate range
                        if (start < 0 || end > size || start >= end) {
                            System.out.println("Invalid slice range");
                            return;
                        }
                        
                        List<Integer> sliced = new ArrayList<>(targetList.subList(start, end));
                        System.out.println(sliced);
                    }
                    
                } catch (NumberFormatException e) {
                    System.out.println("Start and end must be numbers");
                }
    }

    // --- store slice ---
    public static void stroeSlice(String input){
        // Format: set newList slice oldList start end
                String[] parts = input.split(" ");
                if (parts.length < 6) {
                    System.out.println("Usage: set <newList> slice <oldList> <start> <end>");
                    return;
                }
                
                String newListName = parts[1];
                String oldListName = parts[3];
                
                if (!lists.containsKey(oldListName) && !listsNum.containsKey(oldListName)) {
                    System.out.println("List '" + oldListName + "' not found");
                    return;
                }
                
                try {
                    int start = Integer.parseInt(parts[4]);
                    int end = Integer.parseInt(parts[5]);
                    
                    // Handle string lists
                    if (lists.containsKey(oldListName)) {
                        List<String> oldList = lists.get(oldListName);
                        int size = oldList.size();
                        
                        if (start < 0) start = size + start;
                        if (end < 0) end = size + end;
                        
                        if (start < 0 || end > size || start >= end) {
                            System.out.println("Invalid slice range");
                            return;
                        }
                        
                        List<String> newList = new ArrayList<>(oldList.subList(start, end));
                        lists.put(newListName, newList);
                        System.out.println("List '" + newListName + "' created: " + newList);
                    }
                    // Handle numeric lists
                    else if (listsNum.containsKey(oldListName)) {
                        List<Integer> oldList = listsNum.get(oldListName);
                        int size = oldList.size();
                        
                        if (start < 0) start = size + start;
                        if (end < 0) end = size + end;
                        
                        if (start < 0 || end > size || start >= end) {
                            System.out.println("Invalid slice range");
                            return;
                        }
                        
                        List<Integer> newList = new ArrayList<>(oldList.subList(start, end));
                        listsNum.put(newListName, newList);
                        System.out.println("List '" + newListName + "' created: " + newList);
                    }
                    
                } catch (NumberFormatException e) {
                    System.out.println("Start and end must be numbers");
                }
    }

    // --- make nested list ---
    public static void makeNested(String input){
        String[] parts = input.split(" ", 3);
                if (parts.length < 3) {
                    System.out.println("Usage: make nested <name>");
                    return;
                }
                
                String listName = parts[2];
                nestedLists.put(listName, new ArrayList<>());
                System.out.println("Nested list '" + listName + "' created!");
    }

    // --- add sublist ---
    public static void sublist(String input){
        // Format: add sublist nestedListName value1 value2 value3
                String[] parts = input.split(" ", 3);
                if (parts.length < 3) {
                    System.out.println("Usage: add sublist <nestedList> <value1> <value2> ...");
                    return;
                }
                
                String restOfInput = parts[2];
                String[] subParts = restOfInput.split(" ");
                
                if (subParts.length < 2) {
                    System.out.println("Usage: add sublist <nestedList> <value1> <value2> ...");
                    return;
                }
                
                String nestedListName = subParts[0];
                
                if (!nestedLists.containsKey(nestedListName)) {
                    System.out.println("Nested list '" + nestedListName + "' not found");
                    return;
                }
                
                // Create sublist from remaining values
                List<String> sublist = new ArrayList<>();
                for (int i = 1; i < subParts.length; i++) {
                    sublist.add(subParts[i]);
                }
                
                nestedLists.get(nestedListName).add(sublist);
                System.out.println("Added sublist " + sublist + " to '" + nestedListName + "'");
    }

    // --- add existing list to nest ---
    public static void addList(String input){
        // Format: nest existingList into nestedList
                String[] parts = input.split(" ");
                if (parts.length < 4 || !parts[2].equals("into")) {
                    System.out.println("Usage: nest <existingList> into <nestedList>");
                    return;
                }
                
                String existingListName = parts[1];
                String nestedListName = parts[3];
                
                if (!lists.containsKey(existingListName)) {
                    System.out.println("List '" + existingListName + "' not found");
                    return;
                }
                
                if (!nestedLists.containsKey(nestedListName)) {
                    System.out.println("Nested list '" + nestedListName + "' not found");
                    return;
                }
                
                // Create a copy of the existing list and add it
                List<String> copyList = new ArrayList<>(lists.get(existingListName));
                nestedLists.get(nestedListName).add(copyList);
                System.out.println("Added list '" + existingListName + "' to nested list '" + nestedListName + "'");
    }

    // --- get nested ---
    public static void getNested(String input){
        // Format: get nested listName row column
                String[] parts = input.split(" ");
                if (parts.length < 5) {
                    System.out.println("Usage: get nested <nestedList> <row> <column>");
                    return;
                }
                
                String nestedListName = parts[2];
                
                if (!nestedLists.containsKey(nestedListName)) {
                    System.out.println("Nested list '" + nestedListName + "' not found");
                    return;
                }
                
                try {
                    int row = Integer.parseInt(parts[3]);
                    int col = Integer.parseInt(parts[4]);
                    
                    List<List<String>> nestedList = nestedLists.get(nestedListName);
                    
                    if (row < 0 || row >= nestedList.size()) {
                        System.out.println("Row index out of bounds");
                        return;
                    }
                    
                    List<String> sublist = nestedList.get(row);
                    
                    if (col < 0 || col >= sublist.size()) {
                        System.out.println("Column index out of bounds");
                        return;
                    }
                    
                    System.out.println(sublist.get(col));
                    
                } catch (NumberFormatException e) {
                    System.out.println("Row and column must be numbers");
                }
    }

    // --- get row ---
    public static void getRow(String input){
        // Format: get row listName index
                String[] parts = input.split(" ");
                if (parts.length < 4) {
                    System.out.println("Usage: get row <nestedList> <rowIndex>");
                    return;
                }
                
                String nestedListName = parts[2];
                
                if (!nestedLists.containsKey(nestedListName)) {
                    System.out.println("Nested list '" + nestedListName + "' not found");
                    return;
                }
                
                try {
                    int row = Integer.parseInt(parts[3]);
                    List<List<String>> nestedList = nestedLists.get(nestedListName);
                    
                    if (row < 0 || row >= nestedList.size()) {
                        System.out.println("Row index out of bounds");
                        return;
                    }
                    
                    System.out.println(nestedList.get(row));
                    
                } catch (NumberFormatException e) {
                    System.out.println("Row index must be a number");
                }
    }

    // --- transpose ---
    public static void transpose(String input){
        String nestedListName = input.replace("transpose ", "").trim();
                
                if (!nestedLists.containsKey(nestedListName)) {
                    System.out.println("Nested list not found");
                    return;
                }
                
                List<List<String>> original = nestedLists.get(nestedListName);
                if (original.isEmpty()) {
                    System.out.println("[]");
                    return;
                }
                
                int rows = original.size();
                int cols = original.get(0).size();
                
                List<List<String>> transposed = new ArrayList<>();
                
                for (int col = 0; col < cols; col++) {
                    List<String> newRow = new ArrayList<>();
                    for (int row = 0; row < rows; row++) {
                        newRow.add(original.get(row).get(col));
                    }
                    transposed.add(newRow);
                }
                
                System.out.println(transposed);
    }

    // --- flatten ---
    public static void flatten(String input){
        String[] parts = input.split(" ");
                if (parts.length < 2) {
                    System.out.println("Usage: flatten <nestedList>");
                    return;
                }
                
                String nestedListName = parts[1];
                
                if (!nestedLists.containsKey(nestedListName)) {
                    System.out.println("Nested list '" + nestedListName + "' not found");
                    return;
                }
                
                List<String> flattened = new ArrayList<>();
                for (List<String> sublist : nestedLists.get(nestedListName)) {
                    flattened.addAll(sublist);
                }
                
                System.out.println(flattened);
    }

    // -- size nest ---
    public static void sizeNest (String input){
         String nestedListName = input.replace("size nested ", "").trim();
                
                if (!nestedLists.containsKey(nestedListName)) {
                    System.out.println("Nested list '" + nestedListName + "' not found");
                    return;
                }
                
                List<List<String>> nestedList = nestedLists.get(nestedListName);
                System.out.println("Rows: " + nestedList.size());
                
                if (!nestedList.isEmpty()) {
                    System.out.println("Columns in first row: " + nestedList.get(0).size());
                }
    }

    // --- remove from map ---
    public static void removeMap(String input){
        String[] parts = input.split(" from ");
                if (parts.length > 3){
                    System.out.println("usage: remove map <key> from <mapname>");
                }

                String key = parts[0].replace("remove map", "").trim();
                String mapName = parts[1].trim();
                
                if (maps.containsKey(mapName)) {
                    maps.get(mapName).remove(key);
                    System.out.println("Removed key '" + key + "' from " + mapName);
                }
    }

    // --- append to list ---
    public static void appendList (String input){
        String[] parts = input.split(" ", 3); 
                if (parts.length < 3) {
                    System.err.println("Invalid append syntax! Usage: append <value> <listName>");
                    return;
                }

                String varName = parts[2];
                String valueToAppend = parts[1];

                if (listsNum.containsKey(varName)) {
                    try {
                        int number = Integer.parseInt(valueToAppend);
                        listsNum.get(varName).add(number);
                        System.out.println("Appended " + number + " to " + varName);
                    } catch (NumberFormatException e) {
                        System.err.println("Invalid number: " + valueToAppend);
                    }
                } else if (lists.containsKey(varName)) {
                    lists.get(varName).add(valueToAppend);
                    System.out.println("Appended \"" + valueToAppend + "\" to " + varName);

                } else {
                    List<String> newList = new ArrayList<>();
                    newList.add(valueToAppend);
                    lists.put(varName, newList);
                    System.out.println("Created new list \"" + varName + "\" and added value: " + valueToAppend);
                }
    }

    // --- random ---
    public static void random (String input){
        String[] randomParts = input.split(" ", 4);
                if (randomParts.length < 4) {
                    System.out.println("Usage: random <name> <minrand> <maxrand>");
                    return;
                }
                int maxRand = getValue(randomParts[3],vars);
                int minRand = getValue(randomParts[2], vars);

                if (minRand >= maxRand) {
                    System.out.println("Invalid range: min must be less than max");
                    return;
                }
                String varName = randomParts[1];
                Random random = new Random();
                int varValue = random.nextInt(minRand, maxRand); 
                vars.put(varName, varValue);
                System.out.println("Variable '" + varName + "' set to " + varValue);
    }

    // --- for each ---
    public static void foreach (String input){
        String[] parts = input.split(" " ,3);
                String listName = parts[1];
                String cmd = parts[2];
                List<String> list = lists.get(listName);

                if (list != null){
                    for (String xStr : list) {
                        if (cmd.contains("print ")) {
                            System.out.println(xStr);
                        }

                    }
                } else {
                    System.out.println("List not found");
                }
    }

    // --- variable creator ---
    public static void letNset(String input){
        String[] parts = input.replaceFirst("let |set ", "").split(" ", 2);

                if (parts.length < 2) {
                    System.out.println("Invalid syntax. Use: set <name> <value>");
                    return;
                }

                String varName = parts[0].trim();
                String valueExpr = parts[1].trim();

                // ---random command---
                if (input.contains(" random")) {
                    parts = input.split(" ");
                    Random random = new Random();
                    int varValue;
                    varName = parts[1]; 

                    if (parts.length == 3) {
                        varValue = random.nextInt();
                    } else if (parts.length == 5) {
                        int min = Integer.parseInt(parts[3]);
                        int max = Integer.parseInt(parts[4]);

                        if (min >= max) {
                            System.out.println("Invalid range: min must be less than max");
                            return;
                        }

                        varValue = random.nextInt(min, max);
                    } else {
                        System.out.println("Invalid syntax. Use: set <var> random [min max]");
                        return;
                    }

                    vars.put(varName, varValue);
                    System.out.println("Variable '" + varName + "' set to " + varValue);
                    return;
                }

                // --- Check for boolean values ---
                if (valueExpr.equals("true") || valueExpr.equals("false")) {
                    boolean value = valueExpr.equals("true");
                    boolVars.put(varName, value);
                    System.out.println("Boolean '" + varName + "' set to " + value);
                    return;
                }
                
                // --- Check for expressions (contains operators or parentheses) ---
                if (valueExpr.contains("(") || valueExpr.contains("+") || 
                    valueExpr.contains("*") || valueExpr.contains("/") ||
                    (valueExpr.contains("-") && !valueExpr.startsWith("-"))) {
                    
                    try {
                        int result = evaluateExpression(valueExpr, vars);
                        vars.put(varName, result);
                        System.out.println("Variable '" + varName + "' set to " + result);
                        return;
                    } catch (Exception e) {
                        System.out.println("Error evaluating expression: " + e.getMessage());
                        return;
                    }
                }
                
                // --- Try to parse as number ---
                try {
                    int value = getValue(valueExpr, vars);
                    vars.put(varName, value);
                    System.out.println("Variable '" + varName + "' set to " + value);
                } catch (NumberFormatException e) {
                    // It's a string - store in stringVars
                    stringVars.put(varName, valueExpr);
                    System.out.println("String '" + varName + "' set to \"" + valueExpr + "\"");
                } catch (Exception e) {
                    System.out.println("Invalid value: " + valueExpr);
                }

    }

    // --- print and say command ---
    public static void printNsay(String input){
        String varName = input.replace("print ", "").replace("say ", "").trim();

                if (boolVars.containsKey(varName)) {
                    System.out.println(boolVars.get(varName));
                }
                else if (maps.containsKey(varName)){
                    System.out.println(maps.get(varName));
                }
                else if (lists.containsKey(varName)){
                    System.out.println(lists.get(varName));
                }
                else if (vars.containsKey(varName)) {
                    System.out.println(vars.get(varName));
                }
                else if (nestedLists.containsKey(varName)){
                    System.out.println(nestedLists.get(varName));
                }
                else if (stringVars.containsKey(varName)) {
                    System.out.println(stringVars.get(varName));
                }
                else {
                    // Try executing as command/function
                    String result = executeCommand(varName, vars, lists, listsNum, functions, functionParams);
                    if (result != null && !result.isEmpty()) {
                        System.out.println(result);
                    } else {
                        System.out.println(varName);
                    }
                }
    }

    // --- toggle ---
    public static void toggle (String input){
        String varName = input.replace("toggle ", "").trim();
                
                if (boolVars.containsKey(varName)) {
                    boolean newValue = !boolVars.get(varName);
                    boolVars.put(varName, newValue);
                    System.out.println(varName + " toggled to " + newValue);
                } else {
                    System.out.println("Boolean variable '" + varName + "' not found");
                }
    }

    // --- not ---
    public static void not (String input){
        String varName = input.replace("not ", "").trim();
                
                if (boolVars.containsKey(varName)) {
                    boolean result = !boolVars.get(varName);
                    System.out.println(result);
                } else {
                    System.out.println("Boolean variable '" + varName + "' not found");
                }
    }

    // --- boolean check ---
    public static void booleancheck(String input){
        String[] parts = input.split(" ");
                if (parts.length == 3) {
                    String varName = parts[1];
                    boolean checkValue = parts[2].equals("true");
                    
                    if (boolVars.containsKey(varName)) {
                        boolean result = boolVars.get(varName) == checkValue;
                        System.out.println(result);
                    } else {
                        System.out.println("Boolean variable '" + varName + "' not found");
                    }
                }
    }

    // --- what is statement ---
    public static void whatis(String input){
         String[] parts = input.split(" ");
                if (parts.length < 4){
                    String varName = input.replace("what is ", "").trim();
                    String listName = input.replace("what is ", "").trim();
                    if (maps.containsKey(listName)){
                        System.out.println(maps.get(listName));
                    }
                    else if (lists.containsKey(listName)){
                        System.out.println(lists.get(listName));
                    }
                    else if (vars.containsKey(varName)) {
                        System.out.println(vars.get(varName));
                    }
                    else if (nestedLists.containsKey(varName)){
                        System.out.println(nestedLists.get(varName));
                    }
                     else {
                        System.out.println(varName);
                    }
                    return;
                }
                if (parts.length >= 4){
                    input = input.replace("what is ", "").trim();
                    parts = input.split(" ");
                    if (parts.length == 3) {
                        String cmd = parts[0];
                        int a = getValue(parts[1], vars);
                        int b = getValue(parts[2], vars);

                        switch (cmd) {
                            case "add" -> System.out.println("Result: " + add(a, b));
                            case "+" -> System.out.println("Result: " + add(a, b));
                            case "subtract" -> System.out.println("Result: " + subtract(a, b));
                            case "-" -> System.out.println("Result: " + subtract(a, b));
                            case "multiply" -> System.out.println("Result: " + multiply(a, b));
                            case "*" -> System.out.println("Result: " + multiply(a, b));
                            case "divide" -> System.out.println("Result: " + divide(a, b));
                            case "/" -> System.out.println("Result: " + divide(a, b));
                            default -> System.out.println("Unknown command: " + cmd);
                        }
                        return;
                    }

                    System.out.println("Unknown command");
                }
    }

    // --- loop ---
    public static void loop(String input){
        String[] parts = input.split(" ", 3);
                if (parts.length < 3) {
                    System.out.println("Usage: loop <count> { commands } or loop <count> <command>");
                    return;
                }
                
                int times;
                try {
                    times = getValue(parts[1], vars);
                } catch (NumberFormatException e) {
                    System.out.println("Loop count must be a number");
                    return;
                }
                
                String loopContent = parts[2];
                
                // Check if it's a block loop
                if (loopContent.startsWith("{")) {
                    StringBuilder loopBody = new StringBuilder();
                    
                    // Multi-line loop
                    if (!loopContent.contains("}")) {
                        System.out.println("Loop started. Type '}' when done.");
                        while (true) {
                            String line = sc.nextLine().trim();
                            if (line.equals("}")) break;
                            loopBody.append(line).append(";");
                        }
                    } else {
                        // Single line with braces
                        loopBody.append(loopContent.substring(1, loopContent.lastIndexOf("}")));
                    }
                    
                    // Execute loop
                    boolean shouldBreak = false;
                    
                    for (int i = 0; i < times; i++) {
                        String[] commands = loopBody.toString().split(";");
                        
                        for (String cmd : commands) {
                            cmd = cmd.trim();
                            if (cmd.isEmpty()) continue;
                            
                            // Check for break
                            if (cmd.equals("break")) {
                                shouldBreak = true;
                                System.out.println("Loop broken at iteration " + (i + 1));
                                break;
                            }
                            
                            // Check for continue
                            if (cmd.equals("continue")) {
                                break; // Skip remaining commands in this iteration
                            }
                            
                            // Execute command (simplified)
                            String normalized = normalizeCommand(cmd);
                            
                            if (normalized.startsWith("print ")) {
                                String printVar = normalized.replace("print", "").trim();
                                if (vars.containsKey(printVar)) {
                                    System.out.println(vars.get(printVar));
                                } else {
                                    System.out.println(printVar);
                                }
                            } else if (normalized.startsWith("set ")) {
                                String[] setParts = normalized.replaceFirst("set ", "").split(" ", 2);
                                if (setParts.length >= 2) {
                                    try {
                                        int value = getValue(setParts[1], vars);
                                        vars.put(setParts[0], value);
                                    } catch (Exception e) {
                                        // Ignore errors in loop
                                    }
                                }
                            }
                        }
                        
                        if (shouldBreak) break;
                    }
                    
                } else {
                    // Original single-command loop (keep existing code)
                    for (int i = 0; i < times; i++) {
                        String newCmd = normalizeCommand(loopContent);
                        String[] subParts = newCmd.split(" ");
                        String subCmd = subParts[0];

                        if (subParts.length == 3) {
                            int a = getValue(subParts[1], vars);
                            int b = getValue(subParts[2], vars);

                            switch (subCmd) {
                                case "add" -> System.out.println("Result: " + add(a, b));
                                case "subtract" -> System.out.println("Result: " + subtract(a, b));
                                case "multiply" -> System.out.println("Result: " + multiply(a, b));
                                case "divide" -> System.out.println("Result: " + divide(a, b));
                            }
                        }
                    }
                }
    }

    // --- math commands ---
    public static void mathCommands(String input ){
        String[] parts = input.split(" ");
        String cmd = parts[0];
                int a = getValue(parts[1], vars);
                int b = getValue(parts[2], vars);

                switch (cmd) {
                    case "add" -> System.out.println("Result: " + add(a, b));
                    case "+" -> System.out.println("Result: " + add(a, b));
                    case "subtract" -> System.out.println("Result: " + subtract(a, b));
                    case "-" -> System.out.println("Result: " + subtract(a, b));
                    case "multiply" -> System.out.println("Result: " + multiply(a, b));
                    case "*" -> System.out.println("Result: " + multiply(a, b));
                    case "divide" -> System.out.println("Result: " + divide(a, b));
                    case "/" -> System.out.println("Result: " + divide(a, b));
                    default -> System.out.println("Unknown command: " + cmd);
                }
    }

    // --- main program ---
    public static void main(String[] args) {
        
        if (args.length > 0) {
                String scriptFile = args[0];
                executeScript(scriptFile, vars, lists, listsNum, functions, functionParams, stringVars, maps, nestedLists);
                return; // Exit after script execution
            }
            
        // Interactive mode
        System.out.println("Welcome to MyLang Programming Language");
        System.out.println("Type 'help' for commands or 'run <file>' to execute a script");

        while (running) {
            System.out.print("> ");
            String input = sc.nextLine().trim();
            int errorCount = 0;

            try {
                input = normalizeCommand(input);
            } catch (NumberFormatException e) {
                System.out.println(" Error: Invalid number format. Please use numeric values or defined variables.");
                continue;
            } catch (NullPointerException e) {
                System.out.println(" Error: You tried to use something that doesn't exist (maybe a missing variable or list).");
                continue;
            } catch (Exception e) {
                errorCount++;
                System.out.println("⚠ Unexpected error: " + e.getMessage());
                System.out.println("Total errors so far: " + errorCount);
                continue;
            }
            
            // --- RUN SCRIPT ---
            if (input.startsWith("run ") || input.startsWith("script ")) {
                String filename = input.replace("run ", "").replace("script ", "").trim();
                
                // Add .mylang extension if not present
                if (!filename.endsWith(".mylang")) {
                    filename += ".mylang";
                }
                
                executeScript(filename, vars, lists, listsNum, functions, 
                            functionParams, stringVars, maps, nestedLists);
                continue;
            }

            // --- PAUSE (for debugging scripts) ---
            if (input.equals("pause")) {
                System.out.println("Script paused. Press Enter to continue...");
                // In script mode, this would wait for input
                // In interactive mode, it just continues
                continue;
            }

            // --- ECHO (print without variable lookup) ---
            if (input.startsWith("echo ")) {
                String message = input.replace("echo ", "");
                System.out.println(message);
                continue;
            }

            // --- COMMENT (explicit comment command) ---
            if (input.startsWith("comment ") || input.startsWith("# ") || input.startsWith("// ")) {
                // Skip - it's a comment
                continue;
            }

            // Exit command
            if (input.equalsIgnoreCase("exit")) {
                System.out.println("Goodbye!");
                break;
            }

            if (input.equals("")){
                //System.out.println("");
                //System.out.println("no command writen !!");
                continue;
            }

            // --- HELP ---
            if (input.equals("help")) {
                System.out.println("Available Commands:");
                System.out.println("  set <name> <value>                 - Create or change a variable (number or string)");
                System.out.println("  let <name> <value>                 - Alias for 'set'");
                System.out.println("  print <name> / say <name>          - Print variable, list, map, or message");
                System.out.println("  what is <name>                     - Print value (same as print)");
                System.out.println("  what is <op> <a> <b>               - Quick math: add, subtract, multiply, divide");
                System.out.println();
                System.out.println("  add/subtract/multiply/divide <a> <b> / + - * /   - Perform arithmetic");
                System.out.println();
                System.out.println("  make list <name> <value1> <value2> ...     - Create string list");
                System.out.println("  make numlist <name> <n1> <n2> ...          - Create numeric list");
                System.out.println("  make map <name>                            - Create hashmap");
                System.out.println("  make nested <name>                         - Create nested list container");
                System.out.println();
                System.out.println("  append <value> <listname>                  - Add value to end of list");
                System.out.println("  remove <value> <listname>                  - Remove first occurrence of value");
                System.out.println("  remove index <index> <listname>            - Remove by index");
                System.out.println("  clear <listname>                           - Clear all elements in list");
                System.out.println("  length <listname>                          - Show list size");
                System.out.println();
                System.out.println("  get <list> <index>                         - Get element at index (0-based)");
                System.out.println("  get <list> <start> to <end>                - Slice list (inclusive start, exclusive end)");
                System.out.println("  get <list> <start> to <end> step <n>       - Slice with step");
                System.out.println("  slice <list> <start> <end>                 - Print slice");
                System.out.println("  set <newList> slice <oldList> <start> <end> - Create new list from slice");
                System.out.println();
                System.out.println("  sort <listname>                            - Sort ascending");
                System.out.println("  sort <listname> desc                       - Sort descending");
                System.out.println("  reverse <listname>                         - Reverse list order");
                System.out.println("  shuffle <listname>                         - Randomize order");
                System.out.println();
                System.out.println("  find <value> in <listname>                 - Get first index of value");
                System.out.println("  contains list <listname> <value>           - Check if value exists (true/false)");
                System.out.println("  count <value> in <listname>                - Count occurrences");
                System.out.println("  bsearch <value> in <listname>              - Binary search (requires sorted list)");
                System.out.println();
                System.out.println("  min <listname>                             - Minimum value");
                System.out.println("  max <listname>                             - Maximum value");
                System.out.println("  sum <listname>                             - Sum of numeric list");
                System.out.println("  average <listname>                         - Average of numeric list");
                System.out.println("  unique <listname>                          - Remove duplicates in-place");
                System.out.println();
                System.out.println("  put <mapname> <key> <value>                - Add/update key in map");
                System.out.println("  get map <mapname> <key>                    - Get value by key");
                System.out.println("  remove map <key> from <mapname>            - Remove key from map");
                System.out.println("  keys <mapname>                             - List all keys");
                System.out.println();
                System.out.println("  concat <word1> <word2> ...                 - Join strings (removes spaces)");
                System.out.println("  upper <text> / lower <text>                - Case conversion");
                System.out.println("  replace <text> <old> <new>                 - Replace substring");
                System.out.println("  substring <text> <start> <end>             - Extract substring");
                System.out.println("  contains word <text> <search>              - Check if substring exists");
                System.out.println();
                System.out.println("  add sublist <nestname> <v1> <v2> ...       - Add new row to nested list");
                System.out.println("  nest <listname> into <nestname>            - Add existing list as row");
                System.out.println("  get nested <nest> <row> <col>              - Get cell value");
                System.out.println("  get row <nest> <row>                       - Get entire row");
                System.out.println("  size nested <nestname>                     - Show rows and columns");
                System.out.println("  flatten <nestname>                         - Convert to flat list");
                System.out.println("  transpose <nestname>                       - Swap rows and columns");
                System.out.println();
                System.out.println("  random <var> <min> <max>                   - Assign random integer");
                System.out.println("  range <start> <end> [step]                 - Print number sequence");
                System.out.println("  set <list> range <start> <end> [step]      - Create numeric list from range");
                System.out.println("  repeat <value> <times>                     - Print repeated value");
                System.out.println();
                System.out.println("  loop <n> <command>                         - Repeat command n times");
                System.out.println("  if <a> <op> <b> <command>                  - Conditional: > < == != >= <=");
                System.out.println();
                System.out.println("  define <name> <p1> <p2> return <cmd>       - Define simple function");
                System.out.println("  define <name> { ... }                      - Multi-line function with {}");
                System.out.println("  <funcname> <arg1> <arg2>                   - Call function");
                System.out.println("  set <var> call <func> <args>               - Call function and store result");
                System.out.println();
                System.out.println("  save <name> to <file>.txt                  - Save variable/list/map");
                System.out.println("  load <file>.txt                            - Load from file");
                System.out.println("  run <script>.mylang                        - Run script file");
                System.out.println("  exit                                       - Quit program");
                System.out.println();
                System.out.println("Tips:");
                System.out.println("  Use words for numbers: 'five' --> 5");
                System.out.println("  Use quotes for strings with spaces: set msg \"hello world\"");
                System.out.println("  Lists can hold strings or numbers");
                System.out.println("  Maps store key-value pairs");
                System.out.println("  Nested lists are 2D tables");
                continue;
            }

            // --- IMPORT/USE MODULE ---
            if (input.startsWith("import ") || input.startsWith("use ")) { 
                ImportUse(input);
                continue;

            }

            // --- LIST MODULES ---
            if (input.equals("modules") || input.equals("list modules")) {
                ListModual(input);
                continue;
            }

            // --- Import with namespace ---
            if (input.startsWith("import ") && input.contains(" as ")) {
                ImportAs(input);
                continue;
            }

            // --- make HashMap ---
            if (input.startsWith("make map")){
                MakeMap(input);
                continue;
            }

            // --- put (add key to map)---
            if (input.startsWith("put ")){
                Put(input);
                continue;
            }

            // --- DEFINE FUNCTION ---
            if (input.startsWith("define ")) {
                DefineFunc(input);
                continue;
            }

            // --- FUNCTION CALLS ---
            String[] callParts = input.split(" ");
            String funcName = callParts[0];

            if (functions.containsKey(funcName)) {
                funcCall(input);
                continue;
            }

            // --- keys call ---
            if (input.startsWith("keys ")) {
                Keys(input);
                continue;
            }


            // --- CLEAR Command ---
            if (input.startsWith("clear ")) {
                clear(input);
                continue;
            }

            //---remove index ---
            if (input.startsWith("remove index ")){
                removeIndex(input);
                continue;
            }

            // --- SAVE Command ---
            if (input.startsWith("save ")) {
                save(input);
                continue;
            }

            // --- IF / ELIF / ELSE STATEMENT ---
            if (input.startsWith("if ")) {
                ifElse(input);
                continue;
            }

            // --- EVAL (evaluate expression and print result) ---
            if (input.startsWith("eval ")) {
                eval(input);
                continue;
            }

            // --- CALCULATE (alias for eval) ---
            if (input.startsWith("calc ")) {
                calc(input);
                continue;
            }

            // --- LOAD Command ---
            if (input.startsWith("load ")) {
                load(input);
                continue;
            }

            //--- set and call statement---
            if (input.startsWith("set ") && input.contains(" call ")) {
                setCall(input);
                continue;
            }

            // ---remove value---
            if (input.startsWith("remove list")){
                removeList(input);
                continue;
            }

            // --- list lenght ---
            if (input.startsWith("length list")) {
                listLenght(input);
                continue;
            }

            // --- STRING LENGTH ---
            if (input.startsWith("length ") && !lists.containsKey(input.replace("length ", "").trim())) {
                lengthString(input);
                continue;
            }

            // --- lists ---
            if (input.startsWith("make list ")){
                makeList(input);
                continue;
            }

            // --- CONCAT (join strings) ---
            if (input.startsWith("concat ")) {
               concat(input);
               continue;
            }

            // --- UPPER CASE ---
            if (input.startsWith("upper ")) {
                upper(input);
                continue;
            }

            // --- LOWER CASE ---
            if (input.startsWith("lower ")) {
                lower(input);
                continue;
            }

            // --- REPLACE TEXT ---
            if (input.startsWith("replace ")) {
                replace(input);
                continue;
            }

            // --- SUBSTRING (extract part) ---
            if (input.startsWith("substring ")) {
                substring(input);
                continue;
            }

            // --- SPLIT STRING TO LIST ---
            if (input.startsWith("split ")) {
                split(input);
                continue;
            }

            // --- WHILE LOOP ---
            if (input.startsWith("while ")) {
                While(input);
                continue;
            }

            // --- BREAK command (only valid inside loops) ---
            if (input.equals("break")) {
                System.out.println("'break' can only be used inside loops");
                continue;
            }

            // --- CONTINUE command (only valid inside loops) ---
            if (input.equals("continue")) {
                System.out.println("'continue' can only be used inside loops");
                continue;
            }

            // --- get list ---
            if (input.startsWith("get list")){
                getList(input);
                continue;
            }

            // --- get map ---
            if (input.startsWith("get map")) {
                getMap(input);
                continue;
            }
            
            // --- CREATE NUMERIC LIST ---
            if (input.startsWith("make numlist ")) {
                makeNumlist(input);
                continue;
            }

            // --- BINARY SEARCH (only works on sorted lists) ---
            if (input.startsWith("bsearch ") && input.contains(" in ")) {
                bsearch(input);
                continue;
            }

            // --- SORT LIST (ascending by default) ---
            if (input.startsWith("sort ")) {
                sort(input);
                continue;
            }

            // --- FIND element in list (returns index) ---
            if (input.startsWith("in list ")) {
                findList(input);
                continue;
            }

            // --- RANGE (create list of numbers) ---
            if (input.startsWith("range ")) {
                rangeList(input);
                continue;
            }

            // --- RANGE to variable ---
            if (input.startsWith("set ") && input.contains(" range ")) {
                rangeVar(input);
                continue;
            }

            // --- REPEAT (create list with repeated value) ---
            if (input.startsWith("repeat ")) {
                repeat(input);
                continue;
            }

            // --- FILTER (filter list by condition) ---
            if (input.startsWith("filter ") && input.contains(" where ")) {
                filter(input);
                continue;
            }

            // --- MAP (apply operation to each element) ---
            if (input.startsWith("map ") && input.contains(" ")) {
                map(input);
                continue;
            }

            // --- SAVE FILTERED RESULT ---
            if (input.startsWith("set ") && input.contains(" filter ") && input.contains(" where ")) {
                setFilter(input);
                continue;
            }

            // --- CONTAINS (check if list contains value) ---
            if (input.startsWith("contains list ")) {
                containsList(input);
                continue;
            }

            // --- CONTAINS STRING (check if text exists) ---
            if (input.startsWith("contains word")) {
                containsWord(input);
                continue;
            }

            // --- MIN (find minimum value) ---
            if (input.startsWith("min ")) {
                Min(input);
                continue;
            }

            // --- MAX (find maximum value) ---
            if (input.startsWith("max ")) {
                Max(input);
                continue;
            }

            // --- UNIQUE (remove duplicates) ---
            if (input.startsWith("unique ")) {
                unique(input);
                continue;
            }

            // --- UNION (combine two lists, no duplicates) ---
            if (input.startsWith("union ") && input.contains(" and ")) {
                union(input);
                continue;
            }

            // --- INTERSECT (find common elements) ---
            if (input.startsWith("intersect ") && input.contains(" and ")) {
                intersect(input);
                continue;
            }

            // --- DIFFERENCE (elements in list1 but not in list2) ---
            if (input.startsWith("difference ") && input.contains(" and ")) {
                difference(input);
                continue;
            }

            // --- SYMMETRIC DIFFERENCE ---
            if (input.startsWith("symdiff ") && input.contains(" and ")) {
                symdiff(input);
                continue;
            }

            // --- SHUFFLE (randomize order) ---
            if (input.startsWith("shuffle ")) {
                shuffle(input);
                continue;
            }

            // --- DISTINCT (count unique elements) ---
            if (input.startsWith("distinct ")) {
                distinct(input);
                continue;
            }

            // --- COPY list ---
            if (input.startsWith("copy ") && input.contains(" to ")) {
                copyList(input);
                continue;
            }

            // --- MERGE (combine two lists keeping duplicates) ---
            if (input.startsWith("merge ") && input.contains(" and ")) {
                merge(input);
                continue;
            }

            // --- REVERSE list ---
            if (input.startsWith("reverse ")) {
                reverse(input);
                continue;
            }

            // --- COUNT occurrences ---
            if (input.startsWith("count ") && input.contains(" in ")) {
                count(input);
                continue;
            }

            // --- SUM (add all numbers in list) ---
            if (input.startsWith("sum ")) {
                sum(input);
                continue;
            }

            // --- AVERAGE ---
            if (input.startsWith("average ")) {
                average(input);
                continue;
            }
            

            // --- LIST SLICING with "to" keyword ---
            if (input.startsWith("get ") && input.contains(" to ")) {
                Listslicing(input);
                continue;
            }

            // --- SLICE command (alternative syntax) ---
            if (input.startsWith("slice ")) {
                slice(input);
                continue;
            }

            // --- STORE SLICE to new list ---
            if (input.startsWith("set ") && input.contains(" slice ")) {
                stroeSlice(input);
                continue;
            }

            // --- CREATE NESTED LIST ---
            if (input.startsWith("make nested ")) {
                makeNested(input);
                continue;
            }

            // --- ADD SUBLIST to nested list ---
            if (input.startsWith("add sublist ")) {
                sublist(input);
                continue;
            }

            // --- ADD EXISTING LIST to nested list ---
            if (input.startsWith("nest ")) {
                addList(input);
                continue;
            }

            // --- GET NESTED ELEMENT (double index) ---
            if (input.startsWith("get nested ")) {
                getNested(input);
                continue;
            }

            // --- GET ENTIRE ROW ---
            if (input.startsWith("get row ")) {
                getRow(input);
                continue;
            }

            // --- FLATTEN nested list ---
            if (input.startsWith("flatten ")) {
                flatten(input);
                continue;
            }

            // --- SIZE of nested list (rows and columns) ---
            if (input.startsWith("size nested ")) {
                sizeNest(input);
                continue;
            }

            // --- TRANSPOSE (swap rows and columns) ---
            if (input.startsWith("transpose ")) {
                transpose(input);
                continue;
            }

            //--- remove from map ---
            if (input.startsWith("remove map ") && input.contains(" from ")) {
                removeMap(input);
                continue;
            }


            // --- append list ---
            if (input.startsWith("append ")){
                appendList(input);
                continue;
            }

            // --- RANDOM ---
            if (input.startsWith("random ")) {
                random(input);
                continue;
            }

            // --- for each ---
            if (input.startsWith("foreach ")){
                foreach(input);
                continue;
            }

            
            // --- Variable Creation/Update ---
            if (input.startsWith("let ") || input.startsWith("set ")) {
                letNset(input);
                continue;
            }

            // --- PRINT Command ---
            if (input.startsWith("print ") || input.startsWith("say ")) {
                printNsay(input);
                continue;
            }

            // --- TOGGLE (flip boolean) ---
            if (input.startsWith("toggle ")) {
                toggle(input);
                continue;
            }

            // --- NOT (logical negation) ---
            if (input.startsWith("not ")) {
                not(input);
                continue;
            }

            // --- IS TRUE / IS FALSE (check boolean) ---
            if (input.startsWith("is ") && (input.endsWith(" true") || input.endsWith(" false"))) {
                booleancheck(input);
                continue;
            }

            //--- what statment ---
            if (input.startsWith("what is ")){
                whatis(input);
                continue;
            }

            // --- ENHANCED LOOP with break/continue support ---
            if (input.startsWith("loop ")) {
                loop(input);
                continue;
            }

            // --- Math Commands ---
            String[] parts = input.split(" ");
            if (parts.length == 3) {
                mathCommands(input);
                continue;
            }

            System.out.println("Unknown command");

        }
        sc.close();
    }
}