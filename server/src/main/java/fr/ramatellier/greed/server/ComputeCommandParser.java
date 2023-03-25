package fr.ramatellier.greed.server;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Objects;

/**
 * Parse the given command and check if it's valid.
 */
public final class ComputeCommandParser {
    private final String computeCommand;
    private ComputeInfo computeInfo;
    public ComputeCommandParser(String computeCommand){
        this.computeCommand = Objects.requireNonNull(computeCommand);
    }

    /**
     * Check if the command is valid and fill compute information.
     * A valid command has the following format: url[URL] className[STRING] start[LONG] end[LONG]
     * Here is the several causes of invalidity:
     * - The command has not the right number of arguments
     * - The url is not a valid url
     * - The className is not a valid java class name
     * - The start and end are not valid long
     * - The range is not valid (start >= end)
     * @return true if the command is valid, false otherwise.
     */
    public boolean check(){
        var split = computeCommand.split(" ");
        if(split.length != 4){
            return false;
        }
        var url = split[0];
        var className = split[1];
        var startAsString = split[2];
        var endAsString = split[3];
        if(!checkUrl(url) || !checkClassName(className) || !checkRange(startAsString, endAsString)){
            return false;
        }
        var start = Long.parseLong(startAsString);
        var end = Long.parseLong(endAsString);
        computeInfo = new ComputeInfo(url, className, start, end);
        return true;
    }

    /**
     * Get the compute information if the command has been checked.
     * @throws IllegalStateException if the command has not been checked before calling this method.
     * @return the compute information.
     */
    public ComputeInfo get(){
        if(computeInfo == null){
            throw new IllegalStateException("The command has not been checked yet");
        }
        return computeInfo;
    }

    private boolean checkUrl(String urlAsString){
        try {
            new URL(urlAsString);
        } catch (MalformedURLException e) {
            return false;
        }
        return true;
    }
    private boolean checkClassName(String className){
        return className.matches("[a-zA-Z_][a-zA-Z0-9_]*");
    }
    private boolean checkRange(String startAsString, String endAsString){
        try{
            var start = Long.parseLong(startAsString);
            var end = Long.parseLong(endAsString);
            return end > start;
        }catch(NumberFormatException e){
            return false;
        }
    }
}
