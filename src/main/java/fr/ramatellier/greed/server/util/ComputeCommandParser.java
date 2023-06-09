package fr.ramatellier.greed.server.util;

import fr.ramatellier.greed.server.compute.ComputeInfo;
import fr.ramatellier.greed.server.compute.Range;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Objects;

/**
 * Parse the computation command and check if it's valid.
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
        var range = checkRange(startAsString, endAsString);
        if(!checkUrl(url) || range == null){
            return false;
        }
        computeInfo = new ComputeInfo(url, className, range.start(), range.end());
        return true;
    }

    /**
     * Get the information to compute if the command has been checked.
     *
     * @throws IllegalStateException if the command has not been checked before calling this method.
     * @return the information to compute.
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

    private Range checkRange(String startAsString, String endAsString){
        try{
            var start = Long.parseLong(startAsString);
            var end = Long.parseLong(endAsString);
            if(start >= end) return null;
            return new Range(start, end);
        }catch(NumberFormatException e){
            return null;
        }
    }
}
