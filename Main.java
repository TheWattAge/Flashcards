package flashcards;
import java.text.SimpleDateFormat;
import java.util.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.FileNotFoundException;

public class Main {
    
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        CardSet flashCards = new CardSet();
        Log sessionLog = new Log();
        Menu userMenu = new Menu();
        String exportFile = "";
        boolean isRunning = true;

        if (args.length > 0) {
            for (int i = 0; i < args.length; i++) {
                switch (args[i]) {
                    case "-import":
                        flashCards.importCardSet(args[i + 1], sessionLog);
                        break;
                    case "-export":
                        exportFile = args[i + 1];
                        break;
                }
            }
        }
        
        while (isRunning) {
            userMenu.listUserOptions(sessionLog);
            userMenu.selectOption(sc, flashCards, sessionLog, exportFile);
            isRunning = userMenu.isActive();
        }
        
    } // End main method

} // End Main Class

class Menu {
    
    private boolean active = true;
    
    public void listUserOptions(Log sessionLog) {
        sessionLog.updateAndPrint("Input the action " +
                "(add, remove, import, export, ask, exit, log, " +
                "hardest card, reset stats: ");
    }
    
    public void selectOption(Scanner sc, CardSet fc, Log sessionLog, String expFile) {

        switch (sessionLog.strUpdateAndReturn(sc.nextLine()).toLowerCase()) {
            case "add":
                fc.addCardToSet(sc, sessionLog);
                break;
            case "remove":
                fc.removeCardFromSet(sc, sessionLog);
                break;
            case "import":
                fc.importCardSet(sc, sessionLog);
                break;
            case "export":
                fc.exportCardSet(sc, sessionLog);
                break;
            case "ask":
                fc.ask(sc, sessionLog);
                break;
            case "log":
                sessionLog.export(sc);
                break;
            case "hardest card":
                fc.hardestCard(sessionLog);
                break;
            case "reset stats":
                fc.resetStats(sessionLog);
                break;
            case "exit":
                sessionLog.updateAndPrint("Bye bye!");
                if (!expFile.equals("")) {
                    fc.exportCardSet(expFile, sessionLog);
                }
                setActive(false);
                break;
            default:
                sessionLog.updateAndPrint("Sorry, I did not recognise that command.");
        } // end switch
        
    } // end selectOption
    
    public boolean isActive() {
        return active;
    }
    
    public void setActive(boolean activeState) {
        this.active = activeState;
    }
}

// Holds a collection of FlashCards
class CardSet {

    private Map<String, String> cardSet = new LinkedHashMap<>();
    private final Map<String, Integer> mistakeSet = new HashMap<>();
    private int intNumOfCards;

    private CardSet getCurrentCardSet() {
        return this;
    }

    public Map<String, String> getListOfCards() {
        return cardSet;
    }

    public Map<String, Integer> getMistakes() {
        return mistakeSet;
    }

    private void setCardSet(Map<String, String> replaceCards) {
        this.cardSet = replaceCards;
    }

    public CardSet addCardToSet(Scanner sc, Log sessionLog) {
        FlashCard queryCard = new FlashCard(sc, getCurrentCardSet(), sessionLog, 0);
        if (queryCard.getTerm() != null && queryCard.getDefinition() != null) {
            this.cardSet.put(queryCard.getTerm(), queryCard.getDefinition());
            this.mistakeSet.put(queryCard.getTerm(), queryCard.getNumOfMistakes());
            sessionLog.updateAndPrint("The pair (\"" + queryCard.getTerm() + ":\"" + queryCard.getDefinition() + "\") has been added.");
        }
        return this.getCurrentCardSet();
    }

    public void removeCardFromSet(Scanner sc, Log sessionLog) {
        sessionLog.updateAndPrint("The card:");
        String queryTerm = sessionLog.strUpdateAndReturn(sc.nextLine());
        if (termExists(queryTerm)) {
            mistakeSet.remove(queryTerm);
            cardSet.remove(queryTerm);
            sessionLog.updateAndPrint("The card has been removed.");
        } else {
            sessionLog.updateAndPrint("Can't remove \"" + queryTerm + "\": there is no such card.");
        }
    }

    protected boolean termExists(String term) {
        return cardSet.containsKey(term);
    }

    protected boolean definitionExists(String definition) {
        return cardSet.containsValue(definition);
    }

    public void importCardSet(String strFile, Log sessionLog) {

        try {
            String queryFile = sessionLog.strUpdateAndReturn(strFile);
            File savedCardSet = new File("./" + queryFile);
            Scanner reader = new Scanner(savedCardSet);
            int counter = 0;
            while (reader.hasNextLine()) {
                String[] cardAndStats = reader.nextLine().split("\\s+");
                String flashcard = cardAndStats[0];
                String strTerm = flashcard.split("=")[0];
                String strDefinition = flashcard.split("=")[1];
                Integer mistakeCount = Integer.parseInt(cardAndStats[1]);
                this.cardSet.put(strTerm, strDefinition);
                this.mistakeSet.put(strTerm, mistakeCount);
                counter++;
            }
            reader.close();
            sessionLog.updateAndPrint(counter + " cards have been loaded.");
        } catch (FileNotFoundException e) {
            sessionLog.updateAndPrint("File not found.");
        }

    }

    public void importCardSet(Scanner sc, Log sessionLog) {
        
        try {
            sessionLog.updateAndPrint("File name:");
            String queryFile = sessionLog.strUpdateAndReturn(sc.nextLine());
            File savedCardSet = new File("./" + queryFile);
            Scanner reader = new Scanner(savedCardSet);
            int counter = 0;
            while (reader.hasNextLine()) {
                String[] cardAndStats = reader.nextLine().split("\\s+");
                String flashcard = cardAndStats[0];
                String strTerm = flashcard.split("=")[0];
                String strDefinition = flashcard.split("=")[1];
                Integer mistakeCount = Integer.parseInt(cardAndStats[1]);
                this.cardSet.put(strTerm, strDefinition);
                this.mistakeSet.put(strTerm, mistakeCount);
                counter++;
            }
            reader.close();
            sessionLog.updateAndPrint(counter + " cards have been loaded.");
        } catch (FileNotFoundException e) {
            sessionLog.updateAndPrint("File not found.");
        }
    }
    
    public void exportCardSet(Scanner sc, Log sessionLog) {
        try {
            sessionLog.updateAndPrint("File name:");
            String queryFile = sessionLog.strUpdateAndReturn(sc.nextLine());
            FileWriter fw = new FileWriter(queryFile);
            int counter = 0;
            for (Map.Entry<String, String> flashcard : cardSet.entrySet()) {
                fw.write(flashcard.getKey() + "=" + flashcard.getValue() + " " + mistakeSet.get(flashcard.getKey()) + "\n");
                counter++;
            }
            
            fw.close();
            sessionLog.updateAndPrint(counter + " cards have been saved.");
        } catch (IOException e) {
            sessionLog.updateAndPrint("An error has occurred.");
            e.printStackTrace();
        }
    }

    public void exportCardSet(String strFile, Log sessionLog) {
        try {
            String queryFile = sessionLog.strUpdateAndReturn(strFile);
            FileWriter fw = new FileWriter(queryFile);
            int counter = 0;

            for (Map.Entry<String, String> flashcard : cardSet.entrySet()) {
                fw.write(flashcard.getKey() + "=" + flashcard.getValue() + " " + mistakeSet.get(flashcard.getKey()) + "\n");
                counter++;
            }

            fw.close();
            sessionLog.updateAndPrint(counter + " cards have been saved.");
        } catch (IOException e) {
            sessionLog.updateAndPrint("An error has occurred.");
            e.printStackTrace();
        }
    }

    private void updateMistakeSet(String cardTerm) {
        this.mistakeSet.replace(cardTerm, this.mistakeSet.get(cardTerm) + 1);
    }
    
    public void ask(Scanner sc, Log sessionLog) {
        Scanner sc2 = new Scanner(System.in);
        sessionLog.updateAndPrint("How many times to ask?");
        int intNumOfQs = sessionLog.intUpdateAndReturn(sc2.nextInt());
        String strUserAnswer;
        int counter = 0;
        
        while (counter < intNumOfQs) {
            for (Map.Entry<String, String> entry : cardSet.entrySet()) {
                sessionLog.updateAndPrint("Print the definition of \"" + entry.getKey() + "\":");
                strUserAnswer = sessionLog.strUpdateAndReturn(sc.nextLine());
                if (strUserAnswer.equalsIgnoreCase(entry.getValue())) {
                    sessionLog.updateAndPrint("Correct answer");
                    counter++;
                    break;
                } else if (cardSet.containsValue(strUserAnswer)) {
                    for (Map.Entry<String, String> attempt : cardSet.entrySet()) {
                        if (strUserAnswer.equals(attempt.getValue())) {
                            sessionLog.updateAndPrint("Wrong answer. The correct one is \"" + entry.getValue() + "\", you've just written the definition of \"" + attempt.getKey() + "\".");
                            updateMistakeSet(entry.getKey());
                            counter++;
                        }
                    }
                    break;
                } else {
                    sessionLog.updateAndPrint("Wrong answer. The correct one is \"" + entry.getValue() + "\".");
                    updateMistakeSet(entry.getKey());
                    counter++;
                    break;
                } // end else
            }// end forEach
        }// end while    
    } // end method

    public void hardestCard(Log sessionLog) {
        int highestNumOfMistakes = 0;
        ArrayList<String> cardsWithMostMistakes = new ArrayList<>();
        for (Map.Entry<String, Integer> mistakenCard : mistakeSet.entrySet()) {
            if (Integer.valueOf(mistakenCard.getValue()) > 0) {
                if (Integer.valueOf(mistakenCard.getValue()) == highestNumOfMistakes) {
                    cardsWithMostMistakes.add(mistakenCard.getKey());
                } else if (Integer.valueOf(mistakenCard.getValue()) > highestNumOfMistakes) {
                    cardsWithMostMistakes.clear();
                    highestNumOfMistakes = Integer.valueOf(mistakenCard.getValue());
                    cardsWithMostMistakes.add(mistakenCard.getKey());
                }
            }
        }

        if(cardsWithMostMistakes.size() == 1) {
            sessionLog.updateAndPrint("The hardest card is \"" + cardsWithMostMistakes.get(0) + "\". " +
                    "You have " + highestNumOfMistakes + " errors answering it.");
        } else if (cardsWithMostMistakes.size() > 1) {
            StringBuilder output = new StringBuilder();
            output.append("The hardest card is ");
            for (int i = 0; i < cardsWithMostMistakes.size(); i++) {
                if (i == (cardsWithMostMistakes.size() - 1)) {
                    output.append(cardsWithMostMistakes.get(i) + ". ");
                } else {
                    output.append(cardsWithMostMistakes.get(i) + ", ");
                }
            }
            output.append("\"You have \"" + highestNumOfMistakes + "\" errors answering them.\"");
            sessionLog.updateAndPrint(output.toString());
        } else {
            sessionLog.updateAndPrint("There are no cards with errors.");
        }
    }

    public void resetStats(Log sessionLog) {
        for (String key : mistakeSet.keySet()) {
            mistakeSet.replace(key, 0);
        }
        sessionLog.updateAndPrint("Card statistics has been reset.");
    }

} // end CardSet class

// FlashCard holds individual FlashCard methods and properties
class FlashCard extends CardSet {
    
    private String term;
    private String definition;
    private int numOfMistakes = 0;
    
    public FlashCard(Scanner sc, CardSet fc, Log sessionLog, int NumOfMistakes) {
        sessionLog.updateAndPrint("The card: ");
        String queryTerm = sessionLog.strUpdateAndReturn(sc.nextLine());
        
        if (!setTerm(queryTerm, fc, sessionLog))
            return;
        
        sessionLog.updateAndPrint("The definition of the card:");
        String queryDefinition = sessionLog.strUpdateAndReturn(sc.nextLine());
        
        this.setDefinition(queryDefinition, fc, sessionLog);
            
    }
    
    private boolean setTerm(String term, CardSet fc, Log sessionLog) {
        
        if (fc.termExists(term)) {
            sessionLog.updateAndPrint("The card " + term + " already exists.");
            return false;
        }
        
        this.term = term;
        
        return this.getTerm().equals(term);
    }
    
    private void setDefinition(String definition, CardSet fc, Log sessionLog) {
        
        if (fc.definitionExists(definition)) {
            sessionLog.updateAndPrint("The definition " + definition + " already exists.");
        }
        
        this.definition = definition;

    }

    // has to be public to allow for CardSet.resetStats();
    public boolean setNumOfMistakes(int numOfMistakes, CardSet fc, Log sessionLog) {
        if (numOfMistakes > this.numOfMistakes) {
            this.numOfMistakes = numOfMistakes;
        }
        return numOfMistakes == this.numOfMistakes;
    }
    
    public String getTerm() {
        return term;
    }
    
    public String getDefinition() {
        return definition;
    }

    public int getNumOfMistakes() { return numOfMistakes; }
    
}

class Log {
    private final ArrayList<String> activeLog = new ArrayList<>();
    private final Date date = new Date();
    private final SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");

    public ArrayList<String> getActiveLog() {
        return activeLog;
    }

    private void update(String input) {
        activeLog.add(formatter.format(date) + ": " + input);
    }

    public void updateAndPrint(String input) {
        this.update(input);
        System.out.println(input);
    }

    public String strUpdateAndReturn(String input) {
        this.update(input);
        return input;
    }

    public int intUpdateAndReturn(int input) {
        this.update(Integer.toString(input));
        return input;
    }

    public void export(Scanner sc) {
        updateAndPrint("File name: ");
        String logTitle = strUpdateAndReturn(sc.nextLine());
        try {
            FileWriter fw = new FileWriter(logTitle);
            for (Object logEntry : activeLog) {
                fw.write(logEntry.toString() + "\n");
            }
            fw.close();
            updateAndPrint("The log has been saved.");
        } catch (IOException e) {
            this.updateAndPrint("An error has occurred.");
            e.printStackTrace();
        }
    }
}