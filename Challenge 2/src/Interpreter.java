import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

//custom UnknownCommandException
class UnknownCommandException extends Exception {
    public UnknownCommandException(String message) {
        super(message);
    }

    public UnknownCommandException(String message, Throwable cause) {
        super(message, cause);
    }
}

public class Interpreter {
    public static final Map<String, Integer> vars = new HashMap<>();

    public static void processVar(int label, String nameVar){
        int value = 0;
        switch (label) {
            case 0:     //clear
                vars.put(nameVar, value);
//                System.out.println(nameVar + " = 0");
                break;
            case 1:     //incr
                try {
                    if (vars.get(nameVar) == null)
                        throw new IllegalStateException("Underspecification");
                } catch (IllegalStateException e) {
                    System.err.println("Underspecification: you should define a variable before use it.");
                }
                value = vars.getOrDefault(nameVar, 0) + 1;
                vars.put(nameVar, value);
//                System.out.println(nameVar + " = " + vars.get(nameVar));
                break;
            case 2:     //decr
                try {
                    if (vars.get(nameVar) == null)
                        throw new IllegalStateException("Underspecification");
                    value = vars.getOrDefault(nameVar, 0) - 1;
                    //Prevent negative values (throw exception but still try to run)
                    if (value < 0)
                        throw new ArithmeticException("Illegal variable");
                } catch (IllegalStateException e) {
                    System.err.println("Underspecification: you should define a variable before use it.");
                } catch (ArithmeticException e){
                    System.err.println("Illegal variable: variables must be non-negative!");
                    value = 0;
                }
                vars.put(nameVar, value);
//                System.out.println(nameVar + " = " + vars.get(nameVar));
                break;
            case -1:
                try{
                    throw new UnknownCommandException("Unknown command：" + nameVar);
                }catch (UnknownCommandException e){
                    System.err.println("Unknown command: " + nameVar);
                }
        }
        System.out.println("Current variables:");
        printVariables();
        System.out.println();
    }

    //execute Method：execute the BareBones program from startIndex to endIndex
    //processLabel: 0 -> clear; 1 -> incr; 2 -> decr;
    public static void execute(String[] lines, int startIndex, int endIndex){
        for (int i = startIndex; i <= endIndex; i++){
            //check the ';' symbol of each line
            try{
                if (!lines[i].endsWith(";"))
                    throw new IllegalStateException("Do not forget the ';'");
            }catch (IllegalStateException e){
                System.err.println("Underspecification: please do not forget the ';' after each command.");
            }

            //use String.split and regex(Regular Expression) to split reserved words of BareBones
            String[] commands = lines[i].split("[;\\s]+");

            //dealing the while loop
            if (commands[0].equals("while")){
                String varName = commands[1];
                int loopStartIndex = i + 1;
                int loopEndIndex = findEnd(lines, loopStartIndex);
                //use recursion to execute the code in the while loop
                try {   //check whether defined before using the variable from the while loop condition
                    if (vars.get(varName) == null)
                        throw new IllegalStateException("Underspecification");
                }catch (IllegalStateException e){
                    System.err.println("Underspecification: you should define a variable before use it.");
                    vars.put(varName, 0);   //solving the Underspecification Exception
                }
                while (vars.getOrDefault(varName, 0) != 0){
                    execute(lines, loopStartIndex, loopEndIndex);
                }
                //skip to the end line of loop
                i = loopEndIndex;
            }else { //dealing the part of codes which are not while loop
                int processLabel = -1;
                for (String word : commands) {
//                    System.out.println(word);
                    switch (word) {
                        case "clear":
                            processLabel = 0;
                            continue;
                        case "incr":
                            processLabel = 1;
                            continue;
                        case "decr":
                            processLabel = 2;
                            continue;
                        case "end":
                            continue;
                        default:
                            try{
                                //assume the specification for variables of BareBones is "[a-zA-Z]+"
                                if (Pattern.matches("[a-zA-Z]+", word))
                                    processVar(processLabel, word);
                                else
                                    throw new UnknownCommandException("Unknown command: '" + word + "' in the line " + (i+1));
                            }catch (UnknownCommandException e){
                                System.err.println("Unknown command: '" + word + "' in the line " + (i+1));
                            }
                    }
                }
            }
        }
    }

    public static int findEnd(String[] lines, int startIndex) {
        //depth of loops
        int depth = 1;
        for (int i = startIndex; i < lines.length; i++) {
            String currentLine = lines[i];
            //dealing with the nested while loop
            if (currentLine.startsWith("while"))
                depth++;
            if (currentLine.startsWith("end")){
                depth--;
                if (depth == 0)
                    return i;
            }
        }
        //if not find the matching 'end'
        throw new IllegalArgumentException("No matching 'end' found.");
    }

    public static void printVariables(){
        for (Map.Entry<String, Integer> entry : vars.entrySet()){
            System.out.println(entry.getKey() + " = " + entry.getValue());
        }
    }

    public static void main(String[] args){
        try (BufferedReader reader = new BufferedReader(new FileReader("program.bb"))){
            StringBuilder code = new StringBuilder();
            String line, trimmedLine;

            //store whole BareBones program to 'code'
            while ((line = reader.readLine()) != null){
                //remove indentations and spaces
                trimmedLine = line.trim();
                //remove blank lines
                if (!trimmedLine.isEmpty())
                    code.append(trimmedLine).append("\n");
            }

            //split 'code' line by line to a string array 'lines'
            String[] lines = code.toString().split("\n");
            //execute the BareBones Program from index of 0(the first line) to the last line(lines.length - 1)
            execute(lines, 0, lines.length - 1);

            //print the final result
            System.out.println("Final variables:");
            printVariables();

        } catch (IOException e) {
            System.err.println("Error reading the program file: " + e.getMessage());
        }
    }
}