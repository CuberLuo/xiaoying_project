package com.example.materialstudio.web.request;

import java.util.List;

public class SubmitTaskRequest {
    private List<String> fileUrls;

    public List<String> getFileUrls() {
        return fileUrls;
    }

    public void setFileUrls(List<String> fileUrls) {
        this.fileUrls = fileUrls;
    }
}
