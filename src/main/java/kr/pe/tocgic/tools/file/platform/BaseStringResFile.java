package kr.pe.tocgic.tools.file.platform;

import kr.pe.tocgic.tools.util.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

public class BaseStringResFile {
    protected String TAG = getClass().getSimpleName();

    protected boolean isValidFile(File source) {
        if (source == null) {
            Logger.w(TAG, "sourceFilePath is Null");
            return false;
        }
        if (!source.exists() || !source.isFile()) {
            Logger.w(TAG, "file NOT exist or NOT File");
            return false;
        }
        return true;
    }

    protected String getStringFromFile(String jsonFilePath) {
        String jsonString = null;
        if (jsonFilePath != null && jsonFilePath.length() > 0) {
            File jsonFile = new File(jsonFilePath);
            BufferedReader streamReader = null;
            try {
                streamReader = new BufferedReader(new FileReader(jsonFile));
                StringBuilder responseStrBuilder = new StringBuilder();
                String inputStr = null;
                while ((inputStr = streamReader.readLine()) != null) {
                    responseStrBuilder.append(inputStr);
                }
                jsonString = responseStrBuilder.toString();
            } catch (Exception e) {
                Logger.w(TAG, e.getMessage());
            } finally {
                try {
                    if (streamReader != null) {
                        streamReader.close();
                    }
                } catch (Exception eFile) {
                    Logger.w(TAG, eFile.getMessage());
                }
            }
        }
        return jsonString;
    }
}
