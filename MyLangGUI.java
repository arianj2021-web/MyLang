import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;


public class MyLangGUI extends JFrame {
    
    // UI Components
    private JTextArea outputArea;
    private JTextField inputField;
    private JButton sendButton;
    private JButton clearButton;
    private JScrollPane scrollPane;
    
    // MyLang Engine - ALL your variables
    private HashMap<String, HashMap<String, String>> maps = new HashMap<>();
    private HashMap<String, Boolean> boolVars = new HashMap<>();
    private HashMap<String, List<List<String>>> nestedLists = new HashMap<>();
    private HashMap<String, String> stringVars = new HashMap<>();
    private HashMap<String, List<String>> functionParams = new HashMap<>();
    private HashMap<String, List<Integer>> listsNum = new HashMap<>();
    private HashMap<String, List<String>> lists = new HashMap<>();
    private HashMap<String, Integer> vars = new HashMap<>();
    private HashMap<String, String> functions = new HashMap<>();
    
    // Command history
    private ArrayList<String> commandHistory = new ArrayList<>();
    private int historyIndex = -1;
    
    // Output capture
    private ByteArrayOutputStream outputCapture = new ByteArrayOutputStream();
    
    public MyLangGUI() {
        initializeUI();
        displayWelcomeMessage();
    }
    
    private void initializeUI() {
        setTitle("MyLang Programming Language v1.0");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 750);
        setLocationRelativeTo(null);
        
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBackground(new Color(240, 242, 245));
        mainPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        
        JPanel headerPanel = createHeaderPanel();
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        
        outputArea = new JTextArea();
        outputArea.setEditable(false);
        outputArea.setFont(new Font("Consolas", Font.PLAIN, 14));
        outputArea.setLineWrap(true);
        outputArea.setWrapStyleWord(true);
        outputArea.setBackground(Color.WHITE);
        outputArea.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        scrollPane = new JScrollPane(outputArea);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200), 1));
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        
        JPanel inputPanel = createInputPanel();
        mainPanel.add(inputPanel, BorderLayout.SOUTH);
        
        add(mainPanel);
        inputField.requestFocusInWindow();
    }
    
    private JPanel createHeaderPanel() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(75, 85, 99));
        header.setBorder(new EmptyBorder(10, 15, 10, 15));
        
        JLabel titleLabel = new JLabel("MyLang Console");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(Color.WHITE);
        
        JLabel subtitleLabel = new JLabel("Type 'help' for commands");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subtitleLabel.setForeground(new Color(200, 200, 200));
        
        JPanel textPanel = new JPanel(new BorderLayout());
        textPanel.setOpaque(false);
        textPanel.add(titleLabel, BorderLayout.NORTH);
        textPanel.add(subtitleLabel, BorderLayout.SOUTH);
        
        header.add(textPanel, BorderLayout.WEST);
        
        clearButton = new JButton("Clear");
        clearButton.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        clearButton.setFocusPainted(false);
        clearButton.setBackground(new Color(100, 110, 125));
        clearButton.setForeground(Color.BLACK);
        clearButton.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));
        clearButton.addActionListener(e -> clearOutput());
        header.add(clearButton, BorderLayout.EAST);
        
        return header;
    }
    
    private JPanel createInputPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 0));
        panel.setBackground(new Color(240, 242, 245));
        panel.setBorder(new EmptyBorder(10, 0, 0, 0));
        
        inputField = new JTextField();
        inputField.setFont(new Font("Consolas", Font.PLAIN, 14));
        inputField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
            BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        
        inputField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    processCommand();
                } else if (e.getKeyCode() == KeyEvent.VK_UP) {
                    navigateHistory(-1);
                } else if (e.getKeyCode() == KeyEvent.VK_DOWN) {
                    navigateHistory(1);
                }
            }
        });
        
        sendButton = new JButton("Send");
        sendButton.setFont(new Font("Segoe UI", Font.BOLD, 13));
        sendButton.setFocusPainted(false);
        sendButton.setBackground(new Color(37, 99, 235));
        sendButton.setForeground(Color.BLACK);
        sendButton.setBorder(BorderFactory.createEmptyBorder(10, 25, 10, 25));
        sendButton.addActionListener(e -> processCommand());
        
        panel.add(inputField, BorderLayout.CENTER);
        panel.add(sendButton, BorderLayout.EAST);
        
        return panel;
    }
    
    private void displayWelcomeMessage() {
        appendOutput("╔════════════════════════════════════════╗\n");
        appendOutput("║   MyLang Programming Language v1.0     ║\n");
        appendOutput("╚════════════════════════════════════════╝\n\n");
        appendOutput("Welcome! Type 'help' for available commands.\n\n");
        appendDivider();
    }
    
    private void appendOutput(String text) {
        outputArea.append(text);
        outputArea.setCaretPosition(outputArea.getDocument().getLength());
    }
    
    private void appendUserCommand(String command) {
        appendOutput("\n> " + command + "\n");
    }
    
    private void appendDivider() {
        appendOutput("─────────────────────────────────────────\n");
    }
    
    private void clearOutput() {
        outputArea.setText("");
        displayWelcomeMessage();
    }
    
    private void navigateHistory(int direction) {
        if (commandHistory.isEmpty()) return;
        
        historyIndex += direction;
        
        if (historyIndex < 0) {
            historyIndex = 0;
        } else if (historyIndex >= commandHistory.size()) {
            historyIndex = commandHistory.size() - 1;
        }
        
        if (historyIndex >= 0 && historyIndex < commandHistory.size()) {
            inputField.setText(commandHistory.get(historyIndex));
        }
    }
    
    private void processCommand() {
        String input = inputField.getText().trim();
        
        if (input.isEmpty()) return;
        
        commandHistory.add(input);
        historyIndex = commandHistory.size();
        
        appendUserCommand(input);
        inputField.setText("");
        
        try {
            String output = executeMyLangCommand(input);
            if (output != null && !output.isEmpty()) {
                appendOutput(output + "\n");
            }
            appendDivider();
        } catch (Exception e) {
            appendOutput(" Error: " + e.getMessage() + "\n");
            appendDivider();
        }
    }
    
    private String executeMyLangCommand(String input) {
        input = normalizeCommand(input);
        
        // EXIT
        if (input.equalsIgnoreCase("exit")) {
            int choice = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to exit?", "Exit MyLang",
                JOptionPane.YES_NO_OPTION);
            if (choice == JOptionPane.YES_OPTION) System.exit(0);
            return "Exit cancelled.";
        }
        
        if (input.isEmpty()) return "";
        
        // HELP
        if (input.equals("help")) {
            return getHelpText();
        }
        
        // PRINT / SAY
        if (input.startsWith("print ") || input.startsWith("say ")) {
            return printNsay(input);
        }
        
        // SET / LET
        if (input.startsWith("set ") || input.startsWith("let ")) {
            return letNset(input);
        }
        
        // WHAT IS
        if (input.startsWith("what is ")) {
            input = input.replaceAll("what is ", "");
            return whatis(input);
        }
        
        // MAKE LIST
        if (input.startsWith("make list ")) {
            return makeList(input);
        }
        
        // MAKE NUMLIST
        if (input.startsWith("make numlist ")) {
            return makeNumlist(input);
        }
        
        // MAKE MAP
        if (input.startsWith("make map ")) {
            return MakeMap(input);
        }
        
        // MAKE NESTED
        if (input.startsWith("make nested ")) {
            return makeNested(input);
        }
        
        // PUT (map operation)
        if (input.startsWith("put ")) {
            return Put(input);
        }
        
        // GET LIST
        if (input.startsWith("get list ")) {
            return getList(input);
        }
        
        // GET MAP
        if (input.startsWith("get map ")) {
            return getMap(input);
        }
        
        // APPEND
        if (input.startsWith("append ")) {
            return appendList(input);
        }
        
        // REMOVE
        if (input.startsWith("remove index ")) {
            return removeIndex(input);
        }
        if (input.startsWith("remove map ")) {
            return removeMap(input);
        }
        if (input.startsWith("remove ")) {
            return removeList(input);
        }
        
        // CLEAR
        if (input.startsWith("clear ")) {
            return clear(input);
        }
        
        // LENGTH
        if (input.startsWith("length ")) {
            return listLength(input);
        }
        
        // SORT
        if (input.startsWith("sort ")) {
            return sort(input);
        }
        
        // REVERSE
        if (input.startsWith("reverse ")) {
            return reverse(input);
        }
        
        // SHUFFLE
        if (input.startsWith("shuffle ")) {
            return shuffle(input);
        }
        
        // MIN
        if (input.startsWith("min ")) {
            return Min(input);
        }
        
        // MAX
        if (input.startsWith("max ")) {
            return Max(input);
        }
        
        // SUM
        if (input.startsWith("sum ")) {
            return sum(input);
        }
        
        // AVERAGE
        if (input.startsWith("average ")) {
            return average(input);
        }
        
        // FILTER
        if (input.startsWith("filter ") && input.contains(" where ")) {
            return filter(input);
        }
        
        // UNIQUE
        if (input.startsWith("unique ")) {
            return unique(input);
        }
        
        // CONTAINS
        if (input.startsWith("contains list ")) {
            return containsList(input);
        }
        if (input.startsWith("contains word ")) {
            return containsWord(input);
        }
        
        // COUNT
        if (input.startsWith("count ") && input.contains(" in ")) {
            return count(input);
        }
        
        // RANGE
        if (input.startsWith("range ")) {
            return rangeList(input);
        }
        
        // STRING OPERATIONS
        if (input.startsWith("upper ")) {
            return upper(input);
        }
        if (input.startsWith("lower ")) {
            return lower(input);
        }
        if (input.startsWith("concat ")) {
            return concat(input);
        }
        if (input.startsWith("substring ")) {
            return substring(input);
        }
        if (input.startsWith("replace ")) {
            return replace(input);
        }
        if (input.startsWith("split ")) {
            return split(input);
        }
        if (input.startsWith("trim ")) {
            return trim(input);
        }
        if (input.startsWith("startswith ")) {
            return startswith(input);
        }
        if (input.startsWith("endswith ")) {
            return endswith(input);
        }
        if (input.startsWith("indexof ")) {
            return indexof(input);
        }
        if (input.startsWith("charat ")) {
            return charat(input);
        }
        
        // MATH OPERATIONS
        if (input.startsWith("power ")) {
            return power(input);
        }
        if (input.startsWith("sqrt ")) {
            return sqrt(input);
        }
        if (input.startsWith("abs ")) {
            return abs(input);
        }
        if (input.startsWith("floor ")) {
            return floor(input);
        }
        if (input.startsWith("ceil ") || input.startsWith("ceiling ")) {
            return ceil(input);
        }
        if (input.startsWith("round ")) {
            return round(input);
        }
        if (input.startsWith("eval ")) {
            return eval(input);
        }
        if (input.startsWith("calc ")) {
            return calc(input);
        }
        
        // RANDOM
        if (input.startsWith("random ")) {
            return random(input);
        }
        
        // TOGGLE / NOT
        if (input.startsWith("toggle ")) {
            return toggle(input);
        }
        if (input.startsWith("not ")) {
            return not(input);
        }
        
        // TYPEOF
        if (input.startsWith("typeof ")) {
            return typeofVar(input);
        }

        
        // KEYS
        if (input.startsWith("keys ")) {
            return Keys(input);
        }
        
        // DEFINE FUNCTION
        if (input.startsWith("define ")) {
            return DefineFunc(input);
        }
        
        // FUNCTION CALL - check if it's a defined function
        String[] callParts = input.split(" ");
        if (functions.containsKey(callParts[0])) {
            return funcCall(input);
        }
        
        // BASIC MATH (add, subtract, multiply, divide)
        String[] parts = input.split(" ");
        if (parts.length == 3 && isMathOperation(parts[0])) {
            return mathCommands(parts);
        }

        if (parts.length == 3 && isMathoperation(input)){
            return mathcommands(parts);
        }
        
        return " Unknown command: " + input + "\nType 'help' for available commands.";
    }
    
    
    private String normalizeCommand(String input) {
        input = input.toLowerCase().trim();
        input = convertWordsToNumbers(input);
        //input = input.replaceAll("\\bmake\\b", "set");
        input = input.replaceAll(" \\bto\\b ", " ");
        input = input.replaceAll(" \\bof\\b ", " ");
        input = input.replaceAll("\\bbetween\\b ", " ");
        input = input.replaceAll("\\bfrom\\b ", " ");
        input = input.replaceAll("\\bplus\\b", "add");
        input = input.replaceAll(" \\bwith\\b ", " ");
        input = input.replaceAll(" \\band\\b ", " ");
        input = input.replaceAll(" \\bbe\\b ", " ");
        input = input.replaceAll("  +", " "); // Multiple spaces to single
        return input.trim();
    }
    
    private String convertWordsToNumbers(String text) {
        return text.replaceAll("\\bzero\\b", "0")
                .replaceAll("\\bone\\b", "1")
                .replaceAll("\\btwo\\b", "2")
                .replaceAll("\\bthree\\b", "3")
                .replaceAll("\\bfour\\b", "4")
                .replaceAll("\\bfive\\b", "5")
                .replaceAll("\\bsix\\b", "6")
                .replaceAll("\\bseven\\b", "7")
                .replaceAll("\\beight\\b", "8")
                .replaceAll("\\bnine\\b", "9");
    }
    
    private int getValue(String token, HashMap<String, Integer> vars1) {
        if (vars.containsKey(token)) {
            return vars.get(token);
        } else {
            return Integer.parseInt(token);
        }
    }
    
    private boolean isMathOperation(String op) {
        return op.equals("add") || op.equals("subtract") || 
               op.equals("multiply") || op.equals("divide");
    }

    private boolean isMathoperation(String input) {
        return input.contains( "+") || input.contains( "-") ||
               input.contains( "/") || input.contains( "*");
    }
    
    // PRINT / SAY
    private String printNsay(String input) {
        String varName = input.replace("print ", "").replace("say ", "").trim();
        
        if (boolVars.containsKey(varName)) {
            return " " + boolVars.get(varName);
        } else if (maps.containsKey(varName)) {
            return " " + maps.get(varName);
        } else if (lists.containsKey(varName)) {
            return " " + lists.get(varName);
        } else if (vars.containsKey(varName)) {
            return " " + vars.get(varName);
        } else if (nestedLists.containsKey(varName)) {
            return " " + nestedLists.get(varName);
        } else if (stringVars.containsKey(varName)) {
            return " " + stringVars.get(varName);
        } else {
            return " " + varName;
        }
    }
    
    // SET / LET
    private String letNset(String input) {
        String[] parts = input.replaceFirst("let |set ", "").split(" ", 2);
        
        if (parts.length < 2) {
            return " Invalid syntax. Use: set <n> <value>";
        }
        
        String varName = parts[0].trim();
        String valueExpr = parts[1].trim();
        
        // Check for boolean
        if (valueExpr.equals("true") || valueExpr.equals("false")) {
            boolVars.put(varName, valueExpr.equals("true"));
            return " Boolean '" + varName + "' = " + valueExpr;
        }
        
        // Check for expression
        if (valueExpr.contains("(") || valueExpr.contains("+") || 
            valueExpr.contains("*") || valueExpr.contains("/") ||
            (valueExpr.contains("-") && !valueExpr.startsWith("-"))) {
            try {
                int result = evaluateExpression(valueExpr, vars);
                vars.put(varName, result);
                return " Variable '" + varName + "' = " + result;
            } catch (Exception e) {
                return " Expression error: " + e.getMessage();
            }
        }
        
        // Try as number
        try {
            int value = getValue(valueExpr, vars);
            vars.put(varName, value);
            return " Variable '" + varName + "' = " + value;
        } catch (NumberFormatException e) {
            stringVars.put(varName, valueExpr);
            return " String '" + varName + "' = \"" + valueExpr + "\"";
        }
    }
    
    // Expression evaluator (from your code)
    private int evaluateExpression(String expr, HashMap<String, Integer> vars) {
        expr = expr.trim();
        
        while (expr.startsWith("(") && expr.endsWith(")") && 
               matchingParenthesis(expr, 0) == expr.length() - 1) {
            expr = expr.substring(1, expr.length() - 1).trim();
        }
        
        if (!expr.contains("+") && !expr.contains("-") && 
            !expr.contains("*") && !expr.contains("/")) {
            return getValue(expr.trim(), vars);
        }
        
        int parenDepth = 0;
        int addSubIndex = -1;
        int mulDivIndex = -1;
        
        for (int i = expr.length() - 1; i >= 0; i--) {
            char c = expr.charAt(i);
            
            if (c == ')') parenDepth++;
            else if (c == '(') parenDepth--;
            else if (parenDepth == 0) {
                if ((c == '+' || c == '-') && i > 0) {
                    char prev = expr.charAt(i - 1);
                    if (prev != '(' && prev != '+' && prev != '-' && 
                        prev != '*' && prev != '/') {
                        addSubIndex = i;
                        break;
                    }
                } else if ((c == '*' || c == '/' || c == '%') && i > 0 && mulDivIndex == -1) {
                    mulDivIndex = i;
                }
            }
        }
        
        if (addSubIndex != -1) {
            String left = expr.substring(0, addSubIndex).trim();
            String right = expr.substring(addSubIndex + 1).trim();
            char op = expr.charAt(addSubIndex);
            
            int leftVal = evaluateExpression(left, vars);
            int rightVal = evaluateExpression(right, vars);
            
            return op == '+' ? leftVal + rightVal : leftVal - rightVal;
        }
        
        if (mulDivIndex != -1) {
            String left = expr.substring(0, mulDivIndex).trim();
            String right = expr.substring(mulDivIndex + 1).trim();
            char op = expr.charAt(mulDivIndex);
            
            int leftVal = evaluateExpression(left, vars);
            int rightVal = evaluateExpression(right, vars);
            
            switch (op) {
                case '*': return leftVal * rightVal;
                case '/':
                    if (rightVal == 0) throw new ArithmeticException("Division by zero");
                    return leftVal / rightVal;
                case '%':
                    if (rightVal == 0) throw new ArithmeticException("Modulo by zero");
                    return leftVal % rightVal;
            }
        }
        
        return getValue(expr, vars);
    }
    
    private int matchingParenthesis(String str, int openIndex) {
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
    
    // Continue 
    
    private String makeList(String input) {
        String[] parts = input.split(" ");
        if (parts.length < 4) {
            return " Usage: make list <n> <values...>";
        }
        
        String listName = parts[2].trim();
        List<String> listValues = new ArrayList<>();
        for (int i = 3; i < parts.length; i++) {
            listValues.add(parts[i]);
        }
        lists.put(listName, listValues);
        return " List '" + listName + "' created: " + listValues;
    }
    
    private String makeNumlist(String input) {
        String[] parts = input.split(" ");
        if (parts.length < 4) {
            return " Usage: make numlist <n> <num1> <num2> ...";
        }
        
        String listName = parts[2];
        List<Integer> numList = new ArrayList<>();
        
        try {
            for (int i = 3; i < parts.length; i++) {
                numList.add(Integer.parseInt(parts[i]));
            }
            listsNum.put(listName, numList);
            return " Numeric list '" + listName + "' created: " + numList;
        } catch (NumberFormatException e) {
            return " All values must be numbers";
        }
    }
    
    private String mathCommands(String[] parts) {
        try {
            int a = getValue(parts[1], vars);
            int b = getValue(parts[2], vars);
            int result = 0;
            
            switch (parts[0]) {
                case "add": result = a + b; break;
                case "subtract": result = a - b; break;
                case "multiply": result = a * b; break;
                case "divide":
                    if (b == 0) return " Cannot divide by zero";
                    result = a / b;
                    break;
            }
            
            return " Result: " + result;
        } catch (Exception e) {
            return " Math error: " + e.getMessage();
        }
    }

    private String mathcommands(String[] parts) {
        try {
            int a = getValue(parts[0], vars);
            int b = getValue(parts[2], vars);
            int result = 0;
            
            switch (parts[1]) {
                case "+": result = a + b; break;
                case "-": result = a - b; break;
                case "*": result = a * b; break;
                case "/":
                    if (b == 0) return " Cannot divide by zero";
                    result = a / b;
                    break;
            }
            
            return " Result: " + result;
        } catch (Exception e) {
            return " Math error: " + e.getMessage();
        }
    }
     
    private String whatis(String input) { 
        String varName = input.replace("what is ", "").trim();
        String [] parts = varName.split(" ");
        if (boolVars.containsKey(varName)) {
            return " " + boolVars.get(varName);
        }else if (parts.length == 3 && isMathoperation(input)){
            return mathcommands(parts);
        } else if (maps.containsKey(varName)) {
            return " " + maps.get(varName);
        } else if (lists.containsKey(varName)) {
            return " " + lists.get(varName);
        } else if (vars.containsKey(varName)) {
            return " " + vars.get(varName);
        } else if (nestedLists.containsKey(varName)) {
            return " " + nestedLists.get(varName);
        } else if (stringVars.containsKey(varName)) {
            return " " + stringVars.get(varName);
        }else {
            return " " + varName;
        }
    }
    private String MakeMap(String input) { 
        String[] parts = input.split(" ", 3);
        parts[2] = parts[2].replaceAll(" ", "").trim();
        String mapName = parts[2].trim();
        maps.put(mapName, new HashMap<>());
        return " Map '" + mapName + "' created!";
    }
    private String makeNested(String input) { 
        String[] parts = input.split(" ", 3);
        if (parts.length < 3) {
            return ("Usage: make nested <name>");
        }
                
        String listName = parts[2];
        nestedLists.put(listName, new ArrayList<>());
        return ("Nested list '" + listName + "' created!");
    }
    private String Put(String input) { 
        String[] parts = input.split(" ", 4);
        if (parts.length > 4){
        return ("Usage: put <map> <key> <value>");
        }

        String mapName = parts[1];
        String key = parts[2];
        String value = parts[3];

        if (!maps.containsKey(mapName)){
        return ("there is no map called " + mapName);
        }

        maps.get(mapName).put(key, value);
        return " Value added to map"; }
    private String getList(String input) { 
        String[] parts = input.split(" ");
        if (parts.length < 4){
            return ("Invalid get Usage: get list <listName> <index>");
        }
        String listName = parts[2];
        String getValue = parts[3];
        int index = Integer.parseInt(getValue);

        List<String> targetList = lists.get(listName);

        if (targetList != null && index > 0 && index <= targetList.size()){
            return ("Result: " + targetList.get(index - 1));
        }else{
            return ("Invalid list name or index");
        } 
    }
    private String getMap(String input) { 
        String[] parts = input.split(" ");

        if (parts.length < 4){
            return ("Invalid get Usage: get map <mapName> <key>");
        }

        String mapName = parts[2];
        String key = parts[3];
                
        if (maps.containsKey(mapName)) {
            String value = maps.get(mapName).get(key);
            if (value != null) {
                return(value);
            } else {
                return ("Key '" + key + "' not found in " + mapName);
            }
        }else {
            return "Map not found !!!";
        }
    }
    private String appendList(String input) { 
        String[] parts = input.split(" ", 3); 
        if (parts.length < 3) {
            return ("Invalid append syntax! Usage: append <value> <listName>");
        }

        String varName = parts[2];
        String valueToAppend = parts[1];

        if (listsNum.containsKey(varName)) {
            try {
                int number = Integer.parseInt(valueToAppend);
                listsNum.get(varName).add(number);
                return ("Appended " + number + " to " + varName);
            } catch (NumberFormatException e) {
                return ("Invalid number: " + valueToAppend);
            }
        } else if (lists.containsKey(varName)) {
            lists.get(varName).add(valueToAppend);
            return ("Appended \"" + valueToAppend + "\" to " + varName);

        } else {
            List<String> newList = new ArrayList<>();
            newList.add(valueToAppend);
            lists.put(varName, newList);
            return ("Created new list \"" + varName + "\" and added value: " + valueToAppend);
        } 
    }
    private String removeIndex(String input) { 
        String[] parts = input.split(" ", 4);
        if (parts.length < 4){
            return ("usage: remove index <indexvalue> <listname> ");
        } 
        String listName = parts[3].trim();               

        if (!lists.containsKey(listName)) {
            return ("List " + "'" +listName + "'" + " does not exist");
        }

        int indexValue = getValue(parts[2], vars);
        int listmax = lists.get(listName).size();

        if (indexValue >= listmax || indexValue < 0){
            return ("invalid index value");
        }
        String removedValue = (lists.get(listName)).get(indexValue);
        lists.get(listName).remove(indexValue);
        return ("value " + "'" + removedValue + "'" + " was removed from " + "'" + listName + "'");
    }
    private String removeList(String input) { 
        String[] parts = input.split(" ",4);
        if (parts.length < 3){
            return ("usage: remove <value> <liastname>");
        }

        String listName = parts[2];
        if (!lists.containsKey(listName)){
            return ("there is no list called " + "'" + listName + "'");
        }
        String removeName = parts[1];

        if (!lists.get(listName).contains(removeName)){
            return ("invalid remove value: there is no " + "'" + removeName + "'" + " in list " + "'" + listName + "'");
        }

        lists.get(listName).remove(removeName);
        return ("'" + removeName + "'" + " was removed from list " + "'" + listName + "'");
    }
    private String removeMap(String input) { 
        String[] parts = input.split(" ");
        if (parts.length > 4 || parts.length < 4){
            return ("usage: remove map <key> <mapname>");
        }

        String key = parts[2];
        String mapName = parts[3].trim();
                
        if (maps.containsKey(mapName)) {
            maps.get(mapName).remove(key);
            return ("Removed key '" + key + "' from " + mapName);
        }
        return "";
    }
    private String clear(String input) { 
        String listName = input.replace("clear", "").trim();

        if (lists.get(listName) == null) {
            return ("List not found: " + listName);
        }

        lists.get(listName).clear();
        return ("List '" + listName + "' cleared!");
    }
    private String listLength(String input) {
        String listName = input.replace("length", "").trim();

                if (lists.get(listName) == null) {
                    return ("List not found: " + listName);
                }

                return ("List '" + listName + "' has " + lists.get(listName).size() + " elements.");
    }
    private String sort(String input) {
        String[] parts = input.split(" ");
                if (parts.length < 2) {
                    return ("Usage: sort <list> [desc]");
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
                    
                    return ("Sorted " + listName + ": " + list);
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
                        
                        return ("Sorted " + listName + ": " + list);
                    } catch (NumberFormatException e) {
                        // Sort alphabetically
                        if (descending) {
                            list.sort(Collections.reverseOrder());
                        } else {
                            Collections.sort(list);
                        }
                        return ("Sorted " + listName + ": " + list);
                    }
                } else {
                    return ("List '" + listName + "' not found");
                }
    }
    private String reverse(String input) {
        String listName = input.replace("reverse ", "").trim();

        if (listsNum.containsKey(listName)) {
            List<Integer> list = listsNum.get(listName);
            Collections.reverse(list);
            return ("Reversed " + listName + ": " + list);
        } else if (lists.containsKey(listName)) {
            List<String> list = lists.get(listName);
            Collections.reverse(list);
            return ("Reversed " + listName + ": " + list);
        } else {
            return ("List '" + listName + "' not found");
        }
    }
    private String shuffle(String input) { 
        String listName = input.replace("shuffle ", "").trim();
                
        if (listsNum.containsKey(listName)) {
            List<Integer> list = listsNum.get(listName);
            Collections.shuffle(list);
            return ("Shuffled " + listName + ": " + list);
        } else if (lists.containsKey(listName)) {
            List<String> list = lists.get(listName);
            Collections.shuffle(list);
            return ("Shuffled " + listName + ": " + list);
        } else {
            return ("List '" + listName + "' not found");
        }
    }
    private String Min(String input) {
        String listName = input.replace("min ", "").trim();
                
                if (listsNum.containsKey(listName)) {
                    List<Integer> list = listsNum.get(listName);
                    
                    if (list.isEmpty()) {
                        return ("List is empty");
                    }
                    
                    int min = Collections.min(list);
                    return ("Minimum: " + min);
                } else if (lists.containsKey(listName)) {
                    List<String> list = lists.get(listName);
                    
                    if (list.isEmpty()) {
                        return ("List is empty");
                    }
                    
                    // Try numeric comparison
                    try {
                        List<Integer> nums = new ArrayList<>();
                        for (String s : list) {
                            nums.add(Integer.parseInt(s));
                        }
                        int min = Collections.min(nums);
                        return ("Minimum: " + min);
                    } catch (NumberFormatException e) {
                        // Alphabetic comparison
                        String min = Collections.min(list);
                        return ("Minimum: " + min);
                    }
                } else {
                    return ("List '" + listName + "' not found");
                }
    }
    private String Max(String input) { 
        String listName = input.replace("max ", "").trim();
                
                if (listsNum.containsKey(listName)) {
                    List<Integer> list = listsNum.get(listName);
                    
                    if (list.isEmpty()) {
                        return ("List is empty");
                    }
                    
                    int max = Collections.max(list);
                    return ("Maximum: " + max);
                } else if (lists.containsKey(listName)) {
                    List<String> list = lists.get(listName);
                    
                    if (list.isEmpty()) {
                        return ("List is empty");
                    }
                    
                    // Try numeric comparison
                    try {
                        List<Integer> nums = new ArrayList<>();
                        for (String s : list) {
                            nums.add(Integer.parseInt(s));
                        }
                        int max = Collections.max(nums);
                        return ("Maximum: " + max);
                    } catch (NumberFormatException e) {
                        // Alphabetic comparison
                        String max = Collections.max(list);
                        return ("Maximum: " + max);
                    }
                } else {
                    return ("List '" + listName + "' not found");
                }
    }
    private String sum(String input) { 
        String listName = input.replace("sum ", "").trim();
            
            if (listsNum.containsKey(listName)) {
                List<Integer> list = listsNum.get(listName);
                
                // ADD THIS CHECK
                if (list.isEmpty()) {
                    return ("Error: Cannot sum an empty list");
                }
                
                int sum = 0;
                for (int num : list) {
                    sum += num;
                }
                return ("Sum: " + sum);
            }else if (lists.containsKey(listName)) {
                List<String> list = lists.get(listName);
                try {
                    int sum = 0;
                    for (String s : list) {
                        sum += Integer.parseInt(s);
                    }
                    return ("Sum: " + sum);
                } catch (NumberFormatException e) {
                    return ("List contains non-numeric values");
                }
            } else {
                return ("List not found");
            }
    }
    private String average(String input) { 
         String listName = input.replace("average ", "").trim();
                
                if (listsNum.containsKey(listName)) {
                    List<Integer> list = listsNum.get(listName);
                    
                    if (list.isEmpty()) {
                        return ("Error: Cannot sum an empty list");
                    }
                    
                    int sum = 0;
                    for (int num : list) {
                        sum += num;
                    }
                    double avg = (double) sum / list.size();
                    return ("Average: " + avg);
                } else if (lists.containsKey(listName)) {
                    List<String> list = lists.get(listName);
                    
                    if (list.isEmpty()) {
                        return ("Cannot calculate average of empty list");
                    }
                    
                    try {
                        int sum = 0;
                        for (String s : list) {
                            sum += Integer.parseInt(s);
                        }
                        double avg = (double) sum / list.size();
                        return ("Average: " + avg);
                    } catch (NumberFormatException e) {
                        return ("List contains non-numeric values");
                    }
                } else {
                    return ("List not found");
                }
    }
    private String filter(String input) {
        String[] mainParts = input.split(" filter ");
                if (mainParts.length < 2) {
                    return ("Usage: set <newList> filter <oldList> where <condition>");
                }
                
                String newListName = mainParts[0].replace("set ", "").trim();
                String[] filterParts = mainParts[1].split(" where ");
                
                if (filterParts.length < 2) {
                    return ("Usage: set <newList> filter <oldList> where <condition>");
                }
                
                String oldListName = filterParts[0].trim();
                String condition = filterParts[1].trim();
                
                String[] condParts = condition.split(" ");
                if (condParts.length < 2) {
                    return ("Invalid condition");
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
                        return ("List '" + newListName + "' created: " + newList);
                    } catch (NumberFormatException e) {
                        return ("Comparison value must be a number");
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
                        return ("List '" + newListName + "' created: " + newList);
                    } catch (NumberFormatException e) {
                        return ("List must contain numbers");
                    }
                } else {
                    return ("List '" + oldListName + "' not found");
                }
    }
    private String unique(String input) { 
        String listName = input.replace("unique ", "").trim();
                
        if (listsNum.containsKey(listName)) {
            List<Integer> list = listsNum.get(listName);
            List<Integer> uniqueList = new ArrayList<>(new LinkedHashSet<>(list));
            listsNum.put(listName, uniqueList);
            return ("Unique " + listName + ": " + uniqueList);
        } else if (lists.containsKey(listName)) {
            List<String> list = lists.get(listName);
            List<String> uniqueList = new ArrayList<>(new LinkedHashSet<>(list));
            lists.put(listName, uniqueList);
            return ("Unique " + listName + ": " + uniqueList);
        } else {
            return ("List '" + listName + "' not found");
        }
    }
    private String containsList(String input) { 
        String[] parts = input.split(" ", 4);
                
                //System.out.println("DEBUG: parts.length = " + parts.length);
                for (int i = 0; i < parts.length; i++) {
                    //System.out.println("DEBUG: parts[" + i + "] = '" + parts[i] + "'");
                }
                
                if (parts.length < 4) {
                    return ("Usage: contains list <listName> <value>");
                }
                
                String listName = parts[2];
                String value = parts[3];
                
                
                // Check numeric lists
                if (listsNum.containsKey(listName)) {
                    try {
                        int numValue = Integer.parseInt(value);
                        boolean found = listsNum.get(listName).contains(numValue);
                        String foundS = String.valueOf(found);
                        return (foundS);
                    } catch (NumberFormatException e) {
                        return "false";
                    }
                }
                // Check string lists
                else if (lists.containsKey(listName)) {
                    boolean found = lists.get(listName).contains(value);
                    String foundS = String.valueOf(found);
                    return foundS;
                } else {
                    return ("List not found");
                }
    }
    private String containsWord(String input) { 
        String[] parts = input.split(" ", 4);
        if (parts.length < 4) {
            return ("Usage: contains <string> <search>");
        }
                
        String text = parts[2];
        String search = parts[3];
                
        if (stringVars.containsKey(text)) {
            text = stringVars.get(text);
        }
                
        boolean found = text.contains(search);
        String foundS = String.valueOf(found);
        return (foundS);
    }
    private String count(String input) { 
        String[] parts = input.split(" in ");
                if (parts.length < 2) {
                    return ("Usage: count <value> in <list>");
                }
                
                String value = parts[0].replace("count ", "").trim();
                String listName = parts[1].trim();
                
                if (listsNum.containsKey(listName)) {
                    try {
                        int numValue = Integer.parseInt(value);
                        int count = Collections.frequency(listsNum.get(listName), numValue);
                        return ("Count: " + count);
                    } catch (NumberFormatException e) {
                        return ("Value must be a number");
                    }
                } else if (lists.containsKey(listName)) {
                    int count = Collections.frequency(lists.get(listName), value);
                    return ("Count: " + count);
                } else {
                    return ("List not found");
                }
    }
    private String rangeList(String input) { 
        String[] parts = input.split(" ");
                if (parts.length < 3) {
                    return ("Usage: range <start> <end> [step]");
                }
                
                try {
                    int start = Integer.parseInt(parts[1]);
                    int end = Integer.parseInt(parts[2]);
                    int step = (parts.length > 3) ? Integer.parseInt(parts[3]) : 1;
                    
                    if (step == 0) {
                        return ("Step cannot be zero");
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
                    
                    return ("" + rangeList);
                } catch (NumberFormatException e) {
                    return ("Start, end, and step must be numbers");
                }
    }
    private String upper(String input) { 
        String text = input.replace("upper ", "").trim();
                
                if (stringVars.containsKey(text)) {
                    text = stringVars.get(text);
                }
                
                return (text.toUpperCase());
    }
    private String lower(String input) { 
         String text = input.replace("lower ", "").trim();
                
                if (stringVars.containsKey(text)) {
                    text = stringVars.get(text);
                }
                
                return (text.toLowerCase());
    }
    private String concat(String input) { 
        String[] parts = input.split(" ", 2);
                if (parts.length < 2) {
                    return ("Usage: concat <word1> <word2> ...");
                }
                
                String result = parts[1].replace(" ", "");
                return (result);
    }
    private String substring(String input) {
        String[] parts = input.split(" ");
                if (parts.length < 4) {
                    return ("Usage: substring <string> <start> <end>");
                }
                
                String text = parts[1];
                int start = Integer.parseInt(parts[2]);
                int end = Integer.parseInt(parts[3]);
                
                if (stringVars.containsKey(text)) {
                    text = stringVars.get(text);
                }
                
                if (start < 0 || end > text.length() || start >= end) {
                    return ("Invalid substring range");
                }
                
                return (text.substring(start, end));
    }
    private String replace(String input) {
        String[] parts = input.split(" ", 4);
                if (parts.length < 4) {
                    return ("Usage: replace <string> <old> <new>");
                }
                
                String text = parts[1];
                String oldText = parts[2];
                String newText = parts[3];
                
                if (stringVars.containsKey(text)) {
                    text = stringVars.get(text);
                }
                
                String result = text.replace(oldText, newText);
                return  result;
    }
    private String split(String input) {
        String[] parts = input.split(" ", 3);
                if (parts.length < 3) {
                    return ("Usage: split <string> <delimiter>");
                }
                
                String text = parts[1];
                String delimiter = parts[2];
                
                if (stringVars.containsKey(text)) {
                    text = stringVars.get(text);
                }
                
                String[] splitResult = text.split(delimiter);
                List<String> resultList = new ArrayList<>(Arrays.asList(splitResult));
                
                return ("" + resultList);
    }
    private String trim(String input) {
        String text = input.substring(5).trim(); // Get everything after "trim "
                    
        // Remove quotes if present
        if (text.startsWith("\"") && text.endsWith("\"")) {
            text = text.substring(1, text.length() - 1);
        }
                    
        // Check if it's a variable
        if (stringVars.containsKey(text)) {
            text = stringVars.get(text);
        }
                    
        String trimmed = text.trim();
        return (trimmed);
    }
    private String startswith(String input) { 
        String rest = input.substring(11).trim();
                    String[] parts = rest.split(" ", 2);
                    
                    if (parts.length < 2) {
                        return ("Usage: startswith <string> <prefix>");
                    }
                    
                    String text = parts[0];
                    String prefix = parts[1];
                    
                    // Remove quotes if present
                    if (text.startsWith("\"") && text.endsWith("\"")) {
                        text = text.substring(1, text.length() - 1);
                    }
                    if (prefix.startsWith("\"") && prefix.endsWith("\"")) {
                        prefix = prefix.substring(1, prefix.length() - 1);
                    }
                    
                    // Check if it's a variable
                    if (stringVars.containsKey(text)) {
                        text = stringVars.get(text);
                    }
                    
                    boolean result = text.startsWith(prefix);
                    return ("" + result);
    }
    private String endswith(String input) { 
        String[] parts = input.split(" ", 3);
                    if (parts.length < 3) {
                        return ("Usage: endswith <string> <suffix>");
                    }
                    
                    String text = parts[1];
                    String suffix = parts[2];
                    
                    if (stringVars.containsKey(text)) {
                        text = stringVars.get(text);
                    }
                    
                    boolean result = text.endsWith(suffix);
                    return ("" + result);
    }
    private String indexof(String input) { 
        String[] parts = input.split(" ", 3);
        if (parts.length < 3) {
            return ("Usage: indexof <string> <search>");
        }
                    
        String text = parts[1];
        String search = parts[2];
                    
        if (stringVars.containsKey(text)) {
            text = stringVars.get(text);
        }
                    
        int index = text.indexOf(search);
        return ("" + index);
    }
    private String charat(String input) { 
        String[] parts = input.split(" ", 3);
                    if (parts.length < 3) {
                        return ("Usage: charat <string> <index>");
                    }
                    
                    String text = parts[1];
                    
                    if (stringVars.containsKey(text)) {
                        text = stringVars.get(text);
                    }
                    
                    try {
                        int index = Integer.parseInt(parts[2]);
                        
                        if (index < 0 || index >= text.length()) {
                            return ("Index out of range");
                        }
                        
                        char ch = text.charAt(index);
                        return ("" + ch);
                    } catch (NumberFormatException e) {
                        return ("Index must be a number");
                    }
    }
    private String power(String input) { 
        String[] parts = input.split(" ");
                    if (parts.length < 3) {
                        return ("Usage: power <base> <exponent>");
                    }
                    try {
                        int base = getValue(parts[1], vars);
                        int exp = getValue(parts[2], vars);
                        return ("Result: " + Math.pow(base, exp));
                    } catch (Exception e) {
                        return ("Error: " + e.getMessage());
                    }
    }
    public static String sqrt(int num) {
        if (num < 0) {
            return ("Cannot take square root of negative number");
        }
        return ("" + Math.sqrt(num));
    }
    private String sqrt(String input) { 
        String num = input.replace("sqrt", "").trim();
                    try {
                        int value = getValue(num, vars);
                        return ("Result: " + sqrt(value));
                    } catch (Exception e) {
                        return ("Error: " + e.getMessage());
                    }
    }
    private String abs(String input) {
        String num = input.replace("abs", "").trim();
                    try {
                        int value = getValue(num, vars);
                        return ("Result: " + Math.abs(value));
                    } catch (Exception e) {
                        return ("Error: " + e.getMessage());
                    }
    }
    private String floor(String input) { 
        String[] parts = input.split(" ");
                    if (parts.length < 2) {
                        return ("Usage: floor <number>");
                    }
                    
                    try {
                        double value = Double.parseDouble(parts[1]);
                        return ("Result: " + Math.floor(value));
                    } catch (NumberFormatException e) {
                        return ("Invalid number");
                    }
    }
    private String ceil(String input) { 
        String num = input.replace("ceil ", "").replace("ceiling ", "").trim();
                    
                    try {
                        double value = Double.parseDouble(num);
                        return ("Result: " + Math.ceil(value));
                    } catch (NumberFormatException e) {
                        return ("Invalid number");
                    }
    }
    private String round(String input) { 
        String num = input.replace("round ", "").trim();
                    
                    try {
                        double value = Double.parseDouble(num);
                        return ("Result: " + Math.round(value));
                    } catch (NumberFormatException e) {
                        return ("Invalid number");
                    }
    }
    private String eval(String input) { 
        String expr = input.replace("eval ", "").trim();
                
                try {
                    int result = evaluateExpression(expr, vars);
                    return ("Result: " + result);
                } catch (Exception e) {
                    return ("Error: " + e.getMessage());
                }
    }
    private String calc(String input) { 
        String expr = input.replace("calc ", "").trim();
                
                try {
                    int result = evaluateExpression(expr, vars);
                    return ("Result: " + result);
                } catch (Exception e) {
                    return ("Error: " + e.getMessage());
                }
    }
    private String random(String input) { 
        String[] randomParts = input.split(" ", 4);
                if (randomParts.length < 4) {
                    return ("Usage: random <name> <minrand> <maxrand>");
                }
                int maxRand = getValue(randomParts[3],vars);
                int minRand = getValue(randomParts[2], vars);

                if (minRand >= maxRand) {
                    return ("Invalid range: min must be less than max");
                }
                String varName = randomParts[1];
                Random random = new Random();
                int varValue = random.nextInt(minRand, maxRand); 
                vars.put(varName, varValue);
                return ("Variable '" + varName + "' set to " + varValue);
    }
    private String toggle(String input) { 
        String varName = input.replace("toggle ", "").trim();
                
                if (boolVars.containsKey(varName)) {
                    boolean newValue = !boolVars.get(varName);
                    boolVars.put(varName, newValue);
                    return (varName + " toggled to " + newValue);
                } else {
                    return ("Boolean variable '" + varName + "' not found");
                }
    }
    private String not(String input) {
        String varName = input.replace("not ", "").trim();
                
                if (boolVars.containsKey(varName)) {
                    boolean result = !boolVars.get(varName);
                    return ("" + result);
                } else {
                    return ("Boolean variable '" + varName + "' not found");
                }
    }
    private String typeofVar(String input) {
        String varName = input.replace("typeof", "").trim();
                
                if (vars.containsKey(varName)) {
                    return ("int");
                } else if (stringVars.containsKey(varName)) {
                    return ("string");
                } else if (boolVars.containsKey(varName)) {
                    return ("boolean");
                } else if (lists.containsKey(varName)) {
                    return ("list");
                } else if (maps.containsKey(varName)) {
                    return ("map");
                } else {
                    return ("undefined");
                }
    }
    private String Keys(String input) { 
        String mapName = input.replace("keys ", "").trim();
                if (maps.containsKey(mapName)) {
                    return ("" + maps.get(mapName).keySet());
                } else {
                    return ("Map not found");
                }
    }
    private String DefineFunc(String input) {
        String rest = input.substring(7).trim();
        
        // Check if this is brace-style definition (single-line with braces only)
        if (rest.contains("{")) {
            String[] parts = rest.split(" ", 2);
            if (parts.length < 2) {
                return "Usage: define <name> <params> {body} or define <name> { commands }";
            }
            
            String funcName = parts[0];
            String funcRest = parts[1];
            
            int braceStart = funcRest.indexOf("{");
            int braceEnd = funcRest.lastIndexOf("}");
            
            // Single-line definition with braces
            if (braceStart != -1 && braceEnd != -1) {
                String paramSection = funcRest.substring(0, braceStart).trim();
                String bodySection = funcRest.substring(braceStart + 1, braceEnd).trim();
                
                List<String> params = new ArrayList<>();
                if (!paramSection.isEmpty()) {
                    params = Arrays.asList(paramSection.split("\\s+"));
                }
                
                functionParams.put(funcName, params);
                functions.put(funcName, bodySection);
                return "Function '" + funcName + "' defined with params " + params;
            } else {
                return "Invalid function syntax. Use: define <name> <params> {body}";
            }
        }
        // Return-style definition (no braces)
        else {
            String[] tokens = rest.split(" ");
            
            if (tokens.length < 3) {
                return "Usage: define <name> <param1> <param2> return <command>";
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
            return "Function '" + funcName + "' defined with parameters " + params + "\nFunction body: " + funcBody;
        }
    }

    private String funcCall(String input) { 
        String[] callParts = input.split(" ");
        String funcName = callParts[0];
        
        List<String> params = functionParams.get(funcName);
        String funcBody = functions.get(funcName);
        
        if (funcBody == null) {
            return "Function '" + funcName + "' not found";
        }

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

        // Handle return or execute full body
        if (funcBody.startsWith("return ")) {
            String returnExpr = funcBody.substring(7).trim();
            String[] returnParts = returnExpr.split(" ");

            if (returnParts.length > 0) {
                String result = "";
                switch (returnParts[0]) {
                    case "add":
                        if (returnParts.length == 3) {
                            int a = getValue(returnParts[1], vars);
                            int b = getValue(returnParts[2], vars);
                            result = String.valueOf(a + b);
                        } else {
                            result = returnExpr;
                        }
                        break;
                    case "multiply":
                        if (returnParts.length == 3) {
                            int a = getValue(returnParts[1], vars);
                            int b = getValue(returnParts[2], vars);
                            result = String.valueOf(a * b);
                        } else {
                            result = returnExpr;
                        }
                        break;
                    case "divide":
                        if (returnParts.length == 3) {
                            int a = getValue(returnParts[1], vars);
                            int b = getValue(returnParts[2], vars);
                            if (b == 0) {
                                result = "Error: Division by zero";
                            } else {
                                result = String.valueOf(a / b);
                            }
                        } else {
                            result = returnExpr;
                        }
                        break;
                    case "subtract":
                        if (returnParts.length == 3) {
                            int a = getValue(returnParts[1], vars);
                            int b = getValue(returnParts[2], vars);
                            result = String.valueOf(a - b);
                        } else {
                            result = returnExpr;
                        }
                        break;
                    case "print":
                    case "say":
                        if (returnParts.length > 1) {
                            result = returnParts[1];
                        }
                        break;
                    default:
                        result = returnExpr;
                        break;
                }
                return "Returned: " + result;
            }
            return "Returned: ";
        } else {
            // Execute function body - split by semicolons and execute each command
            String[] lines = funcBody.split("[;]+");
            StringBuilder output = new StringBuilder();
            
            for (String line : lines) {
                line = line.trim();
                if (!line.isEmpty()) {
                    String cmdResult = executeMyLangCommand(line);
                    if (cmdResult != null && !cmdResult.isEmpty()) {
                        output.append(cmdResult).append("\n");
                    }
                }
            }
            
            return output.toString().trim();
        }
    }
    
    private String getHelpText() {
        return """
            MyLang Commands Reference:
            
            ═══════════════════════════════════════════
            VARIABLES & BASIC I/O
            ═══════════════════════════════════════════
            set <n> <value>        - Create/update variable
            let <n> <value>        - Alias for set
            print <n>              - Display value
            say <n>                - Alias for print
            what is <n>            - Query variable value
            typeof <n>             - Get variable type
            
            ═══════════════════════════════════════════
            MATH OPERATIONS
            ═══════════════════════════════════════════
            add <a> <b>            - Addition
            subtract <a> <b>       - Subtraction
            multiply <a> <b>       - Multiplication
            divide <a> <b>         - Division
            <a> + <b>              - Addition operator
            <a> - <b>              - Subtraction operator
            <a> * <b>              - Multiplication operator
            <a> / <b>              - Division operator
            power <base> <exp>     - Exponentiation
            sqrt <num>             - Square root
            abs <num>              - Absolute value
            floor <num>            - Round down
            ceil <num>             - Round up
            round <num>            - Round to nearest
            eval <expression>      - Evaluate expression
            calc <expression>      - Calculate expression
            
            ═══════════════════════════════════════════
            LISTS (STRING)
            ═══════════════════════════════════════════
            make list <n> <vals>   - Create string list
            get list <list> <idx>  - Get element at index
            append <val> <list>    - Add to end of list
            remove <val> <list>    - Remove value
            remove index <i> <list>- Remove by index
            clear <list>           - Empty the list
            length <list>          - Get list size
            sort <list> [desc]     - Sort list
            reverse <list>         - Reverse order
            shuffle <list>         - Randomize order
            unique <list>          - Remove duplicates
            contains list <list> <val> - Check if contains
            count <val> in <list>  - Count occurrences
            
            ═══════════════════════════════════════════
            NUMERIC LISTS
            ═══════════════════════════════════════════
            make numlist <n> <nums>- Create number list
            min <list>             - Find minimum
            max <list>             - Find maximum
            sum <list>             - Sum all values
            average <list>         - Calculate average
            range <start> <end> [step] - Generate range
            set <new> filter <old> where <condition>
                                    - Filter by condition
            
            ═══════════════════════════════════════════
            MAPS (KEY-VALUE)
            ═══════════════════════════════════════════
            make map <n>           - Create map
            put <map> <key> <val>  - Add/update entry
            get map <map> <key>    - Get value by key
            remove map <key> <map> - Remove entry
            keys <map>             - List all keys
            
            ═══════════════════════════════════════════
            STRING OPERATIONS
            ═══════════════════════════════════════════
            upper <text>           - Convert to uppercase
            lower <text>           - Convert to lowercase
            concat <w1> <w2> ...   - Concatenate strings
            substring <str> <s> <e>- Extract substring
            replace <str> <old> <new> - Replace text
            split <str> <delim>    - Split into list
            trim <str>             - Remove whitespace
            startswith <str> <pre> - Check prefix
            endswith <str> <suf>   - Check suffix
            indexof <str> <search> - Find position
            charat <str> <idx>     - Get character at index
            contains word <str> <search> - Check contains
            
            ═══════════════════════════════════════════
            FUNCTIONS
            ═══════════════════════════════════════════
            define <n> <params> {body}
                                    - Define function with body
            define <n> <params> return <expr>
                                    - Define function with return
            <funcname> <args>      - Call function
            
            Examples:
                define square x return multiply x x
                square 5
                
                define greet name {print hello; print name}
                greet Alice
            
            ═══════════════════════════════════════════
            BOOLEAN OPERATIONS
            ═══════════════════════════════════════════
            set <n> true/false     - Create boolean
            toggle <n>             - Flip boolean value
            not <n>                - Logical NOT
            
            ═══════════════════════════════════════════
            RANDOM & UTILITIES
            ═══════════════════════════════════════════
            random <n> <min> <max> - Random integer
            make nested <n>        - Create nested list
            
            ═══════════════════════════════════════════
            SYSTEM
            ═══════════════════════════════════════════
            help                   - Show this help
            exit                   - Quit application
            
             Tips:
            - Use arrow keys (↑/↓) for command history
            - Variables are case-sensitive
            - List indices start at 1
            - Use 'what is' for math: what is 5 + 3
            """;
    }
    
    // --- MAIN METHOD ---
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        SwingUtilities.invokeLater(() -> {
            MyLangGUI gui = new MyLangGUI();
            gui.setVisible(true);
        });
    }
}