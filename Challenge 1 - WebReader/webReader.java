import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

public class webReader {
    public static void main(String[] args) throws IOException {
        // 01. New a reader Object(from BufferedReader class) to read ID
        // or use "Scanner scanner = new Scanner(System.in);"
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        System.out.print("Enter email ID here: ");
        String id = reader.readLine();

        // 02. Constructing the full Web page address by string concatenation
        String webLink = "https://www.southampton.ac.uk/people/" + id;

        // 03. Constructing a URL object from the Web address
        URL url = new URL(webLink);

        // 04. Constructing a BufferedReader object that can read from the URL
        BufferedReader urlReader = new BufferedReader(new InputStreamReader(url.openStream()));

        // 05. Ignoring the first lines of input from the Web page and saving the one which contains the name
        String currentLine;
        String nameLine = null;
        boolean find = false;
        while ((currentLine = urlReader.readLine()) != null){
            if (currentLine.contains("\"@type\": \"Person\"")){
                find = true;
                continue;
            }
            if (find){
                nameLine = currentLine;
                break;
            }
        }

        // 06. Use the indexOf() and substring() methods to find and extract the name from the line
        if (nameLine != null){
            int firstC = nameLine.indexOf(":");
            // System.out.println(firstC);
            String ans = nameLine.substring(firstC + 3, nameLine.length() - 2);
            System.out.println("Name: " + ans);
        } else System.out.println("Invalid email ID or not found on the web page.");
        
        reader.close();
        urlReader.close();
    }
}