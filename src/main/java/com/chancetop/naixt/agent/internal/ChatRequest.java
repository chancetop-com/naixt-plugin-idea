package com.chancetop.naixt.agent.internal;

import com.google.gson.annotations.SerializedName;

/**
 * @author stephen
 */
public class ChatRequest {
    public static ChatRequest of(String query, String currentFilePath, Integer currentLineNumber, Integer currentColumnNumber, String model) {
        var req = new ChatRequest();
        req.query = query;
        req.currentFilePath = currentFilePath;
        req.currentLineNumber = currentLineNumber;
        req.currentColumnNumber = currentColumnNumber;
        req.model = model;
        return req;
    }

    @SerializedName("model")
    public String model;

    @SerializedName("query")
    public String query;

    @SerializedName("current_file_path")
    public String currentFilePath;

    @SerializedName("current_line_number")
    public Integer currentLineNumber;

    @SerializedName("current_column_number")
    public Integer currentColumnNumber;
}
