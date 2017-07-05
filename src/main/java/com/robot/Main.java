package com.robot;

import com.robot.commands.Command;
import com.robot.commands.EndLoopCommand;
import com.robot.commands.LoopCommand;

import java.awt.*;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Jo on 17.06.2017.
 * Taking a file name as argument and is going to run the script file.
 */
public class Main {

    /**
     * Current Command Pointer, used by the Executor
     */
    private static int cPointer = -1;

    /**
     * Current State of Loops, describing in which loop we currently are
     */
    private static int loopDeepness;

    /**
     * The List of parsed Commands to execute
     */
    private static ArrayList<Command> commands;

    /**
     * Contains all Entry Command Pointers for loops, used by EndLoopCommand.java
     */
    private static HashMap<Integer, LoopCommand> loopEntries;

    /**
     * The Delay that happens between every Command (Exception: LoopCommand and EndLoopCommand)
     */
    private static long defaultDelay;

    /**
     * Command to convert to DuckyScript
     */
    private final static String convertToDuckyCommand = "-c2D";

    /**
     * Command to convert to RobotScript
     */
    private final static String convertToRobotCommand = "-c2R";

    /**
     * Command to run DuckyScript
     */
    private final static String runDuckyCommand = "-rD";

    public static void main(String[] args) {
        loopEntries = new HashMap<Integer, LoopCommand>();

        int convert = -1;

        if (args.length < 1) {
            throw new IllegalArgumentException("Needs one script as argument.");
        }else if(args.length>1){
            switch(args[1]){
                case convertToDuckyCommand:
                    convert = 1;
                    break;
                case convertToRobotCommand:
                    convert = 2;
                    break;
                case runDuckyCommand:
                    convert = 3;
                    break;
            }
        }

        MyRobot myRobot = null;
        File commandFile = new File(args[0]);
        BufferedReader reader = null;
        try {
            if (convert == -1) {
                reader = new BufferedReader(new FileReader(commandFile));
            }else if(convert==1) {
                String script = ScriptConverter.convertToDucky(commandFile);
                File f = new File(commandFile.getName() + ".ducky");
                if(f.exists()){
                    f.delete();
                }
                f.createNewFile();
                PrintWriter pw = new PrintWriter(new FileWriter(f));
                pw.print(script);
                pw.flush();
                pw.close();
                System.out.println("Converted RobotScript " + commandFile.getName() + " to DuckyScript " + f.getName());
                System.exit(0);
            }else if(convert==2){
                String script = ScriptConverter.convertToRobot(commandFile);
                File f = new File(commandFile.getName() + ".robot");
                if(f.exists()){
                    f.delete();
                }
                f.createNewFile();
                PrintWriter pw = new PrintWriter(new FileWriter(f));
                pw.print(script);
                pw.flush();
                pw.close();
                System.out.println("Converted DuckyScript " + commandFile.getName() + " to RobotScript " + f.getName());
                System.exit(0);
            }else if(convert==3){
                String script = ScriptConverter.convertToRobot(commandFile);
                System.out.println(script);
                StringReader sr = new StringReader(script);
                reader = new BufferedReader(sr);
            }
        }catch(IOException exc){
            exc.printStackTrace();
        }
        try {
            myRobot = new MyRobot();
        } catch (AWTException e) {
            e.printStackTrace();
        }

        //parse and execute script file//
        commands = FileParser.parseFile(reader, myRobot, new InputManager());

        for (cPointer = 0; cPointer < commands.size(); cPointer++) {
            Command c = commands.get(cPointer);
            if(!(c instanceof LoopCommand)&&!(c instanceof EndLoopCommand)){
                try {
                    Thread.sleep(defaultDelay);
                }catch(InterruptedException exc){
                    exc.printStackTrace();
                }
            }
            c.execute();
        }
    }

    /**
     * Returns the current Command Pointer
     * @return Current Command Pointer
     */
    public static int getCurrentCommandPointer(){
        return cPointer;
    }

    /**
     * Sets the Command Pointer to pointer
     * @param pointer The Index of the Command to be executed next
     */
    public static void setNextCommandPointer(int pointer){
        cPointer = pointer - 1;
    }

    /**
     * Returns the Current "Deepness" of Loops
     * @return Deepness of current Loop
     */
    public static int getLoopDeepness(){
        return loopDeepness;
    }

    /**
     * Adds 1 to loopDeepness to indicate a new Loop
     */
    public static void goIntoLoop(){
        loopDeepness++;
    }

    /**
     * Substracts 1 from loopDeepness to indicate that a Loop has been exited
     */
    public static void exitLoop(){
        loopDeepness--;
    }

    /**
     * Returns the Command List
     * @return Command List
     */
    public static ArrayList<Command> getCommands(){
        return commands;
    }

    /**
     * Returns the LoopCommand corresponding to the given Loop (depth)
     * @param loop Loop (depth)
     * @return CommandPointer of Loop
     */
    public static LoopCommand getLoopEntry(int loop){
        return loopEntries.get(loop);
    }

    /**
     * Sets the Pointer (LoopCommand) to the corresponding Loop (depth)
     * @param loop Loop (depth)
     * @param pointer CommandPointer
     */
    public static void setLoopEntry(int loop, LoopCommand pointer){
        loopEntries.put(loop,pointer);
    }

    /**
     * Sets the Default Delay
     * @param defaultDelay Default Delay for Commands
     */
    public static void setDefaultDelay(Long defaultDelay) {
        Main.defaultDelay = defaultDelay;
    }
}
