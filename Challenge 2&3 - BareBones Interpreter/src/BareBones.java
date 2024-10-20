import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class BareBones {
    public static final Map<String, Integer> vars = new HashMap<>();
    public static final Map<String, Integer> subStart = new HashMap<>();
    public static final Map<String, Integer> subEnd = new HashMap<>();

    public static void processVar(int label, String nameVar, int currentLine){
        int value = 0;
        switch (label) {
            case 0:     //clear
                vars.put(nameVar, value);
                break;
            case 1:     //incr
                if (notDefined(nameVar))
                    defineVar(nameVar);
                value = vars.get(nameVar) + 1;
                vars.put(nameVar, value);
                break;
            case 2:     //decr
                if (notDefined(nameVar))
                    defineVar(nameVar);
                value = vars.get(nameVar) - 1;
                //Prevent negative values
                if (value < 0) {
                    System.err.println("Illegal variable: variables must be non-negative!");
                    value = 0;
                }
                vars.put(nameVar, value);
                break;
            case -1:
                System.err.println("Error: unknown command: '" + nameVar + "' in the line " + (currentLine + 1));
        }

        System.out.println("Current variables:");
        printVariables();
        System.out.println();
    }

    //check if a variable is defined
    public static boolean notDefined(String nameVar) {
        return !vars.containsKey(nameVar);
    }

    //define a variable with an initial value of 0
    public static void defineVar(String nameVar) {
        System.err.println("Underspecification: you should define a variable before use it.");
        vars.put(nameVar, 0);
    }

    //execute Method: execute the BareBones program from startIndex to endIndex
    //processLabel: 0 -> clear; 1 -> incr; 2 -> decr;
    public static void execute(String[] lines, int startIndex, int endIndex){
        for (int i = startIndex; i <= endIndex; i++){
            //check the ';' symbol of each line
            if (!lines[i].endsWith(";"))
                System.err.println("Underspecification: please do not forget the ';' after each command.");

            //use String.split and regex(Regular Expression) to split reserved words of BareBones
            String[] commands = lines[i].split("[;\\s]+");

            //Register subroutine definitions
            if (commands[0].equals("sub")) {
                String subName = commands[1];
                subStart.put(subName, i + 1);
                int subEndIndex = findEnd(lines, i + 1, "end");
                subEnd.put(subName, subEndIndex);
                i = subEndIndex;
                continue;
            }
            //Call subroutine
            if (commands[0].equals("call")) {
                String subName = commands[1];
                if (subStart.containsKey(subName)) {
                    int subStartIndex = subStart.get(subName);
                    int subEndIndex = subEnd.get(subName);
                    execute(lines, subStartIndex, subEndIndex - 1);
                } else {
                    System.err.println("Unknown subroutine: '" + subName + "' in the line " + (i + 1));
                }
                continue;
            }

            //dealing with the while loop
            if (commands[0].equals("while")){
                String varName = commands[1];
                int loopStartIndex = i + 1;
                int loopEndIndex = findEnd(lines, loopStartIndex, "end");

                if (notDefined(varName))
                    defineVar(varName);

                while (vars.get(varName) != 0)
                    execute(lines, loopStartIndex, loopEndIndex - 1);
                i = loopEndIndex;
                continue;
            }

            //dealing with if/else statement
            if (commands[0].equals("if")) {
                String varName = commands[1];
                int ifStartIndex = i + 1;
                int ifEndIndex, elseEndIndex;

                try {
                    ifEndIndex = findEnd(lines, ifStartIndex, "else");
                    elseEndIndex = findEnd(lines, ifEndIndex + 1, "end");

                    if (notDefined(varName))
                        defineVar(varName);

                    if (vars.get(varName) != 0){
                        execute(lines, ifStartIndex, ifEndIndex - 1);
                    }
                    else{
                        execute(lines, ifEndIndex + 1, elseEndIndex - 1);
                    }
                    i = elseEndIndex;
                } catch (IllegalArgumentException e) {  //No else block, only if-end
                    ifEndIndex = findEnd(lines, ifStartIndex, "end");

                    if (notDefined(varName))
                        defineVar(varName);

                    if (vars.get(varName) != 0)
                        execute(lines, ifStartIndex, ifEndIndex - 1);
                    i = ifEndIndex;
                }
            }
            //dealing with commands that are not loops or conditionals
            else {
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
                        default:
                            if (Pattern.matches("[a-zA-Z]+", word))
                                processVar(processLabel, word, i);
                            else
                                System.err.println("Error: unknown command: '" + word + "' in the line " + (i + 1));
                    }
                }
            }
        }
    }

    public static int findEnd(String[] lines, int startIndex, String endKeyword) {
        int depth = 1;
        for (int i = startIndex; i < lines.length; i++) {
            String currentLine = lines[i];
            if (currentLine.startsWith("while") || currentLine.startsWith("if"))
                depth++;
            if (currentLine.startsWith(endKeyword)) {
                depth--;
                if (depth == 0)
                    return i;
            }
        }
        //if not find the matching 'end'
        throw new IllegalArgumentException("No matching '" + endKeyword + "' found from the codes of line " + (startIndex + 1));
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
                //remove indentations, spaces, and comments
                trimmedLine = line.split("#", 2)[0].trim();
                //remove blank lines and ignore comments
                if (!trimmedLine.isEmpty()){
                    code.append(trimmedLine).append("\n");
                }
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
