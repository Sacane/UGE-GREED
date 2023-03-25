package fr.ramatellier.greed.server;

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
    public boolean check(){
        var split = computeCommand.split(" ");
        if(split.length != 4){
            return false;
        }
        var url = split[0];
        var className = split[1];
        try{
            var start = Long.parseLong(split[2]);
            var end = Long.parseLong(split[3]);
            if(end < start){
                return false;
            }
            computeInfo = new ComputeInfo(url, className, start, end);
        }catch(NumberFormatException e){
            return false;
        }
        return true;
    }

    public ComputeInfo get(){
        if(computeInfo == null){
            throw new IllegalStateException("The command has not been checked");
        }
        return computeInfo;
    }
}
