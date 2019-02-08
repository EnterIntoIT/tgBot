package com.enterintoit.pupptmstr;

public class AdminMessageParser {

    public String unparsedText;
    private String error ,messageType, contentType, messageText;

    AdminMessageParser(String unparsedText) {
        this.unparsedText = unparsedText;
    }

    private void parse() {
        String[] parsedText = unparsedText.split("#");
        //0й индекс - пустой символ
        messageType = parsedText[1];
        contentType = parsedText[2];
        messageText = parsedText[3];
    }

    public void checkMatchingAndParse() {
        String[] parsedText = unparsedText.split("#");
        if (parsedText.length == 4) {
            parse();
        } else error = "error";
    }

    public String getMessageText() {
        return messageText;
    }

    public String getMessageType() {
        return messageType;
    }

    public String getContentType() {
        return contentType;
    }

    public String getError() {
        return error;
    }
}
